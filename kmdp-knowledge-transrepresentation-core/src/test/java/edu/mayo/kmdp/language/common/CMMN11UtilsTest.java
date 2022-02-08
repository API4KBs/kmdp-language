package edu.mayo.kmdp.language.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;

import edu.mayo.kmdp.language.common.cmmn.CMMN11Utils;
import edu.mayo.kmdp.language.parsers.cmmn.v1_1.CMMN11Parser;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.cmmn._20151109.model.TCaseFileItemDefinition;
import org.omg.spec.cmmn._20151109.model.TDefinitions;
import org.omg.spec.cmmn._20151109.model.TPlanItemDefinition;
import org.omg.spec.cmmn._20151109.model.TStage;
import org.omg.spec.cmmn._20151109.model.TTask;

class CMMN11UtilsTest {

  static TDefinitions sourceModel = loadSource();

  @Test
  void testGetStages() {
    List<TStage> stages = CMMN11Utils.streamStages(sourceModel)
        .collect(Collectors.toList());
    assertEquals(4, stages.size());
  }

  @Test
  void testGetTask() {
    List<TTask> tasks = CMMN11Utils.streamTasks(sourceModel)
        .collect(Collectors.toList());
    assertEquals(5, tasks.size());
  }


  @Test
  void testGetOnSentrySourceElements() {
    List<TPlanItemDefinition> sentryTargets = CMMN11Utils.streamOnSentrySources(sourceModel)
        .collect(Collectors.toList());
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Task 1"::equals));
    assertFalse(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Task 3"::equals));
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Task 4"::equals));
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Stage A"::equals));
    assertEquals(3, sentryTargets.size());
  }

  @Test
  void testGetOnSentryTargetElements() {
    List<TPlanItemDefinition> sentryTargets = CMMN11Utils.streamOnSentryTargets(sourceModel)
        .collect(Collectors.toList());
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Task 2"::equals));
    assertFalse(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Task 3"::equals));
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Task 0"::equals));
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("SubStage C"::equals));
    assertEquals(3, sentryTargets.size());
  }

  @Test
  void testGetOnDataSentryTargetElements() {
    List<TPlanItemDefinition> sentryTargets = CMMN11Utils.streamOnDataSentryTargets(sourceModel)
        .collect(Collectors.toList());
    assertTrue(sentryTargets.stream()
        .map(TPlanItemDefinition::getName).anyMatch("Stage B"::equals));
    assertEquals(1, sentryTargets.size());
  }

  @Test
  void testGetCaseFileItemDefs() {
    List<TCaseFileItemDefinition> sentryTargets = CMMN11Utils.streamCaseFileItemDefs(sourceModel)
        .collect(Collectors.toList());
    assertTrue(sentryTargets.stream()
        .map(TCaseFileItemDefinition::getName).anyMatch("CFX"::equals));
    assertEquals(1, sentryTargets.size());
  }


  private static TDefinitions loadSource() {
    InputStream is = CMMN11UtilsTest.class.getResourceAsStream(
        "/cmmn/v1_1/MixedCaseModel.cmmn.xml");
    KnowledgeCarrier kc = AbstractCarrier.of(is)
        .withRepresentation(rep(CMMN_1_1, XML_1_1, Charset.defaultCharset(), Encodings.DEFAULT));
    return new CMMN11Parser().applyLift(kc, Abstract_Knowledge_Expression, codedRep(CMMN_1_1), null)
        .flatOpt(x -> x.as(TDefinitions.class))
        .orElseGet(Assertions::fail);
  }
}
