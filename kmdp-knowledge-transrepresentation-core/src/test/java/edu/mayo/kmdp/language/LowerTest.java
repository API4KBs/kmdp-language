package edu.mayo.kmdp.language;

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofAst;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.parsers.dmn.v1_2.DMN12Parser;
import edu.mayo.kmdp.language.parsers.surrogate.v1.SurrogateParser;
import edu.mayo.kmdp.language.parsers.surrogate.v2.Surrogate2Parser;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;
import org.omg.spec.dmn._20180521.model.TDMNElement.ExtensionElements;
import org.omg.spec.dmn._20180521.model.TDefinitions;

class LowerTest {

  @Test
  void testSerializeDMN12() {
    TDefinitions dmnModel = new TDefinitions();
    dmnModel.withExtensionElements(new ExtensionElements()
        .withAny(new edu.mayo.kmdp.metadata.annotations.resources.SimpleAnnotation()));

    String str = new DMN12Parser()
        .applyLower( ofAst(dmnModel).withRepresentation(rep(DMN_1_2)),
            Concrete_Knowledge_Expression, null, null)
    .flatOpt(KnowledgeCarrier::asString)
        .orElse("");

    assertTrue(str.contains("definitions"));
    assertTrue(str.contains("simpleAnnotation"));
    assertTrue(str.contains("DMN/20180521/MODEL/"));
  }

  @Test
  void testSerializeSurrogate() {
    KnowledgeAsset surrogate = new KnowledgeAsset()
        .withSubject(new edu.mayo.kmdp.metadata.annotations.SimpleAnnotation());

    String str = new SurrogateParser()
        .applyLower( ofAst(surrogate).withRepresentation(rep(Knowledge_Asset_Surrogate)),
            Concrete_Knowledge_Expression, null, null)
    .flatOpt(KnowledgeCarrier::asString)
        .orElse("");

    assertTrue(str.contains("knowledgeAsset"));
    assertTrue(str.contains("SimpleAnnotation"));
  }


  @Test
  void testSerializeSurrogatev2() {
    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogate2
        = new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset()
        .withAnnotation(new Annotation());

    String str = new edu.mayo.kmdp.language.parsers.surrogate.v2.Surrogate2Parser()
        .applyLower( ofAst(surrogate2).withRepresentation(rep(Knowledge_Asset_Surrogate_2_0)),
            Concrete_Knowledge_Expression, null, null)
    .flatOpt(KnowledgeCarrier::asString)
        .orElse("");

    assertTrue(str.contains("knowledgeAsset"));
    assertTrue(str.contains("annotation"));
  }

  @Test
  void testSerializeWithJson() {
    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogate2
        = new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset()
        .withAnnotation(new Annotation());

    Answer<KnowledgeCarrier> serialized = new Surrogate2Parser()
        .applyLower( ofAst(surrogate2)
                .withRepresentation(rep(Knowledge_Asset_Surrogate_2_0)),
            Concrete_Knowledge_Expression,
            ModelMIMECoder.encode(rep(Knowledge_Asset_Surrogate_2_0, JSON, Charset.defaultCharset())),
            null);

    String str = serialized
    .flatOpt(KnowledgeCarrier::asString)
        .orElse("");

    assertTrue(serialized.isSuccess());
    assertTrue(JSON.sameAs(serialized.get().getRepresentation().getFormat()));
    assertTrue(str.contains("KnowledgeAsset"));
    assertTrue(str.contains("annotation"));
  }


  @Test
  void testRoundtripWithJson() {
    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogate2
        = new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset()
        .withAnnotation(new Annotation());
    Surrogate2Parser parser = new Surrogate2Parser();

    Answer<KnowledgeCarrier> serialized = parser
        .applyLower(ofAst(surrogate2)
                .withRepresentation(rep(Knowledge_Asset_Surrogate_2_0)),
            Concrete_Knowledge_Expression,
            ModelMIMECoder
                .encode(rep(Knowledge_Asset_Surrogate_2_0, JSON, Charset.defaultCharset())),
            null);
    assertTrue(serialized.isSuccess());
    SyntacticRepresentation serializedRep = serialized.get().getRepresentation();
    assertTrue(Knowledge_Asset_Surrogate_2_0.sameAs(serializedRep.getLanguage()));
    assertTrue(JSON.sameAs(serializedRep.getFormat()));
    assertEquals(Charset.defaultCharset().name(),serializedRep.getCharset());

    Answer<KnowledgeCarrier> ast = serialized
        .flatMap(kc -> parser.applyLift(kc, Abstract_Knowledge_Expression, null, null));

    assertTrue(ast.isSuccess());
    assertTrue(ast
        .flatOpt(kc -> kc.as(edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset.class))
        .isSuccess());
  }
}