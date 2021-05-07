package edu.mayo.kmdp.language.validators.fhir.stu3;

import static edu.mayo.kmdp.language.common.fhir.stu3.FHIRPlanDefinitionUtils.getNestedPlanDefs;
import static edu.mayo.kmdp.language.common.fhir.stu3.FHIRPlanDefinitionUtils.getSubActions;
import static edu.mayo.kmdp.language.common.fhir.stu3.FHIRPlanDefinitionUtils.toDisplayTerms;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newVersionId;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Cognitive_Care_Process_Model;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Well_Formedness_Check_Task;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;

import edu.mayo.kmdp.language.validators.cmmn.v1_1.CCPMComponentValidator;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;

@Named
@KPOperation(Well_Formedness_Check_Task)
@KPSupport(FHIR_STU3)
public class CCPMProfilePlanDefinitionValidator extends CCPMComponentValidator {

  public static final UUID id = UUID.fromString("7e2bceb4-632c-4361-b9db-ea8a84c09eff");
  public static final String version = "1.0.0";

  private ResourceIdentifier operatorId;

  public CCPMProfilePlanDefinitionValidator() {
    this.operatorId = SemanticIdentifier.newId(id, version);
  }

  @Override
  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return FHIR_STU3;
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return Collections.singletonList(rep(FHIR_STU3));
  }

  @Override
  protected Answer<Void> validate(KnowledgeAsset knowledgeAsset, KnowledgeCarrier carrier) {
    // validate the metadata in the Surrogate
    return Stream.of(
        validateAssetId(knowledgeAsset, carrier),
        validateAssetVersion(knowledgeAsset, carrier),
        validateArtifactVersion(knowledgeAsset, carrier),
        validateAssetType(knowledgeAsset, carrier, Cognitive_Care_Process_Model),
        validatePublicationStatus(knowledgeAsset, carrier)
    ).reduce(Answer::merge).orElseGet(Answer::failed);
  }

  @Override
  protected Answer<Void> validate(PlanDefinition rootPlanDef,
      KnowledgeCarrier carrier) {
    return Stream.of(
            validateId(rootPlanDef, carrier),
            validateNameTitle(rootPlanDef, carrier),
            validateType(rootPlanDef, carrier),
            validateActionTypes(rootPlanDef, carrier)
//        ,
//            validateDefinitionRefs(rootPlanDef, carrier),
//            validateRelatedAction(rootPlanDef, carrier),
//            validateDocumentation(rootPlanDef, carrier),
//            validateInputOutput(rootPlanDef, carrier)
        ).reduce(Answer::merge).orElseGet(Answer::failed);
  }

  /**
   * Ensure that the Asset ID is set as one of the PlanDef business Identifier
   * @param rootPlanDef
   * @param carrier
   * @return
   */
  private Answer<Void> validateId(PlanDefinition rootPlanDef, KnowledgeCarrier carrier) {
    boolean success = getNestedPlanDefs(rootPlanDef)
        .allMatch(this::hasEnterpriseId);

    return validationResponse(
        carrier,
        success,
        "Component Asset IDs",
        () -> "all components have Asset ID",
        () -> "Missing Asset IDs"
    );
  }

  private boolean hasEnterpriseId(PlanDefinition pd) {
    if (pd.getIdentifier().isEmpty()) {
      return false;
    }
    return pd.getIdentifier().stream()
        .anyMatch(idtf ->
            Util.isNotEmpty(idtf.getValue())
                && idtf.getValue().contains(Registry.MAYO_ASSETS_BASE_URI));
  }


  /**
   * Ensure that Title exists, and Name is a sanitized String
   * @param rootPlanDef
   * @param carrier
   * @return
   */
  private Answer<Void> validateNameTitle(PlanDefinition rootPlanDef, KnowledgeCarrier carrier) {
    return getNestedPlanDefs(rootPlanDef)
        .map(pd -> {
          boolean hasTitle = pd.getTitle() != null;
          boolean hasName = pd.getName() != null
              && Util.ensureUTF8(pd.getName()).equals(pd.getName());

          return validationResponse(
              carrier,
              kc -> mapAssetId(kc, pd.getIdentifier().get(0)),
              hasTitle && hasName,
              "Name / Title",
              () -> "title: " + pd.getTitle(),
              () -> "invalid name: " + pd.getName());
        }).reduce(Answer::merge)
        .orElseGet(Answer::failed);
  }


  /**
   * Ensure that Title exists, and Name is a sanitized String
   * @param rootPlanDef
   * @param carrier
   * @return
   */
  private Answer<Void> validateType(PlanDefinition rootPlanDef, KnowledgeCarrier carrier) {
    return getNestedPlanDefs(rootPlanDef)
        .map(pd -> {
          boolean success = ! pd.getType().getCoding().isEmpty();

          return validationResponse(
              carrier,
              kc -> mapAssetId(kc, pd.getIdentifier().get(0)),
              success,
              "PlanDef Type",
              () -> toDisplayTerms(pd.getType()),
              () -> "NO PlanDef Type found");
        }).reduce(Answer::merge)
        .orElseGet(Answer::failed);
  }

  /**
   * Ensure that Title exists, and Name is a sanitized String
   * @param rootPlanDef
   * @param carrier
   * @return
   */
  private Answer<Void> validateActionTypes(PlanDefinition rootPlanDef, KnowledgeCarrier carrier) {
    return getNestedPlanDefs(rootPlanDef)
        .map(pd -> {
          List<PlanDefinitionActionComponent> untypedActions =
              getSubActions(pd)
                  .filter(act -> act.getType().isEmpty())
                  .collect(Collectors.toList());

          return validationResponse(
              carrier,
              kc -> mapAssetId(kc, pd.getIdentifier().get(0)),
              untypedActions.isEmpty(),
              "Action Type",
              () -> "All actions have types",
              () -> "Untyped : " + untypedActions.stream()
                  .map(PlanDefinitionActionComponent::getTitle).collect(Collectors.joining(",")));
        }).reduce(Answer::merge)
        .orElseGet(Answer::failed);
  }

  private String mapAssetId(KnowledgeCarrier kc, Identifier identifier) {
    return mapResourceId(kc.getAssetId(),1)
        + "|"
        + mapResourceId(newVersionId(URI.create(identifier.getValue())),3);
  }


}
