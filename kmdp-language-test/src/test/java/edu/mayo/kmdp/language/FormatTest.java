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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalTestConfig.class)
@ActiveProfiles(profiles = "test")
@WebAppConfiguration
public class FormatTest {

  @Inject
  @KPComponent
  DeserializeApi parser;

  @Test
  public void testOWL2() {
    InputStream is = FormatTest.class.getResourceAsStream("/artifacts/test.ofn");

    KnowledgeCarrier kc = KnowledgeCarrier.of(is, rep(KRLanguage.OWL_2))
        .flatMap((c) -> parser
            .ensureRepresentation(c,
                rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax,
                    KRFormat.XML_1_1, Charset.defaultCharset().name())));

    assertTrue(kc instanceof ExpressionCarrier);
    try {
      OWLOntology o = OWLManager.createOWLOntologyManager()
          .loadOntologyFromOntologyDocument(new ByteArrayInputStream(
              ((ExpressionCarrier) kc).getSerializedExpression().getBytes()));
      assertEquals(new RDFXMLDocumentFormat(), o.getFormat());
    } catch (OWLOntologyCreationException e) {
      fail(e.getMessage());
    }
  }

}
