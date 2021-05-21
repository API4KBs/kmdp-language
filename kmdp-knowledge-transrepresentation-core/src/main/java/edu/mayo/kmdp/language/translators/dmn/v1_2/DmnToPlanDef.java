/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language.translators.dmn.v1_2;

import static edu.mayo.kmdp.language.common.fhir.stu3.FHIRPlanDefinitionUtils.toCodeableConcept;
import static edu.mayo.kmdp.util.NameUtils.nameToIdentifier;
import static edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries.Captures;
import static edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries.Defines;
import static edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries.Has_Primary_Subject;
import static edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries.In_Terms_Of;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newVersionId;

import edu.mayo.kmdp.language.common.fhir.stu3.FHIRPlanDefinitionUtils;
import edu.mayo.kmdp.util.NameUtils.IdentifierType;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBElement;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DataRequirement;
import org.hl7.fhir.dstu3.model.DataRequirement.DataRequirementCodeFilterComponent;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.ActionRelationshipType;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.dmn._20180521.model.TAuthorityRequirement;
import org.omg.spec.dmn._20180521.model.TBusinessKnowledgeModel;
import org.omg.spec.dmn._20180521.model.TDMNElement.ExtensionElements;
import org.omg.spec.dmn._20180521.model.TDMNElementReference;
import org.omg.spec.dmn._20180521.model.TDRGElement;
import org.omg.spec.dmn._20180521.model.TDecision;
import org.omg.spec.dmn._20180521.model.TDecisionService;
import org.omg.spec.dmn._20180521.model.TDefinitions;
import org.omg.spec.dmn._20180521.model.TInformationRequirement;
import org.omg.spec.dmn._20180521.model.TInputData;
import org.omg.spec.dmn._20180521.model.TKnowledgeRequirement;
import org.omg.spec.dmn._20180521.model.TKnowledgeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmnToPlanDef {

  private static final Logger log = LoggerFactory.getLogger(DmnToPlanDef.class);

  public DmnToPlanDef() {
    // nothing to do
  }

  public PlanDefinition transform(ResourceIdentifier assetId, TDefinitions decisionModel) {
    log.debug("Called Translator DMN to PDF  for {}", decisionModel.getName());
    PlanDefinition cpm = new PlanDefinition();

    mapIdentity(cpm, assetId.getResourceId(), decisionModel);
    mapName(cpm, decisionModel);
    mapSubject(cpm, decisionModel);

    mapDecisions(cpm, decisionModel);

    log.debug("Finished DMN to PDef  for {} ", decisionModel.getName());
    return cpm;
  }

  private void mapDecisions(PlanDefinition cpm,
      TDefinitions decisionModel) {

    Collection<TDecisionService> dmnDecisionServices = streamDecisionServices(decisionModel)
        .collect(Collectors.toList());

    Collection<TDecision> dmnDecisions = streamDecisions(decisionModel)
        .filter(dec -> dmnDecisionServices.stream()
            .noneMatch(ds -> isDecisionServiceScoped(dec, ds)))
        .collect(Collectors.toList());

    Map<String, PlanDefinitionActionComponent> mappedDecisions = new HashMap<>();
    dmnDecisions.stream()
        .map(decision -> processDecision(cpm, cpm::addAction, decisionModel, decision))
        .forEach(act -> mappedDecisions.putIfAbsent(act.getId(), act));

    Map<String, PlanDefinitionActionComponent> mappedDecisionServices = new HashMap<>();
    dmnDecisionServices.stream()
        .map(decisionService -> processDecisionService(cpm, decisionModel, decisionService))
        .forEach(act -> mappedDecisionServices.putIfAbsent(act.getId(), act));

    dmnDecisions
        .forEach(decision -> processDecisionToDecisionDependencies(
            cpm,
            mappedDecisions,
            decision,
            decisionModel));

    dmnDecisions
        .forEach(decision -> processDecisionToDecisionServiceDependencies(
            cpm,
            mappedDecisions,
            mappedDecisionServices,
            decision,
            decisionModel));

    dmnDecisionServices
        .forEach(ds -> processDecisionServiceToDecisionServiceDependencies(
            cpm,
            mappedDecisions,
            mappedDecisionServices,
            ds,
            decisionModel));
  }

  private boolean isDecisionServiceScoped(TDecision dec, TDecisionService ds) {
    boolean isOutput = ds.getOutputDecision().stream()
        .anyMatch(ref -> joins(dec.getId(), ref.getHref()));
    boolean isEncapsulated = ds.getEncapsulatedDecision().stream()
        .anyMatch(ref -> joins(dec.getId(), ref.getHref()));
    boolean isInput = ds.getInputDecision().stream()
        .anyMatch(ref -> joins(dec.getId(), ref.getHref()));
    return isOutput || isEncapsulated || isInput;
  }

  private void processDecisionToDecisionDependencies(
      PlanDefinition cpm,
      Map<String, PlanDefinitionActionComponent> mappedDecisions,
      TDecision dmnDecision,
      TDefinitions decisionModel) {

    PlanDefinitionActionComponent srcAction =
        mappedDecisions.get(asId(dmnDecision.getId()));

    dmnDecision.getInformationRequirement().stream()
        .filter(info -> info.getRequiredDecision() != null)
        .map(info -> {
          URI ref = URI.create(info.getRequiredDecision().getHref());
          if (!Util.isEmpty(ref.getPath())) {
            PlanDefinitionActionComponent pdact = (PlanDefinitionActionComponent) new PlanDefinitionActionComponent()
                .setDefinition(
                    new Reference()
                        .setReference(URIUtil.normalizeURIString(ref))
                        .setDisplay(info.getLabel())
                        .setIdentifier(new Identifier()
                            .setType(new CodeableConcept()
                                .setText("TODO - Knowledge Artifact Fragment Identifier"))
                            .setValue(asId(ref.getFragment())))
                ).setId(asId(ref.getFragment()));
            addToScope(pdact, cpm.getAction(), cpm::addAction);
          }
          return ref.getFragment();
        })
        .forEach(tgtActionId -> srcAction.addRelatedAction()
            .setRelationship(ActionRelationshipType.AFTER)
            .setActionId(idToRef(tgtActionId)));
  }

  private void processDecisionToDecisionServiceDependencies(
      PlanDefinition cpm,
      Map<String, PlanDefinitionActionComponent> mappedDecisions,
      Map<String, PlanDefinitionActionComponent> mappedDecisionServices,
      TDecision dmnDecision,
      TDefinitions decisionModel) {

    PlanDefinitionActionComponent srcAction =
        mappedDecisions.get(asId(dmnDecision.getId()));

    List<PlanDefinitionActionComponent> serviceClientAction = dmnDecision.getKnowledgeRequirement()
        .stream()
        .filter(know -> know.getRequiredKnowledge() != null)
        .flatMap(ref -> lookupAsDecisionService(ref, mappedDecisionServices)
            .or(() -> lookupViaBKM(decisionModel, ref, mappedDecisionServices))
            .stream())
        .collect(Collectors.toList());

    serviceClientAction.forEach(x -> addToScope(x, srcAction.getAction(), srcAction::addAction));
  }

  private void processDecisionServiceToDecisionServiceDependencies(
      PlanDefinition cpm,
      Map<String, PlanDefinitionActionComponent> mappedDecisions,
      Map<String, PlanDefinitionActionComponent> mappedDecisionServices,
      TDecisionService dmnDecisionService,
      TDefinitions decisionModel) {

    PlanDefinitionActionComponent srcAction =
        mappedDecisionServices.get(asId(dmnDecisionService.getId()));

    List<TDecision> serviceDecisions = dmnDecisionService.getOutputDecision().stream()
        .map(out -> streamDecisions(decisionModel)
            .filter(dec -> joins(dec.getId(), out.getHref()))
            .findFirst().orElseThrow())
        .flatMap(dec -> closeDecisionDependencies(dec, decisionModel))
        .distinct()
        .collect(Collectors.toList());
    serviceDecisions
        .forEach(subDec -> {
          subDec.getKnowledgeRequirement().stream()
              .filter(know -> know.getRequiredKnowledge() != null)
              .flatMap(ref -> lookupAsDecisionService(ref, mappedDecisionServices)
                  .or(() -> lookupViaBKM(decisionModel, ref, mappedDecisionServices))
                  .stream())
              .forEach(subAct -> {
                addToScope(subAct, srcAction.getAction(), srcAction::addAction);
                // remap the children's outputs as parent's inputs
                subAct.getOutput().forEach(out -> srcAction.getInput().add(out));
              });
        });
  }

  private Stream<TDecision> closeDecisionDependencies(
      TDecision dec, TDefinitions decisionModel) {
    return Stream.concat(
        Stream.of(dec),
        getSubDecisions(dec, decisionModel)
            .flatMap(d -> closeDecisionDependencies(d, decisionModel)));
  }

  private Stream<TDecision> getSubDecisions(
      TDecision dec, TDefinitions decisionModel) {
    return dec.getInformationRequirement().stream()
        .map(TInformationRequirement::getRequiredDecision) // exclude inputs
        .filter(Objects::nonNull)
        .map(ref -> streamDecisions(decisionModel)
            .filter(d -> joins(d.getId(), ref.getHref()))
            .findFirst().orElseThrow());
  }

  private Optional<PlanDefinitionActionComponent> lookupViaBKM(
      TDefinitions decisionModel,
      TKnowledgeRequirement ref,
      Map<String, PlanDefinitionActionComponent> mappedDecisionServices) {
    return lookupBKM(decisionModel, ref.getRequiredKnowledge())
        .flatMap(bkm -> bkm.getKnowledgeRequirement().stream())
        .filter(know -> know.getRequiredKnowledge() != null)
        .map(TKnowledgeRequirement::getRequiredKnowledge)
        .map(info -> mappedDecisionServices.get(
            refToId(info.getHref())))
        .findAny();
  }

  private Optional<PlanDefinitionActionComponent> lookupAsDecisionService(TKnowledgeRequirement ref,
      Map<String, PlanDefinitionActionComponent> mappedDecisionServices) {
    return Optional
        .ofNullable(mappedDecisionServices.get(refToId(ref.getRequiredKnowledge().getHref())));
  }

  private Stream<TBusinessKnowledgeModel> lookupBKM(TDefinitions decisionModel,
      TDMNElementReference requiredKnowledge) {
    return streamBKM(decisionModel)
        .filter(bkm -> refToId(requiredKnowledge.getHref()).equals(bkm.getId()));
  }

  private void mapIdentity(PlanDefinition cpm, URI assetId, TDefinitions decisionModel) {
    Identifier fhirAssetId = new Identifier()
        .setType(toCodeableConcept(SemanticAnnotationRelTypeSeries.Is_Identified_By))
        .setValue(assetId.toString());

    cpm.setIdentifier(Collections.singletonList(fhirAssetId))
        .setVersion("TODO");

    cpm.setType(toCodeableConcept(KnowledgeAssetTypeSeries.Decision_Model));
    cpm.setId("#" + decisionModel.getNamespace());
  }

  private PlanDefinitionActionComponent processDecision(
      PlanDefinition cpm,
      Consumer<PlanDefinitionActionComponent> cpmScope,
      TDefinitions decisionModel, TDecision decision) {
    PlanDefinitionActionComponent decisionAction = new PlanDefinitionActionComponent();
    decisionAction.setTitle(decision.getName());
    decisionAction.setId(asId(decision.getId()));

    decisionAction.setType(new Coding()
        .setSystem(
            KnowledgeRepresentationLanguageSerializationSeries.DMN_1_2_XML_Syntax.getReferentId()
                .toString())
        .setCode("Decision")
    );

    getSemanticAnnotation(decision.getExtensionElements()).stream()
        .map(FHIRPlanDefinitionUtils::toCodeableConcept)
        .forEach(decisionAction::addCode);

    decision.getAuthorityRequirement()
        .forEach(know -> mapKnowledgeSource(know, cpm, decisionAction, decisionModel));

    decision.getInformationRequirement().stream()
        .filter(info -> info.getRequiredInput() != null)
        .map(info -> findInput(info.getRequiredInput(), decisionModel))
        .flatMap(StreamUtil::trimStream)
        .map(this::mapInput)
        .forEach(decisionAction::addInput);

    addToScope(decisionAction, cpm.getAction(), cpmScope);
    return decisionAction;
  }


  private PlanDefinitionActionComponent processDecisionService(PlanDefinition cpm,
      TDefinitions decisionModel, TDecisionService decisionService) {
    PlanDefinitionActionComponent serviceAction = new PlanDefinitionActionComponent();
    serviceAction.setTitle(decisionService.getName());
    serviceAction.setId(asId(decisionService.getId()));

    serviceAction.setType(new Coding()
        .setSystem(
            KnowledgeRepresentationLanguageSerializationSeries.DMN_1_2_XML_Syntax.getReferentId()
                .toString())
        .setCode("DecisionService")
    );

    getSemanticAnnotation(decisionService.getExtensionElements()).stream()
        .map(FHIRPlanDefinitionUtils::toCodeableConcept)
        .filter(cd -> isKMConcept(cd))
        .forEach(serviceAction::addCode);

    decisionService.getOutputDecision().forEach(
        out -> {
          TDecision outputDecision = findDecision(out, decisionModel)
              .orElseThrow();
          mapOutput(outputDecision, serviceAction);
          outputDecision.getAuthorityRequirement().forEach(
              ks -> mapKnowledgeSource(ks, cpm, serviceAction, decisionModel)
          );
        }
    );

    decisionService.getInputData().forEach(
        input -> {
          TInputData inputData = findInput(input, decisionModel)
              .orElseThrow();
          serviceAction.addInput(mapInput(inputData));
        }
    );

    return serviceAction;
  }


  private DataRequirement mapInput(TInputData input) {
    DataRequirement dataRequirement = new DataRequirement();
    DataRequirementCodeFilterComponent codeFilters = new DataRequirementCodeFilterComponent();
    dataRequirement.addCodeFilter(codeFilters);
//    dataRequirement.setType("TODO Data Shape - Base Resource type");
//    dataRequirement.setProfile(Collections.singletonList(new UriType("http://todo.me/datashape/profile123")));

    getSemanticAnnotation(input.getExtensionElements()).stream()
        .map(FHIRPlanDefinitionUtils::toCodeableConcept)
        .forEach(codeFilters::addValueCodeableConcept);
    return dataRequirement;
  }

  private DataRequirement mapOutput(TDecision output,
      PlanDefinitionActionComponent serviceAction) {
    DataRequirement dataRequirement = new DataRequirement();
    DataRequirementCodeFilterComponent codeFilters = new DataRequirementCodeFilterComponent();
    dataRequirement.addCodeFilter(codeFilters);

    getSemanticAnnotation(output.getExtensionElements()).stream()
        .map(FHIRPlanDefinitionUtils::toCodeableConcept)
        .filter(cd -> !isKMConcept(cd))
        .forEach(codeFilters::addValueCodeableConcept);
    getSemanticAnnotation(output.getExtensionElements()).stream()
        .map(FHIRPlanDefinitionUtils::toCodeableConcept)
        .filter(cd -> isKMConcept(cd))
        .forEach(serviceAction::addCode);

    serviceAction.addOutput(dataRequirement);
    return dataRequirement;
  }

  private boolean isKMConcept(CodeableConcept cd) {
    String codeSystem = cd.getCodingFirstRep().getSystem();
    boolean isDecisionType = DecisionTypeSeries.schemeSeriesIdentifier.getNamespaceUri().toString()
        .equals(codeSystem);
    boolean isKnowledgeAssetType = KnowledgeAssetTypeSeries.schemeSeriesIdentifier.getNamespaceUri()
        .toString()
        .equals(codeSystem);
    return isDecisionType || isKnowledgeAssetType;
  }

  private Optional<TInputData> findInput(TDMNElementReference requiredInput,
      TDefinitions decisionModel) {
    return decisionModel.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TInputData.class))
        .filter(in -> joins(in.getId(), requiredInput.getHref()))
        .findFirst();
  }


  private Optional<TDecision> findDecision(TDMNElementReference requiredDecision,
      TDefinitions decisionModel) {
    return decisionModel.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TDecision.class))
        .filter(in -> joins(in.getId(), requiredDecision.getHref()))
        .findFirst();
  }


  private Optional<TKnowledgeSource> findKnowledgeSource(TDMNElementReference requiredSource,
      TDefinitions decisionModel) {
    return decisionModel.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TKnowledgeSource.class))
        .filter(in -> in.getId().equals(requiredSource.getHref()
            .substring(requiredSource.getHref().lastIndexOf('#') + 1)))
        .findFirst();
  }

  private void mapKnowledgeSource(TAuthorityRequirement know,
      PlanDefinition cpm, PlanDefinitionActionComponent decisionAction,
      TDefinitions decisionModel) {
    findKnowledgeSource(know.getRequiredAuthority(), decisionModel)
        .ifPresent(knowledgeSource ->
            mapKnowledgeSource(knowledgeSource, cpm, decisionAction));
  }

  private void mapKnowledgeSource(TKnowledgeSource knowledgeSource,
      PlanDefinition cpm, PlanDefinitionActionComponent decisionAction) {
    if (Util.isEmpty(knowledgeSource.getLocationURI())) {
      return;
    }

    RelatedArtifact relatedArtifact = new RelatedArtifact()
        .setUrl(knowledgeSource.getLocationURI())
        .setDisplay(knowledgeSource.getName())
        .setDocument(new Attachment()
            .setTitle(knowledgeSource.getName())
            .setUrl(knowledgeSource.getLocationURI())
            .setContentType(knowledgeSource.getType()));

    decisionAction.addDocumentation(relatedArtifact);

    if (knowledgeSource.getExtensionElements() != null &&
        knowledgeSource.getExtensionElements().getAny() != null) {
      List<Term> types = knowledgeSource.getExtensionElements().getAny().stream()
          .flatMap(StreamUtil.filterAs(Annotation.class))
          .map(Annotation::getRef)
          .collect(Collectors.toList());

      if (!types.isEmpty()) {
        CodeableConcept typesCC = toCodeableConcept(types);
        String id = asId(knowledgeSource.getId());
        ResourceIdentifier knowAssetId = newVersionId(URI.create(knowledgeSource.getLocationURI()));
        Library lib = (Library) new Library()
            .addIdentifier(new Identifier()
                .setSystem(knowAssetId.getNamespaceUri().toString())
                .setValue(knowAssetId.getTag() + ":" + knowAssetId.getVersionTag()))
            .setType(typesCC)
            .setId(id);

        relatedArtifact.setResource(new Reference().setReference(idToRef(id)));
        relatedArtifact.addExtension(new Extension()
            .setUrl("http://kmd.mayo.edu/fhirExtensions/knowledgeAssetType")
            .setValue(typesCC));

        cpm.addContained(lib);
      }
    }

  }

  private void mapSubject(PlanDefinition cpm, TDefinitions tCase) {
    this.findSubject(tCase.getExtensionElements())
        .map(FHIRPlanDefinitionUtils::toCodeableConcept)
        .map(Collections::singletonList)
        .ifPresent(cpm::setTopic);
  }


  private void mapName(PlanDefinition cpm, TDefinitions tCase) {
    cpm.setName(nameToIdentifier(tCase.getName(), IdentifierType.CLASS));
    cpm.setTitle(tCase.getName());
  }


  private static Collection<Term> getSemanticAnnotation(ExtensionElements extensionElements) {
    return extensionElements == null
        ? Collections.emptyList()
        : getSemanticAnnotation(extensionElements.getAny());
  }

  private static List<Term> getSemanticAnnotation(List<Object> extensionElements) {
    if (extensionElements == null || extensionElements.isEmpty()) {
      return Collections.emptyList();
    }

    return extensionElements.stream()
        .flatMap(StreamUtil.filterAs(Annotation.class))
        .filter(ann ->
            ann.getRel() == null
                || Defines.getTag().equals(ann.getRel().getTag())
                || Captures.getTag().equals(ann.getRel().getTag())
                || In_Terms_Of.getTag().equals(ann.getRel().getTag())
        )
        .map(Annotation::getRef)
        .map(Term.class::cast)
        .collect(Collectors.toList());
  }


  private Optional<ConceptIdentifier> findSubject(ExtensionElements extensionElements) {
    return this.findSubject(extensionElements.getAny());
  }

  private Optional<ConceptIdentifier> findSubject(List<Object> extensionElements) {
    if (extensionElements != null) {
      List<Annotation> annotations = extensionElements.stream()
          .filter(e -> e instanceof Annotation)
          .map(e -> (Annotation) e)
          .filter(
              annotation -> annotation.getRel() != null
                  && annotation.getRel().getReferentId() != null)
          .filter(annotation -> annotation.getRel().getReferentId()
              .equals(Has_Primary_Subject.getReferentId()))
          .collect(Collectors.toList());

      if (annotations.size() > 1) {
        throw new IllegalStateException("Cannot have more than one subject.");
      }

      if (annotations.size() == 1) {
        Annotation annotation = annotations.get(0);
        return Optional.of(annotation.getRef());
      }
    }

    return Optional.empty();
  }

  private void addToScope(PlanDefinitionActionComponent action,
      List<PlanDefinitionActionComponent> context,
      Consumer<PlanDefinitionActionComponent> scope) {
    boolean contained = context.stream()
        .anyMatch(act -> act.getId().equals(action.getId()));
    if (contained) {
      System.out.println("Double adding of " + action.getTitle() + " | " + action.getLabel());
    } else {
      System.out.println("Initial adding of " + action.getTitle() + " | " + action.getLabel());
    }
    scope.accept(action);
  }

  private Stream<TDecision> streamDecisions(TDefinitions dmn) {
    return streamDRG(dmn, TDecision.class);
  }

  private Stream<TInputData> streamInputs(TDefinitions dmn) {
    return streamDRG(dmn, TInputData.class);
  }

  private Stream<TBusinessKnowledgeModel> streamBKM(TDefinitions dmn) {
    return streamDRG(dmn, TBusinessKnowledgeModel.class);
  }

  private Stream<TDecisionService> streamDecisionServices(TDefinitions dmn) {
    return streamDRG(dmn, TDecisionService.class);
  }

  private <T extends TDRGElement> Stream<T> streamDRG(TDefinitions dmn, Class<T> drgType) {
    return dmn.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(drgType));
  }

  private String refToId(String href) {
    if (!href.startsWith("#")) {
      throw new IllegalStateException(
          "Reference " + href + " expected to be a relative URI fragment");
    }
    return asId(href.substring(1));
  }

  private String idToRef(String id) {
    return "#" + asId(id);
  }

  private String asId(String id) {
    return id.trim().replace("_", "");
  }

  private boolean joins(String pk, String fkHref) {
    return asId(pk).equals(refToId(fkHref));
  }


}