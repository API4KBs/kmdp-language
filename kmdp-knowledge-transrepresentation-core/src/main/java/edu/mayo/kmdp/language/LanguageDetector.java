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

import static org.omg.spec.api4kp._1_0.Answer.aggregate;

import edu.mayo.kmdp.tranx.server.DetectApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Named
@KPServer
public class LanguageDetector implements DetectApiInternal {

  List<DetectApiInternal> detectors;

  @Named
  public LanguageDetector(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperationSeries.Detect_Language_Information_Task)
      List<DetectApiInternal> detectors) {
    this.detectors = detectors;
  }

  @Override
  public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
    return Answer.of(
        aggregate(detectors, DetectApiInternal::getDetectableLanguages)
            .collect(Collectors.toList()));
  }

  @Override
  public Answer<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return Answer.anyDo(detectors,
        detective -> detective.getDetectedRepresentation(sourceArtifact));
  }

  @Override
  public Answer<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return Answer.anyDo(detectors,
        detective -> detective.getDetectedRepresentation(sourceArtifact))
        .map(sourceArtifact::withRepresentation);
  }
}
