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

import java.util.Optional;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public interface Lowerer {


  default Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier) {
    return innerEncode(carrier,null);
  }

  default Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier) {
    return innerExternalize(carrier,null);
  }

  default Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier) {
    return innerSerialize(carrier,null);
  }

  default Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier) {
    return innerConcretize(carrier,null);
  }

  Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier, SyntacticRepresentation into);

  Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier, SyntacticRepresentation into);

  Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier, SyntacticRepresentation into);

  Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier, SyntacticRepresentation into);
}
