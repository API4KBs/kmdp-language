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

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.tranx.v4.DeserializeApi;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
public class FormatTest {

  @Inject
  @KPComponent
  DeserializeApi parser;

  @Test
  @SuppressWarnings("deprecation")
  public void testOWL2() {
    InputStream is = FormatTest.class.getResourceAsStream("/artifacts/test.ofn");

    Answer<KnowledgeCarrier> ans =
        Answer.of(is)
            .map(i -> AbstractCarrier.of(i, rep(OWL_2)))
            .flatMap(c -> parser
                .ensureRepresentation(c,
                    rep(OWL_2,
                        RDF_XML_Syntax,
                        XML_1_1,
                        Charset.defaultCharset().name())));

    assertTrue(ans.isSuccess());
    assertTrue(ans.get() instanceof ExpressionCarrier);
    try {
      OWLOntology o = OWLManager.createOWLOntologyManager()
          .loadOntologyFromOntologyDocument(new ByteArrayInputStream(
              ((ExpressionCarrier) ans.get()).getSerializedExpression().getBytes()));
      assertEquals(new RDFXMLDocumentFormat(), o.getFormat());
    } catch (OWLOntologyCreationException e) {
      fail(e.getMessage());
    }
  }

}
