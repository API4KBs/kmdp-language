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

import static edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage.CMMN_1_1;
import static java.util.Collections.singletonList;

import edu.mayo.kmdp.tranx.server.DetectApiDelegate;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.cmmn._20151109.model.TDefinitions;
import org.springframework.http.ResponseEntity;

@Named
@KPOperation(KnowledgeProcessingOperation.Detect_Language_Information_Task)
public class CMMNDetector extends XMLBasedLanguageDetector<TDefinitions> implements
    DetectApiDelegate {

  public CMMNDetector() {
    this.root = TDefinitions.class;
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getDetectableLanguages() {
    return ResponseHelper.succeed(
        singletonList(new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
            .withLanguage(CMMN_1_1)
            .withFormat(XML_1_1)));
  }

}
