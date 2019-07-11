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


import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperation.Lowering_Task)
@KPOperation(KnowledgeProcessingOperation.Lifting_Task)
public class SurrogateParser extends AbstractDeSerializer implements DeserializeApiDelegate {

  private XMLSurrogateParser xmlParser = new XMLSurrogateParser();
  private JSONSurrogateParser jsonParser = new JSONSurrogateParser();

  private final List<SyntacticRepresentation> supportedRepresentations = Arrays.asList(
      rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.XML_1_1, getDefaultCharset()),
      rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.JSON, getDefaultCharset()));


  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    Optional<ASTCarrier> abstracted = xmlParser.abstrakt(carrier);
    return abstracted.isPresent() ? abstracted : jsonParser.abstrakt(carrier);
  }

  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    Optional<ExpressionCarrier> decoded = xmlParser.decode(carrier);
    return decoded.isPresent() ? decoded : jsonParser.decode(carrier);
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    Optional<DocumentCarrier> deserialized = xmlParser.deserialize(carrier);
    return deserialized.isPresent() ? deserialized : jsonParser.deserialize(carrier);
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    Optional<ASTCarrier> parsed = xmlParser.parse(carrier);
    return parsed.isPresent() ? parsed : jsonParser.parse(carrier);
  }


  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.concretize(carrier);
    }
    switch (into.getFormat()) {
      case XML_1_1:
        return xmlParser.concretize(carrier);
      case JSON:
        return jsonParser.concretize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.encode(carrier);
    }
    switch (into.getFormat()) {
      case XML_1_1:
        return xmlParser.encode(carrier);
      case JSON:
        return jsonParser.encode(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.externalize(carrier);
    }
    switch (into.getFormat()) {
      case XML_1_1:
        return xmlParser.externalize(carrier);
      case JSON:
        return jsonParser.externalize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier,
      SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.serialize(carrier);
    }
    switch (into.getFormat()) {
      case XML_1_1:
        return xmlParser.serialize(carrier);
      case JSON:
        return jsonParser.serialize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }


  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return supportedRepresentations;
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return null;
  }


  private static class XMLSurrogateParser extends XMLBasedLanguageParser<KnowledgeAsset> {

    public XMLSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    protected List<SyntacticRepresentation> getSupportedRepresentations() {
      return Collections
          .singletonList(rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.XML_1_1, getDefaultCharset()));
    }
  }

  private static class JSONSurrogateParser extends
      JSONBasedLanguageParser<KnowledgeAsset> {

    public JSONSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    protected List<SyntacticRepresentation> getSupportedRepresentations() {
      return Collections
          .singletonList(rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.JSON, getDefaultCharset()));
    }
  }


}
