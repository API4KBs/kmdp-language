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

import static java.util.Collections.emptyList;
import static org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor.detectLevel;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public abstract class AbstractDeSerializer implements DeserializeApiInternal, Lifter, Lowerer {

  private static final String CHARSET = Charset.defaultCharset().name();


  @Override
  public Answer<KnowledgeCarrier> lift(KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {

    if (getParsableLanguages().orElse(emptyList()).stream()
        .map(SyntacticRepresentation::getLanguage)
        .noneMatch(lang -> lang.asEnum() == sourceArtifact.getRepresentation().getLanguage())) {
      return Answer.unsupported();
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (ParsingLevelContrastor.singleton.contrast(sourceLevel, into) == Comparison.BROADER) {
      // parsing must lift to a higher level <=> sourceLevel must be lower
      return Answer.unsupported();
    }

    KnowledgeCarrier result = null;

    if (sourceArtifact instanceof BinaryCarrier) {
      BinaryCarrier binary = (BinaryCarrier) sourceArtifact;
      switch (into.asEnum()) {
        case Abstract_Knowledge_Expression:
          Optional<ASTCarrier> parsedBinary = this.decode(binary)
              .flatMap(this::parse);
          result = parsedBinary.orElse(null);
          break;
        case Parsed_Knowedge_Expression:
          Optional<DocumentCarrier> deserializedBinary = this.decode(binary)
              .flatMap(this::deserialize);
          result = deserializedBinary.orElse(null);
          break;
        case Concrete_Knowledge_Expression:
          Optional<ExpressionCarrier> decodedBinary = this.decode((binary));
          result = decodedBinary.orElse(null);
          break;
        case Encoded_Knowledge_Expression:
          result = binary;
          break;
        default:
      }
    } else if (sourceArtifact instanceof ExpressionCarrier) {
      ExpressionCarrier expr = (ExpressionCarrier) sourceArtifact;
      switch (into.asEnum()) {
        case Abstract_Knowledge_Expression:
          Optional<ASTCarrier> parsedExpr = this.parse(expr);
          result = parsedExpr.orElse(null);
          break;
        case Parsed_Knowedge_Expression:
          Optional<DocumentCarrier> deserializedExpr = this.deserialize(expr);
          result = deserializedExpr.orElse(null);
          break;
        case Concrete_Knowledge_Expression:
          result = expr;
          break;
        default:
      }
    } else if (sourceArtifact instanceof DocumentCarrier) {
      DocumentCarrier doc = (DocumentCarrier) sourceArtifact;
      switch (into.asEnum()) {
        case Abstract_Knowledge_Expression:
          Optional<ASTCarrier> parsedExpr = this.abstrakt(doc);
          result = parsedExpr.orElse(null);
          break;
        case Parsed_Knowedge_Expression:
          result = doc;
          break;
        default:
      }
    } else if (sourceArtifact instanceof ASTCarrier) {
      result = sourceArtifact;
    }

    return result != null
        ? Answer.of(
        result.withAssetId(sourceArtifact.getAssetId())
            .withArtifactId(sourceArtifact.getArtifactId())
            .withHref(sourceArtifact.getHref()))
        : Answer.failed();
  }


  @Override
  public Answer<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {
    return lower(sourceArtifact, into, null);
  }

  protected Answer<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel toLevel, SyntacticRepresentation into) {

    if (getSerializableLanguages().get().stream()
        .map(SyntacticRepresentation::getLanguage)
        .noneMatch(lang -> lang.asEnum() == sourceArtifact.getRepresentation().getLanguage())) {
      return Answer.failed();
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (ParsingLevelContrastor.singleton.contrast(sourceLevel, toLevel) == Comparison.NARROWER) {
      // serialization must lower to a lower level <=> sourceLevel must be higher
      return Answer.failed();
    }

    if (sourceArtifact instanceof ASTCarrier) {
      ASTCarrier ast = (ASTCarrier) sourceArtifact;
      return Answer.of(serializeAST(ast, toLevel, into));
    } else if (sourceArtifact instanceof DocumentCarrier) {
      DocumentCarrier doc = (DocumentCarrier) sourceArtifact;
      return Answer.of(serializeDoc(doc, toLevel, into));
    } else if (sourceArtifact instanceof ExpressionCarrier) {
      ExpressionCarrier expr = (ExpressionCarrier) sourceArtifact;
      return Answer.of(serializeExpression(expr, toLevel, into));
    } else if (sourceArtifact instanceof BinaryCarrier) {
      return Answer.of(sourceArtifact);
    }

    return Answer.failed();
  }

  private KnowledgeCarrier serializeExpression(ExpressionCarrier expr, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel.asEnum()) {
      case Concrete_Knowledge_Expression:
        return expr;
      case Encoded_Knowledge_Expression:
      default:
        Optional<BinaryCarrier> encodedExpr = this.encode(expr, into);
        return encodedExpr.orElse(null);
    }
  }

  private KnowledgeCarrier serializeDoc(DocumentCarrier doc, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel.asEnum()) {
      case Parsed_Knowedge_Expression:
        return doc;
      case Concrete_Knowledge_Expression:
        Optional<ExpressionCarrier> serializedDoc = this.serialize(doc, into);
        return serializedDoc.orElse(null);
      case Encoded_Knowledge_Expression:
      default:
        Optional<BinaryCarrier> encodedDoc = this.serialize(doc, into)
            .flatMap(this::encode);
        return encodedDoc.orElse(null);
    }
  }

  protected KnowledgeCarrier serializeAST(ASTCarrier ast, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel.asEnum()) {
      case Abstract_Knowledge_Expression:
        return ast;
      case Parsed_Knowedge_Expression:
        Optional<DocumentCarrier> concretizedAST = this.concretize(ast, into);
        return concretizedAST.orElse(null);
      case Concrete_Knowledge_Expression:
        Optional<ExpressionCarrier> serializedAST = this.externalize(ast, into);
        return serializedAST.orElse(null);
      case Encoded_Knowledge_Expression:
      default:
        Optional<BinaryCarrier> encodedAST = this.externalize(ast, into)
            .flatMap(this::encode);
        return encodedAST.orElse(null);
    }
  }

  @Override
  public Answer<KnowledgeCarrier> ensureRepresentation(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    ParsingLevel tgtLevel = detectLevel(into);
    ParsingLevel srcLevel = sourceArtifact.getLevel();

    if (srcLevel == tgtLevel && sourceArtifact.getRepresentation().equals(into)) {
      return Answer.of(sourceArtifact);
    }
    return lift(sourceArtifact, ParsingLevelSeries.Abstract_Knowledge_Expression)
        .flatMap(kc -> serialize(kc, into));
  }


  @Override
  public Answer<KnowledgeCarrier> deserialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return lift(sourceArtifact, detectLevel(into));
  }

  @Override
  public Answer<KnowledgeCarrier> serialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return lower(sourceArtifact, detectLevel(into), into);
  }


  protected SyntacticRepresentation getParseResultRepresentation(
      KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {
    SyntacticRepresentation rep = (SyntacticRepresentation) sourceArtifact.getRepresentation()
        .clone();
    switch (into.asEnum()) {
      case Abstract_Knowledge_Expression:
        rep.setFormat(null);
        rep.setSerialization(null);
        rep.setCharset(null);
        rep.setEncoding(null);
        break;
      case Parsed_Knowedge_Expression:
        rep.setCharset(null);
        rep.setEncoding(null);
        rep.setSerialization(null);
        break;
      case Concrete_Knowledge_Expression:
        rep.setEncoding(null);
        break;
      case Encoded_Knowledge_Expression:
      default:
        break;
    }
    return rep;
  }

  protected SyntacticRepresentation getSerializeResultRepresentation(
      KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {
    SyntacticRepresentation rep = (SyntacticRepresentation) sourceArtifact.getRepresentation()
        .clone();
    switch (into.asEnum()) {
      case Concrete_Knowledge_Expression:
        rep.setEncoding(null);
        if (rep.getFormat() == null) {
          rep.setFormat(getDefaultFormat());
        }
        if (rep.getCharset() == null) {
          rep.setCharset(getDefaultCharset());
        }
        break;
      case Parsed_Knowedge_Expression:
        rep.setCharset(null);
        rep.setEncoding(null);
        rep.setSerialization(null);
        if (rep.getFormat() == null) {
          rep.setFormat(getDefaultFormat());
        }
        break;
      case Abstract_Knowledge_Expression:
        rep.setFormat(null);
        rep.setCharset(null);
        rep.setEncoding(null);
        rep.setSerialization(null);
        break;
      default:
        break;
    }
    return rep;
  }

  protected abstract List<SyntacticRepresentation> getSupportedRepresentations();

  @Override
  public Answer<List<SyntacticRepresentation>> getParsableLanguages() {
    return Answer.of(getSupportedRepresentations());
  }

  @Override
  public Answer<List<SyntacticRepresentation>> getSerializableLanguages() {
    return Answer.of(getSupportedRepresentations());
  }

  protected String getDefaultCharset() {
    return CHARSET;
  }

  protected abstract SerializationFormat getDefaultFormat();


}
