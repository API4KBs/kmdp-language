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

import static edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage.OWL_2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.omg.spec.api4kp.KnowledgeCarrierHelper.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTxConfig;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTxConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.util.FileUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LocalTestConfig.class)
@ActiveProfiles(profiles = "test")
@WebAppConfiguration
public class TranscreationTest {

  @Inject
  @KPComponent
  TransxionApi transtor;

  @Inject
  @KPComponent
  DeserializeApi parser;

  @Test
  public void testOWLtoSKOS() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/exampleHierarchy.rdf"));
    assertTrue(owl.isPresent());

    OWLtoSKOSTxConfig p = new OWLtoSKOSTxConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE,"http://bar/skos-example");

    ASTCarrier ac = KnowledgeCarrier.of(owl.get(), rep(OWL_2))
        .flatMap((kc) -> transtor.applyTransrepresentation("TODO", kc, p))
        .flatMap((kc) -> parser.lift(kc, ParsingLevel.Abstract_Knowledge_Expression))
        .flatMap(ASTCarrier.class::cast);

    assertNotNull(ac);
    OWLOntology onto = (OWLOntology) ac.getParsedExpression();

    assertNotNull(onto);

    OWLDataFactory f = onto.getOWLOntologyManager().getOWLDataFactory();
    List<String> names = EntitySearcher.getIndividuals(f.getOWLClass(SKOS.CONCEPT.toString()),onto)
        .filter(OWLNamedIndividual.class::isInstance)
        .map(OWLNamedIndividual.class::cast)
        .map(OWLNamedIndividual::getIRI)
        .map(IRI::getFragment)
        .collect(Collectors.toList());
    assertEquals(new HashSet<>(Arrays.asList("A","B","C","skos-example_Scheme_Top")),
        new HashSet<>(names));
  }



}
