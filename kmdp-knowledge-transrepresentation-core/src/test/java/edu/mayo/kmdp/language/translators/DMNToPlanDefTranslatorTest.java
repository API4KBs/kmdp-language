package edu.mayo.kmdp.language.translators;

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
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.PlanDefinition.PlanDefinitionActionComponent;
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

  private TDefinitions loadDMN(String path) {
    KnowledgeCarrier kc = of(DMNToPlanDefTranslatorTest.class.getResourceAsStream(path))
        .withRepresentation(rep(DMN_1_2,XML_1_1, Charset.defaultCharset(), Encodings.DEFAULT));
    return new DMN12Parser()
        .applyLift(kc, Abstract_Knowledge_Expression, codedRep(DMN_1_2), null)
        .flatOpt(x -> x.as(TDefinitions.class))
        .orElseGet(Assertions::fail);
  }
}
