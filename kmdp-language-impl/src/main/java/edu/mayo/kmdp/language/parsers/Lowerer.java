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
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public interface Lowerer {


  default Optional<BinaryCarrier> encode(ExpressionCarrier carrier) {
    return encode(carrier,null);
  }

  default Optional<ExpressionCarrier> externalize(ASTCarrier carrier) {
    return externalize(carrier,null);
  }

  default Optional<ExpressionCarrier> serialize(DocumentCarrier carrier) {
    return serialize(carrier,null);
  }

  default Optional<DocumentCarrier> concretize(ASTCarrier carrier) {
    return concretize(carrier,null);
  }

  Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into);

  Optional<ExpressionCarrier> externalize(ASTCarrier carrier, SyntacticRepresentation into);

  Optional<ExpressionCarrier> serialize(DocumentCarrier carrier, SyntacticRepresentation into);

  Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into);
}
