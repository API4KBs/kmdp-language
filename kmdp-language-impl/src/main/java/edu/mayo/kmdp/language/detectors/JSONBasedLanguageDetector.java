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
import edu.mayo.kmdp.language.DetectApi;
import edu.mayo.kmdp.util.JSonUtil;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;


public abstract class JSONBasedLanguageDetector<T> implements DetectApi {

  protected Class<T> root;

  @Override
  public SyntacticRepresentation getDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    boolean isLang = false;

    try {
      if (sourceArtifact instanceof BinaryCarrier) {
        byte[] data = ((BinaryCarrier) sourceArtifact).getEncodedExpression();
        isLang = JSonUtil.tryParseJson(new String(data), root).isPresent();
      } else if (sourceArtifact instanceof ExpressionCarrier) {
        String str = ((ExpressionCarrier) sourceArtifact).getSerializedExpression();
        isLang = JSonUtil.tryParseJson(str, root).isPresent();
      } else if (sourceArtifact instanceof DocumentCarrier) {
        JsonNode node = (JsonNode) ((DocumentCarrier) sourceArtifact).getStructuredExpression();
        isLang = JSonUtil.tryParseJson(node, root).isPresent();
      } else if (sourceArtifact instanceof ASTCarrier) {
        isLang = root.isInstance(((ASTCarrier) sourceArtifact).getParsedExpression());
      }
    } catch (Exception e) {
      return null;
    }

    if (isLang && !getDetectableLanguages().isEmpty()) {
      return getDetectableLanguages().get(0);
    }
    return null;
  }

  @Override
  public KnowledgeCarrier setDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    return sourceArtifact.withRepresentation(getDetectedRepresentation(sourceArtifact));
  }
}
