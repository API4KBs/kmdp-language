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
package edu.mayo.kmdp.language;

import static edu.mayo.kmdp.comparator.Contrastor.isBroaderOrEqual;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.tranx.v3.server.TransxionApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class TransrepresentationExecutor implements TransxionApiInternal {

  private Map<String, TransxionApiInternal> translatorById;

  @Named
  public TransrepresentationExecutor(
      @Autowired(required = false)
      @KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
      @KPOperation(KnowledgeProcessingOperationSeries.Translation_Task)
          List<TransxionApiInternal> translators) {
    if (translators != null) {
      translatorById = new HashMap<>();

      translators.forEach(t ->
          t.getTransrepresentation(null)
              .map(KnowledgeProcessingOperator::getOperatorId)
              .ifPresent(id -> translatorById.put(id, t)));
    } else {
      this.translatorById = Collections.emptyMap();
    }

  }

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentation(String txionId,
      final KnowledgeCarrier sourceArtifact, Properties config) {
    return Answer.of(getTxOperator(txionId))
        .flatMap(txion -> txion.applyTransrepresentation(txionId, sourceArtifact, config));
  }

  @Override
  public Answer<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return Answer.of(getTxOperator(txionId))
        .flatMap(t -> t.getTransrepresentation(txionId));
  }

  @Override
  public Answer<ParameterDefinitions> getTransrepresentationAcceptedParameters(
      String txionId) {
    return Answer.of(getTxOperator(txionId))
        .flatMap(t -> t.getTransrepresentation(txionId))
        .map(KnowledgeProcessingOperator::getAcceptedParams);
  }

  @Override
  public Answer<SyntacticRepresentation> getTransrepresentationOutput(String txionId) {
    return Answer.of(getTxOperator(txionId))
        .flatMap(t -> t.getTransrepresentation(txionId))
        .map(TransrepresentationOperator::getInto);
  }

  @Override
  public Answer<List<TransrepresentationOperator>> listOperators(
      SyntacticRepresentation from,
      SyntacticRepresentation into,
      String method) {

    return
        translatorById.values().stream()
            .map(tx -> tx.getTransrepresentation(null))
            .collect(Answer.toList(tx -> canTransform(tx, into, from)))
        ;

  }

  private boolean canTransform(TransrepresentationOperator op, SyntacticRepresentation into,
      SyntacticRepresentation from) {
    return (from == null || isBroaderOrEqual(theRepContrastor.contrast(from, op.getFrom())))
        &&
        (into == null || isBroaderOrEqual(theRepContrastor.contrast(into, op.getInto())));
  }


  private Optional<TransxionApiInternal> getTxOperator(String txId) {
    return Optional.ofNullable(translatorById.get(txId));
  }

}
