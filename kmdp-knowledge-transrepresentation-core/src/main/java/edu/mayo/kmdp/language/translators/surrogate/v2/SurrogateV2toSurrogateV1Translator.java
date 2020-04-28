package edu.mayo.kmdp.language.translators.surrogate.v2;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
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

import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate)
public class SurrogateV2toSurrogateV1Translator extends
    AbstractSimpleTranslator<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset, KnowledgeAsset> {

  public static final UUID id = UUID.fromString("d2c5f30a-a406-47e0-af0a-1195d6da422e");
  public static final String version = "1.0.0";

  public SurrogateV2toSurrogateV1Translator() {
    setId(SemanticIdentifier.newId(id, version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(Knowledge_Asset_Surrogate_2_0));
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
  protected Optional<KnowledgeAsset> transformAst(ResourceIdentifier assetId,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(new SurrogateV2ToSurrogateV1().transform(expression));
  }

}
