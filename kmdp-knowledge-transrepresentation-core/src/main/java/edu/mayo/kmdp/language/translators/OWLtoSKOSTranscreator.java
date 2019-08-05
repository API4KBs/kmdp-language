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
package edu.mayo.kmdp.language.translators;

import static edu.mayo.kmdp.util.ws.ResponseHelper.get;
import static edu.mayo.kmdp.util.ws.ResponseHelper.succeed;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.PlatformComponentHelper.asParamDefinitions;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.tranx.server.TransxionApiDelegate;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krserialization._20190801.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon._20190801.Lexicon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.inject.Named;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
import org.springframework.http.ResponseEntity;

@Named
@KPOperation(KnowledgeProcessingOperation.Transcreation_Task)
public class OWLtoSKOSTranscreator implements TransxionApiDelegate {

  public final static String operatorId = "57869ee0-304c-40a4-8759-40ea667c328d";

  @Override
  public ResponseEntity<KnowledgeCarrier> applyTransrepresentation(String txionId,
      KnowledgeCarrier sourceArtifact,
      Properties params) {

    Owl2SkosConfig config = new Owl2SkosConfig(params);

    InputStream is;
    switch (sourceArtifact.getLevel()) {
      case Encoded_Knowledge_Expression:
        is = new ByteArrayInputStream(((BinaryCarrier) sourceArtifact).getEncodedExpression());
        break;
      case Concrete_Knowledge_Expression:
        is = new ByteArrayInputStream(
            ((ExpressionCarrier) sourceArtifact).getSerializedExpression().getBytes());
        break;
      case Parsed_Knowedge_Expression:
      case Abstract_Knowledge_Expression:
      default:
        return ResponseHelper.fail();
    }

    Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model = model.read(is, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new Owl2SkosConverter()
        .apply(model, config)
        .ifPresent((m) -> m.write(baos));

    String skos = new String(baos.toByteArray());
    return Util.isEmpty(skos)
        ? ResponseHelper.fail()
        : succeed(KnowledgeCarrier.of(skos)
            .withRepresentation(
                ResponseHelper.get(getTransrepresentationOutput(txionId)).orElse(null)));
  }

  @Override
  public ResponseEntity<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return succeed(
        new org.omg.spec.api4kp._1_0.services.tranx.resources.TransrepresentationOperator()
            .withOperatorId(operatorId)
            .withAcceptedParams(get(getTransrepresentationAcceptedParameters(txionId)).orElse(null))
            .withFrom(get(getTransrepresentationInput(txionId)).orElse(null))
            .withInto(get(getTransrepresentationOutput(txionId)).orElse(null)));
  }

  @Override
  public ResponseEntity<ParameterDefinitions> getTransrepresentationAcceptedParameters(
      String txionId) {
    return succeed(asParamDefinitions(new Owl2SkosConfig()));
  }

  @Override
  public ResponseEntity<SyntacticRepresentation> getTransrepresentationOutput(String txionId) {
    return succeed(
        rep(KnowledgeRepresentationLanguage.OWL_2,
            KnowledgeRepresentationLanguageSerialization.RDF_XML_Syntax,
            SerializationFormat.XML_1_1)
            .withLexicon(Lexicon.SKOS));
  }

  public ResponseEntity<SyntacticRepresentation> getTransrepresentationInput(String txionId) {
    return succeed(
        rep(KnowledgeRepresentationLanguage.OWL_2,
            KnowledgeRepresentationLanguageSerialization.RDF_XML_Syntax,
            SerializationFormat.XML_1_1));
  }

  @Override
  public ResponseEntity<List<TransrepresentationOperator>> listOperators(
      SyntacticRepresentation from,
      SyntacticRepresentation into,
      String method) {
    return ResponseHelper.map(getTransrepresentation(operatorId),
        Collections::singletonList);
  }


}
