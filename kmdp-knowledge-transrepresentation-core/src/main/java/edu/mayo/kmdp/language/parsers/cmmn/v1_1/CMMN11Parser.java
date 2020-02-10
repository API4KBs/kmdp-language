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
package edu.mayo.kmdp.language.parsers.cmmn.v1_1;


import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.parsers.XMLBasedLanguageParser;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
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
public class CMMN11Parser extends XMLBasedLanguageParser<TDefinitions> implements
    DeserializeApiInternal {

  public CMMN11Parser() {
    this.root = TDefinitions.class;
    this.mapper = new ObjectFactory()::createDefinitions;
  }

  public List<SyntacticRepresentation> getSupportedRepresentations() {
    return
        Collections.singletonList(
            rep(KnowledgeRepresentationLanguageSeries.CMMN_1_1, SerializationFormatSeries.XML_1_1, getDefaultCharset()));
  }

}