/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.omg.spec.api4kp.KnowledgeCarrierHelper.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.spec.api4kp.KnowledgeCarrierHelper;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.w3c.dom.Document;

@RunWith(SpringJUnit4ClassRunner.class)
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

    KnowledgeCarrier bin = KnowledgeCarrierHelper.of(dmn.get())
        .withRepresentation(
            rep(KRLanguage.DMN_1_1, KRFormat.XML_1_1, Charset.defaultCharset().name(), "TODO"));

    ExpressionCarrier expr = (ExpressionCarrier) parser
        .parse(bin, ParsingLevel.Concrete_Knowledge_Expression);
    assertTrue(expr.getSerializedExpression().contains("decision name=\"a\""));

    DocumentCarrier dox = (DocumentCarrier) parser
        .parse(bin, ParsingLevel.Parsed_Knowedge_Expression);
    assertTrue(dox.getStructuredExpression() instanceof Document);

    DocumentCarrier dox2 = (DocumentCarrier) parser
        .parse(expr, ParsingLevel.Parsed_Knowedge_Expression);
    assertTrue(dox2.getStructuredExpression() instanceof Document);
    assertTrue(Arrays.equals(XMLUtil.toByteArray((Document) dox.getStructuredExpression()),
        XMLUtil.toByteArray((Document) dox2.getStructuredExpression())));

    ASTCarrier ast = (ASTCarrier) parser.parse(dox, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast.getParsedExpression() instanceof TDefinitions);
    ASTCarrier ast2 = (ASTCarrier) parser.parse(expr, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast2.getParsedExpression() instanceof TDefinitions);
    ASTCarrier ast3 = (ASTCarrier) parser.parse(bin, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast3.getParsedExpression() instanceof TDefinitions);
  }


  @Test
  public void testSerialize() {
    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier bin = KnowledgeCarrierHelper.of(dmn.get())
        .withRepresentation(rep(KRLanguage.DMN_1_1,
            KRFormat.XML_1_1,
            Charset.defaultCharset().name(),
            "TODO"));

    ASTCarrier ast = (ASTCarrier) parser.parse(bin, ParsingLevel.Abstract_Knowledge_Expression);

    DocumentCarrier dox = (DocumentCarrier) parser
        .serialize(ast, ParsingLevel.Parsed_Knowedge_Expression);
    assertTrue(dox.getStructuredExpression() instanceof Document);

    ExpressionCarrier expr = (ExpressionCarrier) parser
        .serialize(ast, ParsingLevel.Concrete_Knowledge_Expression);
    assertNotNull(expr.getSerializedExpression());
    assertTrue(expr.getSerializedExpression().contains("decision"));


  }

  @Test
  public void testOWLParse() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/test.ofn"));
    assertTrue(owl.isPresent());

    KnowledgeCarrier bin = KnowledgeCarrierHelper.of(owl.get())
        .withRepresentation(rep(KRLanguage.OWL_2));

    ASTCarrier ast = (ASTCarrier) parser.parse(bin, ParsingLevel.Abstract_Knowledge_Expression);
    assertTrue(ast.getParsedExpression() instanceof OWLOntology);
    assertEquals(KRLanguage.OWL_2, ast.getRepresentation().getLanguage());
    assertNull(ast.getRepresentation().getSerialization());

    SyntacticRepresentation rep = parser
        .serialize(ast, ParsingLevel.Concrete_Knowledge_Expression).getRepresentation();
    assertEquals(KRLanguage.OWL_2,rep.getLanguage());
    assertEquals(KRSerialization.RDF_XML_Syntax,rep.getSerialization());
    assertEquals(KRFormat.XML_1_1,rep.getFormat());

  }


}
