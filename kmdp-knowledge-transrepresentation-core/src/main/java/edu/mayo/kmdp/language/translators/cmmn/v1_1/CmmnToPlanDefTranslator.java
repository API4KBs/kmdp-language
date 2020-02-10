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
package edu.mayo.kmdp.language.translators.cmmn.v1_1;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Translation_Task;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.tranx.v3.server.TransxionApiInternal;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
import org.omg.spec.cmmn._20151109.model.TDefinitions;

@Named
@KPOperation(Translation_Task)
public class CmmnToPlanDefTranslator implements TransxionApiInternal {

  private static final String OPERATOR_ID = "87402252-a8a1-46a4-be3a-9b04ce45fde7";

  public String getId() {
    return OPERATOR_ID;
  }

  public org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getFrom() {
    return new SyntacticRepresentation()
        .withLanguage(CMMN_1_1);
  }

  public org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getTo() {
    return new SyntacticRepresentation()
        .withLanguage(FHIR_STU3);
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
    return Answer.of(new ParameterDefinitions());
  }


  public Answer<org.omg.spec.api4kp._1_0.services.SyntacticRepresentation> getTransrepresentationInput(String txionId) {
    if (txionId != null && !OPERATOR_ID.equals(txionId)) {
      return Answer.failed(new UnsupportedOperationException());
    }
    return Answer.of(
        rep(CMMN_1_1));
  }

  @Override
  public Answer<org.omg.spec.api4kp._1_0.services.SyntacticRepresentation> getTransrepresentationOutput(
      String txionId) {
    return Answer.of(
        rep(FHIR_STU3)
            .withLexicon(LexiconSeries.SNOMED_CT, LexiconSeries.PCV));
  }

  @Override
  public Answer<List<TransrepresentationOperator>> listOperators(
      org.omg.spec.api4kp._1_0.services.SyntacticRepresentation from,
      org.omg.spec.api4kp._1_0.services.SyntacticRepresentation into,
      String method) {
    return getTransrepresentation(OPERATOR_ID)
        .map(Collections::singletonList);
  }


  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentation(
      String txId,
      KnowledgeCarrier sourceArtifact,
      Properties params) {
    return Answer.of(AbstractCarrier.ofAst(
        new CmmnToPlanDef().transform(
            sourceArtifact.getAssetId(),
            sourceArtifact.as(TDefinitions.class)
                .orElseThrow(IllegalStateException::new)))
        .withAssetId(sourceArtifact.getAssetId())
        .withArtifactId(DatatypeHelper.uri(UUID.randomUUID().toString()))
        .withRepresentation(rep(FHIR_STU3))
    );
  }

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentationInto(KnowledgeCarrier sourceArtifact,
      org.omg.spec.api4kp._1_0.services.SyntacticRepresentation into) {
    return applyTransrepresentation(
        OPERATOR_ID,
        sourceArtifact,
        null);
  }


}
