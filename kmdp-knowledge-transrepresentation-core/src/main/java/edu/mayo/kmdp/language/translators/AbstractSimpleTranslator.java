package edu.mayo.kmdp.language.translators;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.contrastors.SyntacticRepresentationContrastor.theRepContrastor;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.asEnum;

import edu.mayo.kmdp.language.TransionApiOperator;
import edu.mayo.kmdp.util.PropertiesUtil;
import edu.mayo.kmdp.util.Util;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.DeserializeApiInternal._applyLift;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.DeserializeApiInternal._applyLower;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.TransxionApiInternal._applyNamedTransrepresent;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.TransxionApiInternal._applyTransrepresent;
import org.omg.spec.api4kp._20200801.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;

public abstract class AbstractSimpleTranslator<S, T>
    implements TransionApiOperator, _applyTransrepresent, _applyNamedTransrepresent {

  protected ResourceIdentifier operatorId;

  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  protected void setId(ResourceIdentifier id) {
    this.operatorId = id;
  }

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresent(KnowledgeCarrier knowledgeCarrier,
      String xAccept, String properties) {
    SyntacticRepresentation targetRep = checkTargetRepresentation(knowledgeCarrier,
        toTargetRepresentation(xAccept));
    return Answer.of(
        applyTransrepresentation(knowledgeCarrier, targetRep, readProperties(properties)));
  }

  @Override
  public Answer<KnowledgeCarrier> applyNamedTransrepresent(UUID uuid,
      KnowledgeCarrier knowledgeCarrier, String xAccept, String properties) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyTransrepresent(knowledgeCarrier, xAccept, properties)
        : Answer.unsupported();
  }

  protected SyntacticRepresentation checkTargetRepresentation(KnowledgeCarrier knowledgeCarrier,
      SyntacticRepresentation tgtRep) {
    if (knowledgeCarrier.getRepresentation() == null ||
        !knowledgeCarrier.getRepresentation().getLanguage().sameAs(getSupportedLanguage())) {
      throw new UnsupportedOperationException(
          "Unable to handle representation " + knowledgeCarrier.getRepresentation());
    }
    if (getFrom().stream()
        .noneMatch(
            from -> knowledgeCarrier.getLevel().sameAs(ParsingLevelContrastor.detectLevel(from)))) {
      throw new UnsupportedOperationException();
    }
    if (getInto().stream()
        .noneMatch(into -> theRepContrastor.isNarrowerOrEqual(into, tgtRep))) {
      throw new UnsupportedOperationException("Requested narrower reprsentation than supported");
    }
    return tgtRep;
  }

  protected Optional<KnowledgeCarrier> applyTransrepresentation(
      KnowledgeCarrier src,
      SyntacticRepresentation tgtRep,
      Properties config) {
    switch (asEnum(src.getLevel())) {
      case Encoded_Knowledge_Expression:
        return src.asBinary()
            .flatMap(
                bytes -> transformBinary(src.getAssetId(), bytes, src.getRepresentation(), tgtRep,
                    config))
            .map(out -> wrap(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getAssetId(), src.getArtifactId()),
                src.getLabel()));
      case Serialized_Knowledge_Expression:
        return src.asString()
            .flatMap(str -> transformString(src.getAssetId(), str, src.getRepresentation(), tgtRep,
                config))
            .map(out -> wrap(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getAssetId(), src.getArtifactId()),
                src.getLabel()));
      case Concrete_Knowledge_Expression:
        return transformTree(src.getAssetId(), src.getExpression(), src.getRepresentation(), tgtRep,
            config)
            .map(out -> wrap(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getAssetId(), src.getArtifactId()),
                src.getLabel()));
      case Abstract_Knowledge_Expression:
        return transformAst(src.getAssetId(), (S) src.getExpression(), src.getRepresentation(),
            tgtRep, config)
            .map(out -> wrap(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getAssetId(), src.getArtifactId()),
                src.getLabel()));
      default:
        throw new UnsupportedOperationException();
    }
  }

  protected KnowledgeCarrier wrap(
      SyntacticRepresentation tgtRep,
      T translatedArtifact,
      ResourceIdentifier mappedAssetId,
      ResourceIdentifier mappedArtifactId,
      String label) {
    KnowledgeCarrier kc = TransionApiOperator.newHorizontalCarrier(
        tgtRep,
        rep(tgtRep.getLanguage()),
        translatedArtifact,
        mappedAssetId,
        mappedArtifactId,
        label);
    return getTargetParser()
        .flatMap(parser -> parser.applyLower(
            kc,
            ParsingLevelContrastor.detectLevel(tgtRep),
            codedRep(tgtRep), null))
        .orElse(kc);
  }

  protected Answer<_applyLift> getParser() {
    return Answer.unsupported();
  }

  protected Answer<_applyLower> getTargetParser() {
    return Answer.unsupported();
  }

  protected Optional<T> transformAst(
      ResourceIdentifier assetId, S expression,
      SyntacticRepresentation srcRep,
      SyntacticRepresentation tgtRep,
      Properties config) {
    throw new UnsupportedOperationException();
  }

  protected Optional<T> transformTree(
      ResourceIdentifier assetId, Object expression, SyntacticRepresentation srcRep,
      SyntacticRepresentation tgtRep,
      Properties config) {
    return getParser()
        .flatMap(parser -> parser.applyLift(
            AbstractCarrier.ofTree(expression, srcRep)
                .withLevel(Concrete_Knowledge_Expression),
            Abstract_Knowledge_Expression,
            codedRep(srcRep.getLanguage()),
            PropertiesUtil.serializeProps(config))
            .map(KnowledgeCarrier::getExpression)
            .flatOpt(
                srcAst -> this
                    .transformAst(assetId, (S) srcAst, rep(srcRep.getLanguage()), tgtRep, config)))
        .getOptionalValue();
  }

  protected Optional<T> transformString(
      ResourceIdentifier assetId, String str,
      SyntacticRepresentation srcRep,
      SyntacticRepresentation tgtRep,
      Properties config) {
    return getParser()
        .flatMap(parser -> parser.applyLift(
            AbstractCarrier.ofTree(str, srcRep)
                .withLevel(Serialized_Knowledge_Expression),
            Abstract_Knowledge_Expression,
            codedRep(srcRep.getLanguage()),
            PropertiesUtil.serializeProps(config))
            .map(KnowledgeCarrier::getExpression)
            .flatOpt(
                srcAst -> this
                    .transformAst(assetId, (S) srcAst, rep(srcRep.getLanguage()), tgtRep, config)))
        .getOptionalValue();
  }

  protected Optional<T> transformBinary(
      ResourceIdentifier assetId, byte[] bytes,
      SyntacticRepresentation srcRep,
      SyntacticRepresentation tgtRep,
      Properties config) {
    Answer<KnowledgeCarrier> ast =
        getParser().flatMap(parser ->
            parser.applyLift(
                AbstractCarrier.ofTree(bytes, srcRep)
                    .withLevel(Encoded_Knowledge_Expression),
                Abstract_Knowledge_Expression,
                codedRep(srcRep.getLanguage()),
                PropertiesUtil.serializeProps(config))
        );
    return ast
        .map(KnowledgeCarrier::getExpression)
        .flatOpt(
            srcAst -> this
                .transformAst(assetId, (S) srcAst, rep(srcRep.getLanguage()), tgtRep, config))
        .getOptionalValue();
  }


  protected SyntacticRepresentation toTargetRepresentation(String xAccept) {
    SyntacticRepresentation tgtRep;
    if (Util.isEmpty(xAccept)) {
      tgtRep = getInto().get(0);
    } else {
      tgtRep = ModelMIMECoder.decode(xAccept)
          .orElseThrow(UnsupportedOperationException::new);
    }
    return tgtRep;
  }

  protected Properties readProperties(String properties) {
    return PropertiesUtil.parseProperties(properties);
  }


  @Override
  public boolean can_applyNamedTransrepresent() {
    return true;
  }

  @Override
  public boolean can_applyTransrepresent() {
    return true;
  }
}
