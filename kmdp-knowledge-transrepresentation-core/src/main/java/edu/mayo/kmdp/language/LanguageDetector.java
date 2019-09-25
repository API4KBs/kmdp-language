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

import static edu.mayo.kmdp.util.ws.ResponseHelper.aggregate;
import static edu.mayo.kmdp.util.ws.ResponseHelper.anyDo;
import static edu.mayo.kmdp.util.ws.ResponseHelper.attempt;
import static edu.mayo.kmdp.util.ws.ResponseHelper.succeed;

import edu.mayo.kmdp.tranx.server.DetectApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Named
@KPServer
public class LanguageDetector implements DetectApiDelegate {

  List<DetectApiDelegate> detectors;

  @Named
  public LanguageDetector(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperation.Detect_Language_Information_Task)
      List<DetectApiDelegate> detectors) {
    this.detectors = detectors;
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getDetectableLanguages() {
    return succeed(
        aggregate(
            detectors,
            DetectApiDelegate::getDetectableLanguages));
  }

  @Override
  public ResponseEntity<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return attempt(
        anyDo(detectors,
            detective -> detective.getDetectedRepresentation(sourceArtifact)));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return attempt(
        anyDo(detectors,
            detective -> detective.getDetectedRepresentation(sourceArtifact))
            .map(sourceArtifact::withRepresentation));
  }
}
