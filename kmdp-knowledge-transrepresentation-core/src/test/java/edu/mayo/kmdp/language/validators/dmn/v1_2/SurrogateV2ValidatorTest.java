package edu.mayo.kmdp.language.validators.dmn.v1_2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries;
import org.w3c.dom.Document;

public class SurrogateV2ValidatorTest {
  private KnowledgeCarrier KC_JSON_VALID = validJSONKnowledgeCarrier();
  private KnowledgeCarrier KC_XML_VALID = validXMLKnowledgeCarrier();
  private KnowledgeCarrier KC_JSON_INVALID = invalidJSONKnowledgeCarrier();
  private KnowledgeCarrier KC_XML_INVALID = invalidXMLKnowledgeCarrier();

  @Test
  void test_serialized_knowledge_expression_json_valid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_VALID;
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_serialized_knowledge_expression_json_invalid() {
    // Missing 'language' property on SyntacticRepresentation
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_INVALID;

    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  @Test
  void test_serialized_knowledge_expression_xml_valid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_XML_VALID;
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_serialized_knowledge_expression_xml_invalid() {
    // Missing 'language' property on SyntacticRepresentation
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_XML_INVALID;
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  @Test
  void test_encoded_knowledge_expression_json_valid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_VALID;
    kc.setExpression(kc.getExpression().toString().getBytes());
    kc.setLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_encoded_knowledge_expression_json_invalid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_INVALID;
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  @Test
  void test_encoded_knowledge_expression_xml_valid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_XML_VALID;
    kc.setExpression(kc.getExpression().toString().getBytes());
    kc.setLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_encoded_knowledge_expression_xml_invalid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_XML_INVALID;
    kc.setExpression(kc.getExpression().toString().getBytes());
    kc.setLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  @Test
  void test_concrete_knowledge_expression_json_valid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_VALID;
    JsonNode jsonNode =
        JSonUtil.toJsonNode(
                JSonUtil.parseJson(kc.getExpression().toString(), KnowledgeAsset.class).get())
            .get();
    kc.setExpression(jsonNode);
    kc.setLevel(ParsingLevelSeries.Concrete_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_concrete_knowledge_expression_json_invalid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_INVALID;
    JsonNode jsonNode =
        JSonUtil.toJsonNode(
                JSonUtil.parseJson(kc.getExpression().toString(), KnowledgeAsset.class).get())
            .get();
    kc.setExpression(jsonNode);
    kc.setLevel(ParsingLevelSeries.Concrete_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  @Test
  void test_concrete_knowledge_expression_xml_valid() throws Exception {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_XML_VALID;
    Document doc = XMLUtil.loadXMLDocument(kc.getExpression().toString().getBytes()).get();
    kc.setExpression(doc);
    kc.setLevel(ParsingLevelSeries.Concrete_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_concrete_knowledge_expression_xml_invalid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_XML_INVALID;
    Document doc = XMLUtil.loadXMLDocument(kc.getExpression().toString().getBytes()).get();
    kc.setExpression(doc);
    kc.setLevel(ParsingLevelSeries.Concrete_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  @Test
  void test_abstract_knowledge_expression_valid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_VALID;
    KnowledgeAsset ka =
        JSonUtil.parseJson(kc.getExpression().toString(), KnowledgeAsset.class).get();
    kc.setExpression(ka);
    kc.setLevel(ParsingLevelSeries.Abstract_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertTrue(result.isSuccess());
  }

  @Test
  void test_abstract_knowledge_expression_invalid() {
    SurrogateV2Validator surrogateV2Validator = new SurrogateV2Validator();
    KnowledgeCarrier kc = KC_JSON_INVALID;
    KnowledgeAsset ka =
        JSonUtil.parseJson(kc.getExpression().toString(), KnowledgeAsset.class).get();
    kc.setExpression(ka);
    kc.setLevel(ParsingLevelSeries.Abstract_Knowledge_Expression);
    Answer result =
        surrogateV2Validator.applyNamedValidate(
            UUID.fromString("85ea1656-60e1-46a6-944f-942367de4ec5"), kc, null);
    assertFalse(result.isSuccess());
  }

  private KnowledgeCarrier validJSONKnowledgeCarrier() {
    return JSonUtil.readJson(
            SurrogateV2ValidatorTest.class.getResourceAsStream("/surrogateV2JSON.json"),
            KnowledgeCarrier.class)
        .orElse(null);
  }

  private KnowledgeCarrier invalidJSONKnowledgeCarrier() {
    return JSonUtil.readJson(
        SurrogateV2ValidatorTest.class.getResourceAsStream("/surrogateV2JSONInvalid.json"),
        KnowledgeCarrier.class)
        .orElse(null);
  }

  private KnowledgeCarrier validXMLKnowledgeCarrier() {
    return JSonUtil.readJson(
        SurrogateV2ValidatorTest.class.getResourceAsStream("/surrogateV2XML.json"),
        KnowledgeCarrier.class)
        .orElse(null);
  }

  private KnowledgeCarrier invalidXMLKnowledgeCarrier() {
    return JSonUtil.readJson(
        SurrogateV2ValidatorTest.class.getResourceAsStream("/surrogateV2XMLInvalid.json"),
        KnowledgeCarrier.class)
        .orElse(null);
  }
}
