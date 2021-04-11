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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries.OWL2_RL;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.util.FileUtil;
import java.io.InputStream;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.DetectApi;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
public class DetectorTest {

  @Inject
  @KPComponent
  DetectApi detector;

  @Test
  public void testDMNDetector() {
    assertNotNull(detector);

    Optional<String> dmn = FileUtil.read(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());
    KnowledgeCarrier carrier = AbstractCarrier.of(dmn.get());

    Optional<SyntacticRepresentation> rep = detector.applyDetect(carrier)
        .map(KnowledgeCarrier::getRepresentation)
        .getOptionalValue();

    assertTrue(rep.isPresent());
    assertEquals(DMN_1_1, rep.get().getLanguage());
    assertEquals(XML_1_1, rep.get().getFormat());
  }


  @Test
  public void testDMNDetectorAnnotate() {
    assertNotNull(detector);

    Optional<String> dmn = FileUtil.read(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());
    KnowledgeCarrier carrier = AbstractCarrier.of(dmn.get());

    Optional<SyntacticRepresentation> rep = detector.applyDetect(carrier)
        .map(KnowledgeCarrier::getRepresentation)
        .getOptionalValue();

    assertTrue(rep.isPresent());
    assertEquals(DMN_1_1, rep.get().getLanguage());
    assertEquals(XML_1_1, rep.get().getFormat());
  }


  @Test
  public void testCMMNDetector() {
    assertNotNull(detector);

    Optional<String> cmmn = FileUtil.read(DetectorTest.class.getResource("/artifacts/sample.cmmn"));
    assertTrue(cmmn.isPresent());
    KnowledgeCarrier carrier = AbstractCarrier.of(cmmn.get());

    Optional<SyntacticRepresentation> rep = detector.applyDetect(carrier)
        .map(KnowledgeCarrier::getRepresentation)
        .getOptionalValue();

    assertTrue(rep.isPresent());
    assertEquals(CMMN_1_1, rep.get().getLanguage());
    assertEquals(XML_1_1, rep.get().getFormat());
  }


  @Test
  public void testOWLDetector() {
    InputStream is = DetectorTest.class.getResourceAsStream("/artifacts/test.ofn");
    KnowledgeCarrier carrier = AbstractCarrier.of(is)
        .withRepresentation(rep(OWL_2,OWL_Functional_Syntax,TXT));

    Optional<SyntacticRepresentation> orep = detector.applyDetect(carrier)
        .map(KnowledgeCarrier::getRepresentation)
        .getOptionalValue();

    assertTrue(orep.isPresent());
    SyntacticRepresentation rep = orep.get();

    assertEquals(OWL_2, rep.getLanguage());
    assertEquals(OWL2_RL, rep.getProfile());
    assertEquals(TXT, rep.getFormat());
    assertEquals(OWL_Functional_Syntax, rep.getSerialization());
    assertTrue(rep.getLexicon().isEmpty());
  }


}
