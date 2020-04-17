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

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Parsed_Knowedge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public abstract class JSONBasedLanguageParser<T> extends AbstractDeSerializer {

  protected Class<T> root;

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier) {
    return carrier.asString()
        .map(str -> newVerticalCarrier(carrier, Concrete_Knowledge_Expression, str));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier) {
    return carrier.asString()
        .flatMap(JSonUtil::readJson)
        .map(json -> newVerticalCarrier(carrier, Parsed_Knowedge_Expression, json));
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier) {
    return carrier.asString()
        .flatMap(str -> JSonUtil.parseJson(str, root))
        .map(ast -> newVerticalCarrier(carrier, Abstract_Knowledge_Expression, ast));
  }


  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier) {
    return carrier.as(JsonNode.class)
        .flatMap(jNode -> JSonUtil.parseJson(jNode, root))
        .map(ast -> newVerticalCarrier(carrier, Abstract_Knowledge_Expression, ast));
  }


  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.asBinary()
        .map(bytes -> newVerticalCarrier(carrier, Encoded_Knowledge_Expression, bytes));
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(root)
        .flatMap(JSonUtil::writeJson)
        .flatMap(Util::asString)
        .map(str -> newVerticalCarrier(carrier, Concrete_Knowledge_Expression, str));
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(JsonNode.class)
        .map(JsonNode::toString)
        .map(str -> newVerticalCarrier(carrier, Concrete_Knowledge_Expression, str));
  }

  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(root)
        .flatMap(JSonUtil::toJsonNode)
        .map(json -> newVerticalCarrier(carrier, Parsed_Knowedge_Expression, json));
  }


  @Override
  public List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
        rep(getSupportedLanguage()),
        rep(getSupportedLanguage(), JSON));
  }

  @Override
  public SerializationFormat getDefaultFormat() {
    return SerializationFormatSeries.JSON;
  }

}
