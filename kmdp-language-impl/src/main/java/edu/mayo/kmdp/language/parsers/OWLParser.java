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

import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.DeserializeApi;
import edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
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
@KPOperation(KnowledgeOperations.Lowering_Task)
@KPOperation(KnowledgeOperations.Lifting_Task)
public class OWLParser extends AbstractDeSerializer implements DeserializeApi {


  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(new String(carrier.getEncodedExpression()))
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevel.Concrete_Knowledge_Expression)));
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
          .withLevel(ParsingLevel.Abstract_Knowledge_Expression)
          .withRepresentation(rep(KRLanguage.OWL_2)));
    } catch (OWLOntologyCreationException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier) {
    return Optional.of(new BinaryCarrier()
        .withEncodedExpression(carrier.getSerializedExpression().getBytes())
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevel.Encoded_Knowledge_Expression)));
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier) {
    try {
      OWLOntology onto = (OWLOntology) carrier.getParsedExpression();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLManager.createOWLOntologyManager().saveOntology(onto, new RDFXMLDocumentFormat(), baos);
      return Optional
          .of(new ExpressionCarrier().withLevel(ParsingLevel.Concrete_Knowledge_Expression)
              .withSerializedExpression(new String(baos.toByteArray()))
              .withRepresentation(
                  rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax, KRFormat.XML_1_1)));
    } catch (OWLOntologyStorageException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier) {
    throw new UnsupportedOperationException();
  }


  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
        rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax, KRFormat.XML_1_1));
  }

  @Override
  public List<SyntacticRepresentation> getParsableLanguages() {
    return Arrays.asList(
        rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax, KRFormat.XML_1_1),
        rep(KRLanguage.OWL_2, KRSerialization.OWL_Functional_Syntax, KRFormat.TXT),
        rep(KRLanguage.OWL_2, KRSerialization.OWL_Manchester_Syntax, KRFormat.TXT),
        rep(KRLanguage.OWL_2, KRSerialization.OWL_XML_Serialization, KRFormat.XML_1_1),
        rep(KRLanguage.OWL_2, KRSerialization.Turtle, KRFormat.TXT));
  }

  @Override
  protected KRFormat getDefaultFormat() {
    return KRFormat.XML_1_1;
  }


}
