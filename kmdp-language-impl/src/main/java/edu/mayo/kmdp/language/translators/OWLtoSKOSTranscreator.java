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

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeOperations.Transcreation_Task;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.TransxionApi;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.krformat._2018._08.KRFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KRLanguage;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KRSerialization;
import edu.mayo.ontology.taxonomies.lexicon._2018._08.Lexicon;
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
import org.omg.spec.api4kp._1_0.PlatformComponentHelper;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.language.TransrepresentationOperator;
import org.omg.spec.api4kp._1_0.services.resources.ParameterDefinitions;

@Named
@KPOperation(Transcreation_Task)
public class OWLtoSKOSTranscreator implements TransxionApi {

  public final static String operatorId = "57869ee0-304c-40a4-8759-40ea667c328d";

  @Override
  public KnowledgeCarrier applyTransrepresentation(String txionId, KnowledgeCarrier sourceArtifact,
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
        return null;
    }

    Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model = model.read(is, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new Owl2SkosConverter()
        .apply(model, config)
        .ifPresent((m) -> m.write(baos));

    String skos = new String(baos.toByteArray());
    return Util.isEmpty(skos)
        ? null
        : KnowledgeCarrier.of(skos)
            .withRepresentation(getTransrepresentationOutput(txionId));
  }

  @Override
  public TransrepresentationOperator getTransrepresentation(String txionId) {
    return new org.omg.spec.api4kp._1_0.services.language.resources.TransrepresentationOperator()
        .withOperatorId(operatorId)
        .withAcceptedParams(getTransrepresentationAcceptedParameters(txionId))
        .withFrom(getTransrepresentationInput(txionId))
        .withInto(getTransrepresentationOutput(txionId));
  }

  @Override
  public ParameterDefinitions getTransrepresentationAcceptedParameters(String txionId) {
    return PlatformComponentHelper.asParamDefinitions(new Owl2SkosConfig());
  }

  @Override
  public SyntacticRepresentation getTransrepresentationOutput(String txionId) {
    return rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax, KRFormat.XML_1_1)
        .withLexicon(Lexicon.SKOS);
  }

  public SyntacticRepresentation getTransrepresentationInput(String txionId) {
    return rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax, KRFormat.XML_1_1);
  }

  @Override
  public List<TransrepresentationOperator> listOperators(SyntacticRepresentation from,
      SyntacticRepresentation into,
      String method) {
    return Collections.singletonList(getTransrepresentation(operatorId));
  }


}
