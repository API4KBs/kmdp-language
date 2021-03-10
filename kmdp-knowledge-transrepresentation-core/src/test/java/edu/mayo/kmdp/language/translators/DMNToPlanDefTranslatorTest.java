package edu.mayo.kmdp.language.translators;

import static edu.mayo.kmdp.language.common.fhir.stu3.FHIRPlanDefinitionUtils.getSubActions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.of;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel._20200801.ParsingLevel.Abstract_Knowledge_Expression;

import edu.mayo.kmdp.language.parsers.dmn.v1_2.DMN12Parser;
import edu.mayo.kmdp.language.translators.dmn.v1_2.DmnToPlanDef;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.dmn._20180521.model.TDefinitions;

public class DMNToPlanDefTranslatorTest {

  @Test
  void testDecisionService() {
    DmnToPlanDef translator = new DmnToPlanDef();
    TDefinitions source = loadDMN("/fhir.stu3/DMNService.dmn.xml");
    PlanDefinition planDef = translator.transform(randomId(),source);

    assertEquals(1,planDef.getAction().size());
    PlanDefinitionActionComponent clientAct = planDef.getActionFirstRep();
    assertEquals(1, clientAct.getAction().size());
    PlanDefinitionActionComponent serverAct = clientAct.getActionFirstRep();

    assertEquals(3, serverAct.getInput().size());
    assertEquals(1, serverAct.getOutput().size());
  }


  @Test
  void testKnowledgeSource() {
    DmnToPlanDef translator = new DmnToPlanDef();
    TDefinitions source = loadDMN("/fhir.stu3/decisionKnowSource.dmn.xml");
    PlanDefinition planDef = translator.transform(randomId(),source);

    List<RelatedArtifact> relateds = getKnowledgeSources(planDef).collect(Collectors.toList());
    assertEquals(1, relateds.size());
    RelatedArtifact related = relateds.get(0);
    assertEquals("https://foo.bar/baz", related.getUrl());
    assertEquals("https://foo.bar/baz", related.getDocument().getUrl());
    assertEquals("Sample Source", related.getDisplay());
    assertEquals("Sample Source", related.getDocument().getTitle());
    assertEquals("*/*", related.getDocument().getContentType());

  }

  private Stream<RelatedArtifact> getKnowledgeSources(PlanDefinition x) {
    return getSubActions(x)
        .flatMap(act -> act.getDocumentation().stream());
  }

  private TDefinitions loadDMN(String path) {
    KnowledgeCarrier kc = of(DMNToPlanDefTranslatorTest.class.getResourceAsStream(path))
        .withRepresentation(rep(DMN_1_2,XML_1_1, Charset.defaultCharset(), Encodings.DEFAULT));
    return new DMN12Parser()
        .applyLift(kc, Abstract_Knowledge_Expression, codedRep(DMN_1_2), null)
        .flatOpt(x -> x.as(TDefinitions.class))
        .orElseGet(Assertions::fail);
  }
}
