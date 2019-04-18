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
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.repContrastor;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import edu.mayo.kmdp.language.server.TransxionApiDelegate;
import edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.language.TransrepresentationOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Named
public class TransrepresentationExecutor implements TransxionApiDelegate {


  private Map<SourceToTarget, edu.mayo.kmdp.language.TransxionApi> translatorBySourceTarget;

  private Map<String, edu.mayo.kmdp.language.TransxionApi> translatorById;


  public TransrepresentationExecutor(@Autowired(required = false)
  @KPOperation(KnowledgeOperations.Transcreation_Task)
      List<TransxionApi> translators) {
    super();

    if (translators != null) {
      translatorById = new HashMap<>();
      translators.forEach((t) ->
          translatorById.put(t.getTransrepresentation(null).getOperatorId(), t));
    } else {
      this.translatorBySourceTarget = Collections.emptyMap();
      this.translatorById = Collections.emptyMap();
    }

  }

  @Override
  public ResponseEntity<KnowledgeCarrier> applyTransrepresentation(String txionId,
      final KnowledgeCarrier sourceArtifact, Properties config) {
    TransxionApi txion = translatorById.get(txionId);
    return txion != null ? ok(txion.applyTransrepresentation(txionId, sourceArtifact,config))
        : notFound().build();
  }

  @Override
  public ResponseEntity<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return getTxOperator(txionId)
        .map((t) -> t.getTransrepresentation(txionId))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<ParameterDefinitions> getTransrepresentationAcceptedParameters(String txionId) {
    return getTxOperator(txionId)
        .map((t) -> t.getTransrepresentation(txionId))
        .map(KnowledgeProcessingOperator::getAcceptedParams)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<SyntacticRepresentation> getTransrepresentationOutput(String txionId) {
    return getTxOperator(txionId)
        .map((t) -> t.getTransrepresentation(txionId))
        .map(TransrepresentationOperator::getInto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<List<TransrepresentationOperator>> listOperators(
      SyntacticRepresentation from,
      SyntacticRepresentation into, String method) {
    return ok(translatorById.values().stream()
        .map((tx) -> tx.getTransrepresentation(null))
        .filter((op) -> from == null
            || isBroaderOrEqual(repContrastor.contrast(from, op.getFrom())))
        .filter((op) -> from == null
            || isBroaderOrEqual(repContrastor.contrast(into, op.getInto())))
        .collect(Collectors.toList()));
  }


  private Optional<TransxionApi> getTxOperator(String txId) {
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
