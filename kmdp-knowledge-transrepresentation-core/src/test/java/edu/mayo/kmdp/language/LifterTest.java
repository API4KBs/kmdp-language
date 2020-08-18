package edu.mayo.kmdp.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.CMMN_1_1_XML_Syntax;
import static org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
import static org.omg.spec.api4kp.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;

import edu.mayo.kmdp.language.parsers.cmmn.v1_1.CMMN11Parser;
import edu.mayo.kmdp.language.parsers.dmn.v1_1.DMN11Parser;
import edu.mayo.kmdp.language.parsers.dmn.v1_2.DMN12Parser;
import edu.mayo.kmdp.language.parsers.owl2.OWLParser;
import edu.mayo.kmdp.language.parsers.surrogate.v2.Surrogate2Parser;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder;
import org.omg.spec.api4kp.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.semanticweb.owlapi.model.OWLOntology;

class LifterTest {

  static final String VER = "1.0.0";

  @Test
  void testParseDMN11() {
    testVerticalLift(
        new DMN11Parser(),
        "/dmn11example.dmn",
        DMN_1_1, DMN_1_1_XML_Syntax, XML_1_1,
        org.omg.spec.dmn._20151101.dmn.TDefinitions.class);
  }

  @Test
  void testParseDMN12() {
    testVerticalLift(
        new DMN12Parser(),
        "/dmn12example.dmn",
        DMN_1_2, DMN_1_1_XML_Syntax, XML_1_1,
        org.omg.spec.dmn._20180521.model.TDefinitions.class);
  }

  @Test
  void testParseCMMN() {
    testVerticalLift(
        new CMMN11Parser(),
        "/cmmn11Example.cmmn",
        CMMN_1_1, CMMN_1_1_XML_Syntax, XML_1_1,
        org.omg.spec.cmmn._20151109.model.TDefinitions.class);
  }

  @Test
  void testParseOWL() {
    testVerticalLift(
        new OWLParser(),
        "/owlExample.owl",
        OWL_2, OWL_Manchester_Syntax, TXT,
        OWLOntology.class);
  }

  @Test
  void testParseSurr2() {
    testVerticalLift(
        new Surrogate2Parser(),
        "/surr2.xml",
        Knowledge_Asset_Surrogate_2_0, null, XML_1_1,
        KnowledgeAsset.class);
  }

  private void testVerticalLift(
      DeserializeApiOperator parser,
      String sourcePath,
      KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization ser,
      SerializationFormat fmt,
      Class<?> astRootClass) {

    ResourceIdentifier assetId = SurrogateBuilder.assetId(UUID.randomUUID(), VER);

    KnowledgeCarrier carrier =
        AbstractCarrier.of(LifterTest.class.getResourceAsStream(sourcePath))
            .withRepresentation(rep(language, ser, fmt))
            .withAssetId(assetId);

    assertNotNull(carrier.getAssetId());
    assertEquals(Encoded_Knowledge_Expression, carrier.getLevel().asEnum());
    assertEquals(language, carrier.getRepresentation().getLanguage());

    Answer<KnowledgeCarrier> parsed =
        parser.as_applyLift()
            .orElseGet(Assertions::fail)
            .applyLift(carrier, Abstract_Knowledge_Expression, null, null);

    assertTrue(parsed.isSuccess());
    KnowledgeCarrier knowledgeCarrier = parsed.get();
    assertEquals(carrier.getAssetId(), knowledgeCarrier.getAssetId());
    assertEquals(Abstract_Knowledge_Expression, knowledgeCarrier.getLevel().asEnum());

    assertTrue(knowledgeCarrier.is(astRootClass));
  }


}
