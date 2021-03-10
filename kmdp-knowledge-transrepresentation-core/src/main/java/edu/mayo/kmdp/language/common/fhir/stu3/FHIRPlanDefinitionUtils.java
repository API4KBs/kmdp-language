package edu.mayo.kmdp.language.common.fhir.stu3;

import static edu.mayo.kmdp.language.common.fhir.stu3.FHIRVisitor.getContained;
import static edu.mayo.kmdp.util.StreamUtil.filterAs;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.omg.spec.api4kp._20200801.id.Term;

public final class FHIRPlanDefinitionUtils {

  private FHIRPlanDefinitionUtils() {
    // static functions only
  }

  public static CodeableConcept toCodeableConcept(Term trm) {
    return new CodeableConcept()
        .addCoding(toCoding(trm));
  }

  public static Coding toCoding(Term trm) {
    return new Coding()
        .setCode(trm.getTag())
        .setDisplay(trm.getLabel())
        .setSystem(trm.getNamespaceUri().toString())
        .setVersion(trm.getVersionTag());
  }

  public static Term toTerm(Coding c) {
    return Term.newTerm(
        URI.create(c.getSystem()),
        c.getCode(),
        c.getVersion(),
        c.getDisplay());
  }


  /**
   * Traverses a PlanDefintion, returning a Stream of the nested Action Components
   * @param planDef the root PlanDefinition
   * @return a Stream of the nested Action Components
   */
  public static Stream<PlanDefinitionActionComponent> getSubActions(PlanDefinition planDef) {
    return planDef.getAction().stream()
        .flatMap(FHIRPlanDefinitionUtils::getSubActions);
  }

  /**
   * Traverses a PlanDefintion with nested (contained) PlanDefinitions
   * @param planDef the root PlanDefinition
   * @return a Stream of the Action components, across all nested PlanDefinitions
   */
  public static Stream<PlanDefinitionActionComponent> getDeepNestedSubActions(PlanDefinition planDef) {
    return getNestedPlanDefs(planDef)
        .flatMap(FHIRPlanDefinitionUtils::getSubActions);
  }

  private static Stream<? extends PlanDefinitionActionComponent> getSubActions(
      PlanDefinitionActionComponent act) {
    return Stream.concat(
        Stream.of(act),
        act.getAction().stream().flatMap(FHIRPlanDefinitionUtils::getSubActions));
  }


  /**
   * Retrieves the action in a PlanDefinition that has the given title
   * The search is recursive within a PD, but does not extend to nested PlanDefinitions
   * @param planDefinition
   * @param title
   * @return
   */
  public static Optional<PlanDefinitionActionComponent> getSubActionByTitle(
      PlanDefinition planDefinition, String title) {
    return getSubActions(planDefinition)
        .filter(act -> title.equalsIgnoreCase(act.getTitle()) || title.equalsIgnoreCase(act.getLabel()))
        .findFirst();
  }

  /**
   * Traverses a PlanDefintion with nested (contained) PlanDefinitions
   * @param planDef the root PlanDefinition
   * @return a Stream of the nested PlanDefinitions
   */
  public static Stream<PlanDefinition> getNestedPlanDefs(PlanDefinition planDef) {
    return getContained(planDef, PlanDefinition.class);
  }


  public static <T> Optional<T> resolveInternalReference(
      DomainResource root, Reference ref, Class<T> tgtClass) {
    return getContained(root, DomainResource.class)
        .filter(x -> ref.getReference().contains(x.getId()))
        .flatMap(filterAs(tgtClass))
        .findFirst();
  }
}
