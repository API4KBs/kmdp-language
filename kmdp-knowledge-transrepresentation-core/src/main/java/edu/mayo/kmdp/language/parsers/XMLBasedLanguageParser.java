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

import edu.mayo.kmdp.metadata.annotations.Annotation;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import javax.xml.bind.JAXBElement;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

public abstract class XMLBasedLanguageParser<T> extends AbstractDeSerializer implements
    DeserializeApiInternal {

  protected Class<T> root;
  protected Function<T, JAXBElement<? super T>> mapper;

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier) {
    return carrier.asString()
        .map(str -> new KnowledgeCarrier()
            .withExpression(str)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier) {
    return carrier.asBinary()
        .flatMap(XMLUtil::loadXMLDocument)
        .map(dox -> new KnowledgeCarrier()
            .withExpression(dox)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Parsed_Knowedge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Parsed_Knowedge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier) {
    return carrier.asString()
        .flatMap(str -> JaxbUtil.unmarshall(getClassContext(), root, str))
        .map(ast -> new KnowledgeCarrier()
            .withExpression(ast)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Abstract_Knowledge_Expression)));
  }

  protected Collection<Class<?>> getClassContext() {
    return Arrays.asList(
        root,
        Annotation.class,
        edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation.class);
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier) {
    if (! (carrier.getExpression() instanceof Document)) {
      return Optional.empty();
    }
    Document dox = (Document) carrier.getExpression();
    return JaxbUtil.unmarshall(getClassContext(), root, dox)
        .map(ast -> new KnowledgeCarrier()
            .withExpression(ast)
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression)
            .withRepresentation(
                getParseResultRepresentation(carrier,
                    ParsingLevelSeries.Abstract_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    return carrier.asBinary()
        .map(bytes -> new KnowledgeCarrier()
                .withExpression(bytes)
                .withAssetId(carrier.getAssetId())
                .withArtifactId(carrier.getArtifactId())
                .withLevel(ParsingLevelSeries.Encoded_Knowledge_Expression)
                .withRepresentation(
                    getSerializeResultRepresentation(carrier,
                        ParsingLevelSeries.Encoded_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    if (! root.isInstance(carrier.getExpression())) {
      return Optional.empty();
    }
    T obj = root.cast(carrier.getExpression());
    return Optional.of(new KnowledgeCarrier()
        .withExpression(JaxbUtil.marshall(
            getClassContext(),
            obj,
            mapper,
            JaxbUtil.defaultProperties())
            .flatMap(Util::asString)
            .orElse("FAILED")
        )
        .withAssetId(carrier.getAssetId())
        .withArtifactId(carrier.getArtifactId())
        .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
        .withRepresentation(
            getSerializeResultRepresentation(carrier, ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(Document.class)
        .map(dox -> new KnowledgeCarrier()
            .withExpression(new String(XMLUtil.toByteArray(dox)))
            .withAssetId(carrier.getAssetId())
            .withArtifactId(carrier.getArtifactId())
            .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
            .withRepresentation(getSerializeResultRepresentation(carrier,
                ParsingLevelSeries.Concrete_Knowledge_Expression)));
  }


  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return carrier.as(root)
        .flatMap(obj -> JaxbUtil.marshallDox(
            getClassContext(),
            obj,
            mapper,
            JaxbUtil.defaultProperties()))
        .map(dox -> new KnowledgeCarrier()
            .withExpression(dox)
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
