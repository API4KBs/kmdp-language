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
package edu.mayo.kmdp.language.detectors;

import static java.util.Collections.emptyList;

import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;


public abstract class XMLBasedLanguageDetector<T> implements DetectApiInternal {

  protected Class<T> root;

  @Override
  public Answer<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    boolean isLang = false;

    try {
      switch (sourceArtifact.getLevel().asEnum()) {
        case Encoded_Knowledge_Expression:
          isLang = sourceArtifact.asBinary()
              .filter(data -> Arrays.equals("<".getBytes(), Arrays.copyOfRange(data, 0, 1)))
              .flatMap(data -> JaxbUtil.unmarshall(root, root, new ByteArrayInputStream(data)))
              .isPresent();
          break;
        case Concrete_Knowledge_Expression:
          isLang = sourceArtifact.asString()
              .filter(str -> !Util.isEmpty(str) && str.charAt(0) == '<')
              .flatMap(str -> JaxbUtil.unmarshall(root, root, str))
              .isPresent();
          break;
        case Parsed_Knowedge_Expression:
          Object dox = sourceArtifact.getExpression();
          isLang = dox instanceof Document && JaxbUtil.unmarshall(root,
              root,
              (Document) dox).isPresent();
          break;
        case Abstract_Knowledge_Expression:
          isLang = root.isInstance(sourceArtifact.getExpression());
          break;
        default:
      }
    } catch (Exception e) {
      return Answer.failed();
    }

    if (isLang && !getDetectableLanguages().orElse(emptyList()).isEmpty()) {
      return getDetectableLanguages().map(l -> l.get(0));
    }
    return Answer.failed();
  }

  @Override
  public Answer<KnowledgeCarrier> setDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    return getDetectedRepresentation(sourceArtifact)
        .map(
            sourceArtifact::withRepresentation);
  }

}
