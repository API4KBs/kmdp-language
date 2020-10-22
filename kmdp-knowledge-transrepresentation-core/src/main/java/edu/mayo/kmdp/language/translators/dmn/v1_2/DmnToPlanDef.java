/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.language.translators.dmn.v1_2;

import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DataRequirement;
import org.hl7.fhir.dstu3.model.DataRequirement.DataRequirementCodeFilterComponent;
import org.hl7.fhir.dstu3.model.Element;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.ActionRelationshipType;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.UriType;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.dmn._20180521.model.TAuthorityRequirement;
import org.omg.spec.dmn._20180521.model.TDMNElement.ExtensionElements;
import org.omg.spec.dmn._20180521.model.TDMNElementReference;
import org.omg.spec.dmn._20180521.model.TDecision;
import org.omg.spec.dmn._20180521.model.TDefinitions;
import org.omg.spec.dmn._20180521.model.TInputData;
import org.omg.spec.dmn._20180521.model.TKnowledgeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmnToPlanDef {

  private static final Logger log = LoggerFactory.getLogger(DmnToPlanDef.class);

  public DmnToPlanDef() {
    // nothing to do
  }

  public PlanDefinition transform(ResourceIdentifier assetId, TDefinitions decisionModel) {
    log.debug("Called Translator DMN to PDF  for {}",decisionModel.getName() );
    PlanDefinition cpm = new PlanDefinition();

    mapIdentity(cpm,assetId.getResourceId(),decisionModel);
    mapName(cpm,decisionModel);
    mapSubject(cpm,decisionModel);

    mapDecisions(cpm,decisionModel);

    log.debug("Finished DMN to PDef  for {} ", decisionModel.getName() );
    return cpm;
  }

  private void mapDecisions(PlanDefinition cpm,
      TDefinitions decisionModel) {

    Collection<TDecision> dmnDecisions = decisionModel.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TDecision.class))
        .collect(Collectors.toList());

    Map<String, PlanDefinitionActionComponent> mappedDecisions = dmnDecisions.stream()
        .map(decision -> processDecision(cpm,decisionModel,decision))
        .collect(Collectors.toMap(
            Element::getId,
            Function.identity()
        ));


    dmnDecisions.stream()
        .forEach(decision -> processDecisionDependencies(cpm, mappedDecisions,decision));

  }

  private void processDecisionDependencies(
      PlanDefinition cpm,
      Map<String, PlanDefinitionActionComponent> mappedDecisions, TDecision dmnDecision) {

    PlanDefinitionActionComponent srcAction = mappedDecisions.get(
        "#" + dmnDecision.getId().replace("_",""));

    dmnDecision.getInformationRequirement().stream()
        .filter(info -> info.getRequiredDecision() != null)
        .map(info -> {
          URI ref = URI.create(info.getRequiredDecision().getHref());
          if (!Util.isEmpty(ref.getPath())) {
            cpm.addAction((PlanDefinitionActionComponent) new PlanDefinitionActionComponent()
                .setDefinition(
                    new Reference()
                        .setReference(URIUtil.normalizeURIString(ref))
                        .setDisplay(info.getLabel())
                        .setIdentifier(new Identifier()
                            .setType(new CodeableConcept().setText("TODO - Knowledge Artifact Fragment Identifier"))
                            .setValue(ref.getFragment().replace("_","")))
                ).setId(ref.getFragment().replace("_",""))
            );
          }
          return ref.getFragment();
        })
        .forEach(tgtActionId -> srcAction.addRelatedAction()
            .setRelationship(ActionRelationshipType.AFTER)
            .setActionId("#" + tgtActionId.replace("_","")));
  }

  private void mapIdentity(PlanDefinition cpm, URI assetId, TDefinitions decisionModel) {
    Identifier fhirAssetId = new Identifier()
        .setType(toCode(SemanticAnnotationRelTypeSeries.Is_Identified_By))
        .setValue(assetId.toString());

    cpm.setIdentifier(Collections.singletonList(fhirAssetId))
        .setVersion("TODO");

    cpm.setType(toCode(KnowledgeAssetTypeSeries.Decision_Model));
    cpm.setId("#" + decisionModel.getNamespace());
  }

  private PlanDefinitionActionComponent processDecision(PlanDefinition cpm,
      TDefinitions decisionModel, TDecision decision) {
    PlanDefinitionActionComponent decisionAction = new PlanDefinitionActionComponent();
    decisionAction.setLabel(decision.getName());
    decisionAction.setId("#" + decision.getId().replace("_",""));

    getSemanticAnnotation(decision.getExtensionElements()).stream()
        .map(this::toCode)
        .forEach(decisionAction::addCode);

    decision.getAuthorityRequirement()
        .forEach(know -> mapKnowledgeSource(know,decisionAction,decisionModel));

    decision.getInformationRequirement().stream()
        .filter(info -> info.getRequiredInput() != null)
        .map(info -> findInput(info.getRequiredInput(),decisionModel))
        .flatMap(StreamUtil::trimStream)
        .map(this::mapInput)
        .forEach(decisionAction::addInput);

    cpm.addAction(decisionAction);
    return decisionAction;
  }


  private DataRequirement mapInput(TInputData input) {
    DataRequirement dataRequirement = new DataRequirement();
    DataRequirementCodeFilterComponent codeFilters = new DataRequirementCodeFilterComponent();
    dataRequirement.addCodeFilter(codeFilters);
    dataRequirement.setType("TODO Data Shape - Base Resource type");
    dataRequirement.setProfile(Collections.singletonList(new UriType("http://todo.me/datashape/profile123")));

    getSemanticAnnotation(input.getExtensionElements()).stream()
        .map(this::toCode)
        .forEach(codeFilters::addValueCodeableConcept);
    return dataRequirement;
  }

  private Optional<TInputData> findInput(TDMNElementReference requiredInput, TDefinitions decisionModel) {
    return decisionModel.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TInputData.class))
        // TODO Fix this ID mess....
        .filter(in ->
            in.getId()
                .replace("#", "")
                .replace("_", "")
                .equals(
                    requiredInput.getHref()
                        .substring(requiredInput.getHref().lastIndexOf('#'))
                        .replace("#", "")
                        .replace("_", "")
                ))
        .findFirst();
  }

  private Optional<TKnowledgeSource> findKnowledgeSource(TDMNElementReference requiredSource, TDefinitions decisionModel) {
    return decisionModel.getDrgElement().stream()
        .map(JAXBElement::getValue)
        .flatMap(StreamUtil.filterAs(TKnowledgeSource.class))
        .filter(in -> in.getId().equals(requiredSource.getHref()
            .substring(requiredSource.getHref().lastIndexOf('#') + 1)))
        .findFirst();
  }

  private void mapKnowledgeSource(TAuthorityRequirement know,
      PlanDefinitionActionComponent decisionAction, TDefinitions decisionModel) {
    findKnowledgeSource(know.getRequiredAuthority(), decisionModel)
        .ifPresent(knowledgeSource ->
            decisionAction.addDocumentation(
                new RelatedArtifact()
                    .setUrl(knowledgeSource.getLocationURI())
                    .setDisplay(knowledgeSource.getName())));
  }

  private void mapSubject(PlanDefinition cpm, TDefinitions tCase) {
    this.findSubject(tCase.getExtensionElements())
        .map(this::toCode)
        .map(Collections::singletonList)
        .ifPresent(cpm::setTopic);
  }

  private CodeableConcept toCode(Term cid) {
    return new CodeableConcept()
        .setCoding(Collections.singletonList(
            new Coding()
                .setCode("_" + cid.getUuid().toString())
                .setDisplay(cid.getLabel())
                .setSystem(cid.getNamespaceUri().toString())
                .setVersion(cid.getVersionTag())));
  }

  private void mapName(PlanDefinition cpm, TDefinitions tCase) {
    cpm.setName(tCase.getName());
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
                    || AnnotationRelTypeSeries.Defines.getTag().equals(ann.getRel().getTag())
                    || AnnotationRelTypeSeries.Captures.getTag().equals(ann.getRel().getTag())
                    || AnnotationRelTypeSeries.In_Terms_Of.getTag().equals(ann.getRel().getTag())
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
              annotation -> annotation.getRel() != null && annotation.getRel().getReferentId() != null)
          .filter(annotation -> annotation.getRel().getReferentId()
              .equals(AnnotationRelTypeSeries.Has_Primary_Subject.getReferentId()))
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



}