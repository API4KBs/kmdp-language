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
package edu.mayo.kmdp.language.translators.dmn.v1_2;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Translation_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.PCV;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.tranx.v3.server.TransxionApiInternal;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;
import org.omg.spec.dmn._20180521.model.TDefinitions;

@Named
@KPOperation(Translation_Task)
public class DmnToPlanDefTranslator extends AbstractSimpleTranslator {

  private static final String OPERATOR_ID = "0e990fd3-66ea-45f6-a435-0be83e9654d3";

  public String getId() {
    return OPERATOR_ID;
  }

  public org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getFrom() {
    return new SyntacticRepresentation()
        .withLanguage(DMN_1_2);
  }

  public org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getTo() {
    return new SyntacticRepresentation()
        .withLanguage(FHIR_STU3)
        .withLexicon(PCV,SNOMED_CT);
  }

  @Override
  protected KnowledgeCarrier doTransform(KnowledgeCarrier sourceArtifact, Properties props) {
    return AbstractCarrier.ofAst(
        new DmnToPlanDef().transform(
            sourceArtifact.getAssetId(),
            sourceArtifact.as(TDefinitions.class)
                .orElseThrow(IllegalStateException::new)));
  }

}
