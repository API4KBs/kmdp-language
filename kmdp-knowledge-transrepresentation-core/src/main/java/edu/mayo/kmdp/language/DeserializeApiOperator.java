package edu.mayo.kmdp.language;

import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofAst;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofTree;

import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.KnowledgePlatformOperator;
import org.omg.spec.api4kp._1_0.id.IdentifierConstants;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.DeserializationOperator;

public interface DeserializeApiOperator
    extends DeserializeApiInternal.Operator, KnowledgePlatformOperator<DeserializationOperator> {

  List<SyntacticRepresentation> getFrom();

  List<SyntacticRepresentation> getInto();

  default DeserializationOperator getDescriptor() {
    return new DeserializationOperator()
        .withFrom(getFrom())
        .withInto(getInto())
        .withOperatorId(getOperatorId());
  }

  default ResourceIdentifier mapArtifactId(ResourceIdentifier artifactId) {
    return artifactId;
  }

  default ResourceIdentifier mapAssetId(ResourceIdentifier assetId) {
    return assetId;
  }


  static KnowledgeCarrier newVerticalCarrier(
      KnowledgeCarrier source,
      ParsingLevel targetLevel,
      SyntacticRepresentation targetRepresentation,
      Object targetArtifact) {

    KnowledgeCarrier newCarrier;
    switch (targetLevel.asEnum()) {
      case Abstract_Knowledge_Expression:
        newCarrier = ofAst(targetArtifact);
        break;
      case Parsed_Knowedge_Expression:
        newCarrier = ofTree(targetArtifact);
        break;
      case Concrete_Knowledge_Expression:
        newCarrier = of(targetArtifact.toString());
        break;
      case Encoded_Knowledge_Expression:
      default:
        newCarrier = of((byte[]) targetArtifact);
    }

    return newCarrier.withAssetId(source.getAssetId())
        .withArtifactId(source.getArtifactId())
        .withLevel(targetLevel)
        .withRepresentation(targetRepresentation);
  }


}
