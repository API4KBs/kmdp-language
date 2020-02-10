package edu.mayo.kmdp.language.translators.fhir.stu3.structDef;

import static edu.mayo.kmdp.comparator.Contrastor.isBroaderOrEqual;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.tranx.v3.server.TransxionApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;

/**
 * AST to AST Translator that translates Information Model Assets,
 *   from : FHIR 3.0.2 | StructureDefinition
 *   into : DMN 1.2 | TypeDefinition + FEEL
 *
 */
@Named
@KPOperation(KnowledgeProcessingOperationSeries.Translation_Task)
public class StructDefToDMNProjectingTranslator implements TransxionApiInternal {

  private static final String OPERATOR_ID = "7cf2a645-c489-48a6-af77-06ef4a08b623";

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentation(String txId,
      KnowledgeCarrier sourceArtifact, Properties params) {
    return OPERATOR_ID.equals(txId)
        ? doTranslate(sourceArtifact)
        : Answer.unsupported();
  }

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentationInto(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return isBroaderOrEqual(
        SyntacticRepresentationContrastor.theRepContrastor
            .contrast(into, getTargetRepresentation()))
        ? doTranslate(sourceArtifact)
        : Answer.unsupported();
  }

  private Answer<KnowledgeCarrier> doTranslate(KnowledgeCarrier sourceArtifact) {
    return Answer.of(sourceArtifact.as(StructureDefinition.class)
        .map(sd -> new StructDefToDMN().transformRootElementToFrame(sd))
        .map(AbstractCarrier::ofAst)
        .map(kc -> kc.withRepresentation(getTargetRepresentation())
            // This transformation is lossy
            .withAssetId(DatatypeHelper.uri(UUID.randomUUID().toString()))
            .withLabel(sourceArtifact.getLabel())
            .withArtifactId(DatatypeHelper.uri(UUID.randomUUID().toString()))
        )
    );
  }

  @Override
  public Answer<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return Answer.of(
        new org.omg.spec.api4kp._1_0.services.tranx.resources.TransrepresentationOperator()
            .withOperatorId(OPERATOR_ID)
            .withAcceptedParams(getTransrepresentationAcceptedParameters(txionId).orElse(null))
            .withFrom(getTransrepresentationInput(txionId).orElse(null))
            .withInto(getTransrepresentationOutput(txionId).orElse(null)));
  }

  @Override
  public Answer<ParameterDefinitions> getTransrepresentationAcceptedParameters(String txId) {
    return Answer.of(new ParameterDefinitions());
  }



  public Answer<org.omg.spec.api4kp._1_0.services.SyntacticRepresentation> getTransrepresentationInput(String txionId) {
    if (txionId != null && !OPERATOR_ID.equals(txionId)) {
      return Answer.failed(new UnsupportedOperationException());
    }
    return Answer.of(
        rep(FHIR_STU3));
  }


  @Override
  public Answer<SyntacticRepresentation> getTransrepresentationOutput(String txId) {
    return Answer.of(getTargetRepresentation());
  }

  private SyntacticRepresentation getTargetRepresentation() {
    return rep(KnowledgeRepresentationLanguageSeries.DMN_1_2);
  }

  @Override
  public Answer<List<TransrepresentationOperator>> listOperators(SyntacticRepresentation from,
      SyntacticRepresentation into, String method) {
    return getTransrepresentation(OPERATOR_ID)
        .map(Collections::singletonList);
  }
}
