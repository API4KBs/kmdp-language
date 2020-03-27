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

import static org.omg.spec.api4kp._1_0.Answer.anyAble;
import static org.omg.spec.api4kp._1_0.Answer.delegateTo;

import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class LanguageDeSerializer implements DeserializeApiInternal {

  private Set<DeserializeApiInternal> parsers = new HashSet<>();
  private List<SyntacticRepresentation> supportedLanguages;

  @Named
  public LanguageDeSerializer(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
  @KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
      List<DeserializeApiInternal> parsers) {
    this.parsers.addAll(parsers);
    this.supportedLanguages = getSupportedLanguages();
  }

  private List<SyntacticRepresentation> getSupportedLanguages() {
    return Stream.concat(
        Answer.aggregate(parsers, DeserializeApiInternal::getParsableLanguages),
        Answer.aggregate(parsers, DeserializeApiInternal::getSerializableLanguages))
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public Answer<List<SyntacticRepresentation>> getParsableLanguages() {
    return Answer.of(supportedLanguages);
  }

  @Override
  public Answer<List<SyntacticRepresentation>> getSerializableLanguages() {
    return Answer.of(supportedLanguages);
  }

  @Override
  public Answer<KnowledgeCarrier> lift(KnowledgeCarrier sourceArtifact,
      ParsingLevel level) {
    return delegateTo(
        anyAble(parsers,
            p -> supportsLifting(p, sourceArtifact.getRepresentation())),
        parser -> parser.lift(sourceArtifact, level));
  }

  @Override
  public Answer<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel level) {
    return delegateTo(
        anyAble(parsers,
            p -> supportsLowering(p, sourceArtifact.getRepresentation())),
        parser -> parser.lower(sourceArtifact, level));
  }

  @Override
  public Answer<KnowledgeCarrier> ensureRepresentation(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return delegateTo(
        anyAble(parsers,
            p -> supportsLowering(p, sourceArtifact.getRepresentation())
                && supportsLifting(p, sourceArtifact.getRepresentation())),
        parser -> parser.ensureRepresentation(sourceArtifact, into));
  }

  @Override
  public Answer<KnowledgeCarrier> deserialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return delegateTo(
        anyAble(parsers,
            p -> supportsLifting(p, sourceArtifact.getRepresentation())),
        parser -> parser.deserialize(sourceArtifact, into));
  }

  @Override
  public Answer<KnowledgeCarrier> serialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return delegateTo(
        anyAble(parsers,
            p -> supportsLowering(p, sourceArtifact.getRepresentation())),
        parser -> parser.serialize(sourceArtifact, into));
  }

  private boolean supportsLifting(DeserializeApiInternal parser,
      SyntacticRepresentation representation) {
    return parser.getParsableLanguages().orElse(Collections.emptyList()).stream()
        .anyMatch(r -> r.getLanguage() == representation.getLanguage());
  }

  private boolean supportsLowering(DeserializeApiInternal parser,
      SyntacticRepresentation representation) {
    return parser.getParsableLanguages().orElse(Collections.emptyList()).stream()
        .anyMatch(r -> r.getLanguage() == representation.getLanguage());
  }


}
