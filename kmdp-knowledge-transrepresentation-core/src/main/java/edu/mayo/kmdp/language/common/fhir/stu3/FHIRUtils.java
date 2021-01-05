package edu.mayo.kmdp.language.common.fhir.stu3;

import edu.mayo.kmdp.util.StreamUtil;
import java.net.URI;
import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

public final class FHIRUtils {

  private FHIRUtils() {
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
    // TODO fixme add constructor that supports label
    ConceptIdentifier cid =
        (ConceptIdentifier) Term.newTerm(URI.create(c.getSystem()), c.getCode(), c.getVersion());
    cid.withName(c.getDisplay());
    return cid;
  }


  /**
   * Traverses a PlanDefintion, returning a Stream of the nested Action Components
   * @param planDef the root PlanDefinition
   * @return a Stream of the nested Action Components
   */
  public static Stream<PlanDefinitionActionComponent> getNestedActions(PlanDefinition planDef) {
    return planDef.getAction().stream()
        .flatMap(FHIRUtils::getNestedActions);
  }

  /**
   * Traverses a PlanDefintion with nested (contained) PlanDefinitions
   * @param planDef the root PlanDefinition
   * @return a Stream of the Action components, across all nested PlanDefinitions
   */
  public static Stream<PlanDefinitionActionComponent> getDeepNestedActions(PlanDefinition planDef) {
    return getNestedPlanDefs(planDef)
        .flatMap(FHIRUtils::getNestedActions);
  }

  private static Stream<? extends PlanDefinitionActionComponent> getNestedActions(
      PlanDefinitionActionComponent act) {
    return Stream.concat(
        Stream.of(act),
        act.getAction().stream().flatMap(FHIRUtils::getNestedActions));
  }

  /**
   * Traverses a PlanDefintion with nested (contained) PlanDefinitions
   * @param planDef the root PlanDefinition
   * @return a Stream of the nested PlanDefinitions
   */
  public static Stream<PlanDefinition> getNestedPlanDefs(PlanDefinition planDef) {
    return Stream.concat(Stream.of(planDef),
        planDef.getContained().stream()
            .flatMap(StreamUtil.filterAs(PlanDefinition.class))
            .flatMap(FHIRUtils::getNestedPlanDefs));
  }

}
