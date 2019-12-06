/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language.translators;

import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.PlatformComponentHelper.asParamDefinitions;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.tranx.v3.server.TransxionApiInternal;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
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
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
public class OWLtoSKOSTranscreator implements TransxionApiInternal {

  public static final String OPERATOR_ID = "57869ee0-304c-40a4-8759-40ea667c328d";

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentation(String txionId,
      KnowledgeCarrier sourceArtifact,
      Properties params) {

    Owl2SkosConfig config = new Owl2SkosConfig(params);

    InputStream is;
    switch (sourceArtifact.getLevel().asEnum()) {
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
        return Answer.failed(new UnsupportedOperationException());
    }

    Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model = model.read(is, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new Owl2SkosConverter()
        .apply(model, config)
        .ifPresent(m -> m.write(baos));

    String skos = new String(baos.toByteArray());
    return Util.isEmpty(skos)
        ? Answer.failed(new UnsupportedOperationException())
        : Answer.of(AbstractCarrier.of(skos)
            .withRepresentation(
                getTransrepresentationOutput(txionId).orElse(null)));
  }

  @Override
  public Answer<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return Answer.of(
        new org.omg.spec.api4kp._1_0.services.tranx.resources.TransrepresentationOperator()
            .withOperatorId(OPERATOR_ID)
            .withAcceptedParams(getTransrepresentationAcceptedParameters(txionId).orElse(null))
            .withFrom(getTransrepresentationInput(txionId).orElse(null))
            .withInto(getTransrepresentationOutput(txionId).orElse(null)));
  }

  @Override
  public Answer<ParameterDefinitions> getTransrepresentationAcceptedParameters(
      String txionId) {
    return Answer.of(asParamDefinitions(new Owl2SkosConfig()));
  }

  @Override
  public Answer<SyntacticRepresentation> getTransrepresentationOutput(String txionId) {
    return Answer.of(
        rep(KnowledgeRepresentationLanguageSeries.OWL_2,
            KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax,
            SerializationFormatSeries.XML_1_1)
            .withLexicon(LexiconSeries.SKOS));
  }

  public Answer<SyntacticRepresentation> getTransrepresentationInput(String txionId) {
    if (txionId != null && !OPERATOR_ID.equals(txionId)) {
      return Answer.failed(new UnsupportedOperationException());
    }
    return Answer.of(
        rep(KnowledgeRepresentationLanguageSeries.OWL_2,
            KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax,
            SerializationFormatSeries.XML_1_1));
  }

  @Override
  public Answer<List<TransrepresentationOperator>> listOperators(
      SyntacticRepresentation from,
      SyntacticRepresentation into,
      String method) {
    return getTransrepresentation(OPERATOR_ID)
        .map(Collections::singletonList);
  }


}
