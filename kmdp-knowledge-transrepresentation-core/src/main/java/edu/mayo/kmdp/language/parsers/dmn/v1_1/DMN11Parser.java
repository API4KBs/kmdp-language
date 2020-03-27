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
package edu.mayo.kmdp.language.parsers.dmn.v1_1;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.parsers.XMLBasedLanguageParser;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20151101.dmn.ObjectFactory;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;

@Named
@KPOperation(Lifting_Task)
@KPOperation(Lowering_Task)
@KPSupport(DMN_1_1)
public class DMN11Parser extends XMLBasedLanguageParser<TDefinitions> implements
    DeserializeApiInternal {

  public DMN11Parser() {
    this.root = TDefinitions.class;
    this.mapper = new ObjectFactory()::createDefinitions;
  }

  public List<SyntacticRepresentation> getSupportedRepresentations() {
    return
        Collections
            .singletonList(rep(DMN_1_1,
                SerializationFormatSeries.XML_1_1, getDefaultCharset()));
  }

}