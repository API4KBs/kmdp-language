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

import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.Answer.anyAble;

import edu.mayo.kmdp.tranx.v4.server.DiscoveryApiInternal;
import edu.mayo.kmdp.tranx.v4.server.ValidateApiInternal;
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
import org.omg.spec.api4kp._1_0.services.tranx.ValidationOperator;
import org.omg.spec.api4kp._1_0.services.tranx.Validator;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class LanguageValidator implements KnowledgePlatformComponent<Validator>,
    ValidateApiInternal, DiscoveryApiInternal {

  private UUID id = UUID.randomUUID();
  private Validator descriptor;

  Map<KeyIdentifier,ValidateApiOperator> validators;

  @Named
  public LanguageValidator(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperationSeries.Well_Formedness_Check_Task)
      List<ValidateApiOperator> validators) {

    this.validators = validators.stream()
        .collect(Collectors.toMap(
            det -> det.getOperatorId().asKey(),
            det -> det
        ));

    this.descriptor = toKPComponent(getComponentId());
  }


  @Override
  public Answer<Validator> getValidationComponent(UUID componentId) {
    return Answer.of(getDescriptor());
  }

  @Override
  public Answer<List<Validator>> listValidationComponents() {
    return Answer.of(singletonList(getDescriptor()));
  }

  @Override
  public Answer<ValidationOperator> getValidationOperator(UUID operatorId) {
    return Answer.of(getValidator(operatorId)
        .map(ValidateApiOperator::getDescriptor));
  }

  @Override
  public Answer<List<ValidationOperator>> listValidationOperators() {
    return Answer.of(validators.values().stream()
        .map(ValidateApiOperator::getDescriptor)
        .collect(Collectors.toList()));
  }

  @Override
  public Answer<Void> applyValidate(KnowledgeCarrier sourceArtifact) {
    return Answer.of(anyAble(validators.values(),
        ValidateApiOperator::can_applyValidate))
        .flatOpt(ValidateApiOperator::as_applyValidate)
        .flatMap(v -> v.applyValidate(sourceArtifact));
  }

  @Override
  public Answer<Void> applyNamedValidate(UUID operatorId,
      KnowledgeCarrier sourceArtifact) {
    return Answer.of(getValidator(operatorId))
        .flatOpt(ValidateApiOperator::as_applyNamedValidate)
        .flatMap(v -> v.applyNamedValidate(operatorId, sourceArtifact));
  }


  @Override
  public UUID getComponentUuid() {
    return id;
  }

  @Override
  public Validator toKPComponent(ResourceIdentifier componentId) {
    return new Validator()
        .withInstanceId(getComponentId());
  }

  @Override
  public Validator getDescriptor() {
    return descriptor;
  }


  private Optional<ValidateApiOperator> getValidator(UUID operatorId) {
    return validators.keySet().stream()
        .filter(key -> key.getUuid().equals(operatorId))
        .findFirst()
        .map(validators::get);
  }
}

