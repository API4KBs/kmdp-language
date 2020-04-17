/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language;

import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.Answer.anyAble;

import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.kmdp.tranx.v4.server.DiscoveryApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
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
import org.omg.spec.api4kp._1_0.services.tranx.DeserializationOperator;
import org.omg.spec.api4kp._1_0.services.tranx.Deserializer;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class LanguageDeSerializer implements KnowledgePlatformComponent<Deserializer>,
    DeserializeApiInternal, DiscoveryApiInternal {

  private UUID id = UUID.randomUUID();
  private Deserializer descriptor;

  Map<KeyIdentifier, DeserializeApiOperator> deserializers;

  @Named
  public LanguageDeSerializer(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
  @KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
      List<DeserializeApiOperator> deserializers) {

    this.deserializers = deserializers.stream()
        .collect(Collectors.toMap(
            det -> det.getOperatorId().asKey(),
            det -> det
        ));

    this.descriptor = toKPComponent(getComponentId());
  }

  @Override
  public Answer<Deserializer> getDeserializationComponent(UUID componentId) {
    return Answer.of(getDescriptor());
  }

  @Override
  public Answer<List<Deserializer>> listDeserializationComponents() {
    return Answer.of(singletonList(getDescriptor()));
  }

  @Override
  public Answer<KnowledgeCarrier> applyLift(KnowledgeCarrier sourceArtifact, ParsingLevel levelTag, String xAccept) {
    return Answer.of(anyAble(deserializers.values(),
        DeserializeApiOperator::can_applyLift))
        .flatOpt(DeserializeApiOperator::as_applyLift)
        .flatMap(a -> a.applyLift(sourceArtifact, levelTag, xAccept));
  }

  @Override
  public Answer<KnowledgeCarrier> applyLower(KnowledgeCarrier sourceArtifact, ParsingLevel levelTag, String xAccept) {
    return Answer.of(anyAble(deserializers.values(),
        DeserializeApiOperator::can_applyLower))
        .flatOpt(DeserializeApiOperator::as_applyLower)
        .flatMap(a -> a.applyLower(sourceArtifact, levelTag, xAccept));
  }

  @Override
  public Answer<KnowledgeCarrier> applyNamedLift(UUID operatorId, KnowledgeCarrier sourceArtifact, ParsingLevel levelTag, String xAccept) {
    return Answer.of(getDeserializer(operatorId))
        .flatOpt(DeserializeApiOperator::as_applyNamedLift)
        .flatMap(a -> a.applyNamedLift(operatorId, sourceArtifact, levelTag, xAccept));
  }

  @Override
  public Answer<KnowledgeCarrier> applyNamedLower(UUID operatorId, KnowledgeCarrier sourceArtifact, ParsingLevel levelTag, String xAccept) {
    return Answer.of(getDeserializer(operatorId))
        .flatOpt(DeserializeApiOperator::as_applyNamedLower)
        .flatMap(a -> a.applyNamedLower(operatorId, sourceArtifact, levelTag, xAccept));
  }



  @Override
  public Answer<DeserializationOperator> getLiftOperator(UUID operatorId) {
    return Answer.of(
        getDeserializer(operatorId)
            .filter(op -> op instanceof _applyLift)
            .map(DeserializeApiOperator::getDescriptor)
    );
  }

  @Override
  public Answer<DeserializationOperator> getLowerOperator(UUID operatorId) {
    return Answer.of(
        getDeserializer(operatorId)
            .filter(op -> op instanceof _applyLower)
            .map(DeserializeApiOperator::getDescriptor)
    );
  }

  @Override
  public Answer<List<DeserializationOperator>> listLiftOperators() {
    return Answer.of(
        deserializers.values().stream()
            .filter(op -> op instanceof _applyLift)
            .map(DeserializeApiOperator::getDescriptor)
            .collect(Collectors.toList()));
  }

  @Override
  public Answer<List<DeserializationOperator>> listLowerOperators() {
    return Answer.of(
        deserializers.values().stream()
            .filter(op -> op instanceof _applyLower)
            .map(DeserializeApiOperator::getDescriptor)
            .collect(Collectors.toList()));
  }

  @Override
  public UUID getComponentUuid() {
    return id;
  }

  @Override
  public Deserializer toKPComponent(ResourceIdentifier componentId) {
    return new Deserializer()
        .withInstanceId(getComponentId());
  }

  @Override
  public Deserializer getDescriptor() {
    return descriptor;
  }

  private Optional<DeserializeApiOperator> getDeserializer(UUID operatorId) {
    return deserializers.keySet().stream()
        .filter(key -> key.getUuid().equals(operatorId))
        .findFirst()
        .map(deserializers::get);
  }

}
