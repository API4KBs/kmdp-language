/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language.parsers;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import javax.xml.bind.JAXBElement;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.w3c.dom.Document;

public abstract class XMLBasedLanguageParser<T> extends AbstractDeSerializeOperator {

  protected Class<T> root;
  protected Function<T, JAXBElement<? super T>> mapper;

  protected Collection<Class<?>> getClassContext() {
    return Arrays.asList(
        root,
        Annotation.class);
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier, Properties config) {
    return carrier.asBinary()
        .flatMap(XMLUtil::loadXMLDocument)
        .map(dox -> newVerticalCarrier(carrier, Concrete_Knowledge_Expression, null, dox));
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier, Properties config) {
    return carrier.asString()
        .flatMap(str -> JaxbUtil.unmarshall(getClassContext(), root, str))
        .map(ast -> newVerticalCarrier(carrier, Abstract_Knowledge_Expression, null, ast));
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier, Properties config) {
    return carrier.as(Document.class)
        .flatMap(dox -> JaxbUtil.unmarshall(getClassContext(), root, dox))
        .map(ast -> newVerticalCarrier(carrier, Abstract_Knowledge_Expression, null, ast));
  }

  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties config) {
    return carrier.asBinary()
        .map(str -> newVerticalCarrier(carrier, Encoded_Knowledge_Expression, into, str));
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties config) {
    if (into.getFormat() != null && ! into.getFormat().sameAs(XML_1_1)) {
      return Optional.empty();
    }
    return carrier.as(root)
        .flatMap(obj -> JaxbUtil.marshall(getClassContext(), obj, mapper))
        .flatMap(Util::asString)
        .map(str -> newVerticalCarrier(carrier, Serialized_Knowledge_Expression, into, str));
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties config) {
    return carrier.as(Document.class)
        .map(dox -> new String(XMLUtil.toByteArray(dox)))
        .map(str -> newVerticalCarrier(carrier, Serialized_Knowledge_Expression, into, str));
  }


  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties config) {
    return carrier.as(root)
        .flatMap(obj -> JaxbUtil.marshallDox(getClassContext(), obj, mapper))
        .map(dox -> newVerticalCarrier(carrier, Concrete_Knowledge_Expression, into, dox));
  }


  @Override
  public List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
        rep(getSupportedLanguage()),
        rep(getSupportedLanguage(), XML_1_1),
        rep(getSupportedLanguage(), XML_1_1, Charset.defaultCharset()),
        rep(getSupportedLanguage(), XML_1_1, Charset.defaultCharset(), Encodings.DEFAULT));
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return XML_1_1;
  }

}
