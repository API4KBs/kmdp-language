package edu.mayo.kmdp.language.translators.surrogate.v1;

import static edu.mayo.kmdp.util.StreamUtil.filterAs;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.*;
import edu.mayo.kmdp.metadata.v2.surrogate.Citation;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

import java.util.*;
import java.util.Collection;

public class SurrogateV1ToSurrogateV2 {

  Set<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset> assetCollection = new HashSet<>();

  public Collection<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset> transform(
      KnowledgeAsset knowledgeAsset) {
    transformKnowledgeAsset(knowledgeAsset);
    return assetCollection;
  }

  private void transformKnowledgeAsset(KnowledgeAsset surrogateV1) {
    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2 = new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset();

    mapToResourceId(surrogateV1, surrogateV2);
    surrogateV2.withFormalCategory(surrogateV1.getFormalCategory());
    surrogateV2.withFormalType(surrogateV1.getFormalType());
    surrogateV2.withProcessingMethod(surrogateV1.getProcessingMethod());
    surrogateV2.withRole(surrogateV1.getRole());
    surrogateV2.withName(surrogateV1.getName());
    surrogateV2.withDescription(surrogateV1.getDescription());
    mapLifecycleToSurrogateV2(surrogateV1.getLifecycle(), surrogateV2);
    surrogateV1.getCitations().forEach(cit -> mapCitationToSurrogateV2(cit, surrogateV2));
    surrogateV1.getRelated().forEach(assoc -> mapRelatedItemToLinkList(assoc, surrogateV2));

    surrogateV1.getSubject().stream().flatMap(filterAs(SimpleAnnotation.class))
        .forEach(ann -> mapAnnotationToSurrogate(ann, surrogateV2));
    surrogateV1.getCarriers().forEach(car -> mapCarrierToSurrogateV2(car, surrogateV2));

    //add to collection.
    assetCollection.add(surrogateV2);
  }

  private void mapToResourceId(KnowledgeAsset knowledgeAsset,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2) {
    if (knowledgeAsset.getAssetId() == null) {
      throw new IllegalArgumentException("Surrogates MUST have an asset ID - none found");
    }
    URIIdentifier oldAssetId = knowledgeAsset.getAssetId();
    ResourceIdentifier newAssetId = SemanticIdentifier.newVersionId(oldAssetId.getVersionId());
    surrogateV2.setAssetId(newAssetId);
  }

  private void mapLifecycleToSurrogateV2(Publication lifecycle,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2) {
    edu.mayo.kmdp.metadata.v2.surrogate.Publication newPublication = new edu.mayo.kmdp.metadata.v2.surrogate.Publication();
    if (Optional.ofNullable(lifecycle).isPresent()) {
      //TODO: missing .withAssociatedTo(new Party().withPublishingRole(Author))
      newPublication.withPublicationStatus(lifecycle.getPublicationStatus());
      surrogateV2.withLifecycle(newPublication);
    }
  }

  private void mapCarrierToSurrogateV2(edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact car,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2) {

    if (car instanceof ComputableKnowledgeArtifact) {
      ComputableKnowledgeArtifact oldCarrier = (ComputableKnowledgeArtifact) car;
      edu.mayo.kmdp.metadata.v2.surrogate.ComputableKnowledgeArtifact newCarrier = new edu.mayo.kmdp.metadata.v2.surrogate.ComputableKnowledgeArtifact();

      newCarrier
          .withArtifactId(SemanticIdentifier.newVersionId(oldCarrier.getArtifactId().getUri()));
      newCarrier.withAlternativeTitle(oldCarrier.getAlternativeTitle());
      newCarrier.withLocalization(oldCarrier.getLocalization());
      newCarrier.withExpressionCategory(oldCarrier.getExpressionCategory());
      newCarrier.withTitle(oldCarrier.getTitle());
      edu.mayo.kmdp.metadata.v2.surrogate.Summary newSummary = new edu.mayo.kmdp.metadata.v2.surrogate.Summary();
      if(Optional.ofNullable(oldCarrier.getSummary()).isPresent()) {
        newSummary.withRel(oldCarrier.getSummary().getRel());
        newCarrier.withSummary(newSummary);
      }
      if(Optional.ofNullable(oldCarrier.getInlined()).isPresent()) {
        InlinedRepresentation inlinedRepresentation = oldCarrier.getInlined();
        newCarrier.setInlinedExpression(inlinedRepresentation.getExpr());
      }
      Representation oldCarrierRepresentation = oldCarrier.getRepresentation();
      SyntacticRepresentation newRep = new SyntacticRepresentation();

      newRep.withLanguage(oldCarrierRepresentation.getLanguage());
      newRep.withProfile(oldCarrierRepresentation.getProfile());
      newRep.withFormat(oldCarrierRepresentation.getFormat());
      newRep.withLexicon(oldCarrierRepresentation.getLexicon());
      newRep.withSerialization(oldCarrierRepresentation.getSerialization());
      List<SyntacticRepresentation> subLanguages = new ArrayList<>();
      oldCarrierRepresentation.getWith()
          .forEach(sl -> subLanguages.add(new SyntacticRepresentation().withRole(sl.getRole())));
      newRep.withSubLanguage(subLanguages);
      newCarrier.withRepresentation(newRep);
      surrogateV2.withCarriers(newCarrier);
    } else {
      throw new UnsupportedOperationException("Knowledge Artifact isn't a ComputableKnowledgeArtifact so no mapping at this level can be done.");
    }
  }

  private void mapCitationToSurrogateV2(edu.mayo.kmdp.metadata.surrogate.Citation cit,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2) {
    surrogateV2.withCitations(
        new Citation().withBibliography(cit.getBibliography()).withRel(cit.getRel()));
  }

  private void mapAnnotationToSurrogate(SimpleAnnotation ann,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2) {
    edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation newAnnotation = new edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation();
    newAnnotation.withRel(DatatypeHelper.toConceptIdentifier(ann.getRel()));
    newAnnotation.withRef(DatatypeHelper.toConceptIdentifier(ann.getExpr()));
    surrogateV2.withAnnotation(newAnnotation);
  }

  private ResourceIdentifier getResourceIdFromOldAssociation(KnowledgeResource resource) {
    if (resource instanceof edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset) {
      edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset targetAsset = (edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset)resource;
      URIIdentifier oldAssetId = targetAsset.getAssetId();
      return SemanticIdentifier.newVersionId(oldAssetId.getVersionId());
    } else {
      throw new IllegalStateException("Knowledge Resource is not a Knowledge Asset.");
    }
  }

  private void mapRelatedItemToLinkList(Association assoc,
      edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2) {

    //create the new ResourceId
    ResourceIdentifier resourceId = getResourceIdFromOldAssociation(assoc.getTgt());
    KnowledgeResource resource = assoc.getTgt();
    if (resource instanceof edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset && this.isFullResource((edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset)resource)) {
      edu.mayo.kmdp.metadata.v2.surrogate.Dependency dependencyLink = (new edu.mayo.kmdp.metadata.v2.surrogate.Dependency()).withHref(resourceId);
      dependencyLink.setRel(((Dependency)assoc).getRel());
      surrogateV2.getLinks().add(dependencyLink);
      this.transformKnowledgeAsset((edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset)resource);
    } else if (assoc instanceof Derivative) {
      //need to create the new knowledge asset and add to list.
      edu.mayo.kmdp.metadata.v2.surrogate.Derivative newDerivative = new edu.mayo.kmdp.metadata.v2.surrogate.Derivative()
          .withHref(resourceId);
      newDerivative.setRel(((Derivative) assoc).getRel());
      surrogateV2.getLinks().add(newDerivative);
    } else if (assoc instanceof Version) {
      edu.mayo.kmdp.metadata.v2.surrogate.Version newVersionLink = new edu.mayo.kmdp.metadata.v2.surrogate.Version()
          .withHref(resourceId);
      newVersionLink.setRel(((Version) assoc).getRel());
      surrogateV2.getLinks().add(newVersionLink);
    } else if (assoc instanceof Dependency) {
      edu.mayo.kmdp.metadata.v2.surrogate.Dependency dependencyLink = new edu.mayo.kmdp.metadata.v2.surrogate.Dependency()
          .withHref(resourceId);
      dependencyLink.setRel(((Dependency) assoc).getRel());
      surrogateV2.getLinks().add(dependencyLink);
      this.transformKnowledgeAsset((edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset)resource);
    } else if (assoc instanceof Component) {
      edu.mayo.kmdp.metadata.v2.surrogate.Component dependencyLink = new edu.mayo.kmdp.metadata.v2.surrogate.Component()
          .withHref(resourceId);
      dependencyLink.setRel(((Component) assoc).getRel());
      surrogateV2.getLinks().add(dependencyLink);
    } else if (assoc instanceof Variant) {
      edu.mayo.kmdp.metadata.v2.surrogate.Variant variantLink = new edu.mayo.kmdp.metadata.v2.surrogate.Variant()
          .withHref(resourceId);
      variantLink.setRel(((Variant) assoc).getRel());
      surrogateV2.getLinks().add(variantLink);
    }
  }

  private boolean isFullResource(KnowledgeAsset resource) {
    return !resource.getFormalCategory().isEmpty() || !resource.getRelated().isEmpty() || !resource
        .getCarriers().isEmpty();
  }


}
