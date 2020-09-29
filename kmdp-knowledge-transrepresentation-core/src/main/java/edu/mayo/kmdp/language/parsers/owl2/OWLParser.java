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

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Lifting_Task;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Lowering_Task;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_XML_Serialization;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.snapshot.ParsingLevel.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.snapshot.ParsingLevel.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.snapshot.ParsingLevel.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.language.DeserializeApiOperator;
import edu.mayo.kmdp.language.parsers.AbstractDeSerializeOperator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.apache.jena.vocabulary.DCTerms;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
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
public class OWLParser extends AbstractDeSerializeOperator {

  public static final UUID id = UUID.fromString("6527546e-925d-413a-ad97-e8bf9142ea51");
  public static final String version = "1.0.0";

  public OWLParser() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier, Properties properties) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier, Properties properties) {
    return carrier.asBinary()
        .map(String::new)
        .map(str -> newVerticalCarrier(
            carrier,
            Serialized_Knowledge_Expression,
            getParseResultRepresentation(carrier,
                Serialized_Knowledge_Expression),
            str
        ));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier, Properties properties) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier, Properties properties) {
    try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration()
          .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
          .addIgnoredImport(IRI.create(DCTerms.getURI()));
      manager.setOntologyLoaderConfiguration(conf);

      Optional<byte[]> bytes = carrier.asBinary();
      if (bytes.isEmpty()) {
        return Optional.empty();
      }
      OWLOntology onto = manager.loadOntologyFromOntologyDocument(
          new ByteArrayInputStream(bytes.get()));

      return Optional.ofNullable(
          newVerticalCarrier(
              carrier,
              Abstract_Knowledge_Expression,
              rep(OWL_2),
              onto));
    } catch (OWLOntologyCreationException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    return carrier.asBinary()
        .map(bytes -> newVerticalCarrier(
            carrier,
            Encoded_Knowledge_Expression,
            getParseResultRepresentation(carrier,
                Encoded_Knowledge_Expression),
            bytes
        ));
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    try {
      Optional<OWLOntology> onto = carrier.as(OWLOntology.class);
      if (!onto.isPresent()) {
        return Optional.empty();
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLManager.createOWLOntologyManager()
          .saveOntology(onto.get(), new RDFXMLDocumentFormat(), baos);
      return Optional.ofNullable(
          newVerticalCarrier(
              carrier,
              Serialized_Knowledge_Expression,
              rep(OWL_2, RDF_XML_Syntax, XML_1_1),
              new String(baos.toByteArray())));
    } catch (OWLOntologyStorageException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    throw new UnsupportedOperationException();
  }


  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
        rep(OWL_2, RDF_XML_Syntax, XML_1_1),
        rep(OWL_2, OWL_Functional_Syntax, TXT),
        rep(OWL_2, OWL_Manchester_Syntax, TXT),
        rep(OWL_2, OWL_XML_Serialization, XML_1_1),
        rep(OWL_2, Turtle, TXT));
  }


  @Override
  protected SerializationFormat getDefaultFormat() {
    return XML_1_1;
  }


  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return OWL_2;
  }
}
