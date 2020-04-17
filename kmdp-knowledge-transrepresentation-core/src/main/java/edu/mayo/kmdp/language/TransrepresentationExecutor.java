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

import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.Answer.anyAble;

import edu.mayo.kmdp.tranx.v4.server.DiscoveryApiInternal;
import edu.mayo.kmdp.tranx.v4.server.TransxionApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.KnowledgePlatformComponent;
import org.omg.spec.api4kp._1_0.id.KeyIdentifier;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
import org.omg.spec.api4kp._1_0.services.tranx.Transrepresentator;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class TransrepresentationExecutor implements KnowledgePlatformComponent<Transrepresentator>,
    TransxionApiInternal, DiscoveryApiInternal {

  private UUID id = UUID.randomUUID();
  private Transrepresentator descriptor;

  private Map<KeyIdentifier, TransionApiOperator> translators;

  @Named
  public TransrepresentationExecutor(
      @Autowired(required = false)
      @KPOperation(KnowledgeProcessingOperationSeries.Transcreation_Task)
      @KPOperation(KnowledgeProcessingOperationSeries.Translation_Task)
          List<TransionApiOperator> translators) {

    this.translators = translators.stream()
        .collect(Collectors.toMap(
            tx -> tx.getOperatorId().asKey(),
            tx -> tx
        ));

    this.descriptor = toKPComponent(getComponentId());
  }

  @Override
  public Answer<Transrepresentator> getTxComponent(UUID componentId) {
    return Answer.of(getDescriptor());
  }

  @Override
  public Answer<List<Transrepresentator>> listTxComponents() {
    return Answer.of(singletonList(getDescriptor()));
  }

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresent(KnowledgeCarrier sourceArtifact, String xAccept) {
    return Answer.of(anyAble(translators.values(),
        TransionApiOperator::can_applyTransrepresent))
        .flatOpt(TransionApiOperator::as_applyTransrepresent)
        .flatMap(a -> a.applyTransrepresent(sourceArtifact, xAccept));
  }

  @Override
  public Answer<KnowledgeCarrier> applyNamedTransrepresent(UUID operatorId,
      KnowledgeCarrier sourceArtifact, String xAccept) {
    return Answer.of(getTransrepresentator(operatorId))
        .flatOpt(TransionApiOperator::as_applyNamedTransrepresent)
        .flatMap(a -> a.applyNamedTransrepresent(operatorId, sourceArtifact, xAccept));
  }

  @Override
  public Answer<TransrepresentationOperator> getTxionOperator(UUID operatorId) {
    return Answer.of(getTransrepresentator(operatorId)
        .map(TransionApiOperator::getDescriptor));
  }

  @Override
  public Answer<List<TransrepresentationOperator>> listTxionOperators() {
    return Answer.of(translators.values().stream()
        .map(TransionApiOperator::getDescriptor)
        .collect(Collectors.toList()));
  }


  @Override
  public UUID getComponentUuid() {
    return id;
  }

  @Override
  public Transrepresentator toKPComponent(ResourceIdentifier componentId) {
    return new Transrepresentator()
        .withInstanceId(getComponentId());
  }

  @Override
  public Transrepresentator getDescriptor() {
    return descriptor;
  }

  private Optional<TransionApiOperator> getTransrepresentator(UUID operatorId) {
    return translators.keySet().stream()
        .filter(key -> key.getUuid().equals(operatorId))
        .findFirst()
        .map(translators::get);
  }

}
