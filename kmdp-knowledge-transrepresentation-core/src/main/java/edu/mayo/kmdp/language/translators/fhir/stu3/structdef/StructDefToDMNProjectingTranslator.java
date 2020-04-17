package edu.mayo.kmdp.language.translators.fhir.stu3.structdef;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Translation_Task;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20180521.model.TDefinitions;

/**
 * AST to AST Translator that translates Information Model Assets,
 *   from : FHIR 3.0.2 | StructureDefinition
 *   into : DMN 1.2 | TypeDefinition + FEEL
 *
 */
@Named
@KPOperation(Translation_Task)
@KPSupport({FHIR_STU3,DMN_1_2})
public class StructDefToDMNProjectingTranslator extends AbstractSimpleTranslator<StructureDefinition,TDefinitions> {

  public static final UUID id = UUID.fromString("7cf2a645-c489-48a6-af77-06ef4a08b623");
  public static final String version = "1.0.0";

  public StructDefToDMNProjectingTranslator() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(FHIR_STU3));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(DMN_1_2));
  }

  @Override
  protected Optional<TDefinitions> transformAst(ResourceIdentifier assetId,
      StructureDefinition expression, SyntacticRepresentation tgtRep) {
    return Optional.ofNullable(new StructDefToDMN().transformRootElementToFrame(expression));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return FHIR_STU3;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return DMN_1_2;
  }

}
