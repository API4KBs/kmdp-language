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
package edu.mayo.kmdp.language;

import static edu.mayo.kmdp.comparator.Contrastor.isBroaderOrEqual;
import static edu.mayo.kmdp.util.ws.ResponseHelper.collect;
import static edu.mayo.kmdp.util.ws.ResponseHelper.delegate;
import static edu.mayo.kmdp.util.ws.ResponseHelper.get;
import static edu.mayo.kmdp.util.ws.ResponseHelper.map;
import static edu.mayo.kmdp.util.ws.ResponseHelper.matches;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.repContrastor;

import edu.mayo.kmdp.tranx.server.TransxionApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeProcessingOperation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Named
@KPServer
public class TransrepresentationExecutor implements TransxionApiDelegate {

  private Map<SourceToTarget, TransxionApiDelegate> translatorBySourceTarget;

  private Map<String, TransxionApiDelegate> translatorById;


  public TransrepresentationExecutor(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperation.Transcreation_Task)
  @KPOperation(KnowledgeProcessingOperation.Translation_Task)
      List<TransxionApiDelegate> translators) {
    super();

    if (translators != null) {
      translatorById = new HashMap<>();

      translators.forEach((t) ->
          get(map(t.getTransrepresentation(null),
              KnowledgeProcessingOperator::getOperatorId))
              .ifPresent((id) -> translatorById.put(id, t)));
    } else {
      this.translatorBySourceTarget = Collections.emptyMap();
      this.translatorById = Collections.emptyMap();
    }

  }

  @Override
  public ResponseEntity<KnowledgeCarrier> applyTransrepresentation(String txionId,
      final KnowledgeCarrier sourceArtifact, Properties config) {
    return delegate(getTxOperator(txionId),
        (txion) -> txion.applyTransrepresentation(txionId, sourceArtifact, config));
  }

  @Override
  public ResponseEntity<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return delegate(getTxOperator(txionId),
        (t) -> t.getTransrepresentation(txionId));
  }

  @Override
  public ResponseEntity<ParameterDefinitions> getTransrepresentationAcceptedParameters(
      String txionId) {
    return map(
        delegate(getTxOperator(txionId),
            (t) -> t.getTransrepresentation(txionId)),
        KnowledgeProcessingOperator::getAcceptedParams);
  }

  @Override
  public ResponseEntity<SyntacticRepresentation> getTransrepresentationOutput(String txionId) {
    return map(
        delegate(getTxOperator(txionId),
            (t) -> t.getTransrepresentation(txionId)),
        TransrepresentationOperator::getInto);
  }

  @Override
  public ResponseEntity<List<TransrepresentationOperator>> listOperators(
      SyntacticRepresentation from,
      SyntacticRepresentation into,
      String method) {

    return collect(
        translatorById.values().stream()
        .map((tx) -> tx.getTransrepresentation(null))
        .filter((r) -> matches(r, (txOperator) -> canTransform(txOperator,into,from))));

  }

  private boolean canTransform(TransrepresentationOperator op, SyntacticRepresentation into,
      SyntacticRepresentation from) {
    return (from == null || isBroaderOrEqual(repContrastor.contrast(from, op.getFrom())))
        &&
        (into == null || isBroaderOrEqual(repContrastor.contrast(into, op.getInto())));
  }


  private Optional<TransxionApiDelegate> getTxOperator(String txId) {
    return Optional.ofNullable(translatorById.get(txId));
  }


  private static class SourceToTarget {

    public SyntacticRepresentation from;

    public SyntacticRepresentation to;

    public SourceToTarget(SyntacticRepresentation from, SyntacticRepresentation to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SourceToTarget that = (SourceToTarget) o;
      return Objects.equals(from, that.from) &&
          Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to);
    }
  }
}
