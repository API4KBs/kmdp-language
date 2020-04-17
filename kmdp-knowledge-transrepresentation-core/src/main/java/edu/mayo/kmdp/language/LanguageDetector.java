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

import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import edu.mayo.kmdp.tranx.v4.server.DiscoveryApiInternal;
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
import org.omg.spec.api4kp._1_0.services.tranx.DetectionOperator;
import org.omg.spec.api4kp._1_0.services.tranx.Detector;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class LanguageDetector implements KnowledgePlatformComponent<Detector>,
    DetectApiInternal, DiscoveryApiInternal {

  private UUID id = UUID.randomUUID();
  private Detector descriptor;

  Map<KeyIdentifier,DetectApiOperator> detectors;

  @Named
  public LanguageDetector(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperationSeries.Detect_Language_Information_Task)
      List<DetectApiOperator> detectors) {

    this.detectors = detectors.stream()
        .collect(Collectors.toMap(
            det -> det.getOperatorId().asKey(),
            det -> det
        ));

    this.descriptor = toKPComponent(getComponentId());
  }

  @Override
  public Answer<Detector> getDetectComponent(UUID componentId) {
    return Answer.of(getDescriptor());
  }

  @Override
  public Answer<List<Detector>> listDetectComponents() {
    return Answer.of(singletonList(getDescriptor()));
  }

  @Override
  public Answer<KnowledgeCarrier> applyDetect(KnowledgeCarrier sourceArtifact) {
    return Answer.of(anyAble(detectors.values(),
        DetectApiOperator::can_applyDetect))
        .flatOpt(DetectApiOperator::as_applyDetect)
        .flatMap(a -> a.applyDetect(sourceArtifact));
  }

  @Override
  public Answer<KnowledgeCarrier> applyNamedDetect(UUID operatorId,
      KnowledgeCarrier sourceArtifact) {
    return Answer.of(getDetector(operatorId))
        .flatOpt(DetectApiOperator::as_applyNamedDetect)
        .flatMap(a -> a.applyNamedDetect(operatorId, sourceArtifact));
  }

  @Override
  public Answer<DetectionOperator> getDetectionOperator(UUID operatorId) {
    return Answer.of(getDetector(operatorId)
        .map(DetectApiOperator::getDescriptor));
  }

  @Override
  public Answer<List<DetectionOperator>> listDetectionOperators() {
    return Answer.of(detectors.values().stream()
        .map(DetectApiOperator::getDescriptor)
        .collect(Collectors.toList()));
  }


  @Override
  public UUID getComponentUuid() {
    return id;
  }

  @Override
  public Detector toKPComponent(ResourceIdentifier componentId) {
    return new Detector()
        .withInstanceId(getComponentId());
  }

  @Override
  public Detector getDescriptor() {
    return descriptor;
  }

  private Optional<DetectApiOperator> getDetector(UUID operatorId) {
    return detectors.keySet().stream()
        .filter(key -> key.getUuid().equals(operatorId))
        .findFirst()
        .map(detectors::get);
  }
}
