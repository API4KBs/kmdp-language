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
package edu.mayo.kmdp.language;

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Parsed_Knowedge_Expression;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries.Cognitive_Process_Model;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.tranx.v4.DeserializeApi;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;

@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
public class DeserializationTest {

  @Inject
  @KPComponent
  DeserializeApi parser;

  @Test
  public void testParse() {
    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier bin = AbstractCarrier.of(dmn.get())
        .withRepresentation(
            rep(DMN_1_1, XML_1_1, Charset.defaultCharset(), "default"));

    Optional<KnowledgeCarrier> expr = parser
        .applyLift(bin, Concrete_Knowledge_Expression)
        .getOptionalValue();
    assertTrue(expr.isPresent());
    assertTrue(expr.get().asString().orElse("").contains("decision name=\"a\""));

    KnowledgeCarrier dox = parser
        .applyLift(bin, Parsed_Knowedge_Expression)
        .getOptionalValue()
        .orElse(null);
    assertNotNull(dox);
    assertTrue(dox.is(Document.class));

    KnowledgeCarrier dox2 = parser
        .applyLift(expr.get(), Parsed_Knowedge_Expression)
        .getOptionalValue()
        .orElse(null);

    assertNotNull(dox2);
    assertTrue(dox2.is(Document.class));
    assertArrayEquals(
        XMLUtil.toByteArray(dox.as(Document.class).orElseGet(Assertions::fail)),
        XMLUtil.toByteArray(dox2.as(Document.class).orElseGet(Assertions::fail)));

    assertTrue(
        parser.applyLift(dox, Abstract_Knowledge_Expression)
        .map(KnowledgeCarrier::getExpression)
        .map(TDefinitions.class::isInstance)
        .isSuccess());

    assertTrue(
        parser.applyLift(expr.get(), Abstract_Knowledge_Expression)
            .map(KnowledgeCarrier::getExpression)
            .map(TDefinitions.class::isInstance)
            .isSuccess());

    assertTrue(
        parser.applyLift(bin, Abstract_Knowledge_Expression)
            .map(KnowledgeCarrier::getExpression)
            .map(TDefinitions.class::isInstance)
            .isSuccess());
  }


  @Test
  public void testSerialize() {
    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier bin = AbstractCarrier.of(dmn.get())
        .withRepresentation(rep(DMN_1_1,
            XML_1_1,
            Charset.defaultCharset(),
            "TODO"));

    Answer<KnowledgeCarrier> ast = parser.applyLift(bin, Abstract_Knowledge_Expression);

    Answer<KnowledgeCarrier> dox = ast
        .flatMap((a) -> parser.applyLower(a, Parsed_Knowedge_Expression));

    assertTrue(dox.isSuccess());
    assertTrue(dox.getOptionalValue().isPresent());
    assertTrue(dox.getOptionalValue().get().getExpression() instanceof Document);

    Optional<KnowledgeCarrier> expr = ast
        .flatMap((a) -> parser.applyLower(a, Concrete_Knowledge_Expression))
        .getOptionalValue();

    assertTrue(expr.isPresent());
    assertTrue(expr.get().asString().isPresent());
    assertTrue(expr.get().asString().orElse("").contains("decision"));


  }

  @Test
  public void testOWLParse() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/test.ofn"));
    assertTrue(owl.isPresent());

    KnowledgeCarrier bin = AbstractCarrier.of(owl.get())
        .withRepresentation(rep(OWL_2));

    Answer<KnowledgeCarrier> aast = parser
        .applyLift(bin, Abstract_Knowledge_Expression);

    assertTrue(aast.isSuccess());
    assertTrue(aast.getOptionalValue().isPresent());
    KnowledgeCarrier ast = aast.getOptionalValue().get();

    assertTrue(ast.is(OWLOntology.class));
    assertEquals(OWL_2, ast.getRepresentation().getLanguage());
    assertNull(ast.getRepresentation().getSerialization());

    Answer<SyntacticRepresentation> arep = parser
        .applyLower(ast, Concrete_Knowledge_Expression)
        .map(KnowledgeCarrier::getRepresentation);

    assertTrue(arep.isSuccess());
    assertTrue(arep.getOptionalValue().isPresent());
    SyntacticRepresentation rep = arep.getOptionalValue().get();

    assertEquals(OWL_2,rep.getLanguage());
    assertEquals(RDF_XML_Syntax,rep.getSerialization());
    assertEquals(XML_1_1,rep.getFormat());

  }


  @Test
  public void testSerializeSurrogate() {

    KnowledgeAsset asset = new edu.mayo.kmdp.metadata.surrogate.resources.KnowledgeAsset()
        .withAssetId(new URIIdentifier().withUri(
            URI.create(Registry.MAYO_ASSETS_BASE_URI + "2c6572ea-867d-4863-963a-b4bc5357429b")))
        .withFormalType(Cognitive_Process_Model);

    String serializedAsset = JaxbUtil.marshallToString(
        Arrays.asList(asset.getClass(), Annotation.class,
            edu.mayo.kmdp.metadata.annotations.Annotation.class),
        asset,
        JaxbUtil.defaultProperties());

    KnowledgeCarrier ast = AbstractCarrier.ofAst(asset)
        .withRepresentation(rep(Knowledge_Asset_Surrogate))
        .withLevel(Abstract_Knowledge_Expression);

    Answer<String> ser = parser
        .applyLower(ast,Concrete_Knowledge_Expression)
        .flatOpt(AbstractCarrier::asString);

    assertEquals(serializedAsset, ser.orElse("Fail"));
  }


  @Test
  public void testSerializeSurrogateJson() {

    KnowledgeAsset asset = new edu.mayo.kmdp.metadata.surrogate.resources.KnowledgeAsset()
        .withAssetId(new URIIdentifier().withUri(
            URI.create(Registry.MAYO_ASSETS_BASE_URI + "2c6572ea-867d-4863-963a-b4bc5357429b")))
        .withFormalType(Cognitive_Process_Model);

    String serializedAsset = JSonUtil.printJson(asset).orElse("");

    KnowledgeCarrier ast = AbstractCarrier.ofAst(asset)
        .withRepresentation(rep(Knowledge_Asset_Surrogate))
        .withLevel(Abstract_Knowledge_Expression);

    Answer<String> ser = parser.applyLower(
        ast,
        Concrete_Knowledge_Expression,
        codedRep(ast.getRepresentation().getLanguage(), JSON, Charset.defaultCharset()),
        null)
        .flatOpt(AbstractCarrier::asString);

    assertEquals(serializedAsset, ser.orElse("Fail"));

    Answer<String> ser2 = parser.applyLower(
        ast,
        Concrete_Knowledge_Expression,
        codedRep(ast.getRepresentation().getLanguage(), JSON, Charset.defaultCharset()), null)
        .flatOpt(AbstractCarrier::asString);

    assertEquals(serializedAsset, ser2.orElse("Fail"));


  }


}
