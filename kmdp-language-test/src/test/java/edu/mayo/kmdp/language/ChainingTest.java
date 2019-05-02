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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KRLanguage;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalTestConfig.class)
@ActiveProfiles("test")
public class ChainingTest {

  @Inject
  @KPComponent
  TransxionApi transformationApi;

  @Inject
  @KPComponent
  DeserializeApi deserializeApi;

  @Inject
  @KPComponent
  DetectApi detectApi;

  @Test
  public void stepwiseTest() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier carrier1 = KnowledgeCarrier.of(dmn.get());
    SyntacticRepresentation rep = detectApi.getDetectedRepresentation(carrier1);

    assertSame(KRLanguage.DMN_1_1, rep.getLanguage());

    carrier1.withRepresentation(rep);

    KnowledgeCarrier carrier2 = deserializeApi
        .lift(carrier1, ParsingLevel.Abstract_Knowledge_Expression);

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(carrier2.getRepresentation().getLanguage(), KRLanguage.DMN_1_1);
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());

  }

  @Test
  public void implicitChaining() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    KnowledgeCarrier carrier1 = KnowledgeCarrier.of(dmn.get())
        .withRepresentation(rep(KRLanguage.DMN_1_1));

    KnowledgeCarrier carrier2 = deserializeApi
        .lift(carrier1, ParsingLevel.Abstract_Knowledge_Expression);

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(carrier2.getRepresentation().getLanguage(), KRLanguage.DMN_1_1);
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }

  @Test
  public void functionalInversion() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());
    KnowledgeCarrier carrier1 = KnowledgeCarrier.of(dmn.get());

    Function<KnowledgeCarrier, KnowledgeCarrier> detect = detectApi::setDetectedRepresentation;
    BiFunction<KnowledgeCarrier, ParsingLevel, KnowledgeCarrier> lift = deserializeApi::lift;

    KnowledgeCarrier carrier2 = detect
        .andThen((c1) -> lift.apply(c1, ParsingLevel.Abstract_Knowledge_Expression))
        .apply(carrier1);

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(carrier2.getRepresentation().getLanguage(), KRLanguage.DMN_1_1);
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }


  @Test
  public void monadicChain() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    Optional<KnowledgeCarrier> out = dmn.map(KnowledgeCarrier::of)
        .map(detectApi::setDetectedRepresentation)
        .map((c1) -> deserializeApi.lift(c1, ParsingLevel.Abstract_Knowledge_Expression));

    assertTrue(out.isPresent());
    KnowledgeCarrier carrier2 = out.get();

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(carrier2.getRepresentation().getLanguage(), KRLanguage.DMN_1_1);
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }


  @Test
  public void anonymousChain() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    Optional<KnowledgeCarrier> out = dmn.map(KnowledgeCarrier::of)
        .map(detectApi::setDetectedRepresentation)
        .map((c1) -> deserializeApi.lift(c1, ParsingLevel.Abstract_Knowledge_Expression));

    assertTrue(out.isPresent());
    KnowledgeCarrier carrier2 = out.get();

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(carrier2.getRepresentation().getLanguage(), KRLanguage.DMN_1_1);
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());

    Optional<KnowledgeCarrier> c3 = out
        .map((c2) -> deserializeApi.lower(c2, ParsingLevel.Concrete_Knowledge_Expression));
    assertTrue(c3.isPresent());
    assertTrue(c3.get() instanceof ExpressionCarrier);
  }


  @Test
  public void testTranscreation() {
    assertNotNull(transformationApi);
  }

}

