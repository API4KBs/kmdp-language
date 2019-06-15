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

import static edu.mayo.kmdp.util.ws.ResponseHelper.getAll;
import static edu.mayo.kmdp.util.ws.ResponseHelper.map;

import edu.mayo.kmdp.tranx.server.DetectApiDelegate;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;


public abstract class XMLBasedLanguageDetector<T> implements DetectApiDelegate {

  protected Class<T> root;

  @Override
  public ResponseEntity<SyntacticRepresentation> getDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    boolean isLang = false;

    try {
      if (sourceArtifact instanceof BinaryCarrier) {
        byte[] data = ((BinaryCarrier) sourceArtifact).getEncodedExpression();
        isLang = Arrays.equals("<".getBytes(), Arrays.copyOfRange(data, 0, 1))
            && JaxbUtil.unmarshall(root,
            root,
            new ByteArrayInputStream(data),
            JaxbUtil.defaultProperties()).isPresent();
      } else if (sourceArtifact instanceof ExpressionCarrier) {
        String str = ((ExpressionCarrier) sourceArtifact).getSerializedExpression();

        isLang = str.startsWith("<") && JaxbUtil.unmarshall(root,
            root,
            str,
            JaxbUtil.defaultProperties()).isPresent();
      } else if (sourceArtifact instanceof DocumentCarrier) {
        Object dox = ((DocumentCarrier) sourceArtifact).getStructuredExpression();
        isLang = dox instanceof Document && JaxbUtil.unmarshall(root,
            root,
            (Document) dox,
            JaxbUtil.defaultProperties()).isPresent();
      } else if (sourceArtifact instanceof ASTCarrier) {
        isLang = root.isInstance(((ASTCarrier) sourceArtifact).getParsedExpression());
      }
    } catch (Exception e) {
      return ResponseHelper.fail();
    }

    if (isLang && !getAll(getDetectableLanguages()).isEmpty()) {
      return map(getDetectableLanguages(), (l) -> l.get(0));
    }
    return ResponseHelper.fail();
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> setDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    return map(getDetectedRepresentation(sourceArtifact),
        sourceArtifact::withRepresentation);
  }

}
