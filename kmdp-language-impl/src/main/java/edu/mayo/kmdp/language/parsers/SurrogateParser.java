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

import static org.omg.spec.api4kp.KnowledgeCarrierHelper.rep;

import edu.mayo.kmdp.language.DeserializeApi;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeOperations.Lowering_Task)
@KPOperation(KnowledgeOperations.Lifting_Task)
public class SurrogateParser extends AbstractDeSerializer implements DeserializeApi {

  private XMLSurrogateParser xmlParser = new XMLSurrogateParser();
  private JSONSurrogateParser jsonParser = new JSONSurrogateParser();

  private final List<SyntacticRepresentation> supportedRepresentations = Arrays.asList(
      rep(KRLanguage.Asset_Surrogate, KRFormat.XML_1_1, getDefaultCharset()),
      rep(KRLanguage.Asset_Surrogate, KRFormat.JSON, getDefaultCharset()));


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
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier) {
    Optional<DocumentCarrier> concretized = xmlParser.concretize(carrier);
    return concretized.isPresent() ? concretized : jsonParser.concretize(carrier);
  }

  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier) {
    Optional<BinaryCarrier> encoded = xmlParser.encode(carrier);
    return encoded.isPresent() ? encoded : jsonParser.encode(carrier);
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier) {
    Optional<ExpressionCarrier> externalized = xmlParser.externalize(carrier);
    return externalized.isPresent() ? externalized : jsonParser.externalize(carrier);
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier) {
    Optional<ExpressionCarrier> serialized = xmlParser.serialize(carrier);
    return serialized.isPresent() ? serialized : jsonParser.serialize(carrier);
  }

  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return supportedRepresentations;
  }

  @Override
  protected KRFormat getDefaultFormat() {
    return null;
  }


  private static class XMLSurrogateParser extends XMLBasedLanguageParser<KnowledgeAsset> {

    public XMLSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    protected List<SyntacticRepresentation> getSupportedRepresentations() {
      return Collections.singletonList(rep(KRLanguage.Asset_Surrogate, KRFormat.XML_1_1, getDefaultCharset()));
    }
  }

  private static class JSONSurrogateParser extends
      JSONBasedLanguageParser<KnowledgeAsset> {

    public JSONSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    protected List<SyntacticRepresentation> getSupportedRepresentations() {
      return Collections.singletonList(rep(KRLanguage.Asset_Surrogate, KRFormat.JSON, getDefaultCharset()));
    }
  }


}
