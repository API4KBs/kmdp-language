package edu.mayo.kmdp.language.translators;

import static edu.mayo.kmdp.comparator.Contrastor.isNarrowerOrEqual;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.tranx.v3.server.TransxionApiInternal;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;

public abstract class AbstractSimpleTranslator implements TransxionApiInternal {

  @Override
  public Answer<TransrepresentationOperator> getTransrepresentation(String txionId) {
    return Answer.of(
        new org.omg.spec.api4kp._1_0.services.tranx.resources.TransrepresentationOperator()
            .withOperatorId(getId())
            .withAcceptedParams(getTransrepresentationAcceptedParameters(txionId).orElse(null))
            .withFrom(getTransrepresentationInput(txionId).orElse(null))
            .withInto(getTransrepresentationOutput(txionId).orElse(null)));
  }

  @Override
  public Answer<ParameterDefinitions> getTransrepresentationAcceptedParameters(
      String txionId) {
    return Answer.of(new ParameterDefinitions());
  }


  public Answer<org.omg.spec.api4kp._1_0.services.SyntacticRepresentation> getTransrepresentationInput(String txionId) {
    if (txionId != null && !getId().equals(txionId)) {
      return Answer.failed(new UnsupportedOperationException());
    }
    return Answer.of(getFrom());
  }

  @Override
  public Answer<org.omg.spec.api4kp._1_0.services.SyntacticRepresentation> getTransrepresentationOutput(
      String txionId) {
    return Answer.of(getTo());
  }

  @Override
  public Answer<List<TransrepresentationOperator>> listOperators(
      org.omg.spec.api4kp._1_0.services.SyntacticRepresentation from,
      org.omg.spec.api4kp._1_0.services.SyntacticRepresentation into,
      String method) {
    return getTransrepresentation(getId())
        .map(Collections::singletonList);
  }


  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentation(
      String txId,
      KnowledgeCarrier sourceArtifact,
      Properties params) {
    return Answer.of(
        doTransform(sourceArtifact)
            .withAssetId(mapAssetId(sourceArtifact.getAssetId()))
            .withArtifactId(mapArtifactId(sourceArtifact.getArtifactId()))
            .withLabel(sourceArtifact.getLabel())
            .withRepresentation(getTo())
    );
  }

  @Override
  public Answer<KnowledgeCarrier> applyTransrepresentationInto(KnowledgeCarrier sourceArtifact,
      org.omg.spec.api4kp._1_0.services.SyntacticRepresentation into) {
    if (isNarrowerOrEqual(theRepContrastor.contrast(getTo(), into))) {
      return applyTransrepresentation(
          getId(),
          sourceArtifact,
          new Properties());
    } else {
      return Answer.unsupported();
    }
  }

  public abstract String getId();

  public abstract org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getFrom();

  public abstract org.omg.spec.api4kp._1_0.services.SyntacticRepresentation getTo();

  protected KnowledgeCarrier doTransform(KnowledgeCarrier sourceArtifact) {
    return sourceArtifact;
  }

  protected KnowledgeCarrier doTransform(KnowledgeCarrier sourceArtifact, Properties params) {
    return doTransform(sourceArtifact);
  }

  protected URIIdentifier mapArtifactId(URIIdentifier artifactId) {
    return DatatypeHelper.uri(UUID.randomUUID().toString());
  }

  protected URIIdentifier mapAssetId(URIIdentifier assetId) {
    return assetId;
  }

}
