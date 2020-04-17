package edu.mayo.kmdp.language;

import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofAst;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.ofTree;

import edu.mayo.kmdp.tranx.v4.server.TransxionApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.omg.spec.api4kp._1_0.KnowledgePlatformOperator;
import org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._1_0.id.IdentifierConstants;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.TransrepresentationOperator;

public interface TransionApiOperator
    extends TransxionApiInternal.Operator, KnowledgePlatformOperator<TransrepresentationOperator> {

  List<SyntacticRepresentation> getFrom();

  List<SyntacticRepresentation> getInto();

  KnowledgeRepresentationLanguage getTargetLanguage();

  default ResourceIdentifier mapArtifactId(ResourceIdentifier artifactId) {
    // in general, transformations preserve the asset, but create brand new artifacts
    return SemanticIdentifier.newId(UUID.randomUUID(), IdentifierConstants.VERSION_ZERO);
  }

  default ResourceIdentifier mapAssetId(ResourceIdentifier assetId) {
    return assetId;
  }


  @Override
  default TransrepresentationOperator getDescriptor() {
    return new TransrepresentationOperator()
        .withFrom(getFrom())
        .withInto(getInto())
        .withOperatorId(getOperatorId());
  }

  static KnowledgeCarrier newHorizontalCarrier(
      SyntacticRepresentation targetRepresentation,
      Object targetArtifact,
      ResourceIdentifier mappedAssetId,
      ResourceIdentifier mappedArtifactId) {

    ParsingLevel targetLevel = ParsingLevelContrastor.detectLevel(targetRepresentation);

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

    return newCarrier.withAssetId(mappedAssetId)
        .withArtifactId(mappedArtifactId)
        .withLevel(targetLevel)
        .withRepresentation(targetRepresentation);
  }

}
