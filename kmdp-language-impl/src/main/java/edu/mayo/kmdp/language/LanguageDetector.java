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

import edu.mayo.kmdp.language.server.DetectApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeProcessingOperation;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Named
public class LanguageDetector implements DetectApiDelegate {

  List<edu.mayo.kmdp.language.DetectApi> detectors;

  @Named
  public LanguageDetector(@Autowired(required = false)
  @KPOperation( KnowledgeProcessingOperation.Detect_Language_Information_Task )
      List<edu.mayo.kmdp.language.DetectApi> detectors) {
    this.detectors = detectors;
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getDetectableLanguages() {
    List<SyntacticRepresentation> allSupported = new LinkedList<>();
    detectors.stream()
        .map(DetectApi::getDetectableLanguages)
        .forEach(allSupported::addAll);
    return new ResponseEntity<>(allSupported, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    Optional<SyntacticRepresentation> rep = detectors.stream()
        .map((detective) -> detective.getDetectedRepresentation(sourceArtifact))
        .filter(Objects::nonNull)
        .findAny();
    return rep.map(
        syntacticRepresentation -> new ResponseEntity<>(syntacticRepresentation, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    Optional<SyntacticRepresentation> rep = detectors.stream()
        .map((detective) -> detective.getDetectedRepresentation(sourceArtifact))
        .filter(Objects::nonNull)
        .findAny();
    return rep.map(
        syntacticRepresentation -> new ResponseEntity<>(
            sourceArtifact.withRepresentation(syntacticRepresentation), HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
