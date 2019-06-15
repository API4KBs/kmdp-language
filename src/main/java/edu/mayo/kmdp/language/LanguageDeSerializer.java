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
import static edu.mayo.kmdp.util.ws.ResponseHelper.anyAble;
import static edu.mayo.kmdp.util.ws.ResponseHelper.delegate;

import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Named
@KPServer
public class LanguageDeSerializer implements DeserializeApiDelegate {

  private Set<DeserializeApiDelegate> parsers = new HashSet<>();
  private List<SyntacticRepresentation> supportedLanguages;

  @Named
  public LanguageDeSerializer(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperation.Lifting_Task)
  @KPOperation(KnowledgeProcessingOperation.Lowering_Task)
      List<DeserializeApiDelegate> parsers) {
    this.parsers.addAll(parsers);
    this.supportedLanguages = getSupportedLanguages();
  }

  private List<SyntacticRepresentation> getSupportedLanguages() {
    return Stream.concat(
        aggregate(parsers, DeserializeApiDelegate::getParsableLanguages).stream(),
        aggregate(parsers, DeserializeApiDelegate::getSerializableLanguages).stream())
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getParsableLanguages() {
    return ResponseHelper.succeed(supportedLanguages);
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getSerializableLanguages() {
    return ResponseHelper.succeed(supportedLanguages);
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> lift(KnowledgeCarrier sourceArtifact,
      ParsingLevel level) {
    return delegate(
        anyAble(parsers,
            (p) -> supportsLifting(p, sourceArtifact.getRepresentation())),
        (parser) -> parser.lift(sourceArtifact, level));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel level) {
    return delegate(
        anyAble(parsers,
            (p) -> supportsLowering(p, sourceArtifact.getRepresentation())),
        (parser) -> parser.lower(sourceArtifact, level));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> ensureRepresentation(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return delegate(
        anyAble(parsers,
            (p) -> supportsLowering(p, sourceArtifact.getRepresentation())
                && supportsLifting(p, sourceArtifact.getRepresentation())),
        (parser) -> parser.ensureRepresentation(sourceArtifact, into));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> deserialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return delegate(
        anyAble(parsers,
            (p) -> supportsLifting(p, sourceArtifact.getRepresentation())),
        (parser) -> parser.deserialize(sourceArtifact, into));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> serialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return delegate(
        anyAble(parsers,
            (p) -> supportsLowering(p, sourceArtifact.getRepresentation())),
        (parser) -> parser.serialize(sourceArtifact, into));
  }

  private boolean supportsLifting(DeserializeApiDelegate parser,
      SyntacticRepresentation representation) {
    return parser.getParsableLanguages().getBody() != null
        && parser.getParsableLanguages().getBody().stream()
        .anyMatch((r) -> r.getLanguage() == representation.getLanguage());
  }

  private boolean supportsLowering(DeserializeApiDelegate parser,
      SyntacticRepresentation representation) {
    return parser.getSerializableLanguages().getBody() != null
        && parser.getSerializableLanguages().getBody().stream()
        .anyMatch((r) -> r.getLanguage() == representation.getLanguage());
  }


}
