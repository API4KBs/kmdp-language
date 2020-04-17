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

import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal._applyLift;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal._applyNamedLift;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public interface Lifter extends _applyLift, _applyNamedLift {


  @Override
  default Answer<KnowledgeCarrier> applyNamedLift(UUID uuid, KnowledgeCarrier knowledgeCarrier,
      ParsingLevel parsingLevel, String xAccept) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyLift(knowledgeCarrier, parsingLevel, xAccept)
        : Answer.unsupported();
  }

  ResourceIdentifier getOperatorId();

  /**
   * Lifts a binary-encoded expression (byte[])
   * into a concrete expression (String)
   *
   * @see Lowerer#innerEncode(KnowledgeCarrier)
   * @param carrier A binary carrier
   * @return A string carrier
   */
  Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier);

  /**
   * Lifts a concrete expression (String)
   * into a parsed expression (parse tree)
   * @param carrier A string carrier
   * @return A parse tree carrier
   */
  Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier);

  /**
   * Lifts a concrete expression (String)
   * into an abstract expression (abstract syntax tree)
   * @param carrier A string carrier
   * @return An abstract syntax tree carrier
   */
  Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier);


  /**
   * Lifts a parsed expression (parse tree)
   * into an abstract expression (abstract syntax tree)
   * @param carrier A parse tree carrier
   * @return An abstract syntax tree carrier
   */
  Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier);
}