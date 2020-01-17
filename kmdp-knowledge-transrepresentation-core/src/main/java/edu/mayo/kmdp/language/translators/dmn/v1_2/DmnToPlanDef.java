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

import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.dmn._20180521.model.TDMNElement.ExtensionElements;
import org.omg.spec.dmn._20180521.model.TDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmnToPlanDef {

  private static final Logger log = LoggerFactory.getLogger(DmnToPlanDef.class);

  public DmnToPlanDef() {
    // nothing to do
  }

  public PlanDefinition transform(URIIdentifier assetId, TDefinitions decisionModel) {

    PlanDefinition cpm = new PlanDefinition();

    mapIdentity(cpm,assetId.getUri(),decisionModel);
    mapName(cpm,decisionModel);
    mapSubject(cpm,decisionModel);

//    Optional<URI> id = this.findCcpmId(tCase.getExtensionElements());
//    URI ccpmId = id.orElseThrow(() -> new RuntimeException("CCPM must have an ID."));
//    cpm.setAssetID(new URIIdentifier().withUri(ccpmId));
//
//    TStage stage = tCase.getCasePlanModel();
//
//    flagTaskDecisions(stage, ccpmId, index);
//
//    Set<CognitiveP
//    rocessResource> cognitiveElements = consolidate(
//        this.process(stage, ccpmId, index));
//
//    cpm.getDecisionTaskDef()
//        .addAll(cognitiveElements.stream().filter(TaskDecision.class::isInstance)
//            .map(TaskDecision.class::cast)
//            .collect(Collectors.toList()));
//    cpm.getCognitiveTaskDef()
//        .addAll(cognitiveElements.stream().filter(CognitiveTask.class::isInstance)
//            .map(CognitiveTask.class::cast)
//            .collect(Collectors.toList()));
//
//    TCaseFile tCaseFile = tCase.getCaseFileModel();
//    if (tCaseFile != null) {
//      Decision caseData = new Decision()
//          .withDecisionName(cpm.getSubject().getLabel() + " - Case Data");
//      caseData.withType(DecisionTypeSeries.Aggregation_Decision, DecisionTypeSeries.Assessment_Decision);
//
//      for (TCaseFileItem tCaseFileItem : tCaseFile.getCaseFileItem()) {
//        this.process(tCaseFileItem,
//            ccpmId,
//            index,
//            cpm::withSubDecisions
////            caseData::withDataInputs,
////            cpm::withKnowledgeSources
//        );
//      }
//
//      if (!caseData.getDataInputs().isEmpty()) {
//        cpm.withSubDecisions(caseData);
//      }
//    }

    return cpm;
  }

  private void mapIdentity(PlanDefinition cpm, URI assetId, TDefinitions decisionModel) {
    // TODO Need formal "Asset ID" and "Artifact ID" roles
    Identifier fhirAssetId = new Identifier()
        .setType(toCode(AnnotationRelTypeSeries.Has_ID.asConcept()))
        .setValue(assetId.toString());

    Identifier fhirArtifactId = new Identifier()
        .setType(toCode(AnnotationRelTypeSeries.Has_ID.asConcept()))
        .setValue(decisionModel.getId());

    cpm.setIdentifier(Arrays.asList(fhirAssetId, fhirArtifactId))
        .setVersion("TODO");
  }

  private void mapSubject(PlanDefinition cpm, TDefinitions tCase) {
    this.findSubject(tCase.getExtensionElements())
        .map(this::toCode)
        .map(Collections::singletonList)
        .ifPresent(cpm::setTopic);
  }

  private CodeableConcept toCode(ConceptIdentifier conceptIdentifier) {
    return new CodeableConcept()
        .setCoding(Collections.singletonList(
            new Coding()
                .setCode(conceptIdentifier.getTag())
                .setDisplay(conceptIdentifier.getLabel())
                .setSystem(conceptIdentifier.getNamespace().getId().toString())
                .setVersion(conceptIdentifier.getNamespace().getVersion())));
  }

  private void mapName(PlanDefinition cpm, TDefinitions tCase) {
    cpm.setName(tCase.getName());
    cpm.setTitle(tCase.getName());
  }

  /*
  private Set<CognitiveProcessResource> consolidate(List<CognitiveProcessResource> resources) {
    Map<URIIdentifier, CognitiveProcessResource> map = new HashMap<>();
    for (CognitiveProcessResource res : resources) {
      if (!map.containsKey(res.getAbstractionOf())) {
        map.put(res.getAbstractionOf(), res);
      }
    }
    return new HashSet<>(map.values());
  }

  private void process(TCaseFileItem caseFileItem, URI id, Index index,
      Consumer<Decision> subDecisionConsumer) {
    String definitionId = caseFileItem.getDefinitionRef().getLocalPart();

    TCaseFileItemDefinition definition = index.get(definitionId, TCaseFileItemDefinition.class);

    Optional<ClinicalSituation> pco = findPco(definition.getExtensionElements());

    // if the ClinicalSituation is present, look for a Decision
    if (pco.isPresent()) {
      org.omg.spec.dmn._20180521.model.TDecision tDecision =
          index.get(pco.get().getRef().toString(), org.omg.spec.dmn._20180521.model.TDecision.class);

      if (tDecision != null) {
        // if the Decision is present, attach it
        subDecisionConsumer.accept(this.process(tDecision, id, index));
      } else {
        // if the Decision is NOT present, attach a 'case-data' decision with the ClinicalSituation as a data input
      }
    } else {
      log.warn(caseFileItem.getName() + " does not have a" +
          "recognized ClinicalSituation.");
    }
  }


  private List<CognitiveProcessResource> process(TStage stage, URI ccpmId, Index index) {
    List<CognitiveProcessResource> decisions = new ArrayList<>();

    for (TPlanItem planItem : stage.getPlanItem()) {
      List<TSentry> sentries = collectSentries(planItem);

      Object definition = planItem.getDefinitionRef();

      List<CognitiveProcessResource> innerDecisions = new ArrayList<>();

      visitDefinition(definition, ccpmId, index, sentries, innerDecisions);

      for (TSentry sentry : sentries) {
        for (CognitiveProcessResource cog : innerDecisions) {
          if (cog instanceof TaskDecision) {
            TaskDecision taskDecision = (TaskDecision) cog;
            processInnerDecision(taskDecision, sentry, index);
          }
        }
      }

      decisions.addAll(innerDecisions);
    }

    return decisions;
  }

  private void processInnerDecision(TaskDecision taskDecision,
      TSentry sentry, Index index) {
    if (sentry.getOnPart() != null && !sentry.getOnPart().isEmpty()) {
      TOnPart onPart = sentry.getOnPart().get(0).getValue();

      if (onPart instanceof TPlanItemOnPart) {
        TPlanItemOnPart planItemOnPartPart = (TPlanItemOnPart) onPart;
        TPlanItem source = (TPlanItem) planItemOnPartPart.getSourceRef();
        TDecision decision = index.get(
            ((TDecisionTask) source.getDefinitionRef()).getDecisionRef().getLocalPart(),
            TDecision.class);

        org.omg.spec.dmn._20180521.model.TDecision externalDecision = index
            .get(decision.getExternalRef().getLocalPart(),
                org.omg.spec.dmn._20180521.model.TDecision.class);

        processExternalDecision(taskDecision,externalDecision);
      }
    }
  }

  private void processExternalDecision(TaskDecision taskDecision,
      org.omg.spec.dmn._20180521.model.TDecision externalDecision) {
    if (externalDecision != null) {
      Optional<ClinicalSituation> pco = findPco(
          externalDecision.getExtensionElements());

      if (pco.isPresent()) {
        ClinicalSituation subject = pco.get();
        if (taskDecision.getReadiness() == null) {
          taskDecision.setReadiness(new ReadinessLogic());
        }
        boolean alreadyAdded = taskDecision.getReadiness().getBlockerConcepts()
            .stream()
            .anyMatch(blocker -> blocker.getRef().equals(subject.getRef()));
        if (!alreadyAdded) {
          taskDecision.getReadiness().getBlockerConcepts().add(subject);
        }
      } else {
        log.warn("Did not find ClinicalSituation for {} ", externalDecision.getName());
      }
    }
  }

  private void visitDefinition(Object definition, URI ccpmId, Index index, List<TSentry> sentries,
      List<CognitiveProcessResource> innerDecisions) {
    if (definition != null) {
      if (definition instanceof TDecisionTask) {
        innerDecisions.add(this.process((TDecisionTask) definition, ccpmId, index));
      } else if (definition instanceof TStage) {
        innerDecisions.addAll(this.process((TStage) definition, ccpmId, index));
      } else if (definition instanceof TProcessTask) {
        // Implement mapping of TProcessTask
      } else if (definition instanceof TEventListener) {
        // Implement mapping of TEventListener
      } else if (definition instanceof THumanTask) {
        innerDecisions.add(this.process((THumanTask) definition, ccpmId, index, sentries));
      } else {
        throw new UnsupportedOperationException(
            "Cannot process " + definition.getClass().getName());
      }
    }
  }

  private List<TSentry> collectSentries(TPlanItem planItem) {
    List<TSentry> sentries = new ArrayList<>();
    if (!planItem.getEntryCriterion().isEmpty()) {
      for (TEntryCriterion entryCriterion : planItem.getEntryCriterion()) {
        sentries.add((TSentry) entryCriterion.getSentryRef());
      }
    }
    return sentries;
  }

  private void flagTaskDecisions(TStage stage, URI ccpmId, Index index) {
    for (TPlanItem planItem : stage.getPlanItem()) {
      Object definition = planItem.getDefinitionRef();
      if (definition != null) {
        if (definition instanceof TDecisionTask) {
          org.omg.spec.dmn._20180521.model.TDecision dmnDecisionElement = getDecisionForTask(
              (TDecisionTask) definition,
              ccpmId,
              index);
          URIIdentifier uriId = uri(
              ccpmId.toString() + "#" + dmnDecisionElement.getOtherAttributes().get(SHAPE_ID));
          index.decisionsAsTask.add(uriId);
        } else if (definition instanceof TStage) {
          flagTaskDecisions((TStage) definition, ccpmId, index);
        }
      }
    }
  }

  private CognitiveTask process(THumanTask tHumanTask, URI ccpmId, Index index,
      List<TSentry> sentries) {
    CognitiveTask ct = new CognitiveTask().withTaskName(tHumanTask.getName())
        .withAbstractionOf(uri(ccpmId.toString() + "#" + tHumanTask.getId()));
    for (TSentry sentry : sentries) {
      sentry.getOnPart().stream()
          .map(JAXBElement::getValue)
          .filter(TCaseFileItemOnPart.class::isInstance)
          .map(TCaseFileItemOnPart.class::cast)
          .map(TCaseFileItemOnPart::getSourceRef)
          .filter(TCaseFileItem.class::isInstance)
          .map(TCaseFileItem.class::cast)
          .forEach(tc -> {
            TCaseFileItemDefinition definition = index
                .get(tc.getDefinitionRef().getLocalPart(), TCaseFileItemDefinition.class);
            Knowledge know = new Knowledge().withLabel(definition.getName())
                .withKnowledgeResName(definition.getName())
                .withAssetIDRef(
                    new URIIdentifier().withUri(URI.create(definition.getDefinitionType())));
            ct.withKnowledgeSources(know);

          });
    }
    return ct;
  }

  private TaskDecision process(TDecisionTask tDecisionTask, URI ccpmId, Index index) {
    org.omg.spec.dmn._20180521.model.TDecision externalDecision = getDecisionForTask(tDecisionTask,
        ccpmId, index);

    TaskDecision returnDecision;
    Decision tmp = this.process(externalDecision, ccpmId, index);
    returnDecision = (TaskDecision) tmp;
    returnDecision.setTaskName(tDecisionTask.getName());
    returnDecision.setRequired(tDecisionTask.getDefaultControl() != null &&
        tDecisionTask.getDefaultControl().getRequiredRule() != null);

    return returnDecision;
  }

  private org.omg.spec.dmn._20180521.model.TDecision getDecisionForTask(TDecisionTask tDecisionTask,
      URI ccpmId, Index index) {
    QName qname = tDecisionTask.getDecisionRef();
    if (qname == null) {
      throw new IllegalStateException();
    }
    String id = qname.getLocalPart();

    TDecision decision = index.get(id, TDecision.class);

    QName externalRef = decision.getExternalRef();
    if (externalRef == null) {
      throw new IllegalStateException("Case " + ccpmId.toString() +
          " : Decision Task " +
          tDecisionTask.getName() +
          " without an associated Decision Model ");
    }

    org.omg.spec.dmn._20180521.model.TDecision externalDecision = index
        .get(externalRef.getLocalPart(),
            org.omg.spec.dmn._20180521.model.TDecision.class);
    if (externalDecision == null) {
      throw new IllegalStateException(
          "Broken decision reference from a CMMN to a DMN model " + externalRef);
    }

    return externalDecision;
  }

  private Decision process(org.omg.spec.dmn._20180521.model.TDecision dmnDecisionElement,
      URI ccpmId,
      Index index) {
    URIIdentifier uriId = uri(
        ccpmId.toString() + "#" + dmnDecisionElement.getOtherAttributes().get(SHAPE_ID));

    if (index.sharableDecisions.containsKey(uriId)) {
      return index.sharableDecisions.get(uriId);
    }

    Decision ccpmDecision =
        index.decisionsAsTask.contains(uriId) ? new TaskDecision() : new Decision();
    index.sharableDecisions.put(uriId, ccpmDecision);

    this.process(ccpmDecision, dmnDecisionElement, ccpmId, index);

    if (ccpmDecision.getProposition() != null
        && index.decisionsByConcept.containsKey(ccpmDecision.getProposition().asEnum())) {
      ccpmDecision = index.decisionsByConcept.get(ccpmDecision.getProposition().asEnum());
      index.sharableDecisions.put(uriId, ccpmDecision);
      return ccpmDecision;
    } else if (ccpmDecision.getProposition() != null) {
      index.decisionsByConcept.put(ccpmDecision.getProposition().asEnum(), ccpmDecision);
    }

    if (ccpmDecision.getDataInputs().isEmpty() && ccpmDecision.getKnowledgeSources().isEmpty()
        && ccpmDecision.getSubDecisions().size() == 1) {
      Decision subDecision = ccpmDecision.getSubDecisions().get(0);
      if (ccpmDecision.getDecisionName().equals(subDecision.getDecisionName())
          && ccpmDecision.getProposition() == subDecision.getProposition()) {
        index.sharableDecisions.put(uriId, subDecision);
        ccpmDecision = subDecision;
      }
    }

    log.info(
        "Created {} Decision {}",
        (ccpmDecision instanceof TaskDecision ? " Task " : ""),
        ccpmDecision.getDecisionName());
    return ccpmDecision;
  }

  private void process(Decision ccpmDecision,
      org.omg.spec.dmn._20180521.model.TDecision dmnDecisionElement,
      URI ccpmId,
      Index index) {
    URIIdentifier uriId = uri(
        ccpmId.toString() + "#" + dmnDecisionElement.getOtherAttributes().get(SHAPE_ID));

    ccpmDecision.withDid(uriId.getUri());
    if (ccpmDecision instanceof TaskDecision) {
      ((TaskDecision) ccpmDecision).withTid(uriId.getUri());
    }

    Optional<ClinicalSituation> pco = findPco(dmnDecisionElement.getExtensionElements());

    String decisionName = dmnDecisionElement.getLabel();

    ccpmDecision.withAbstractionOf(uriId)
        .withQuestion(dmnDecisionElement.getQuestion())
        .withDecisionName(decisionName)
        .withProposition(pco.orElse(null));
    findType(dmnDecisionElement.getExtensionElements()).ifPresent(ccpmDecision::withType);

    ccpmDecision.withSalience(detectSalience(dmnDecisionElement).orElse(0));

    dmnDecisionElement.getAuthorityRequirement()
        .forEach(auth -> this.process(auth, ccpmDecision, index));

    // this processes sub-decisions
    dmnDecisionElement.getInformationRequirement()
        .forEach(info ->
            this.process(ccpmDecision,
                info,
                ccpmId,
                index,
                ccpmDecision::withDataInputs,
                ccpmDecision::withSubDecisions,
                this::setParentApplicabilityCriteria)
        );
    setAdmissibleAnswers(ccpmDecision);
  }

  private Optional<Integer> detectSalience(
      org.omg.spec.dmn._20180521.model.TDecision dmnDecisionElement) {
    return dmnDecisionElement.getExtensionElements() != null
        ? dmnDecisionElement.getExtensionElements().getAny().stream()
        .filter(DatatypeAnnotation.class::isInstance)
        .map(DatatypeAnnotation.class::cast)
        .filter(a -> a.getRel().getRef()
            .equals(URI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C48192")))
        .map(DatatypeAnnotation::getValue)
        .map(Integer::valueOf)
        .findAny()
        : Optional.empty();
  }

  private void setAdmissibleAnswers(Decision returnDecision) {
    if (hasType(returnDecision, DecisionTypeSeries.Choice_Decision)
        && returnDecision.getProposition() != null) {
      // Link data concept definition when supported again
    }
  }

  private void process(TAuthorityRequirement auth, Decision returnDecision, Index index) {
    TDMNElementReference requiredAuthority = auth.getRequiredAuthority();

    if (requiredAuthority != null) {
      TKnowledgeSource knowledgeSource =
          index.get(StringUtils.substringAfter(requiredAuthority.getHref(), "#"),
              TKnowledgeSource.class);
      returnDecision.withKnowledgeSources(new Knowledge().withLabel(knowledgeSource.getLabel())
          .withKnowledgeResName(knowledgeSource.getName())
          .withAssetIDRef(getAssetId(knowledgeSource)));
    }
  }

  private URIIdentifier getAssetId(TKnowledgeSource knowledgeSource) {
    String uri = knowledgeSource.getLocationURI();
    try {
      return Util.isEmpty(uri) ? null : uri(uri);
    } catch (Exception e) {
      Log.warn(CmmnToPlanDef.class, "Unable to set Knowledge Source Asset ID from : " + uri);
      // Handle placeholders that may cause
      return null;
    }
  }

  private void setParentApplicabilityCriteria(TDecisionTable decisionTable, Decision disEnabler,
      Decision target) {
    BasicApplicability logic = new BasicApplicability();

    logic.setEnabler(hasType(disEnabler, DecisionTypeSeries.Enabler_Decision));

    ClinicalSituation pco = disEnabler.getProposition();
    if (pco == null) {
      log.warn("ClinicalSituation for Enabler/Disabler null on: {}", disEnabler.getDecisionName());
    } else {
      logic.setResolvedConcept(pco);
    }

    //only get True rules
    List<TDecisionRule> trueRules = decisionTable.getRule().stream()
        .filter(rule -> rule.getOutputEntry().stream()
            .anyMatch(expr -> expr.getText().equalsIgnoreCase("true")))
        .collect(Collectors.toList());

    trueRules.forEach(rule ->
        rule.getInputEntry().forEach(input ->
            logic.getValues()
                .add(new ConceptIdentifier().withTag(StringUtils.strip(input.getText(), "\"")))
        )
    );

    logic.setAny(!trueRules.isEmpty());

    target.setApplicability(logic);
  }

  private static boolean hasType(Decision ccpmDecision, DecisionType type) {
    return ccpmDecision.getType().contains(type);
  }

  private void process(Decision parent,
      TInformationRequirement info,
      URI ccpmId,
      Index index,
      Consumer<Data> inputHandler,
      Consumer<Decision> subDecisionHandler,
      TriConsumer<TDecisionTable, Decision, Decision> enablerOrDisabler) {

    TDMNElementReference inputHref = info.getRequiredInput();
    if (inputHref != null) {
      org.omg.spec.dmn._20180521.model.TInputData inputData = index
          .get(StringUtils.removeStart(inputHref.getHref(), "#"),
              org.omg.spec.dmn._20180521.model.TInputData.class);
      inputHandler.accept(this.process(inputData, index));
    }

    TDMNElementReference decisionHref = info.getRequiredDecision();
    if (decisionHref != null) {
      org.omg.spec.dmn._20180521.model.TDecision foundDecision = index
          .get(StringUtils.removeStart(decisionHref.getHref(), "#"),
              org.omg.spec.dmn._20180521.model.TDecision.class);

      Decision subDecision = this.process(foundDecision, ccpmId, index);

      if (hasType(subDecision, DecisionTypeSeries.Defeater_Decision) || hasType(subDecision,
          DecisionTypeSeries.Enabler_Decision)) {
        enablerOrDisabler
            .accept((TDecisionTable) foundDecision.getExpression().getValue(), subDecision, parent);
      } else {
        subDecisionHandler.accept(subDecision);
      }
    }
  }

  private Data process(org.omg.spec.dmn._20180521.model.TInputData info, Index index) {
    Data data = new Data();

    String localVar = info.getVariable().getTypeRef();
    TItemDefinition itemDefinition = index.get(localVar, TItemDefinition.class);

    if (itemDefinition != null && itemDefinition.getExtensionElements() != null) {
      findPco(itemDefinition.getExtensionElements())
          .ifPresent((pco -> {
            // Set data concept definition when supported
          }));
    }

    if (Util.isEmpty(data.getInputName())) {
      data.setInputName(info.getLabel());
    }

    return data;
  }


  private Optional<List<DecisionType>> findType(TDMNElement.ExtensionElements extensionElements) {
    if (extensionElements != null) {
      List<SimpleAnnotation> simpleAnnotations =
          extensionElements.getAny().stream()
              .filter(e -> e instanceof SimpleAnnotation)
              .map(e -> (SimpleAnnotation) e)
              .filter(annotation -> annotation.getRel() != null
                  && annotation.getRel().getRef() != null)
              .filter(annotation -> annotation.getRel().getRef().equals(
                  AnnotationRelTypeSeries.Knowledge_Representation_Of.getRef()))
              .collect(Collectors.toList());

      List<DecisionType> types = new ArrayList<>();

      for (SimpleAnnotation simpleAnnotation : simpleAnnotations) {
        DecisionTypeSeries.resolveRef(simpleAnnotation.getExpr().getRef().toString())
            .ifPresent(types::add);
      }

      List<MultiwordAnnotation> multiwordAnnotations =
          extensionElements.getAny().stream()
              .filter(e -> e instanceof MultiwordAnnotation)
              .map(e -> (MultiwordAnnotation) e)
              .filter(annotation -> annotation.getRel() != null
                  && annotation.getRel().getRef() != null)
              .filter(annotation -> annotation.getRel().getRef()
                  .equals(AnnotationRelTypeSeries.Knowledge_Representation_Of.getRef()))
              .collect(Collectors.toList());

      for (MultiwordAnnotation multiwordAnnotation : multiwordAnnotations) {
        for (ConceptIdentifier conceptIdentifier : multiwordAnnotation.getExpr()) {
          DecisionTypeSeries.resolveRef(conceptIdentifier.getRef().toString()).ifPresent(types::add);
        }
      }

      if (!types.isEmpty()) {
        return Optional.of(types);
      }
    }

    return Optional.empty();
  }

  private Optional<URI> findCcpmId(TExtensionElements extensionElements) {
    if (extensionElements != null) {
      List<BasicAnnotation> annotations = extensionElements.getAny().stream()
          .filter(e -> e instanceof BasicAnnotation)
          .map(e -> (BasicAnnotation) e)
          .filter(
              annotation -> annotation.getRel() != null && annotation.getRel().getRef() != null)
          .filter(annotation -> annotation.getRel().getRef().equals(
              AnnotationRelTypeSeries.Has_ID.getRef())).collect(Collectors.toList());

      if (annotations.size() > 1) {
        throw new IllegalStateException("Cannot have more than one subject.");
      }

      if (annotations.size() == 1) {
        BasicAnnotation annotation = annotations.get(0);
        return Optional.of(annotation.getExpr());
      }
    }

    return Optional.empty();
  }



  private static Optional<ClinicalSituation> findPco(TExtensionElements extensionElements) {
    return findPco(extensionElements.getAny());
  }

  private static Optional<ClinicalSituation> findPco(
      TDMNElement.ExtensionElements extensionElements) {
    return findPco(extensionElements.getAny());
  }

  private static Optional<ClinicalSituation> findPco(List<Object> extensionElements) {
    if (extensionElements == null || extensionElements.isEmpty()) {
      return Optional.empty();
    }

    return extensionElements.stream()
        .filter(SimpleAnnotation.class::isInstance)
        .map(SimpleAnnotation.class::cast)
        .filter(ann -> AnnotationRelTypeSeries.Captures.getRef().equals(ann.getRel().getRef()))
        .map(SimpleAnnotation::getExpr)
        .map(ClinicalSituationSeries::resolve)
        .filter(Optional::isPresent).map(Optional::get)
        .findAny();

  }

/*
   */

  private Optional<ConceptIdentifier> findSubject(ExtensionElements extensionElements) {
    return this.findSubject(extensionElements.getAny());
  }

  private Optional<ConceptIdentifier> findSubject(List<Object> extensionElements) {
    if (extensionElements != null) {
      List<SimpleAnnotation> annotations = extensionElements.stream()
          .filter(e -> e instanceof SimpleAnnotation)
          .map(e -> (SimpleAnnotation) e)
          .filter(
              annotation -> annotation.getRel() != null && annotation.getRel().getRef() != null)
          .filter(annotation -> annotation.getRel().getRef()
              .equals(AnnotationRelTypeSeries.Has_Subject.getRef()))
          .collect(Collectors.toList());

      if (annotations.size() > 1) {
        throw new IllegalStateException("Cannot have more than one subject.");
      }

      if (annotations.size() == 1) {
        SimpleAnnotation annotation = annotations.get(0);
        return Optional.of(annotation.getExpr());
      }
    }

    return Optional.empty();
  }



}