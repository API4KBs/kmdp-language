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
package edu.mayo.kmdp.language.parsers;


import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation.Lowering_Task;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.cmmn._20151109.model.ObjectFactory;
import org.omg.spec.cmmn._20151109.model.TDefinitions;

@Named
@KPOperation(Lifting_Task)
@KPOperation(Lowering_Task)
public class CMMNParser extends XMLBasedLanguageParser<TDefinitions> implements
    DeserializeApiDelegate {

  public CMMNParser() {
    this.root = TDefinitions.class;
    this.mapper = new ObjectFactory()::createDefinitions;
  }

  public List<SyntacticRepresentation> getSupportedRepresentations() {
    return
        Collections.singletonList(
            rep(KnowledgeRepresentationLanguage.CMMN_1_1, SerializationFormat.XML_1_1, getDefaultCharset()));
  }

}