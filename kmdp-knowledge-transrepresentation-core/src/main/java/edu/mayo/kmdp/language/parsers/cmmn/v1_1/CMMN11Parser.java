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


import static org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Lifting_Task;
import static org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Lowering_Task;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;

import edu.mayo.kmdp.language.DeserializeApiOperator;
import edu.mayo.kmdp.language.parsers.XMLBasedLanguageParser;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.cmmn._20151109.model.ObjectFactory;
import org.omg.spec.cmmn._20151109.model.TDefinitions;

@Named
@KPOperation(Lifting_Task)
@KPOperation(Lowering_Task)
@KPSupport(CMMN_1_1)
public class CMMN11Parser extends XMLBasedLanguageParser<TDefinitions>
    implements DeserializeApiOperator {

  public static final UUID id = UUID.fromString("8994de01-11dc-483c-b29e-5fc093567a4b");
  public static final String version = "1.0.0";

  public CMMN11Parser() {
    setId(SemanticIdentifier.newId(id,version));
    this.root = TDefinitions.class;
    this.mapper = new ObjectFactory()::createDefinitions;
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return CMMN_1_1;
  }

}