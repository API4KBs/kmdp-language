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
package edu.mayo.kmdp.language.parsers;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_XML_Serialization;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.tranx.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
@KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
public class OWLParser extends AbstractDeSerializer implements DeserializeApiInternal {


  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(new String(carrier.getEncodedExpression()))
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    try {
      OWLOntology onto = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
          new ByteArrayInputStream(carrier.getSerializedExpression().getBytes()));
      return Optional.of(new ASTCarrier().withParsedExpression(onto)
          .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression)
          .withRepresentation(rep(OWL_2)));
    } catch (OWLOntologyCreationException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into) {
    return Optional.of(new BinaryCarrier()
        .withEncodedExpression(carrier.getSerializedExpression().getBytes())
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevelSeries.Encoded_Knowledge_Expression)));
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier, SyntacticRepresentation into) {
    try {
      OWLOntology onto = (OWLOntology) carrier.getParsedExpression();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLManager.createOWLOntologyManager().saveOntology(onto, new RDFXMLDocumentFormat(), baos);
      return Optional
          .of(new ExpressionCarrier().withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
              .withSerializedExpression(new String(baos.toByteArray()))
              .withRepresentation(
                  rep(OWL_2, RDF_XML_Syntax, XML_1_1)));
    } catch (OWLOntologyStorageException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier,
      SyntacticRepresentation into) {
    throw new UnsupportedOperationException();
  }


  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
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
