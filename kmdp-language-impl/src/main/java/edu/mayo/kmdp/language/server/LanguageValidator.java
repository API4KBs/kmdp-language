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
package edu.mayo.kmdp.language.server;

import edu.mayo.kmdp.language.server.ValidateApiDelegate;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.http.ResponseEntity;

@Named
public class LanguageValidator implements ValidateApiDelegate {


  @Override
  public ResponseEntity<Void> validate(KnowledgeCarrier sourceArtifact, SyntacticRepresentation into) {
    return null;
  }

}
