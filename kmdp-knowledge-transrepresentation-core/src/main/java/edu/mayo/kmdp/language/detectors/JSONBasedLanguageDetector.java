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
package edu.mayo.kmdp.language.detectors;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import edu.mayo.kmdp.util.JSonUtil;
import java.util.Collections;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;


public abstract class JSONBasedLanguageDetector<T> implements DetectApiInternal {

  protected Class<T> root;

  @Override
  public Answer<SyntacticRepresentation> getDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    boolean isLang = false;

    try {
      switch (sourceArtifact.getLevel().asEnum()) {
        case Encoded_Knowledge_Expression:
          isLang = sourceArtifact.asBinary()
              .flatMap(data -> JSonUtil.tryParseJson(new String(data), root))
              .isPresent();
          break;
        case Concrete_Knowledge_Expression:
          isLang = sourceArtifact.asString()
              .flatMap(str -> JSonUtil.tryParseJson(str, root))
              .isPresent();
          break;
        case Parsed_Knowedge_Expression:
          Object node = sourceArtifact.getExpression();
          isLang = node instanceof JsonNode
              && JSonUtil.tryParseJson((JsonNode) node, root).isPresent();
          break;
        case Abstract_Knowledge_Expression:
          isLang = root.isInstance(sourceArtifact.getExpression());
          break;
        default:
      }
    } catch (Exception e) {
      return Answer.failed();
    }

    if (isLang &&
        !getDetectableLanguages().orElse(Collections.emptyList()).isEmpty()) {
      return getDetectableLanguages()
          .map(l -> l.get(0));
    }
    return Answer.failed();
  }

  @Override
  public Answer<KnowledgeCarrier> setDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    return getDetectedRepresentation(sourceArtifact)
        .map(sourceArtifact::withRepresentation);
  }
}
