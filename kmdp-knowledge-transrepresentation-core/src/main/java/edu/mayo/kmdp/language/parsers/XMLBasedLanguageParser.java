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

import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
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
    DeserializeApiInternal {

  protected Class<T> root;
  protected Function<T, JAXBElement<? super T>> mapper;


  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.of(new ExpressionCarrier()
        .withSerializedExpression(new String(carrier.getEncodedExpression()))
        .withAssetId(carrier.getAssetId())
        .withArtifactId(carrier.getArtifactId())
        .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
        .withRepresentation(
            getParseResultRepresentation(carrier, ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    return XMLUtil.loadXMLDocument(carrier.getSerializedExpression().getBytes())
        .map(dox -> new DocumentCarrier()
            .withStructuredExpression(dox)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Parsed_Knowedge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier, ParsingLevelSeries.Parsed_Knowedge_Expression)));
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    return JaxbUtil.unmarshall(root, root, carrier.getSerializedExpression())
        .map(ast -> new ASTCarrier()
            .withParsedExpression(ast)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier, ParsingLevelSeries.Abstract_Knowledge_Expression)));
  }

  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    Document dox = (Document) carrier.getStructuredExpression();
    return JaxbUtil.unmarshall(root, root, dox)
        .map(ast -> new ASTCarrier()
            .withParsedExpression(ast)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier, ParsingLevelSeries.Abstract_Knowledge_Expression)));
  }


  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into) {
    return Optional.of(new BinaryCarrier()
        .withEncodedExpression(carrier.getSerializedExpression().getBytes())
        .withAssetId(carrier.getAssetId())
        .withArtifactId(carrier.getArtifactId())
        .withLevel(ParsingLevelSeries.Encoded_Knowledge_Expression)
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevelSeries.Encoded_Knowledge_Expression)));
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
        .withAssetId(carrier.getAssetId())
        .withArtifactId(carrier.getArtifactId())
        .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier,
      SyntacticRepresentation into) {
    Document dox = (Document) carrier.getStructuredExpression();
    return Optional
        .of(new ExpressionCarrier().withSerializedExpression(new String(XMLUtil.toByteArray(dox)))
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
            .withRepresentation(getSerializeResultRepresentation(carrier,
                ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }


  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    T obj = (T) carrier.getParsedExpression();
    return JaxbUtil.marshallDox(
        Collections.singleton(root),
        obj,
        mapper,
        JaxbUtil.defaultProperties())
        .map(dox -> new DocumentCarrier()
            .withStructuredExpression(dox)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Parsed_Knowedge_Expression)
            .withRepresentation(
                getSerializeResultRepresentation(carrier,
                    ParsingLevelSeries.Parsed_Knowedge_Expression)));
  }


  @Override
  protected SerializationFormat getDefaultFormat() {
    return SerializationFormatSeries.XML_1_1;
  }
}
