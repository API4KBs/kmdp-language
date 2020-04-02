/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language;

import static edu.mayo.kmdp.util.StreamUtil.filterAs;
import static edu.mayo.kmdp.util.Util.uuid;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.language.translators.owl2.OWLtoSKOSTranscreator;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.tranx.v4.DeserializeApi;
import edu.mayo.kmdp.tranx.v4.TransxionApi;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.PlatformComponentHelper;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
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

    Answer<KnowledgeCarrier> ans =
        Answer.of(owl)
            .map(o -> AbstractCarrier.of(o, rep(OWL_2)))
            .flatMap(
                kc -> transtor.applyTransrepresentation(OWLtoSKOSTranscreator.OPERATOR_ID, kc, p))
            .flatMap(kc -> parser.lift(kc, Abstract_Knowledge_Expression))
            .flatOpt(Util.as(KnowledgeCarrier.class));

    assertTrue(ans.isSuccess());
    checkSKOS(ans.get());
  }

  private void checkSKOS(KnowledgeCarrier ac) {
    assertNotNull(ac);
    OWLOntology onto = ac.as(OWLOntology.class)
        .orElseGet(Assertions::fail);

    OWLDataFactory f = onto.getOWLOntologyManager().getOWLDataFactory();
    List<UUID> names = EntitySearcher.getIndividuals(f.getOWLClass(SKOS.CONCEPT.toString()), onto)
        .flatMap(filterAs(OWLNamedIndividual.class))
        .map(OWLNamedIndividual::getIRI)
        .map(IRI::toString)
        .map(NameUtils::getTrailingPart)
        .map(Util::ensureUUID)
        .flatMap(StreamUtil::trimStream)
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
        && ops.get().contains(OWLtoSKOSTranscreator.OPERATOR_ID));

    Optional<Set<String>> ops2 = transtor
        .listOperators(rep(OWL_2), rep(OWL_2).withLexicon(LexiconSeries.SKOS), null)
        .map((l) -> l.stream()
            .map(KnowledgeProcessingOperator::getOperatorId)
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertTrue(ops2.isPresent()
        && ops2.get().contains(OWLtoSKOSTranscreator.OPERATOR_ID));

    Optional<Set<String>> ops3 = transtor
        .listOperators(rep(OWL_2), rep(DMN_1_1), null)
        .map((l) -> l.stream()
            .map(KnowledgeProcessingOperator::getOperatorId)
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertFalse(ops3.isPresent()
        && ops3.get().contains(OWLtoSKOSTranscreator.OPERATOR_ID));


  }

  @Test
  public void testTransrepresentationDiscovery() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/exampleHierarchy.rdf"));
    assertTrue(owl.isPresent());

    KnowledgeCarrier kc = AbstractCarrier.of(owl.get(), rep(OWL_2));

    Answer<KnowledgeCarrier> ac = transtor
        .listOperators(kc.getRepresentation(), rep(OWL_2).withLexicon(LexiconSeries.SKOS), null)
        .flatMap(Answer::first)
        .flatMap((op) -> transtor.applyTransrepresentation(
            op.getOperatorId(),
            kc,
            new Owl2SkosConfig(PlatformComponentHelper.defaults(op.getAcceptedParams()))
                .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://bar/skos-example")))
        .flatMap((out) -> parser.lift(out, Abstract_Knowledge_Expression));

    assertTrue(ac.isSuccess());
    checkSKOS(ac.get());
  }


}
