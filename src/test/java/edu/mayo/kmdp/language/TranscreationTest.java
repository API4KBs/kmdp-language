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

import static edu.mayo.kmdp.util.Util.uuid;
import static edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage.OWL_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTranscreator;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.tranx.DeserializeApi;
import edu.mayo.kmdp.tranx.TransxionApi;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.lexicon._2018._08.Lexicon;
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
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.PlatformComponentHelper;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
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

    Optional<ASTCarrier> ac = KnowledgeCarrier.of(owl.get(), rep(OWL_2))
        .flatMap((kc) -> transtor.applyTransrepresentation(OWLtoSKOSTranscreator.operatorId, kc, p))
        .flatMap((kc) -> parser.lift(kc, ParsingLevel.Abstract_Knowledge_Expression))
        .filter(ASTCarrier.class::isInstance)
        .map(ASTCarrier.class::cast)
        .getOptionalValue();

    assertTrue(ac.isPresent());
    checkSKOS(ac.get());
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
        uuid("skos-example_Top"))),
        new HashSet<>(names));
  }


  @Test
  public void testTransrepresentationFilter() {

    Optional<Set<String>> ops = transtor
        .listOperators(null, null, null)
        .map((l) -> l.stream()
            .map(KnowledgeProcessingOperator::getOperatorId)
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertTrue(ops.isPresent()
        && ops.get().contains(OWLtoSKOSTranscreator.operatorId));

    Optional<Set<String>> ops2 = transtor
        .listOperators(rep(OWL_2), rep(OWL_2).withLexicon(Lexicon.SKOS), null)
        .map((l) -> l.stream()
            .map(KnowledgeProcessingOperator::getOperatorId)
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertTrue(ops2.isPresent()
        && ops2.get().contains(OWLtoSKOSTranscreator.operatorId));

    Optional<Set<String>> ops3 = transtor
        .listOperators(rep(OWL_2), rep(DMN_1_1), null)
        .map((l) -> l.stream()
            .map(KnowledgeProcessingOperator::getOperatorId)
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertFalse(ops3.isPresent()
        && ops3.get().contains(OWLtoSKOSTranscreator.operatorId));


  }

  @Test
  public void testTransrepresentationDiscovery() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/exampleHierarchy.rdf"));
    assertTrue(owl.isPresent());

    KnowledgeCarrier kc = KnowledgeCarrier.of(owl.get(), rep(OWL_2));

    Optional<ASTCarrier> ac = transtor
        .listOperators(kc.getRepresentation(), rep(OWL_2).withLexicon(Lexicon.SKOS), null)
        .flatMap(Answer::first)
        .flatMap((op) -> transtor.applyTransrepresentation(
                op.getOperatorId(),
                kc,
                new Owl2SkosConfig(PlatformComponentHelper.defaults(op.getAcceptedParams()))
                    .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://bar/skos-example")))
        .flatMap((out) -> parser.lift(out, ParsingLevel.Abstract_Knowledge_Expression))
        .map(ASTCarrier.class::cast)
        .getOptionalValue();

    assertTrue(ac.isPresent());
    checkSKOS(ac.get());
  }


}