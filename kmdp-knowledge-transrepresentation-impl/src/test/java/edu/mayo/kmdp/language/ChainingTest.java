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
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.tranx.DeserializeApi;
import edu.mayo.kmdp.tranx.DetectApi;
import edu.mayo.kmdp.tranx.TransxionApi;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
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
    SyntacticRepresentation rep = detectApi.getDetectedRepresentation(carrier1)
        .orElse(null);
    assertNotNull(rep);

    assertSame(DMN_1_1, rep.getLanguage());

    carrier1.withRepresentation(rep);

    KnowledgeCarrier carrier2 = deserializeApi
        .lift(carrier1, Abstract_Knowledge_Expression)
        .orElse(null);

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
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
        .withRepresentation(rep(DMN_1_1));

    KnowledgeCarrier carrier2 = deserializeApi
        .lift(carrier1, Abstract_Knowledge_Expression)
        .orElse(null);

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
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

    Function<KnowledgeCarrier, Answer<KnowledgeCarrier>> detect = detectApi::setDetectedRepresentation;
    BiFunction<KnowledgeCarrier, ParsingLevel, Answer<KnowledgeCarrier>> lift = deserializeApi::lift;

    KnowledgeCarrier carrier2 = detect
        .apply(carrier1)
        .flatMap(c1 -> lift.apply(c1, Abstract_Knowledge_Expression))
        .orElse(null);

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }


  @Test
  public void monadicChain() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    Answer<KnowledgeCarrier> out = Answer.of(dmn)
        .map(KnowledgeCarrier::of)
        .flatMap(detectApi::setDetectedRepresentation)
        .flatMap(c1 -> deserializeApi.lift(c1, Abstract_Knowledge_Expression));

    assertTrue(out.isSuccess());
    KnowledgeCarrier carrier2 = out.get();

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }


  @Test
  public void anonymousChain() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());

    Answer<KnowledgeCarrier> out = Answer.of(dmn)
        .map(KnowledgeCarrier::of)
        .flatMap(detectApi::setDetectedRepresentation)
        .flatMap(c1 -> deserializeApi.lift(c1, Abstract_Knowledge_Expression));

    assertTrue(out.isSuccess());
    KnowledgeCarrier carrier2 = out.get();

    assertTrue(carrier2 instanceof ASTCarrier);
    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());

    Answer<KnowledgeCarrier> c3 = out
        .flatMap(c2 -> deserializeApi.lower(c2, Concrete_Knowledge_Expression));
    assertTrue(c3.isSuccess());
    assertTrue(c3.get() instanceof ExpressionCarrier);
  }


  @Test
  public void testTranscreation() {
    assertNotNull(transformationApi);
  }

}

