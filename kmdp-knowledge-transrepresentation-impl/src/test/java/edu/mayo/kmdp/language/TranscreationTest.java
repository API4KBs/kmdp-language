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

import static edu.mayo.kmdp.util.Util.uuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;

import edu.mayo.kmdp.language.config.LocalTestConfig;
import edu.mayo.kmdp.language.translators.owl2.OWLtoSKOSTranscreator;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.DeserializeApi;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.TransxionApi;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries;
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

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://bar/skos-example");

    Answer<KnowledgeCarrier> ans =
        Answer.of(owl)
            .map(o -> AbstractCarrier.of(o, rep(OWL_2,RDF_XML_Syntax,XML_1_1)))
            .flatMap(
                kc -> transtor.applyNamedTransrepresent(
                    OWLtoSKOSTranscreator.id,
                    kc,
                    ModelMIMECoder.encode(rep(OWL_2)),
                    cfg.encode()))
            .flatMap(kc -> parser.applyLift(kc, Abstract_Knowledge_Expression));

    assertTrue(ans.isSuccess());
    checkSKOS(ans.get());
  }

  private void checkSKOS(KnowledgeCarrier ac) {
    assertNotNull(ac);
    OntModel onto = ac.as(OntModel.class)
        .orElseGet(Assertions::fail);

    List<UUID> names = onto.listIndividuals()
        .filterKeep(ind -> ind.hasOntClass(SKOS.Concept))
        .mapWith(Resource::getURI)
        .mapWith(NameUtils::getTrailingPart)
        .mapWith(Util::ensureUUID)
        .filterKeep(Optional::isPresent)
        .mapWith(Optional::get)
        .toList();

    assertEquals(new HashSet<>(Arrays.asList(
        uuid("A"),
        uuid("B"),
        uuid("C"),
        uuid("skos-example_Top"))),
        new HashSet<>(names));
  }


  @Test
  public void testTransrepresentationFilter() {
    Optional<Set<UUID>> ops = transtor
        .listTxionOperators()
        .map((l) -> l.stream()
            .map(op -> op.getOperatorId().getUuid())
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertTrue(ops.isPresent()
        && ops.get().contains(OWLtoSKOSTranscreator.id));
  }


  @Test
  public void testTransrepresentationFilter2() {
    Optional<Set<UUID>> ops2 = transtor
        .listTxionOperators(
            codedRep(OWL_2, RDF_XML_Syntax, XML_1_1, Charset.defaultCharset()),
            codedRep(OWL_2, LexiconSeries.SKOS))
        .map(l -> l.stream()
            .map(op -> op.getOperatorId().getUuid())
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertTrue(ops2.isPresent()
        && ops2.get().contains(OWLtoSKOSTranscreator.id));
  }


  @Test
  public void testTransrepresentationFilter3() {
    Optional<Set<UUID>> ops3 = transtor
        .listTxionOperators(codedRep(OWL_2), codedRep(DMN_1_1))
        .map((l) -> l.stream()
            .map(op -> op.getOperatorId().getUuid())
            .collect(Collectors.toSet()))
        .getOptionalValue();

    assertFalse(ops3.isPresent()
        && ops3.get().contains(OWLtoSKOSTranscreator.id));
  }

  @Test
  public void testTransrepresentationDiscovery() {
    Optional<byte[]> owl = FileUtil
        .readBytes(DetectorTest.class.getResource("/artifacts/exampleHierarchy.rdf"));

    assertTrue(owl.isPresent());
    byte[] owlBytes = owl.get();
    KnowledgeCarrier kc = AbstractCarrier.of(owlBytes, rep(OWL_2, RDF_XML_Syntax, XML_1_1));

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://bar/skos-example");

    Answer<KnowledgeCarrier> ac = transtor
        .listTxionOperators(codedRep(kc.getRepresentation()), codedRep(OWL_2,LexiconSeries.SKOS))
        .flatMap(Answer::first)
        .flatMap(op -> transtor.applyNamedTransrepresent(
            op.getOperatorId().getUuid(),
            kc,
            ModelMIMECoder.encode(rep(OWL_2)),
            cfg.encode()))
        .flatMap(out -> parser.applyLift(out, Abstract_Knowledge_Expression));

    assertTrue(ac.isSuccess());
    checkSKOS(ac.get());
  }


}
