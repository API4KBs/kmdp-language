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

import static edu.mayo.kmdp.util.ws.ResponseHelper.getAll;
import static edu.mayo.kmdp.util.ws.ResponseHelper.succeed;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor.detectLevel;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.http.ResponseEntity;

public abstract class AbstractDeSerializer implements DeserializeApiDelegate, Lifter, Lowerer {

  private static final String CHARSET = Charset.defaultCharset().name();


  @Override
  public ResponseEntity<KnowledgeCarrier> lift(KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {

    if (getAll(getParsableLanguages()).stream()
        .map(SyntacticRepresentation::getLanguage)
        .noneMatch(lang -> lang == sourceArtifact.getRepresentation().getLanguage())) {
      return null;
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (ParsingLevelContrastor.singleton.contrast(sourceLevel, into) == Comparison.BROADER) {
      // parsing must lift to a higher level <=> sourceLevel must be lower
      return null;
    }

    KnowledgeCarrier result = null;

    if (sourceArtifact instanceof BinaryCarrier) {
      BinaryCarrier binary = (BinaryCarrier) sourceArtifact;
      switch (into) {
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
      switch (into) {
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
      switch (into) {
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

    return ResponseHelper.attempt(Optional.ofNullable(result));
  }


  @Override
  public ResponseEntity<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {
    return lower(sourceArtifact, into, null);
  }

  protected ResponseEntity<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel toLevel, SyntacticRepresentation into) {

    if (getAll(getSerializableLanguages()).stream()
        .map(SyntacticRepresentation::getLanguage)
        .noneMatch(lang -> lang == sourceArtifact.getRepresentation().getLanguage())) {
      return null;
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (ParsingLevelContrastor.singleton.contrast(sourceLevel, toLevel) == Comparison.NARROWER) {
      // serialization must lower to a lower level <=> sourceLevel must be higher
      return ResponseHelper.fail();
    }

    if (sourceArtifact instanceof ASTCarrier) {
      ASTCarrier ast = (ASTCarrier) sourceArtifact;
      return succeed(serializeAST(ast, toLevel, into));
    } else if (sourceArtifact instanceof DocumentCarrier) {
      DocumentCarrier doc = (DocumentCarrier) sourceArtifact;
      return succeed(serializeDoc(doc, toLevel, into));
    } else if (sourceArtifact instanceof ExpressionCarrier) {
      ExpressionCarrier expr = (ExpressionCarrier) sourceArtifact;
      return succeed(serializeExpression(expr, toLevel, into));
    } else if (sourceArtifact instanceof BinaryCarrier) {
      return succeed(sourceArtifact);
    }

    return ResponseHelper.fail();
  }

  private KnowledgeCarrier serializeExpression(ExpressionCarrier expr, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel) {
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
    switch (toLevel) {
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
    switch (toLevel) {
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
  public ResponseEntity<KnowledgeCarrier> ensureRepresentation(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    ParsingLevel tgtLevel = detectLevel(into);
    ParsingLevel srcLevel = sourceArtifact.getLevel();

    if (srcLevel == tgtLevel && sourceArtifact.getRepresentation().equals(into)) {
      return succeed(sourceArtifact);
    }
    return ResponseHelper.flatMap(lift(sourceArtifact, Abstract_Knowledge_Expression),
        kc -> serialize(kc, into));
  }


  @Override
  public ResponseEntity<KnowledgeCarrier> deserialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return lift(sourceArtifact, detectLevel(into));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> serialize(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation into) {
    return lower(sourceArtifact, detectLevel(into), into);
  }


  protected SyntacticRepresentation getParseResultRepresentation(
      KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {
    SyntacticRepresentation rep = (SyntacticRepresentation) sourceArtifact.getRepresentation()
        .clone();
    switch (into) {
      case Abstract_Knowledge_Expression:
        rep.setFormat(null);
        rep.setCharset(null);
        rep.setEncoding(null);
        break;
      case Parsed_Knowedge_Expression:
        rep.setCharset(null);
        rep.setEncoding(null);
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
    switch (into) {
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
        if (rep.getFormat() == null) {
          rep.setFormat(getDefaultFormat());
        }
        break;
      case Abstract_Knowledge_Expression:
        rep.setFormat(null);
        rep.setCharset(null);
        rep.setEncoding(null);
        break;
      default:
        break;
    }
    return rep;
  }

  protected abstract List<SyntacticRepresentation> getSupportedRepresentations();

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getParsableLanguages() {
    return succeed(getSupportedRepresentations());
  }

  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getSerializableLanguages() {
    return succeed(getSupportedRepresentations());
  }

  protected String getDefaultCharset() {
    return CHARSET;
  }

  protected abstract SerializationFormat getDefaultFormat();


}
