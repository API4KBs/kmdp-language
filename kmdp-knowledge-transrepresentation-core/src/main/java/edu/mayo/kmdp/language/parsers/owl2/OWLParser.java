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
package edu.mayo.kmdp.language.parsers.owl2;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_XML_Serialization;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.parsers.AbstractDeSerializer;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.apache.jena.vocabulary.DCTerms;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

@Named
@KPOperation(Lowering_Task)
@KPOperation(Lifting_Task)
@KPSupport(OWL_2)
public class OWLParser extends AbstractDeSerializer implements DeserializeApiInternal {


  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier) {
    return carrier.asBinary()
        .map(String::new)
        .map(str -> new KnowledgeCarrier()
            .withExpression(str)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier) {
    try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration()
          .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
          .addIgnoredImport(IRI.create(DCTerms.getURI()));
      manager.setOntologyLoaderConfiguration(conf);

      Optional<byte[]> bytes = carrier.asBinary();
      if (!bytes.isPresent()) {
        return Optional.empty();
      }
      OWLOntology onto = manager.loadOntologyFromOntologyDocument(
          new ByteArrayInputStream(bytes.get()));
      return Optional.of(new KnowledgeCarrier()
          .withExpression(onto)
          .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression)
          .withRepresentation(rep(OWL_2)));
    } catch (OWLOntologyCreationException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.asBinary()
        .map(bytes -> new KnowledgeCarrier()
            .withExpression(bytes)
            .withRepresentation(
                getSerializeResultRepresentation(carrier,
                    ParsingLevelSeries.Encoded_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    try {
      Optional<OWLOntology> onto = carrier.as(OWLOntology.class);
      if (!onto.isPresent()) {
        return Optional.empty();
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLManager.createOWLOntologyManager()
          .saveOntology(onto.get(), new RDFXMLDocumentFormat(), baos);
      return Optional
          .of(new KnowledgeCarrier()
              .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
              .withExpression(new String(baos.toByteArray()))
              .withRepresentation(
                  rep(OWL_2, RDF_XML_Syntax, XML_1_1)));
    } catch (OWLOntologyStorageException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    throw new UnsupportedOperationException();
  }


  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return Collections.singletonList(
        rep(OWL_2, RDF_XML_Syntax, XML_1_1));
  }

  @Override
  public Answer<List<SyntacticRepresentation>> getParsableLanguages() {
    return Answer.of(
        Arrays.asList(
            rep(OWL_2, RDF_XML_Syntax, XML_1_1),
            rep(OWL_2, OWL_Functional_Syntax, TXT),
            rep(OWL_2, OWL_Manchester_Syntax, TXT),
            rep(OWL_2, OWL_XML_Serialization, XML_1_1),
            rep(OWL_2, Turtle, TXT)));
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return XML_1_1;
  }


}
