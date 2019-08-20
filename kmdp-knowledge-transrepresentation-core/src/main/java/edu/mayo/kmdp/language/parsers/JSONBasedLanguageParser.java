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
import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public abstract class JSONBasedLanguageParser<T> extends AbstractDeSerializer implements
    DeserializeApiDelegate {

  protected Class<T> root;

  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(new String(carrier.getEncodedExpression()))
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevel.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    return JSonUtil.readJson(carrier.getSerializedExpression().getBytes())
        .map(json -> new DocumentCarrier()
            .withStructuredExpression(json)
            .withRepresentation(
                getParseResultRepresentation(carrier, ParsingLevel.Parsed_Knowedge_Expression)));
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    return JSonUtil.parseJson(carrier.getSerializedExpression(), root)
        .map(ast -> new ASTCarrier()
            .withParsedExpression(ast)
            .withRepresentation(
                getParseResultRepresentation(carrier, ParsingLevel.Abstract_Knowledge_Expression)));
  }


  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    JsonNode jNode = (JsonNode) carrier.getStructuredExpression();
    return JSonUtil.parseJson(jNode, root)
        .map(ast -> new ASTCarrier()
            .withParsedExpression(ast)
            .withRepresentation(
                getParseResultRepresentation(carrier, ParsingLevel.Abstract_Knowledge_Expression)));
  }


  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into) {
    return Optional.of(new BinaryCarrier()
        .withEncodedExpression(carrier.getSerializedExpression().getBytes())
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevel.Encoded_Knowledge_Expression)));
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier, SyntacticRepresentation into) {
    T obj = (T) carrier.getParsedExpression();
    return JSonUtil.writeJson(obj)
        .flatMap(Util::asString)
        .map(str -> new ExpressionCarrier()
            .withSerializedExpression(str)
            .withRepresentation(
                getSerializeResultRepresentation(
                    carrier, ParsingLevel.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier,
      SyntacticRepresentation into) {
    JsonNode jNode = (JsonNode) carrier.getStructuredExpression();
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(jNode.toString())
        .withRepresentation(getSerializeResultRepresentation(
            carrier,
            ParsingLevel.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    T obj = (T) carrier.getParsedExpression();
    return JSonUtil.toJsonNode(obj)
        .map(json -> new DocumentCarrier()
            .withStructuredExpression(json)
            .withRepresentation(
                getSerializeResultRepresentation(
                    carrier,
                    ParsingLevel.Parsed_Knowedge_Expression)));
  }

  @Override
  public SerializationFormat getDefaultFormat() {
    return SerializationFormat.JSON;
  }

}
