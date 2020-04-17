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

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Parsed_Knowedge_Expression;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofAst;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofTree;
import static org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor.detectLevel;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.kmdp.language.DeserializeApiOperator;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;

/**
 * Abstract class that implements a generic parser/serializer for a Knowledge Representation Language
 * and its serializations
 */
public abstract class AbstractDeSerializer
    implements DeserializeApiOperator, Lifter, Lowerer {

  private static final Charset CHARSET = Charset.defaultCharset();

  protected ResourceIdentifier operatorId;

  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  protected void setId(ResourceIdentifier id) {
    this.operatorId = id;
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return getSupportedRepresentations();
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return getSupportedRepresentations();
  }

  @Override
  public Answer<KnowledgeCarrier> applyLift(KnowledgeCarrier knowledgeCarrier,
      ParsingLevel parsingLevel, String s) {
    return Answer.of(
        lift(knowledgeCarrier, parsingLevel,
            ModelMIMECoder.decode(s).orElse(null)));
  }

  @Override
  public Answer<KnowledgeCarrier> applyLower(KnowledgeCarrier knowledgeCarrier,
      ParsingLevel parsingLevel, String s) {
    return Answer.of(
        lower(knowledgeCarrier, parsingLevel,
            ModelMIMECoder.decode(s).orElse(null)));
  }


  public Optional<KnowledgeCarrier> lift(KnowledgeCarrier sourceArtifact,
      ParsingLevel into, SyntacticRepresentation targetRepresentation) {
    checkLiftConsistency(sourceArtifact, into, targetRepresentation);

    switch (sourceArtifact.getLevel().asEnum()) {
      case Encoded_Knowledge_Expression:
        switch (into.asEnum()) {
          case Abstract_Knowledge_Expression:
            return this.innerDecode(sourceArtifact)
                .flatMap(this::innerParse);
          case Parsed_Knowedge_Expression:
            return this.innerDecode(sourceArtifact)
                .flatMap(this::innerDeserialize);
          case Concrete_Knowledge_Expression:
            return this.innerDecode((sourceArtifact));
          case Encoded_Knowledge_Expression:
            return Optional.of(sourceArtifact);
          default:
            return Optional.empty();
        }
      case Concrete_Knowledge_Expression:
        switch (into.asEnum()) {
          case Abstract_Knowledge_Expression:
            return this.innerParse(sourceArtifact);
          case Parsed_Knowedge_Expression:
            return this.innerDeserialize(sourceArtifact);
          case Concrete_Knowledge_Expression:
            return Optional.of(sourceArtifact);
          default:
            return Optional.empty();
        }
      case Parsed_Knowedge_Expression:
        switch (into.asEnum()) {
          case Abstract_Knowledge_Expression:
            return this.innerAbstract(sourceArtifact);
          case Parsed_Knowedge_Expression:
            return Optional.of(sourceArtifact);
          default:
            return Optional.empty();
        }
      case Abstract_Knowledge_Expression:
        return Optional.of(sourceArtifact);
      default:
        return Optional.empty();
    }
  }


  protected Optional<KnowledgeCarrier> lower(KnowledgeCarrier sourceArtifact,
      ParsingLevel toLevel, SyntacticRepresentation into) {
    checkLowerConsistency(sourceArtifact,toLevel,into);

    switch (sourceArtifact.getLevel().asEnum()) {
      case Abstract_Knowledge_Expression:
        return serializeAST(sourceArtifact, toLevel, into);
      case Parsed_Knowedge_Expression:
        return serializeDoc(sourceArtifact, toLevel, into);
      case Concrete_Knowledge_Expression:
        return serializeExpression(sourceArtifact, toLevel, into);
      case Encoded_Knowledge_Expression:
      default:
        return Optional.of(sourceArtifact);
    }

  }


  protected void checkLiftConsistency(KnowledgeCarrier sourceArtifact, ParsingLevel into,
      SyntacticRepresentation targetRepresentation) {
    checkConsistency(sourceArtifact, into, targetRepresentation, Comparison.BROADER);
  }

  protected void checkLowerConsistency(KnowledgeCarrier sourceArtifact, ParsingLevel into,
      SyntacticRepresentation targetRepresentation) {
    checkConsistency(sourceArtifact, into, targetRepresentation, Comparison.NARROWER);
  }

  protected void checkConsistency(KnowledgeCarrier sourceArtifact, ParsingLevel into,
      SyntacticRepresentation targetRepresentation, Comparison test) {

    if (!getSupportedLanguage().sameAs(sourceArtifact.getRepresentation().getLanguage())) {
      throw new UnsupportedOperationException(
          "Parser Cannot handle language : " + sourceArtifact.getRepresentation().getLanguage());
    }

    if (targetRepresentation != null && !targetRepresentation.getLanguage()
        .sameAs(sourceArtifact.getRepresentation().getLanguage())) {
      throw new UnsupportedOperationException(
          "Parser Cannot handle artifact in: " + sourceArtifact.getRepresentation().getLanguage());
    }

    ParsingLevel sourceLevel = detectLevel(sourceArtifact);
    if (ParsingLevelContrastor.singleton.contrast(sourceLevel, into) == test) {
      // parsing must lift to a higher level <=> sourceLevel must be lower
      throw new UnsupportedOperationException(
          "Cannot lift to a lower level : " + sourceArtifact.getLevel());
    }

  }





  private Optional<KnowledgeCarrier> serializeExpression(KnowledgeCarrier expr, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel.asEnum()) {
      case Concrete_Knowledge_Expression:
        return Optional.ofNullable(expr);
      case Encoded_Knowledge_Expression:
      default:
        return this.innerEncode(expr, into);
    }
  }

  private Optional<KnowledgeCarrier> serializeDoc(KnowledgeCarrier doc, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel.asEnum()) {
      case Parsed_Knowedge_Expression:
        return Optional.ofNullable(doc);
      case Concrete_Knowledge_Expression:
        return this.innerSerialize(doc, into);
      case Encoded_Knowledge_Expression:
      default:
        return this.innerSerialize(doc, into)
            .flatMap(this::innerEncode);
    }
  }

  protected Optional<KnowledgeCarrier> serializeAST(KnowledgeCarrier ast, ParsingLevel toLevel,
      SyntacticRepresentation into) {
    switch (toLevel.asEnum()) {
      case Abstract_Knowledge_Expression:
        return Optional.ofNullable(ast);
      case Parsed_Knowedge_Expression:
        return this.innerConcretize(ast, into);
      case Concrete_Knowledge_Expression:
        return this.innerExternalize(ast, into);
      case Encoded_Knowledge_Expression:
      default:
        return this.innerExternalize(ast, into)
            .flatMap(this::innerEncode);
    }
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
          rep.setCharset(getDefaultCharset().name());
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


  protected Charset getDefaultCharset() {
    return CHARSET;
  }

  protected abstract SerializationFormat getDefaultFormat();

  protected SyntacticRepresentation newRepresentation(KnowledgeCarrier source,
      ParsingLevel targetLevel) {
    return ParsingLevelContrastor.singleton.compare(source.getLevel(), targetLevel) < 0
        ? getParseResultRepresentation(source, Abstract_Knowledge_Expression)
        : getSerializeResultRepresentation(source, Parsed_Knowedge_Expression);
  }

  protected KnowledgeCarrier newVerticalCarrier(
      KnowledgeCarrier source,
      ParsingLevel targetLevel,
      Object targetArtifact) {
    return DeserializeApiOperator.newVerticalCarrier(
        source,
        targetLevel,
        newRepresentation(source,targetLevel),
        targetArtifact);
  }


}
