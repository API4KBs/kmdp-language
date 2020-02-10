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
package edu.mayo.kmdp.language.detectors.dmn.v1_1;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static java.util.Collections.singletonList;

import edu.mayo.kmdp.language.detectors.XMLBasedLanguageDetector;
import edu.mayo.kmdp.tranx.v3.server.DetectApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Detect_Language_Information_Task)
public class DMN11Detector extends XMLBasedLanguageDetector<TDefinitions> implements
    DetectApiInternal {

  public DMN11Detector() {
    this.root = TDefinitions.class;
  }

  @Override
  public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
    return Answer.of(
        singletonList(new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
            .withLanguage(DMN_1_1)
            .withFormat(XML_1_1)));
  }
}
