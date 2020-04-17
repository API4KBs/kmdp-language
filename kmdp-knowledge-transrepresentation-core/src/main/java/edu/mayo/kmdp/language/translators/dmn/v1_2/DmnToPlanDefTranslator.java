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
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.PCV;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20180521.model.TDefinitions;

@Named
@KPOperation(Translation_Task)
@KPSupport({FHIR_STU3,DMN_1_2})
public class DmnToPlanDefTranslator extends AbstractSimpleTranslator<TDefinitions,PlanDefinition> {

  public static final UUID id = UUID.fromString("0e990fd3-66ea-45f6-a435-0be83e9654d3");
  public static final String version = "1.0.0";

  public DmnToPlanDefTranslator() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(DMN_1_2));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(
        rep(FHIR_STU3)
            .withLexicon(PCV, SNOMED_CT));
  }

  @Override
  protected Optional<PlanDefinition> transformAst(ResourceIdentifier assetId,
      TDefinitions expression, SyntacticRepresentation tgtRep) {
    return Optional.ofNullable(new DmnToPlanDef().transform(assetId, expression));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return DMN_1_2;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return FHIR_STU3;
  }

}
