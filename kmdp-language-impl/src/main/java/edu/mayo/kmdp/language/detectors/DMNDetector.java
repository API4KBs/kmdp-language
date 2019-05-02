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

import edu.mayo.kmdp.language.DetectApi;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import edu.mayo.ontology.taxonomies.krformat._2018._08.KRFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KRLanguage;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;

@Named
@KPOperation(KnowledgeOperations.Detect_Language_Information_Task)
public class DMNDetector extends XMLBasedLanguageDetector<TDefinitions> implements DetectApi {

  public DMNDetector() {
    this.root = TDefinitions.class;
  }

  @Override
  public List<SyntacticRepresentation> getDetectableLanguages() {
    return Collections
        .singletonList(new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
            .withLanguage(KRLanguage.DMN_1_1)
            .withFormat(KRFormat.XML_1_1));
  }
}
