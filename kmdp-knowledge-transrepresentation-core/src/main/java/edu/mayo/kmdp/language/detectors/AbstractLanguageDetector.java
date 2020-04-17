package edu.mayo.kmdp.language.detectors;

import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal._applyDetect;
import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal._applyNamedDetect;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public abstract class AbstractLanguageDetector
    implements _applyDetect, _applyNamedDetect {

  protected ResourceIdentifier operatorId;

  @Override
  public Answer<KnowledgeCarrier> applyNamedDetect(UUID uuid, KnowledgeCarrier knowledgeCarrier) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyDetect(knowledgeCarrier)
        : Answer.unsupported();
  }

  @Override
  public Answer<KnowledgeCarrier> applyDetect(KnowledgeCarrier sourceArtifact) {
    try {
      return Answer.of(
          detect(sourceArtifact)
              .map(rep -> applyDetectedRepresentation(rep, sourceArtifact)));
    } catch (Exception e) {
      return Answer.failed(e);
    }
  }

  protected Optional<SyntacticRepresentation> detect(KnowledgeCarrier sourceArtifact) {
    switch (sourceArtifact.getLevel().asEnum()) {
      case Encoded_Knowledge_Expression:
        return sourceArtifact.asBinary().flatMap(this::detectBinary);
      case Concrete_Knowledge_Expression:
        return sourceArtifact.asString().flatMap(this::detectString);
      case Parsed_Knowedge_Expression:
        return detectTree(sourceArtifact.getExpression());
      case Abstract_Knowledge_Expression:
        return detectAST(sourceArtifact.getExpression());
      default:
        return Optional.empty();
    }
  }

  protected KnowledgeCarrier applyDetectedRepresentation(
      SyntacticRepresentation rep, KnowledgeCarrier sourceArtifact) {
    SyntacticRepresentation mergedRep = checkConsistency(rep,sourceArtifact.getRepresentation());
    KnowledgeCarrier copy = new KnowledgeCarrier();
    sourceArtifact.copyTo(copy);
    return sourceArtifact.withRepresentation(mergedRep);
  }

  protected SyntacticRepresentation checkConsistency(SyntacticRepresentation detected, SyntacticRepresentation original) {
    if (original != null) {
      switch (theRepContrastor.contrast(detected,original)) {
        case IDENTICAL:
        case EQUAL:
        case EQUIVALENT:
          return detected;
        case BROADER:
          return original;
        case NARROWER:
          return detected;
        default:
          throw new IllegalStateException("Provided and Detected representations are inconsistent");
      }
    } else {
      return detected;
    }
  }

  public abstract KnowledgeRepresentationLanguage getSupportedLanguage();

  public abstract List<SyntacticRepresentation> getSupportedRepresentations();

  protected abstract Optional<SyntacticRepresentation> detectBinary(byte[] bytes);

  protected abstract Optional<SyntacticRepresentation> detectString(String string);

  protected abstract Optional<SyntacticRepresentation> detectTree(Object parseTree);

  protected abstract Optional<SyntacticRepresentation> detectAST(Object ast);


  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  protected void setId(ResourceIdentifier id) {
    this.operatorId = id;
  }

}
