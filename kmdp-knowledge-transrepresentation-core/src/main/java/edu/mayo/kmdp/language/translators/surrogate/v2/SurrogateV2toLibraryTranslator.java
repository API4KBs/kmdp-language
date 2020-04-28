package edu.mayo.kmdp.language.translators.surrogate.v2;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import org.hl7.fhir.dstu3.model.*;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

import javax.inject.Named;
import java.util.*;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.*;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
@KPSupport(Knowledge_Asset_Surrogate_2_0)
public class SurrogateV2toLibraryTranslator extends
    AbstractSimpleTranslator<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset, Library> {

  public static final UUID id = UUID.fromString("e526d596-50d7-47b4-b435-879f6a31e622");
  public static final String version = "1.0.0";

  public SurrogateV2toLibraryTranslator() {
    setId(SemanticIdentifier.newId(id, version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(Knowledge_Asset_Surrogate_2_0));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(FHIR_STU3));
  }

  @Override
  protected Optional<Library> transformAst(ResourceIdentifier assetId,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(new SurrogateV2ToLibrary().transform(expression));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return Knowledge_Asset_Surrogate_2_0;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return FHIR_STU3;
  }
}
