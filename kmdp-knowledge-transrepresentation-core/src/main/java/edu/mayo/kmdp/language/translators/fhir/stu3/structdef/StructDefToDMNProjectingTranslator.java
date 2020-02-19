package edu.mayo.kmdp.language.translators.fhir.stu3.structdef;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Translation_Task;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import java.util.Properties;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

/**
 * AST to AST Translator that translates Information Model Assets,
 *   from : FHIR 3.0.2 | StructureDefinition
 *   into : DMN 1.2 | TypeDefinition + FEEL
 *
 */
@Named
@KPOperation(Translation_Task)
@KPSupport({FHIR_STU3,DMN_1_2})
public class StructDefToDMNProjectingTranslator extends AbstractSimpleTranslator {

  private static final String OPERATOR_ID = "7cf2a645-c489-48a6-af77-06ef4a08b623";

  @Override
  public String getId() {
    return OPERATOR_ID;
  }

  @Override
  public SyntacticRepresentation getFrom() {
    return new SyntacticRepresentation()
        .withLanguage(FHIR_STU3);
  }

  @Override
  public SyntacticRepresentation getTo() {
    return new SyntacticRepresentation()
        .withLanguage(DMN_1_2);
  }

  @Override
  protected KnowledgeCarrier doTransform(KnowledgeCarrier sourceArtifact, Properties props) {
    return sourceArtifact.as(StructureDefinition.class)
        .map(sd -> new StructDefToDMN().transformRootElementToFrame(sd))
        .map(AbstractCarrier::ofAst)
        .orElseThrow(IllegalStateException::new);
  }

}
