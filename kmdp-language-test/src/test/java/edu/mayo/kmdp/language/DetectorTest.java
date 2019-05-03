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


import static edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat.JSON;
import static edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat.TXT;
import static edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage.Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage.CMMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage.OWL_2;
import static edu.mayo.ontology.taxonomies.krprofile._2018._08.KnowledgeRepresentationLanguageProfile.OWL_2_RL;
import static edu.mayo.ontology.taxonomies.krserialization._2018._08.KnowledgeRepresentationLanguageSerialization.OWL_Functional_Syntax;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.util.FileUtil;
import java.io.InputStream;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
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
    KnowledgeCarrier carrier = KnowledgeCarrier.of(dmn.get());

    SyntacticRepresentation rep = detector.getDetectedRepresentation(carrier);

    assertNotNull(rep);
    assertEquals(DMN_1_1, rep.getLanguage());
    assertEquals(XML_1_1, rep.getFormat());
  }


  @Test
  public void testDMNDetectorAnnotate() {
    assertNotNull(detector);

    Optional<String> dmn = FileUtil.read(DetectorTest.class.getResource("/artifacts/sample.dmn"));
    assertTrue(dmn.isPresent());
    KnowledgeCarrier carrier = KnowledgeCarrier.of(dmn.get());

    SyntacticRepresentation rep = detector.getDetectedRepresentation(carrier);

    assertNotNull(rep);
    assertEquals(DMN_1_1, rep.getLanguage());
    assertEquals(XML_1_1, rep.getFormat());
  }


  @Test
  public void testCMMNDetector() {
    assertNotNull(detector);

    Optional<String> cmmn = FileUtil.read(DetectorTest.class.getResource("/artifacts/sample.cmmn"));
    assertTrue(cmmn.isPresent());
    KnowledgeCarrier carrier = KnowledgeCarrier.of(cmmn.get());

    SyntacticRepresentation rep = detector.getDetectedRepresentation(carrier);

    assertNotNull(rep);
    assertEquals(CMMN_1_1, rep.getLanguage());
    assertEquals(XML_1_1, rep.getFormat());
  }


  @Test
  public void testSurrogateDetector() {
    assertNotNull(detector);

    Optional<String> surr = FileUtil
        .read(DetectorTest.class.getResource("/artifacts/sample.surr.xml"));
    assertTrue(surr.isPresent());
    KnowledgeCarrier carrier = KnowledgeCarrier.of(surr.get());

    SyntacticRepresentation rep = detector.getDetectedRepresentation(carrier);

    assertNotNull(rep);
    assertEquals(Asset_Surrogate, rep.getLanguage());
    assertEquals(XML_1_1, rep.getFormat());

    Optional<String> jsonSurr = FileUtil
        .read(DetectorTest.class.getResource("/artifacts/sample.surr.json"));
    assertTrue(jsonSurr.isPresent());
    KnowledgeCarrier carrier2 = KnowledgeCarrier.of(jsonSurr.get());

    SyntacticRepresentation rep2 = detector.getDetectedRepresentation(carrier2);

    assertNotNull(rep2);
    assertEquals(Asset_Surrogate, rep2.getLanguage());
    assertEquals(JSON, rep2.getFormat());
  }

  @Test
  public void testOWLDetector() {
    InputStream is = DetectorTest.class.getResourceAsStream( "/artifacts/test.ofn" );
    KnowledgeCarrier carrier = KnowledgeCarrier.of(is);

    SyntacticRepresentation rep = detector.getDetectedRepresentation(carrier);
    assertEquals(OWL_2,rep.getLanguage());
    assertEquals(OWL_2_RL,rep.getProfile());
    assertEquals(TXT,rep.getFormat());
    assertEquals(OWL_Functional_Syntax,rep.getSerialization());
    assertTrue(rep.getLexicon().isEmpty());
  }


}
