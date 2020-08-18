package edu.mayo.kmdp.language.translators.surrogate.v1;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings.DEFAULT;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Transcreation_Task;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;

import edu.mayo.kmdp.language.parsers.surrogate.v1.SurrogateParser;
import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.DeserializeApiInternal._applyLift;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateHelper;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;

@KPOperation(Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate)
public class SurrogateV1ToSurrogateV2Translator extends
    AbstractSimpleTranslator<KnowledgeAsset, Collection<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset>> {
  public static final UUID id = UUID.fromString("ca69756f-6ba6-439f-88a0-ca957f5454e0");
  public static final String version = "1.0.0";

  private _applyLift parser = new SurrogateParser();

  public SurrogateV1ToSurrogateV2Translator() {
    setId(SemanticIdentifier.newId(id, version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return Arrays.asList(
        rep(Knowledge_Asset_Surrogate),
        rep(Knowledge_Asset_Surrogate, JSON),
        rep(Knowledge_Asset_Surrogate, JSON, defaultCharset()),
        rep(Knowledge_Asset_Surrogate, JSON, defaultCharset(), DEFAULT));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(Knowledge_Asset_Surrogate_2_0));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return Knowledge_Asset_Surrogate;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return Knowledge_Asset_Surrogate_2_0;
  }


  @Override
  protected Optional<KnowledgeCarrier> applyTransrepresentation(
      KnowledgeCarrier src,
      SyntacticRepresentation tgtRep,
      Properties config) {
    Optional<Collection<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset>> legacySurr;

    switch (src.getLevel().asEnum()) {
      case Encoded_Knowledge_Expression:
      case Concrete_Knowledge_Expression:
      case Serialized_Knowledge_Expression:
        legacySurr = parser
            .applyLift(src, Abstract_Knowledge_Expression, null, null)
            .flatOpt(kc -> transformAst(
                src.getAssetId(),
                (KnowledgeAsset) kc.getExpression(),
                tgtRep, config)).getOptionalValue();
        break;
      case Abstract_Knowledge_Expression:
        legacySurr = transformAst(
            src.getAssetId(),
            (KnowledgeAsset) src.getExpression(),
            tgtRep, config);
        break;
      default:
        throw new UnsupportedOperationException();
    }

    return legacySurr.map(out -> wrap(
        tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getArtifactId())));
  }


  @Override
  protected Optional<Collection<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset>> transformAst(
      ResourceIdentifier assetId, KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(new SurrogateV1ToSurrogateV2().transform(expression));
  }

  @Override
  protected KnowledgeCarrier wrap(
      SyntacticRepresentation tgtRep,
      Collection<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset> translatedArtifact,
      ResourceIdentifier mappedAssetId,
      ResourceIdentifier mappedArtifactId) {

    return AbstractCarrier.ofIdentifiableTree(
            rep(getTargetLanguage()),
        org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset.KnowledgeAsset::getAssetId,
            ka ->
                SurrogateHelper.getSurrogateId(ka, Knowledge_Asset_Surrogate_2_0, JSON)
                    .orElse(SemanticIdentifier.randomId()),
            getImmediateChildrenFunction(),
            mappedAssetId,
            translatedArtifact.stream()
                .collect(
                    Collectors.toMap(
                        edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset::getAssetId,
                        knowledgeAsset -> knowledgeAsset)))
        .withRootId(mappedAssetId);
  }
}
