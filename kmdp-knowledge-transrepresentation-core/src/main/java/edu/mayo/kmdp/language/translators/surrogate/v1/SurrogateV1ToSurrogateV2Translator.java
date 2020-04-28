package edu.mayo.kmdp.language.translators.surrogate.v1;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.*;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

import java.util.*;

@KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate_2_0)
public class SurrogateV1ToSurrogateV2Translator extends
    AbstractSimpleTranslator<KnowledgeAsset, Collection<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset>> {
  public static final UUID id = UUID.fromString("ca69756f-6ba6-439f-88a0-ca957f5454e0");
  public static final String version = "1.0.0";

  public SurrogateV1ToSurrogateV2Translator() {
    setId(SemanticIdentifier.newId(id, version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(Knowledge_Asset_Surrogate));
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
  protected Optional<Collection<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset>> transformAst(
      ResourceIdentifier assetId, KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(new SurrogateV1ToSurrogateV2().transform(expression));
  }

}
