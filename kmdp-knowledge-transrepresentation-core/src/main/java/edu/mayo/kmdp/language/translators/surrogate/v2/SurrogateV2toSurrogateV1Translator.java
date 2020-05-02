package edu.mayo.kmdp.language.translators.surrogate.v2;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.language.parsers.surrogate.v2.Surrogate2Parser;
import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal._applyLift;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate_2_0)
public class SurrogateV2toSurrogateV1Translator extends
    AbstractSimpleTranslator<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset, KnowledgeAsset> {

  public static final UUID id = UUID.fromString("d2c5f30a-a406-47e0-af0a-1195d6da422e");
  public static final String version = "1.0.0";

  public SurrogateV2toSurrogateV1Translator() {
    setId(SemanticIdentifier.newId(id, version));
  }

  private _applyLift parser = new Surrogate2Parser();

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return Arrays.asList(
        rep(Knowledge_Asset_Surrogate_2_0),
        rep(Knowledge_Asset_Surrogate_2_0, JSON),
        rep(Knowledge_Asset_Surrogate_2_0, JSON, defaultCharset()),
        rep(Knowledge_Asset_Surrogate_2_0, JSON, defaultCharset(), Encodings.DEFAULT));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(Knowledge_Asset_Surrogate));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return Knowledge_Asset_Surrogate_2_0;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return Knowledge_Asset_Surrogate;
  }

  @Override
  protected Optional<KnowledgeCarrier> applyTransrepresentation(
      KnowledgeCarrier src,
      SyntacticRepresentation tgtRep,
      Properties config) {
    Optional<KnowledgeAsset> legacySurr;

    switch (src.getLevel().asEnum()) {
      case Encoded_Knowledge_Expression:
      case Concrete_Knowledge_Expression:
      case Parsed_Knowedge_Expression:
        legacySurr = parser
            .applyLift(src, ParsingLevelSeries.Abstract_Knowledge_Expression, null, null)
            .flatOpt(kc -> transformAst(
                src.getAssetId(),
                (edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset) kc.getExpression(),
                tgtRep, config)).getOptionalValue();
        break;
      case Abstract_Knowledge_Expression:
        legacySurr = transformAst(
            src.getAssetId(),
            (edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset) src.getExpression(),
            tgtRep, config);
        break;
      default:
        throw new UnsupportedOperationException();
    }

    return legacySurr.map(out -> wrap(
        tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getArtifactId())));
  }

  @Override
  protected Optional<KnowledgeAsset> transformAst(ResourceIdentifier assetId,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(
        new KnowledgeAsset().withAssetId(DatatypeHelper.toURIIdentifier(expression.getAssetId()))
    );
  }

}
