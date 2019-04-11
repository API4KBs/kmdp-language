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

import static org.omg.spec.api4kp.KnowledgeCarrierHelper.compare;
import static org.omg.spec.api4kp.KnowledgeCarrierHelper.detectLevel;

import edu.mayo.kmdp.language.DeserializeApi;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public abstract class AbstractDeSerializer implements DeserializeApi, Lifter, Lowerer {

  private final static String charset = Charset.defaultCharset().name();


  @Override
  public KnowledgeCarrier parse(KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {

    if (getParsableLanguages().stream()
        .map(SyntacticRepresentation::getLanguage)
        .noneMatch((lang) -> lang == sourceArtifact.getRepresentation().getLanguage())) {
      return null;
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (compare(sourceLevel, into) > 0) {
      // parsing must lift to a higher level <=> sourceLevel must be lower
      return null;
    }

    if (sourceArtifact instanceof BinaryCarrier) {
      BinaryCarrier binary = (BinaryCarrier) sourceArtifact;
      switch (into) {
        case Abstract_Knowledge_Expression:
          Optional<ASTCarrier> parsedBinary = this.decode(binary)
              .flatMap(this::parse);
          return parsedBinary.orElse(null);
        case Parsed_Knowedge_Expression:
          Optional<DocumentCarrier> deserializedBinary = this.decode(binary)
              .flatMap(this::deserialize);
          return deserializedBinary.orElse(null);
        case Concrete_Knowledge_Expression:
          Optional<ExpressionCarrier> decodedBinary = this.decode((binary));
          return decodedBinary.orElse(null);
        case Encoded_Knowledge_Expression:
          return binary;
      }
    } else if (sourceArtifact instanceof ExpressionCarrier) {
      ExpressionCarrier expr = (ExpressionCarrier) sourceArtifact;
      switch (into) {
        case Abstract_Knowledge_Expression:
          Optional<ASTCarrier> parsedExpr = this.parse(expr);
          return parsedExpr.orElse(null);
        case Parsed_Knowedge_Expression:
          Optional<DocumentCarrier> deserializedExpr = this.deserialize(expr);
          return deserializedExpr.orElse(null);
        case Concrete_Knowledge_Expression:
          return expr;
      }
    } else if (sourceArtifact instanceof DocumentCarrier) {
      DocumentCarrier doc = (DocumentCarrier) sourceArtifact;
      switch (into) {
        case Abstract_Knowledge_Expression:
          Optional<ASTCarrier> parsedExpr = this.abstrakt(doc);
          return parsedExpr.orElse(null);
        case Parsed_Knowedge_Expression:
          return doc;
      }
    } else if (sourceArtifact instanceof ASTCarrier) {
      return sourceArtifact;
    }

    return null;
  }


  @Override
  public KnowledgeCarrier serialize(KnowledgeCarrier sourceArtifact,
      ParsingLevel into) {

    if (getSerializableLanguages().stream()
        .map(SyntacticRepresentation::getLanguage)
        .noneMatch((lang) -> lang == sourceArtifact.getRepresentation().getLanguage())) {
      return null;
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (compare(sourceLevel, into) < 0) {
      // serialization must lower to a lower level <=> sourceLevel must be higher
      return null;
    }

    if (sourceArtifact instanceof ASTCarrier) {
      ASTCarrier ast = (ASTCarrier) sourceArtifact;
      switch (into) {
        case Abstract_Knowledge_Expression:
          return ast;
        case Parsed_Knowedge_Expression:
          Optional<DocumentCarrier> concretizedAST = this.concretize(ast);
          return concretizedAST.orElse(null);
        case Concrete_Knowledge_Expression:
          Optional<ExpressionCarrier> serializedAST = this.externalize(ast);
          return serializedAST.orElse(null);
        case Encoded_Knowledge_Expression:
          Optional<BinaryCarrier> encodedAST = this.externalize(ast)
              .flatMap(this::encode);
          return encodedAST.orElse(null);
      }
    } else if (sourceArtifact instanceof DocumentCarrier) {
      DocumentCarrier doc = (DocumentCarrier) sourceArtifact;
      switch (into) {
        case Parsed_Knowedge_Expression:
          return doc;
        case Concrete_Knowledge_Expression:
          Optional<ExpressionCarrier> serializedDoc = this.serialize(doc);
          return serializedDoc.orElse(null);
        case Encoded_Knowledge_Expression:
          Optional<BinaryCarrier> encodedDoc = this.serialize(doc)
              .flatMap(this::encode);
          return encodedDoc.orElse(null);
      }
    } else if (sourceArtifact instanceof ExpressionCarrier) {
      ExpressionCarrier expr = (ExpressionCarrier) sourceArtifact;
      switch (into) {
        case Concrete_Knowledge_Expression:
          return expr;
        case Encoded_Knowledge_Expression:
          Optional<BinaryCarrier> encodedExpr = this.encode(expr);
          return encodedExpr.orElse(null);
      }
    } else if (sourceArtifact instanceof BinaryCarrier) {
      return sourceArtifact;
    }

    return null;
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
    }
    return rep;
  }

  protected abstract List<SyntacticRepresentation> getSupportedRepresentations();

  @Override
  public List<SyntacticRepresentation> getParsableLanguages() {
    return getSupportedRepresentations();
  }

  @Override
  public List<SyntacticRepresentation> getSerializableLanguages() {
    return getSupportedRepresentations();
  }

  protected ConceptIdentifier getDefaultCharset() {
    return new ConceptIdentifier().withTag(charset);
  }

  protected abstract KRFormat getDefaultFormat();


}
