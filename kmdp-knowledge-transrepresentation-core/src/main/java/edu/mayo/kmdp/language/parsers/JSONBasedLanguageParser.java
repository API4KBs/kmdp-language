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

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public abstract class JSONBasedLanguageParser<T> extends AbstractDeSerializer implements
    DeserializeApiInternal {

  protected Class<T> root;

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier) {
    return carrier.asString()
        .map(str -> new KnowledgeCarrier()
            .withExpression(str)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier) {
    return carrier.asString()
        .flatMap(JSonUtil::readJson)
        .map(json -> new KnowledgeCarrier()
            .withExpression(json)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Parsed_Knowedge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier) {
    return carrier.asString()
        .flatMap(str -> JSonUtil.parseJson(str, root))
        .map(ast -> new KnowledgeCarrier()
            .withExpression(ast)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Abstract_Knowledge_Expression)));
  }


  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier) {
    return carrier.as(JsonNode.class)
        .flatMap(jNode -> JSonUtil.parseJson(jNode, root))
        .map(ast -> new KnowledgeCarrier()
            .withExpression(ast)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Abstract_Knowledge_Expression)));
  }


  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.asString()
        .map(str -> new KnowledgeCarrier()
            .withExpression(str.getBytes())
            .withRepresentation(
                getSerializeResultRepresentation(carrier,
                    ParsingLevelSeries.Encoded_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(root)
        .flatMap(JSonUtil::writeJson)
        .flatMap(Util::asString)
        .map(str -> new KnowledgeCarrier()
            .withExpression(str)
            .withRepresentation(
                getSerializeResultRepresentation(
                    carrier, ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(JsonNode.class)
        .map(jNode -> new KnowledgeCarrier()
            .withExpression(jNode.toString())
            .withRepresentation(getSerializeResultRepresentation(
                carrier,
                ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(root)
        .flatMap(JSonUtil::toJsonNode)
        .map(json -> new KnowledgeCarrier()
            .withExpression(json)
            .withRepresentation(
                getSerializeResultRepresentation(
                    carrier,
                    ParsingLevelSeries.Parsed_Knowedge_Expression)));
  }

  @Override
  public SerializationFormat getDefaultFormat() {
    return SerializationFormatSeries.JSON;
  }

}
