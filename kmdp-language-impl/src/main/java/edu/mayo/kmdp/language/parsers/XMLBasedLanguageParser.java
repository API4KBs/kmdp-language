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

import edu.mayo.kmdp.language.DeserializeApi;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import javax.xml.bind.JAXBElement;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

public abstract class XMLBasedLanguageParser<T> extends AbstractDeSerializer implements
    DeserializeApi {

  protected Class<T> root;
  protected Function<T, JAXBElement<? super T>> mapper;


  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(new String(carrier.getEncodedExpression()))
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevel.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    return Optional.of(new DocumentCarrier().withStructuredExpression(
        XMLUtil.loadXMLDocument(carrier.getSerializedExpression().getBytes())
            .get())
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevel.Parsed_Knowedge_Expression)));
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    return Optional.of(new ASTCarrier().withParsedExpression(JaxbUtil.unmarshall(root,
        root,
        carrier.getSerializedExpression(),
        JaxbUtil.defaultProperties())
        .get())
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevel.Abstract_Knowledge_Expression)));

  }

  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    Document dox = (Document) carrier.getStructuredExpression();
    return Optional.of(new ASTCarrier().withParsedExpression(JaxbUtil.unmarshall(root,
        root,
        dox,
        JaxbUtil.defaultProperties())
        .get())
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
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(JaxbUtil.marshall(
            Collections.singleton(obj.getClass()),
            obj,
            mapper,
            JaxbUtil.defaultProperties())
            .flatMap(Util::asString)
            .get()
        )
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevel.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier, SyntacticRepresentation into) {
    Document dox = (Document) carrier.getStructuredExpression();
    return Optional
        .of(new ExpressionCarrier().withSerializedExpression(new String(XMLUtil.toByteArray(dox)))
            .withRepresentation(getSerializeResultRepresentation(carrier,
                ParsingLevel.Concrete_Knowledge_Expression)));
  }


  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    T obj = (T) carrier.getParsedExpression();
    return Optional.of(new DocumentCarrier()
        .withStructuredExpression(JaxbUtil.marshallDox(Collections.singleton(root),
            obj,
            mapper,
            JaxbUtil.defaultProperties())
            .get()
        )
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevel.Parsed_Knowedge_Expression)));
  }


  @Override
  protected SerializationFormat getDefaultFormat() {
    return SerializationFormat.XML_1_1;
  }
}
