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

import edu.mayo.kmdp.language.server.DeserializeApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Named
public class LanguageDeSerializer implements DeserializeApiDelegate {

  private Set<edu.mayo.kmdp.language.DeserializeApi> parsers = new HashSet<>();
  private List<SyntacticRepresentation> supportedLanguages;

  @Named
  public LanguageDeSerializer(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperation.Lifting_Task)
  @KPOperation(KnowledgeProcessingOperation.Lowering_Task)

      List<edu.mayo.kmdp.language.DeserializeApi> parsers) {
    this.parsers.addAll(parsers);
    this.supportedLanguages = getSupportedLanguages();
  }

  private List<SyntacticRepresentation> getSupportedLanguages() {
    return parsers.stream()
        .map(edu.mayo.kmdp.language.DeserializeApi::getParsableLanguages)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getParsableLanguages() {
    return supportedLanguages.isEmpty() ? err() : ok(supportedLanguages);
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getSerializableLanguages() {
    return supportedLanguages.isEmpty() ? err() : ok(supportedLanguages);
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> lift(KnowledgeCarrier sourceArtifact,
      ParsingLevel level) {
    if (sourceArtifact.getRepresentation() == null) {
      return err();
    }
    return parsers.stream()
        .filter((p) -> supportsLifting(p,sourceArtifact.getRepresentation()))
        .findAny()
        .map((parser)->parser.lift(sourceArtifact,level))
        .map(this::ok)
        .orElse(err());
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel level) {
    if (sourceArtifact.getRepresentation() == null) {
      return err();
    }
    return parsers.stream()
        .filter((p) -> supportsLowering(p,sourceArtifact.getRepresentation()))
        .findAny()
        .map((parser)->parser.lower(sourceArtifact,level))
        .map(this::ok)
        .orElse(err());
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> ensureRepresentation(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    if (sourceArtifact.getRepresentation() == null) {
      return err();
    }
    return parsers.stream()
        .filter((p) -> supportsLowering(p,sourceArtifact.getRepresentation()))
        .findAny()
        .map((parser)->parser.ensureRepresentation(sourceArtifact,into))
        .map(this::ok)
        .orElse(err());
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> deserialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return parsers.stream()
        .filter((p) -> supportsLifting(p,sourceArtifact.getRepresentation()))
        .findAny()
        .map((parser)->parser.deserialize(sourceArtifact,into))
        .map(this::ok)
        .orElse(err());
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> serialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return parsers.stream()
        .filter((p) -> supportsLowering(p,sourceArtifact.getRepresentation()))
        .findAny()
        .map((parser)->parser.serialize(sourceArtifact,into))
        .map(this::ok)
        .orElse(err());
  }

  private boolean supportsLifting(edu.mayo.kmdp.language.DeserializeApi parser, SyntacticRepresentation representation) {
    return parser.getParsableLanguages().stream().anyMatch((r)-> r.getLanguage() == representation.getLanguage());
  }

  private boolean supportsLowering(DeserializeApi parser, SyntacticRepresentation representation) {
    return parser.getSerializableLanguages().stream().anyMatch((r)-> r.getLanguage() == representation.getLanguage());
  }

  private <T> ResponseEntity<T> ok(T carrier) {
    return new ResponseEntity<>(carrier, HttpStatus.OK);
  }

  private <T> ResponseEntity<T> err() {
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

}
