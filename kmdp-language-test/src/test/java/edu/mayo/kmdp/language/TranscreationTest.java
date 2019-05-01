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

import static edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage.DMN_1_1;
import static edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage.OWL_2;
import static edu.mayo.kmdp.util.Util.uuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTranscreator;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.terms.lexicon._2018._08.Lexicon;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.spec.api4kp._1_0.PlatformComponentHelper;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
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

    Owl2SkosConfig p = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://bar/skos-example");

    ASTCarrier ac = KnowledgeCarrier.of(owl.get(), rep(OWL_2))
        .flatMap((kc) -> transtor.applyTransrepresentation(OWLtoSKOSTranscreator.operatorId, kc, p))
        .flatMap((kc) -> parser.lift(kc, ParsingLevel.Abstract_Knowledge_Expression))
        .flatMap(ASTCarrier.class::cast);

    checkSKOS(ac);
  }

  private void checkSKOS(ASTCarrier ac) {
    assertNotNull(ac);
    OWLOntology onto = (OWLOntology) ac.getParsedExpression();

    assertNotNull(onto);

    OWLDataFactory f = onto.getOWLOntologyManager().getOWLDataFactory();
    List<UUID> names = EntitySearcher.getIndividuals(f.getOWLClass(SKOS.CONCEPT.toString()), onto)
        .filter(OWLNamedIndividual.class::isInstance)
        .map(OWLNamedIndividual.class::cast)
        .map(OWLNamedIndividual::getIRI)
        .map(IRI::toString)
        .map(NameUtils::getTrailingPart)
        .map(Util::ensureUUID)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
    assertEquals(new HashSet<>(Arrays.asList(
        uuid("A"),
        uuid("B"),
        uuid("C"),
        uuid("skos-example_Scheme_Top"))),
        new HashSet<>(names));
  }


  @Test
  public void testTransrepresentationFilter() {

    Set<String> ops = transtor.listOperators(null, null, null).stream()
        .map(KnowledgeProcessingOperator::getOperatorId).collect(
            Collectors.toSet());
    assertTrue(ops.contains(OWLtoSKOSTranscreator.operatorId));

    Set<String> ops2 = transtor.listOperators(rep(OWL_2), rep(OWL_2).withLexicon(Lexicon.SKOS), null).stream()
        .map(KnowledgeProcessingOperator::getOperatorId).collect(
            Collectors.toSet());
    assertTrue(ops2.contains(OWLtoSKOSTranscreator.operatorId));

    Set<String> ops3 = transtor.listOperators(rep(OWL_2), rep(DMN_1_1), null).stream()
        .map(KnowledgeProcessingOperator::getOperatorId).collect(
            Collectors.toSet());
    assertFalse(ops3.contains(OWLtoSKOSTranscreator.operatorId));


  }

  @Test
  public void testTransrepresentationDiscovery() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/exampleHierarchy.rdf"));
    assertTrue(owl.isPresent());

    KnowledgeCarrier kc = KnowledgeCarrier.of(owl.get(), rep(OWL_2));

    ASTCarrier ac = transtor
        .listOperators(kc.getRepresentation(), rep(OWL_2).withLexicon(Lexicon.SKOS), null).stream()
        .findAny()
        .map((op) -> transtor.applyTransrepresentation(
            op.getOperatorId(),
            kc,
            new Owl2SkosConfig(PlatformComponentHelper.defaults(op.getAcceptedParams()))
                .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://bar/skos-example")))
        .map((out) -> parser.lift(out, ParsingLevel.Abstract_Knowledge_Expression))
        .map(ASTCarrier.class::cast)
        .get();

    checkSKOS(ac);
  }


}
