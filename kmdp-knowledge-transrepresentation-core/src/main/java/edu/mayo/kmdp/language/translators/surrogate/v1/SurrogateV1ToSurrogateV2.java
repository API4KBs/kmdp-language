package edu.mayo.kmdp.language.translators.surrogate.v1;

import static edu.mayo.kmdp.util.StreamUtil.filterAs;

import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.Association;
import edu.mayo.kmdp.metadata.surrogate.Component;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeResource;
import edu.mayo.kmdp.metadata.surrogate.Publication;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.Variant;
import edu.mayo.kmdp.metadata.surrogate.Version;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._1_0.id.ScopedIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Citation;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.Language;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory.IKnowledgeArtifactCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory.KnowledgeArtifactCategorySeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRole;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRole;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatus;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries;

public class SurrogateV1ToSurrogateV2 {

  Set<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset> assetCollection = new HashSet<>();

  public Collection<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset> transform(
      KnowledgeAsset knowledgeAsset) {
    transformKnowledgeAsset(knowledgeAsset);
    return assetCollection;
  }

  private void transformKnowledgeAsset(KnowledgeAsset surrogateV1) {
    org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2
        = new org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset();

    mapToResourceId(surrogateV1, surrogateV2);
    surrogateV2.withFormalCategory(mapTerm(surrogateV1.getFormalCategory(),
        KnowledgeAssetCategorySeries::resolveUUID, KnowledgeAssetCategory.class));
    surrogateV2.withFormalType(mapTerm(surrogateV1.getFormalType(),
        KnowledgeAssetTypeSeries::resolveUUID, KnowledgeAssetType.class));
    surrogateV2.withProcessingMethod(mapTerm(surrogateV1.getProcessingMethod(),
        KnowledgeProcessingTechniqueSeries::resolveUUID, KnowledgeProcessingTechnique.class));
    surrogateV2.withRole(mapTerm(surrogateV1.getRole(),
        KnowledgeAssetRoleSeries::resolveUUID, KnowledgeAssetRole.class));
    surrogateV2.withName(surrogateV1.getName());
    surrogateV2.withDescription(surrogateV1.getDescription());
    if (Optional.ofNullable(surrogateV1.getLifecycle()).isEmpty()) {
      throw new IllegalArgumentException("Source surrogate must have lifecycle - none found");
    }
    mapLifecycleToSurrogateV2(surrogateV1.getLifecycle(), surrogateV2);
    surrogateV1.getCitations().forEach(cit -> mapCitationToSurrogateV2(cit, surrogateV2));
    surrogateV1.getRelated().forEach(assoc -> mapRelatedItemToLinkList(assoc, surrogateV2));

    surrogateV1.getSubject().stream()
        .flatMap(filterAs(SimpleAnnotation.class))
        .forEach(ann -> mapAnnotationToSurrogate(ann, surrogateV2));
    surrogateV1.getCarriers().forEach(car -> mapCarrierToSurrogateV2(car, surrogateV2));

    // add to collection.
    assetCollection.add(surrogateV2);
  }

  private void mapToResourceId(KnowledgeAsset knowledgeAsset,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2) {
    if (knowledgeAsset.getAssetId() == null) {
      throw new IllegalArgumentException("Surrogates MUST have an asset ID - none found");
    }
    URIIdentifier oldAssetId = knowledgeAsset.getAssetId();
    ResourceIdentifier newAssetId = SemanticIdentifier.newVersionId(oldAssetId.getVersionId());
    surrogateV2.setAssetId(newAssetId);
  }


  private void mapLifecycleToSurrogateV2(Publication lifecycle,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2) {
    org.omg.spec.api4kp._20200801.surrogate.Publication newPublication = new org.omg.spec.api4kp._20200801.surrogate.Publication();
    //TODO: missing .withAssociatedTo(new Party().withPublishingRole(Author))
    newPublication.withPublicationStatus(mapTerm(lifecycle.getPublicationStatus(),
        PublicationStatusSeries::resolveUUID, PublicationStatus.class));
    surrogateV2.withLifecycle(newPublication);
  }

  private void mapCarrierToSurrogateV2(edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact car,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2) {

    if (car instanceof ComputableKnowledgeArtifact) {
      ComputableKnowledgeArtifact oldCarrier = (ComputableKnowledgeArtifact) car;
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact newCarrier = new org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact();

      newCarrier.withArtifactId(
          SemanticIdentifier.newVersionId(oldCarrier.getArtifactId().getUri()));
      newCarrier.withAlternativeTitle(oldCarrier.getAlternativeTitle());
      newCarrier.withLocalization(mapTerm(oldCarrier.getLocalization(), LanguageSeries::resolveUUID,
          Language.class));
      newCarrier.withExpressionCategory(mapTerm(oldCarrier.getExpressionCategory(),
          KnowledgeArtifactCategorySeries::resolveUUID, IKnowledgeArtifactCategory.class));
      newCarrier.withTitle(oldCarrier.getTitle());

      org.omg.spec.api4kp._20200801.surrogate.Summary newSummary = new org.omg.spec.api4kp._20200801.surrogate.Summary();
      newSummary.withRel(TermMapper.mapSummarization(oldCarrier.getSummary().getRel()));
      newCarrier.withSummary(newSummary);

      //TODO: Should be representation and syntacticRepresentation from V1 mapped to syntacticRepresentation on V2.
      Representation oldCarrierRepresentation = oldCarrier.getRepresentation();
      SyntacticRepresentation newRep = new SyntacticRepresentation();

      newRep.withLanguage(
          mapTerm(oldCarrierRepresentation.getLanguage(),
              KnowledgeRepresentationLanguageSeries::resolveUUID,
              KnowledgeRepresentationLanguage.class));
      newRep.withProfile(
          mapTerm(oldCarrierRepresentation.getProfile(),
              KnowledgeRepresentationLanguageProfileSeries::resolveUUID,
              KnowledgeRepresentationLanguageProfile.class));
      newRep.withFormat(
          mapTerm(oldCarrierRepresentation.getFormat(),
              SerializationFormatSeries::resolveUUID,
              SerializationFormat.class));
      newRep.withLexicon(
          mapTerm(oldCarrierRepresentation.getLexicon(),
              LexiconSeries::resolveUUID, Lexicon.class));
      newRep.withSerialization(
          mapTerm(oldCarrierRepresentation.getSerialization(),
              KnowledgeRepresentationLanguageSerializationSeries::resolveUUID,
              KnowledgeRepresentationLanguageSerialization.class));
      //sub language logic... I think...
      List<SyntacticRepresentation> subLanguages = new ArrayList<>();
      oldCarrierRepresentation.getWith()
          .forEach(sl -> subLanguages.add(new SyntacticRepresentation()
              .withRole(
                  mapTerm(sl.getRole(), KnowledgeRepresentationLanguageRoleSeries::resolveUUID,
                      KnowledgeRepresentationLanguageRole.class))));
      newRep.withSubLanguage(subLanguages);
      newCarrier.withRepresentation(newRep);
      surrogateV2.withCarriers(newCarrier);

      if (car.getInlined() != null) {
        newCarrier.setInlinedExpression(car.getInlined().getExpr());
      }
    } else {
      throw new UnsupportedOperationException(
          "Knowledge Artifact isn't a ComputableKnowledgeArtifact so no mapping at this level can be done.");
    }
  }

  // Should be more elegant, but the tangled type hierarchies confuse the resolution of the parametric types
  private <N, T> N mapTerm(T oldTerm, Function<UUID, ?> mapper, Class<N> ret) {
    return Optional.ofNullable(oldTerm)
        .map(ScopedIdentifier.class::cast)
        .map(ScopedIdentifier::getUuid)
        .map(mapper)
        .map(Optional.class::cast)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(ret::cast)
        .orElseThrow();
  }

  // Should be more elegant, but the tangled type hierarchies confuse the resolution of the parametric types
  private <N, T> Collection<N> mapTerm(Collection<T> oldTerm, Function<UUID, ?> mapper,
      Class<N> ret) {
    return oldTerm.stream()
        .map(ScopedIdentifier.class::cast)
        .map(ScopedIdentifier::getUuid)
        .map(mapper)
        .map(Optional.class::cast)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(ret::cast)
        .collect(Collectors.toList());
  }

  private void mapCitationToSurrogateV2(edu.mayo.kmdp.metadata.surrogate.Citation cit,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2) {
    surrogateV2.withCitations(
        new Citation().withBibliography(cit.getBibliography())
            .withRel(mapTerm(cit.getRel(), BibliographicCitationTypeSeries::resolveUUID,
                BibliographicCitationType.class).asConceptIdentifier()));
  }

  private void mapAnnotationToSurrogate(SimpleAnnotation ann,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2) {
    org.omg.spec.api4kp._20200801.surrogate.Annotation newAnnotation = new org.omg.spec.api4kp._20200801.surrogate.Annotation();
    newAnnotation.withRel(fromLegacyConceptIdentifier(ann.getRel()));
    newAnnotation.withRef(fromLegacyConceptIdentifier(ann.getExpr()));
    surrogateV2.withAnnotation(newAnnotation);
  }

  private ResourceIdentifier getResourceIdFromOldAssociation(KnowledgeResource resource) {
    if (resource instanceof edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset) {
      edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset targetAsset =
          (edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset) resource;
      URIIdentifier oldAssetId = targetAsset.getAssetId();
      return SemanticIdentifier.newVersionId(oldAssetId.getVersionId());
    } else {
      throw new IllegalStateException("Knowledge Resource is not a Knowledge Asset.");
    }
  }

  private void mapRelatedItemToLinkList(Association assoc,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2) {

    // create the new ResourceId
    ResourceIdentifier resourceId = getResourceIdFromOldAssociation(assoc.getTgt());

    KnowledgeResource resource = assoc.getTgt();
    if (isFullAssetResource(resource)) {
      this.transformKnowledgeAsset((edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset) resource);
    }

    if (assoc instanceof Derivative) {
      //need to create the new knowledge asset and add to list.
      org.omg.spec.api4kp._20200801.surrogate.Derivative newDerivative
          = new org.omg.spec.api4kp._20200801.surrogate.Derivative().withHref(resourceId);
      newDerivative.setRel(TermMapper.mapDerivative(((Derivative) assoc).getRel()));
      surrogateV2.getLinks().add(newDerivative);
    } else if (assoc instanceof Version) {
      org.omg.spec.api4kp._20200801.surrogate.Version newVersionLink
          = new org.omg.spec.api4kp._20200801.surrogate.Version().withHref(resourceId);
      newVersionLink.setRel(TermMapper.mapRelatedVersion(((Version) assoc).getRel()));
      surrogateV2.getLinks().add(newVersionLink);
    } else if (assoc instanceof Dependency) {
      org.omg.spec.api4kp._20200801.surrogate.Dependency dependencyLink
          = new org.omg.spec.api4kp._20200801.surrogate.Dependency().withHref(resourceId);
      dependencyLink.setRel(TermMapper.mapDependency(((Dependency) assoc).getRel()));
      surrogateV2.getLinks().add(dependencyLink);
    } else if (assoc instanceof Component) {
      org.omg.spec.api4kp._20200801.surrogate.Component dependencyLink
          = new org.omg.spec.api4kp._20200801.surrogate.Component().withHref(resourceId);
      dependencyLink.setRel(TermMapper.mapComponentRels(((Component) assoc).getRel()));
      surrogateV2.getLinks().add(dependencyLink);
    } else if (assoc instanceof Variant) {
      org.omg.spec.api4kp._20200801.surrogate.Variant variantLink =
          new org.omg.spec.api4kp._20200801.surrogate.Variant().withHref(resourceId);
      variantLink.setRel(TermMapper.mapVariant(((Variant) assoc).getRel()));
      surrogateV2.getLinks().add(variantLink);
    }
  }

  private boolean isFullAssetResource(KnowledgeResource resource) {
    if (!(resource instanceof KnowledgeAsset)) {
      return false;
    }
    KnowledgeAsset axx = (KnowledgeAsset) resource;
    return !axx.getFormalCategory().isEmpty()
        || !axx.getRelated().isEmpty()
        || !axx.getCarriers().isEmpty();
  }

  public static org.omg.spec.api4kp._20200801.id.ConceptIdentifier fromLegacyConceptIdentifier(
      ConceptIdentifier v) {
    if (v == null) {
      return null;
    }
    return new org.omg.spec.api4kp._20200801.id.ConceptIdentifier()
        .withUuid(v.getConceptUUID())
        .withResourceId(v.getConceptId())
        .withTag(v.getTag())
        .withNamespaceUri(v.getNamespace().getId())
        .withName(v.getLabel())
        .withReferentId(v.getRef());
  }
}
