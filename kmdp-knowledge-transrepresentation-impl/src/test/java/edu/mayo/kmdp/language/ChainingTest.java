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
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.util.FileUtil;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.DeserializeApi;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.DetectApi;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.TransxionApi;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

;

@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
public class ChainingTest {

  private static final String SRC = "/artifacts/sample.dmn";

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
        .readBytes(DetectorTest.class.getResource(SRC));
    byte[] dmnBytes = dmn.orElseGet(Assertions::fail);

    KnowledgeCarrier carrier1 = AbstractCarrier.of(dmnBytes);
    SyntacticRepresentation rep = detectApi.applyDetect(carrier1)
        .map(KnowledgeCarrier::getRepresentation)
        .orElse(null);
    assertNotNull(rep);

    assertSame(DMN_1_1, rep.getLanguage());

    carrier1.withRepresentation(rep);

    KnowledgeCarrier carrier2 = deserializeApi
        .applyLift(carrier1, Abstract_Knowledge_Expression)
        .orElse(null);

    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());

  }

  @Test
  public void implicitChaining() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource(SRC));
    byte[] dmnBytes = dmn.orElseGet(Assertions::fail);

    KnowledgeCarrier carrier1 = AbstractCarrier.of(dmnBytes)
        .withRepresentation(rep(DMN_1_1));

    KnowledgeCarrier carrier2 = deserializeApi
        .applyLift(carrier1, Abstract_Knowledge_Expression)
        .orElse(null);

    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }

  @Test
  public void functionalInversion() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource(SRC));
    byte[] dmnBytes = dmn.orElseGet(Assertions::fail);
    KnowledgeCarrier carrier1 = AbstractCarrier.of(dmnBytes);

    Function<KnowledgeCarrier, Answer<KnowledgeCarrier>> detect = detectApi::applyDetect;
    BiFunction<KnowledgeCarrier, ParsingLevel, Answer<KnowledgeCarrier>> applyLift = deserializeApi::applyLift;

    KnowledgeCarrier carrier2 = detect
        .apply(carrier1)
        .flatMap(c1 -> applyLift.apply(c1, Abstract_Knowledge_Expression))
        .orElse(null);

    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }


  @Test
  public void monadicChain() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource(SRC));
    assertTrue(dmn.isPresent());

    Answer<KnowledgeCarrier> out = Answer.of(dmn)
        .map(AbstractCarrier::of)
        .flatMap(detectApi::applyDetect)
        .flatMap(c1 -> deserializeApi.applyLift(c1, Abstract_Knowledge_Expression));

    assertTrue(out.isSuccess());
    KnowledgeCarrier carrier2 = out.get();

    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());
  }


  @Test
  public void anonymousChain() {

    Optional<byte[]> dmn = FileUtil
        .readBytes(DetectorTest.class.getResource(SRC));
    assertTrue(dmn.isPresent());

    Answer<KnowledgeCarrier> out = Answer.of(dmn)
        .map(AbstractCarrier::of)
        .flatMap(detectApi::applyDetect)
        .flatMap(c1 -> deserializeApi.applyLift(c1, Abstract_Knowledge_Expression));

    assertTrue(out.isSuccess());
    KnowledgeCarrier carrier2 = out.get();

    assertSame(DMN_1_1, carrier2.getRepresentation().getLanguage());
    assertNull(carrier2.getRepresentation().getFormat());
    assertNull(carrier2.getRepresentation().getEncoding());
    assertNull(carrier2.getRepresentation().getCharset());

    Answer<KnowledgeCarrier> c3 = out
        .flatMap(c2 -> deserializeApi.applyLower(c2, Serialized_Knowledge_Expression));
    assertTrue(c3.isSuccess());
  }


  @Test
  public void testTranscreation() {
    assertNotNull(transformationApi);
  }

}

