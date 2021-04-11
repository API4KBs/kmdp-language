package edu.mayo.kmdp.language.validators.cmmn.v1_1;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Care_Process_Model;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Well_Formedness_Check_Task;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;

import edu.mayo.kmdp.util.StreamUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import javax.xml.bind.JAXBElement;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.cmmn._20151109.model.TAssociation;
import org.omg.spec.cmmn._20151109.model.TCase;
import org.omg.spec.cmmn._20151109.model.TCaseFile;
import org.omg.spec.cmmn._20151109.model.TCaseFileItem;
import org.omg.spec.cmmn._20151109.model.TCaseFileItemDefinition;
import org.omg.spec.cmmn._20151109.model.TCmmnElement;
import org.omg.spec.cmmn._20151109.model.TDecision;
import org.omg.spec.cmmn._20151109.model.TDecisionTask;
import org.omg.spec.cmmn._20151109.model.TDefinitions;
import org.omg.spec.cmmn._20151109.model.TExtensionElements;
import org.omg.spec.cmmn._20151109.model.TMilestone;
import org.omg.spec.cmmn._20151109.model.TPlanItem;
import org.omg.spec.cmmn._20151109.model.TPlanItemOnPart;
import org.omg.spec.cmmn._20151109.model.TSentry;
import org.omg.spec.cmmn._20151109.model.TStage;
import org.omg.spec.cmmn._20151109.model.TTask;

@Named
@KPOperation(Well_Formedness_Check_Task)
@KPSupport(CMMN_1_1)
public class CCPMProfileCMMNValidator extends CCPMComponentValidator {

  public static final UUID id = UUID.fromString("10ea04ea-c6b0-4dc6-9032-5e52fe1971f5");
  public static final String version = "1.0.0";

  private ResourceIdentifier operatorId;

  public CCPMProfileCMMNValidator() {
    this.operatorId = SemanticIdentifier.newId(id, version);
  }

  @Override
  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return CMMN_1_1;
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return Collections.singletonList(rep(CMMN_1_1));
  }

  @Override
  protected Answer<Void> validate(KnowledgeAsset knowledgeAsset, KnowledgeCarrier carrier) {
    return allOf(
        validateAssetId(knowledgeAsset, carrier),
        validateAssetVersion(knowledgeAsset, carrier),
        validateArtifactVersion(knowledgeAsset, carrier),
        validateAssetType(knowledgeAsset, carrier, Care_Process_Model),
        validatePublicationStatus(knowledgeAsset, carrier)
    );
  }

  @Override
  protected Answer<Void> validate(TDefinitions caseModel, KnowledgeCarrier carrier) {
    return allOf(
        validateTaskTypes(caseModel, carrier),
        validateDecisionTaskLinks(caseModel, carrier),
        validateCaseFileItems(caseModel, carrier),
        validateMilestones(caseModel, carrier)
    );
  }


  /**
   * Validates that each Decision Task is linked to an external (Decision) model
   *
   * @param caseModel
   * @param carrier
   * @return
   */
  protected Answer<Void> validateDecisionTaskLinks(TDefinitions caseModel,
      KnowledgeCarrier carrier) {
    List<TTask> decisionTaskWithoutLink =
        new CaseTaskCollector().visit(caseModel).stream()
            .flatMap(StreamUtil.filterAs(TDecisionTask.class))
            .filter(dTask ->
                dTask.getDecisionRef() != null &&
                    caseModel.getDecision().stream()
                        .filter(dec -> dec.getId().equals(dTask.getDecisionRef().getLocalPart()))
                        .map(TDecision::getExternalRef)
                        .anyMatch(Objects::isNull))
            .collect(Collectors.toList());

    boolean success = decisionTaskWithoutLink.isEmpty();
    return validationResponse(
        carrier,
        success,
        "D-Task Links",
        () -> "all Linked",
        () -> "MISSING Links " + toString(decisionTaskWithoutLink, TTask::getName)
    );
  }

  /**
   * Validates that each task is annotated with a Task Type from the CTO
   *
   * @param caseModel
   * @param carrier
   * @return
   */
  protected Answer<Void> validateTaskTypes(TDefinitions caseModel, KnowledgeCarrier carrier) {
    List<TTask> tasksWithoutAnnotation =
        new CaseTaskCollector().visitCase(caseModel).stream()
            .filter(task -> !hasTaskTypeAnnotation(task))
            .collect(Collectors.toList());

    boolean success = tasksWithoutAnnotation.isEmpty();
    return validationResponse(
        carrier,
        success,
        "Task Types",
        () -> "all Present",
        () -> "TASKS with no Type " + toString(tasksWithoutAnnotation, TTask::getName)
    );
  }

  /**
   * Data Case file item is annotated with the concepts it resolves
   * Task which creates generates the data
   * has input and output bound to the name of the case file item
   *
   * @param caseModel
   * @param carrier
   * @return
   */
  private Answer<Void> validateMilestones(TDefinitions caseModel, KnowledgeCarrier carrier) {
    Set<TTask> tasks = new CaseTaskCollector().visitCase(caseModel);
    Set<TMilestone> miles
        = new CaseMilestoneCollector().visitCase(caseModel);
    Map<TMilestone, String> partial = new HashMap<>();

    // Check connected to Task
    miles.stream()
        .filter(m -> !isLinked(m, tasks, caseModel))
        .forEach(m ->
            addInMap(m, partial, "Link", (x, y) -> String.join("/", x, y)));

    // Check for Annotation
    miles.stream()
        .filter(m -> m.getExtensionElements().getAny().stream()
            .noneMatch(Annotation.class::isInstance))
        .forEach(m ->
            addInMap(m, partial, "Conceot", (x, y) -> String.join("/", x, y)));

    return validationResponse(
        carrier,
        partial.isEmpty(),
        "Milest",
        () -> miles.isEmpty() ? "none" : "all configured",
        () -> "PARTIAL " + toString(partial.keySet(), c -> c.getName() + ":" + partial.get(c))
    );
  }

  private boolean isLinked(TMilestone miles,
      Set<TTask> tasks, TDefinitions caseModel) {
    Set<TPlanItem> planItems = new CasePlanItemCollector().visitCase(caseModel);

    // Get the PlanItems that reference a Milestone
    Optional<TPlanItem> milPlanItem = planItems.stream()
        .filter(pi -> pi.getDefinitionRef() == miles)
        .findFirst();
    if (milPlanItem.isEmpty()) {
      return false;
    }

    // Get the Sentry whose OnPart
    //  references a PlanItem that references a Milestone
    Set<TSentry> milestoneSentries = new CaseSentryCollector().visitCase(caseModel).stream()
        .filter(s -> s.getOnPart().stream()
            .map(JAXBElement::getValue)
            .flatMap(StreamUtil.filterAs(TPlanItemOnPart.class))
            .anyMatch(on -> on.getSourceRef() == milPlanItem.get()))
        .collect(Collectors.toSet());

    // Get the PlanItems whose Entry/Exit criterion is
    //   a Sentry whose OnPart
    //     references a PlanItem that references a Milestone
    Set<TPlanItem> milestoneLinkedItems = planItems.stream()
        .filter(pi ->
            pi.getEntryCriterion().stream()
                .anyMatch(in -> milestoneSentries.stream().anyMatch(s -> s == in.getSentryRef()))
            || pi.getExitCriterion().stream()
                .anyMatch(out -> milestoneSentries.stream().anyMatch(s -> s == out.getSentryRef())))
        .collect(Collectors.toSet());

    // See if any Task is the definition of
    //   a PlanItem whose Entry/Exit criterion is
    //     a Sentry whose OnPart
    //       references a PlanItem that references a Milestone
    return milestoneLinkedItems.stream()
        .anyMatch(pi ->
            tasks.stream().anyMatch(t -> t == pi.getDefinitionRef()));
  }


  /**
   * Data Case file item is annotated with the concepts it resolves
   * Task which creates generates the data
   * has input and output bound to the name of the case file item
   *
   * @param caseModel
   * @param carrier
   * @return
   */
  private Answer<Void> validateCaseFileItems(TDefinitions caseModel, KnowledgeCarrier carrier) {
    Set<TTask> tasks = new CaseTaskCollector().visitCase(caseModel);
    Map<TCaseFileItem, TCaseFileItemDefinition> cfis
        = new CaseFileItemCollector().visitCase(caseModel);

    Map<TCaseFileItem, String> partial = new HashMap<>();

    // Check connected to Task
    cfis.keySet().stream()
        .filter(cfi -> !isLinked(cfi, tasks, caseModel))
        .forEach(cfi ->
            addInMap(cfi, partial, "Link", (x, y) -> String.join("/", x, y)));

    // If document, must have a asset:UUID
    cfis.keySet().stream()
        .filter(cfi -> isDocument(cfis.get(cfi)))
        .filter(cfi -> cfis.get(cfi).getStructureRef() == null)
        .forEach(cfi ->
            addInMap(cfi, partial, "assetUUID", (x, y) -> String.join("/", x, y)));

    // If input/output, must have a Concept annotation
    cfis.keySet().stream()
        .filter(cfi -> isInputOutput(cfi, tasks))
        .filter(cfi -> cfi.getExtensionElements().getAny().stream()
            .noneMatch(Annotation.class::isInstance))
        .forEach(cfi ->
            addInMap(cfi, partial, "Concept", (x, y) -> String.join("/", x, y)));

    // Check either input/output or document
    cfis.keySet().stream()
        .filter(cfi -> ! isDocument(cfis.get(cfi)))
        .filter(cfi -> ! isInputOutput(cfi, tasks))
        .forEach(cfi ->
            addInMap(cfi, partial, "Type", (x, y) -> String.join("/", x, y)));

    return validationResponse(
        carrier,
        partial.isEmpty(),
        "CFIs",
        () -> cfis.isEmpty() ? "none" : "all configured",
        () -> "PARTIAL " + toString(partial.keySet(), c -> c.getName() + ":" + partial.get(c))
    );
  }

  private boolean isInputOutput(TCaseFileItem cfi, Set<TTask> tasks) {
    return tasks.stream().anyMatch(
        task -> task.getInput() != null && task.getInput().stream()
            .anyMatch(in -> cfi == in.getBindingRef()))
        && tasks.stream().anyMatch(
            task -> task.getOutput() != null && task.getOutput().stream()
                .anyMatch(out -> cfi == out.getBindingRef()));
  }

  private boolean isLinked(TCaseFileItem cfi,
      Set<TTask> tasks, TDefinitions caseModel) {
    return caseModel.getArtifact().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TAssociation.class))
        .anyMatch(assoc ->
            pointsTo(assoc, cfi)
            && tasks.stream()
                .anyMatch(t -> pointsTo(assoc, t)));
  }

  private boolean pointsTo(TAssociation assoc, TCaseFileItem cfi) {
    return assoc.getTargetRef() == cfi || assoc.getSourceRef() == cfi;
  }

  private boolean pointsTo(TAssociation assoc, TMilestone miles) {
    return assoc.getTargetRef() == miles || assoc.getSourceRef() == miles;
  }

  private boolean pointsTo(TAssociation assoc, TTask cfi) {
    return (assoc.getTargetRef() instanceof TPlanItem
        && ((TPlanItem) assoc.getTargetRef()).getDefinitionRef() == cfi)
        || (assoc.getSourceRef() instanceof TPlanItem
        && ((TPlanItem) assoc.getSourceRef()).getDefinitionRef() == cfi);
  }

  private boolean isDocument(TCaseFileItemDefinition cfid) {
    return "http://www.omg.org/spec/CMMN/DefinitionType/CMISDocument"
        .equals(cfid.getDefinitionType());
  }

  private boolean hasTaskTypeAnnotation(TTask task) {
    return Optional.ofNullable(task.getExtensionElements())
        .stream()
        .map(TExtensionElements::getAny)
        .flatMap(Collection::stream)
        .flatMap(StreamUtil.filterAs(Annotation.class))
        .map(Annotation::getRef)
        .map(ResourceIdentifier::getNamespaceUri)
        .anyMatch(ns -> "https://ontology.mayo.edu/taxonomies/ClinicalTasks".equals(ns.toString()));
  }

  private <X,V> void addInMap(X key, Map<X, V> map, V val, BinaryOperator<V> joiner) {
    map.compute(key, (k,v) -> v == null ? val : joiner.apply(v,val));
  }

  private abstract class CaseCollector<T extends TCmmnElement> {

    Set<T> elements = new HashSet<>();

    protected Set<T> visit(TDefinitions caseModel) {
      caseModel.getCase().forEach(this::visit);
      return elements;
    }

    protected void visit(TCase caseElement) {
      visit(caseElement.getCasePlanModel());
    }

    protected void visit(TStage stage) {
      stage.getPlanItem().stream()
          .map(TPlanItem::getDefinitionRef)
          .flatMap(StreamUtil.filterAs(TStage.class))
          .forEach(this::visit);
      collect(stage);
    }

    protected void collect(TStage stage, Class<T> type) {
      stage.getPlanItemDefinition().stream()
          .map(JAXBElement::getValue)
          .flatMap(StreamUtil.filterAs(type))
          .forEach(elements::add);
    }

    protected abstract void collect(TStage stage);
  }

  private class CaseFileItemCollector extends CaseCollector<TCaseFileItem> {

    public Map<TCaseFileItem, TCaseFileItemDefinition> visitCase(TDefinitions caseModel) {
      caseModel.getCase().forEach(this::visit);
      return elements.stream()
          .collect(Collectors.toMap(
              cfi -> cfi,
              cfi -> getDefinition(cfi, caseModel)
          ));
    }

    protected void visit(TCase caseElement) {
      super.visit(caseElement);
      visit(caseElement.getCaseFileModel());
    }

    private void visit(TCaseFile caseFileModel) {
      if (caseFileModel != null && caseFileModel.getCaseFileItem() != null) {
        elements.addAll(caseFileModel.getCaseFileItem());
      }
    }

    private TCaseFileItemDefinition getDefinition(TCaseFileItem cfi, TDefinitions caseModel) {
      return caseModel.getCaseFileItemDefinition().stream()
          .filter(cfid -> cfid.getId().equals(cfi.getDefinitionRef().getLocalPart()))
          .findFirst().orElseThrow();
    }

    @Override
    protected void collect(TStage stage) {
      collect(stage, TCaseFileItem.class);
    }

  }


  private class CaseMilestoneCollector extends CaseCollector<TMilestone> {

    public Set<TMilestone> visitCase(TDefinitions caseModel) {
      caseModel.getCase().forEach(this::visit);
      return new HashSet<>(elements);
    }

    @Override
    protected void collect(TStage stage) {
      collect(stage, TMilestone.class);
    }

  }


  private class CasePlanItemCollector extends CaseCollector<TPlanItem> {

    public Set<TPlanItem> visitCase(TDefinitions caseModel) {
      caseModel.getCase().forEach(this::visit);
      return new HashSet<>(elements);
    }

    @Override
    protected void collect(TStage stage) {
      elements.addAll(stage.getPlanItem());
    }

  }

  private class CaseTaskCollector extends CaseCollector<TTask> {

    public Set<TTask> visitCase(TDefinitions caseModel) {
      return visit(caseModel);
    }

    @Override
    protected void collect(TStage stage) {
      collect(stage, TTask.class);
    }
  }

  private class CaseSentryCollector extends CaseCollector<TSentry> {

    public Set<TSentry> visitCase(TDefinitions caseModel) {
      return visit(caseModel);
    }

    @Override
    protected void collect(TStage stage) {
      elements.addAll(stage.getSentry());
    }
  }
}
