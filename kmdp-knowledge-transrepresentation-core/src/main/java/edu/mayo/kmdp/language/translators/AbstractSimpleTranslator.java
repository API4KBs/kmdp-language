package edu.mayo.kmdp.language.translators;

import static edu.mayo.kmdp.comparator.Contrastor.isNarrowerOrEqual;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.language.TransionApiOperator;
import edu.mayo.kmdp.tranx.v4.server.TransxionApiInternal._applyNamedTransrepresent;
import edu.mayo.kmdp.tranx.v4.server.TransxionApiInternal._applyTransrepresent;
import edu.mayo.kmdp.util.Util;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._1_0.id.IdentifierConstants;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;

public abstract class AbstractSimpleTranslator<S,T>
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
      String xAccept) {
    SyntacticRepresentation targetRep = checkTargetRepresentation(knowledgeCarrier,
        toTargetRepresentation(xAccept));
    return Answer.of(
        applyTransrepresentation(knowledgeCarrier, targetRep));
  }

  @Override
  public Answer<KnowledgeCarrier> applyNamedTransrepresent(UUID uuid,
      KnowledgeCarrier knowledgeCarrier, String xAccept) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyTransrepresent(knowledgeCarrier, xAccept)
        : Answer.unsupported();
  }

  protected SyntacticRepresentation checkTargetRepresentation(KnowledgeCarrier knowledgeCarrier,
      SyntacticRepresentation tgtRep) {
    if (knowledgeCarrier.getRepresentation() == null ||
        !knowledgeCarrier.getRepresentation().getLanguage().sameAs(getSupportedLanguage())) {
      throw new UnsupportedOperationException();
    }
    if (getFrom().stream()
        .noneMatch(
            from -> knowledgeCarrier.getLevel().sameAs(ParsingLevelContrastor.detectLevel(from)))) {
      throw new UnsupportedOperationException();
    }
    if (getInto().stream()
        .noneMatch(into -> isNarrowerOrEqual(theRepContrastor.contrast(into, tgtRep)))) {
      throw new UnsupportedOperationException("Requested narrower reprsentation than supported");
    }
    return tgtRep;
  }

  protected Optional<KnowledgeCarrier> applyTransrepresentation(
      KnowledgeCarrier src,
      SyntacticRepresentation tgtRep) {
    switch (src.getLevel().asEnum()) {
      case Encoded_Knowledge_Expression:
        return src.asBinary()
            .flatMap(bytes -> transformBinary(src.getAssetId(), bytes,tgtRep))
            .map(out -> TransionApiOperator.newHorizontalCarrier(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getArtifactId())));
      case Concrete_Knowledge_Expression:
        return src.asString()
            .flatMap(str -> transformString(src.getAssetId(), str,tgtRep))
            .map(out -> TransionApiOperator.newHorizontalCarrier(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getArtifactId())));
      case Parsed_Knowedge_Expression:
        return transformTree(src.getAssetId(), src.getExpression(), tgtRep)
            .map(out -> TransionApiOperator.newHorizontalCarrier(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getArtifactId())));
      case Abstract_Knowledge_Expression:
        return transformAst(src.getAssetId(), (S) src.getExpression(), tgtRep)
            .map(out -> TransionApiOperator.newHorizontalCarrier(
                tgtRep, out, mapAssetId(src.getAssetId()), mapArtifactId(src.getArtifactId())));
      default:
        throw new UnsupportedOperationException();
    }
  }

  protected Optional<T> transformAst(
      ResourceIdentifier assetId, S expression,
      SyntacticRepresentation tgtRep) {
    throw new UnsupportedOperationException();
  }

  protected Optional<T> transformTree(
      ResourceIdentifier assetId, Object tree,
      SyntacticRepresentation tgtRep) {
    throw new UnsupportedOperationException();
  }

  protected Optional<T> transformString(
      ResourceIdentifier assetId, String str,
      SyntacticRepresentation tgtRep) {
    throw new UnsupportedOperationException();
  }

  protected Optional<T> transformBinary(
      ResourceIdentifier assetId, byte[] bytes,
      SyntacticRepresentation tgtRep) {
    throw new UnsupportedOperationException();
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


}
