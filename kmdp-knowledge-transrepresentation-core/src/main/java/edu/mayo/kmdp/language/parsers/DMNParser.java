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

import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20151101.dmn.ObjectFactory;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
@KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
public class DMNParser extends XMLBasedLanguageParser<TDefinitions> implements
    DeserializeApiDelegate {

  public DMNParser() {
    this.root = TDefinitions.class;
    this.mapper = new ObjectFactory()::createDefinitions;
  }

  public List<SyntacticRepresentation> getSupportedRepresentations() {
    return
        Collections
            .singletonList(rep(KnowledgeRepresentationLanguageSeries.DMN_1_1, SerializationFormatSeries.XML_1_1, getDefaultCharset()));
  }

}