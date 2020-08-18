package edu.mayo.kmdp.language.translators.surrogate.v2;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Transcreation_Task;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp.taxonomy.parsinglevel.snapshot.ParsingLevel.Abstract_Knowledge_Expression;

import edu.mayo.kmdp.language.parsers.surrogate.v2.Surrogate2Parser;
import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.DeserializeApiInternal._applyLift;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;

@Named
@KPOperation(Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate_2_0)
public class SurrogateV2toSurrogateV1Translator extends
    AbstractSimpleTranslator<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset, KnowledgeAsset> {

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
      case Serialized_Knowledge_Expression:
        legacySurr = parser
            .applyLift(src, Abstract_Knowledge_Expression, null, null)
            .flatOpt(kc -> transformAst(
                src.getAssetId(),
                (org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset) kc.getExpression(),
                tgtRep, config)).getOptionalValue();
        break;
      case Abstract_Knowledge_Expression:
        legacySurr = transformAst(
            src.getAssetId(),
            (org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset) src.getExpression(),
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
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(
        new KnowledgeAsset()
            .withAssetId(new URIIdentifier()
                .withUri(expression.getAssetId().getResourceId())
                .withVersionId(expression.getAssetId().getVersionId()))
    );
  }

}
