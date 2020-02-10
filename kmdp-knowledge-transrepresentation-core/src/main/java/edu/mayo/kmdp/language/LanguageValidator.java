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
package edu.mayo.kmdp.language;

import static org.omg.spec.api4kp._1_0.Answer.aggregate;
import static org.omg.spec.api4kp._1_0.Answer.anyAble;

import edu.mayo.kmdp.tranx.v3.server.DetectApiInternal;
import edu.mayo.kmdp.tranx.v3.server.ValidateApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@KPServer
public class LanguageValidator implements ValidateApiInternal {

  List<ValidateApiInternal> validators;

  public LanguageValidator(@Autowired(required = false)
  @KPOperation(KnowledgeProcessingOperationSeries.Well_Formedness_Check_Task)
  List<ValidateApiInternal> validateApiInternalList) {
    this.validators = new ArrayList<>(validateApiInternalList);
  }

  @Override
  //TODO FIXME Should be renamed 'validateAs'
  public Answer<Void> validate(KnowledgeCarrier sourceArtifact, SyntacticRepresentation into) {
    return
        anyAble(validators, validator -> validator != null)   // TODO FIXME Needs a 'canDo' thing
            .map(v -> v.validate(sourceArtifact, into))
            .orElse(Answer.failed());
  }

}
