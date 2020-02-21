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
package edu.mayo.kmdp.language.translators.cmmn.v1_1;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Translation_Task;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.PCV;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import java.util.Properties;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation;
import org.omg.spec.cmmn._20151109.model.TDefinitions;

@Named
@KPOperation(Translation_Task)
@KPSupport({FHIR_STU3,CMMN_1_1})
public class CmmnToPlanDefTranslator extends AbstractSimpleTranslator {

  private static final String OPERATOR_ID = "87402252-a8a1-46a4-be3a-9b04ce45fde7";

  public String getId() {
    return OPERATOR_ID;
  }

  public org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getFrom() {
    return new SyntacticRepresentation()
        .withLanguage(CMMN_1_1);
  }

  public org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getTo() {
    return new SyntacticRepresentation()
        .withLanguage(FHIR_STU3)
        .withLexicon(PCV, SNOMED_CT);
  }

  @Override
  protected KnowledgeCarrier doTransform(KnowledgeCarrier sourceArtifact, Properties props) {
    return AbstractCarrier.ofAst(
        new CmmnToPlanDef().transform(
            sourceArtifact.getAssetId(),
            sourceArtifact.as(TDefinitions.class)
                .orElseThrow(IllegalStateException::new)));
  }

}
