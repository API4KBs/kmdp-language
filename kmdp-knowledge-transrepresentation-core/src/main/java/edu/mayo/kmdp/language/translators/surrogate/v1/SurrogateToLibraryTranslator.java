package edu.mayo.kmdp.language.translators.surrogate.v1;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.Library;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate)
public class SurrogateToLibraryTranslator extends AbstractSimpleTranslator<KnowledgeAsset,Library> {

  public static final UUID id = UUID.fromString("da544016-f112-4a69-afef-d64726330476");
  public static final String version = "1.0.0";

  public SurrogateToLibraryTranslator() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(Knowledge_Asset_Surrogate));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(FHIR_STU3));
  }

  @Override
  protected Optional<Library> transformAst(ResourceIdentifier assetId,
      KnowledgeAsset expression, SyntacticRepresentation tgtRep, Properties config) {
    return Optional.ofNullable(new SurrogateToLibrary().transform(expression));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return Knowledge_Asset_Surrogate;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return FHIR_STU3;
  }
}
