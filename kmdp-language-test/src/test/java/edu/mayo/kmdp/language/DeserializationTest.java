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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KnowledgeRepresentationLanguageSerialization;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.w3c.dom.Document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalTestConfig.class)
@ActiveProfiles(profiles = "test")
@WebAppConfiguration
public class DeserializationTest {

  @Inject
  @KPComponent
  DeserializeApi parser;

  @Test
  public void testParse() {
    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier bin = KnowledgeCarrier.of(dmn.get())
        .withRepresentation(
            rep(KnowledgeRepresentationLanguage.DMN_1_1, SerializationFormat.XML_1_1, Charset.defaultCharset().name(), "TODO"));

    ExpressionCarrier expr = (ExpressionCarrier) parser
        .lift(bin, ParsingLevel.Concrete_Knowledge_Expression);
    assertTrue(expr.getSerializedExpression().contains("decision name=\"a\""));

    DocumentCarrier dox = (DocumentCarrier) parser
        .lift(bin, ParsingLevel.Parsed_Knowedge_Expression);
    assertTrue(dox.getStructuredExpression() instanceof Document);

    DocumentCarrier dox2 = (DocumentCarrier) parser
        .lift(expr, ParsingLevel.Parsed_Knowedge_Expression);
    assertTrue(dox2.getStructuredExpression() instanceof Document);
    assertTrue(Arrays.equals(XMLUtil.toByteArray((Document) dox.getStructuredExpression()),
        XMLUtil.toByteArray((Document) dox2.getStructuredExpression())));

    ASTCarrier ast = (ASTCarrier) parser.lift(dox, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast.getParsedExpression() instanceof TDefinitions);
    ASTCarrier ast2 = (ASTCarrier) parser.lift(expr, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast2.getParsedExpression() instanceof TDefinitions);
    ASTCarrier ast3 = (ASTCarrier) parser.lift(bin, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast3.getParsedExpression() instanceof TDefinitions);
  }


  @Test
  public void testSerialize() {
    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier bin = KnowledgeCarrier.of(dmn.get())
        .withRepresentation(rep(KnowledgeRepresentationLanguage.DMN_1_1,
            SerializationFormat.XML_1_1,
            Charset.defaultCharset().name(),
            "TODO"));

    ASTCarrier ast = (ASTCarrier) parser.lift(bin, ParsingLevel.Abstract_Knowledge_Expression);

    DocumentCarrier dox = (DocumentCarrier) parser
        .lower(ast, ParsingLevel.Parsed_Knowedge_Expression);
    assertTrue(dox.getStructuredExpression() instanceof Document);

    ExpressionCarrier expr = (ExpressionCarrier) parser
        .lower(ast, ParsingLevel.Concrete_Knowledge_Expression);
    assertNotNull(expr.getSerializedExpression());
    assertTrue(expr.getSerializedExpression().contains("decision"));


  }

  @Test
  public void testOWLParse() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/test.ofn"));
    assertTrue(owl.isPresent());

    KnowledgeCarrier bin = KnowledgeCarrier.of(owl.get())
        .withRepresentation(rep(KnowledgeRepresentationLanguage.OWL_2));

    ASTCarrier ast = (ASTCarrier) parser.lift(bin, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast.getParsedExpression() instanceof OWLOntology);
    assertEquals(KnowledgeRepresentationLanguage.OWL_2, ast.getRepresentation().getLanguage());
    assertNull(ast.getRepresentation().getSerialization());

    SyntacticRepresentation rep = parser
        .lower(ast, ParsingLevel.Concrete_Knowledge_Expression).getRepresentation();
    assertEquals(KnowledgeRepresentationLanguage.OWL_2,rep.getLanguage());
    assertEquals(KnowledgeRepresentationLanguageSerialization.RDF_XML_Syntax,rep.getSerialization());
    assertEquals(SerializationFormat.XML_1_1,rep.getFormat());

  }


  @Test
  public void testSerializeSurrogate() {

    KnowledgeAsset asset = new edu.mayo.kmdp.metadata.surrogate.resources.KnowledgeAsset()
        .withResourceId(new URIIdentifier().withUri(
            URI.create(Registry.MAYO_ASSETS_BASE_URI + "2c6572ea-867d-4863-963a-b4bc5357429b")))
        .withType(KnowledgeAssetType.Cognitive_Process_Model);
    String serializedAsset = JaxbUtil.marshallToString(Collections.singleton(asset.getClass()),asset, JaxbUtil.defaultProperties());

    KnowledgeCarrier ast = KnowledgeCarrier.ofAst(asset)
        .withRepresentation(rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate))
        .withLevel(ParsingLevel.Abstract_Knowledge_Expression);

    ExpressionCarrier ser = (ExpressionCarrier) parser.lower(ast,ParsingLevel.Concrete_Knowledge_Expression);

    assertEquals(serializedAsset, ser.getSerializedExpression());
  }


  @Test
  public void testSerializeSurrogateJson() {

    KnowledgeAsset asset = new edu.mayo.kmdp.metadata.surrogate.resources.KnowledgeAsset()
        .withResourceId(new URIIdentifier().withUri(
            URI.create(Registry.MAYO_ASSETS_BASE_URI + "2c6572ea-867d-4863-963a-b4bc5357429b")))
        .withType(KnowledgeAssetType.Cognitive_Process_Model);

    String serializedAsset = JSonUtil.printJson(asset).orElse("");

    KnowledgeCarrier ast = KnowledgeCarrier.ofAst(asset)
        .withRepresentation(rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate))
        .withLevel(ParsingLevel.Abstract_Knowledge_Expression);


    ExpressionCarrier ser = (ExpressionCarrier) parser.serialize(
        ast,
        rep(ast.getRepresentation())
            .withFormat(SerializationFormat.JSON)
            .withCharset(Charset.defaultCharset().name()));

    assertEquals(serializedAsset, ser.getSerializedExpression());


    ExpressionCarrier ser2 = (ExpressionCarrier) parser.ensureRepresentation(
        ast,
        rep(ast.getRepresentation())
            .withFormat(SerializationFormat.JSON)
            .withCharset(Charset.defaultCharset().name()));

    assertEquals(serializedAsset, ser2.getSerializedExpression());


  }


}
