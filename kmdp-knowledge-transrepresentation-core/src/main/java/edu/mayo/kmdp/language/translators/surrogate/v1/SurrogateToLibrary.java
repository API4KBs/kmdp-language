package edu.mayo.kmdp.language.translators.surrogate.v1;

import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.SurrogateHelper;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.annotations.SimpleApplicability;
import edu.mayo.kmdp.metadata.surrogate.Association;
import edu.mayo.kmdp.metadata.surrogate.Component;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Publication;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.Version;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelType;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import java.util.Collections;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Contributor;
import org.hl7.fhir.dstu3.model.DataRequirement;
import org.hl7.fhir.dstu3.model.DataRequirement.DataRequirementCodeFilterComponent;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Identifier.IdentifierUse;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.RelatedArtifact.RelatedArtifactType;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;

public class SurrogateToLibrary {


  public Library transform(KnowledgeAsset knowledgeAsset) {
    Library lib = new Library();

    mapSurrogateId(knowledgeAsset,lib);
    mapIdentifier(knowledgeAsset,lib);
    mapDescriptive(knowledgeAsset,lib);

    mapType(knowledgeAsset,lib);

    mapPublicationStatus(knowledgeAsset,lib);
    mapLifecycle(knowledgeAsset,lib);

    // FHIR and KMDP deal with applicability / domain semantics
    // in a similar but not directly compatible way.
    // Differences in granularity and pre/post-coordination make the mapping complex
    mapIntentAndApplicability(knowledgeAsset, lib);
    mapUseContext(knowledgeAsset, lib);

    mapPolicies(knowledgeAsset,lib);

    mapTopics(knowledgeAsset, lib);

    mapContributors(knowledgeAsset, lib);

    mapRelateds(knowledgeAsset, lib);
    mapBiblio(knowledgeAsset,lib);

    // in KMDP data dependencies are 'semantic', i.e. defined just in terms of a concept
    // The service "Concept Glossary Library' allows to map concepts to data definitions,
    // including FHIR profiles, etc..
    // This 'weaving' happens at a later stage, after the Library has been translated into
    mapDataRequirements(knowledgeAsset,lib);

    // For each artifact, map it..
    knowledgeAsset.getCarriers().stream()
        .flatMap(StreamUtil.filterAs(ComputableKnowledgeArtifact.class))
        .forEach(cka -> lib.addContent(mapKnowledgeArtifact(cka)));

    return lib;
  }

  private Attachment mapKnowledgeArtifact(ComputableKnowledgeArtifact cka) {
    Attachment artifact = new Attachment();

    // (Version) URI are supposed to be dereferenceable
    artifact.setUrl(cka.getArtifactId().getVersionId().toString());

    if (! cka.getLocalization().isEmpty()) {
      artifact.setLanguage(cka.getLocalization().get(0).getTag());
    }

    artifact.setTitle(cka.getTitle());

    if (cka.getRepresentation() != null) {
      Representation r = cka.getRepresentation();
      artifact.setContentType(ModelMIMECoder.encode(
          rep(r.getLanguage(), r.getProfile(), r.getSerialization(), r.getFormat(),
              null, null, r.getLexicon())));
    }

    if (cka.getLifecycle() != null) {
      artifact.setCreation(cka.getLifecycle().getCreatedOn());
    }

    if (cka.getInlined() != null) {
      artifact.setData(cka.getInlined().getExpr().getBytes());
    }

    return artifact;
  }

  private void mapDataRequirements(KnowledgeAsset knowledgeAsset, Library lib) {
    knowledgeAsset.getSubject().stream()
        .flatMap(StreamUtil.filterAs(SimpleAnnotation.class))
        .forEach(ann -> mapInputAnnotations(ann,lib));
  }

  private void mapInputAnnotations(SimpleAnnotation ann, Library lib) {
    Optional<AnnotationRelType> rel = AnnotationRelTypeSeries
        .resolveUUID(ann.getRel().getConceptUUID());
    if (rel.isPresent() &&
        AnnotationRelTypeSeries.In_Terms_Of.sameAs(rel.get().asEnum())) {
      lib.getDataRequirement().add(
          new DataRequirement()
              .setCodeFilter(Collections.singletonList(
                  new DataRequirementCodeFilterComponent()
                      .addValueCodeableConcept(toCode(ann.getExpr()))
              ))
      );
    }
  }

  private void mapBiblio(KnowledgeAsset knowledgeAsset, Library lib) {
    knowledgeAsset.getCitations()
        .forEach(cit -> lib.getRelatedArtifact().add(
            new RelatedArtifact()
                .setType(RelatedArtifactType.CITATION)
                .setCitation(cit.getBibliography())
        ));
  }

  private void mapRelateds(KnowledgeAsset knowledgeAsset, Library lib) {
    knowledgeAsset.getRelated().stream()
        .filter(assoc -> assoc.getTgt() instanceof KnowledgeAsset) // simplified in v2
        .forEach(assoc -> lib.getRelatedArtifact().add(
            new RelatedArtifact()
                .setType(mapRelatedType(assoc))
                .setDisplay(assoc.getTgt().getName())
                .setUrl(((KnowledgeAsset) assoc.getTgt()).getAssetId().getVersionId().toString())
        ));
  }

  private RelatedArtifactType mapRelatedType(Association assoc) {
    // KMDP Relationship will be simplified in v2
    if (assoc instanceof Dependency) {
      return RelatedArtifactType.DEPENDSON;
    }
    if (assoc instanceof Derivative) {
      return RelatedArtifactType.DERIVEDFROM;
    }
    if (assoc instanceof Component) {
      return RelatedArtifactType.COMPOSEDOF;
    }
    if (assoc instanceof Version) {
      Version v = (Version) assoc;
      if (RelatedVersionTypeSeries.Has_Prior_Version.sameAs(v.getRel())
          || RelatedVersionTypeSeries.Has_Previous_Version.sameAs(v.getRel())
          || RelatedVersionTypeSeries.Has_Original.sameAs(v.getRel())
      ) {
        return RelatedArtifactType.SUCCESSOR;
      } else {
        return RelatedArtifactType.PREDECESSOR;
      }
    }
    return RelatedArtifactType.DOCUMENTATION;
  }

  private void mapContributors(KnowledgeAsset knowledgeAsset, Library lib) {
    Publication pub = knowledgeAsset.getLifecycle();

    // name or reference?
    // what about all the other various roles?
    pub.getAssociatedTo().stream()
        .filter(party -> PublishingRoleSeries.Contributor.sameAs(party.getPublishingRole()))
        .forEach(contri -> lib.getContributor().add(
            new Contributor()
                .setName(contri.getIdentifier().getUri().toString())));
  }

  private void mapTopics(KnowledgeAsset knowledgeAsset, Library lib) {
    knowledgeAsset.getSubject().stream()
        .flatMap(StreamUtil.filterAs(SimpleAnnotation.class))
        .forEach(ann -> mapSubjectAnnotation(ann,lib));
  }

  private void mapSubjectAnnotation(SimpleAnnotation ann, Library lib) {
    Optional<AnnotationRelType> rel = AnnotationRelTypeSeries.resolveUUID(ann.getRel().getConceptUUID());
    if (rel.isPresent()) {
      switch (rel.get().asEnum()) {
        case Has_Primary_Subject:
        case Secondary_Subject:
        case Has_Focus:
          lib.getTopic().add(toCode(ann.getExpr()));
          break;
        default:
      }
    }
  }

  private void mapPolicies(KnowledgeAsset knowledgeAsset, Library lib) {
    // KMDP does not yet implement policies in general, nor jurisdictions in particular
    knowledgeAsset.getPolicies();
    lib.setJurisdiction(null);
    lib.setCopyright("TODO");
  }

  private void mapUseContext(KnowledgeAsset knowledgeAsset, Library lib) {
    knowledgeAsset.getSubject().stream()
        .flatMap(StreamUtil.filterAs(SimpleAnnotation.class))
        .forEach(ann -> mapUsageAnnotation(ann,lib));
  }

  private void mapUsageAnnotation(SimpleAnnotation ann, Library lib) {
    Optional<AnnotationRelType> rel = AnnotationRelTypeSeries.resolveUUID(ann.getRel().getConceptUUID());
    if (rel.isPresent()) {
      // Mayo has some annotations for 'Usage Context', but they are not yet implemented in KMDP
    }
    lib.setUseContext(Collections.emptyList());
  }

  private void mapIntentAndApplicability(KnowledgeAsset knowledgeAsset, Library lib) {
    // applicability is less formal in FHIR
    if (knowledgeAsset.getApplicableIn() instanceof SimpleApplicability) {
      SimpleApplicability app = (SimpleApplicability) knowledgeAsset.getApplicableIn();
      if (app.getSituation() != null) {
        lib.setUsage(app.getSituation().getLabel());
      }
    }

    // but purpose is not modelled yet
    lib.setPurpose("TODO");
  }

  private void mapLifecycle(KnowledgeAsset knowledgeAsset, Library lib) {
    Publication pub = knowledgeAsset.getLifecycle();
    // Assuming Knowledge Resources are immutable, and that the 'createdon' refers to the latest version
    lib.setDate(pub.getCreatedOn());

    // by name or identifier?
    pub.getAssociatedTo().stream()
        .filter(party -> PublishingRoleSeries.Publisher.sameAs(party.getPublishingRole()))
        .findFirst()
        .ifPresent(publisher -> lib.setPublisher(publisher.getIdentifier().getUri().toString()));

    // Are these the right mapping?
    if (pub.getIssuedOn() != null) {
      lib.setApprovalDate(pub.getIssuedOn().toGregorianCalendar().getTime());
    }
    if (pub.getLastReviewedOn() != null) {
      lib.setLastReviewDate(pub.getLastReviewedOn().toGregorianCalendar().getTime());
    }
  }

  private void mapType(KnowledgeAsset knowledgeAsset, Library lib) {
    if (knowledgeAsset.getFormalType().isEmpty()) {
      return;
    }
    // No more than one type is supported..
    lib.setType(toCode(
        SurrogateHelper.toLegacyConceptIdentifier(knowledgeAsset.getFormalType().get(0))));
  }


  private void mapPublicationStatus(KnowledgeAsset knowledgeAsset, Library lib) {
    lib.setExperimental(false);

    // Need a more precise mapping of publication statuses
    if (knowledgeAsset.getLifecycle() == null
        || knowledgeAsset.getLifecycle().getPublicationStatus() == null) {
      lib.setStatus(PublicationStatus.NULL);
      return;
    }
    switch (knowledgeAsset.getLifecycle().getPublicationStatus().asEnum()) {
      case Published:
        lib.setStatus(PublicationStatus.ACTIVE);
        break;
      case Draft:
        lib.setStatus(PublicationStatus.DRAFT);
        break;
      case Archived:
        lib.setStatus(PublicationStatus.RETIRED);
        break;
      default:
        lib.setStatus(PublicationStatus.UNKNOWN);
    }
  }

  private void mapDescriptive(KnowledgeAsset knowledgeAsset, Library lib) {
    lib.setName(knowledgeAsset.getName());
    lib.setDescription(knowledgeAsset.getDescription());
    lib.setTitle(knowledgeAsset.getTitle());
  }

  private void mapIdentifier(KnowledgeAsset knowledgeAsset, Library lib) {
    if (knowledgeAsset.getAssetId() == null) {
      throw new IllegalArgumentException("Surrogates MUST have an asset ID - none found");
    }
    URIIdentifier vid = knowledgeAsset.getAssetId();
    Identifier assetId = new Identifier()
        .setValue(SurrogateHelper.getTag(vid))
        .setSystem(Registry.MAYO_ASSETS_BASE_URI)
        .setPeriod(new Period().setStart(knowledgeAsset.getAssetId().getEstablishedOn()))
        .setUse(IdentifierUse.OFFICIAL);

    lib.setIdentifier(Collections.singletonList(assetId));
    lib.setVersion(SurrogateHelper.getVersionTag(vid));
  }

  private void mapSurrogateId(KnowledgeAsset knowledgeAsset, Library lib) {
    if (knowledgeAsset.getSurrogate().isEmpty()) {
      return;
    }
    KnowledgeArtifact surrogate = knowledgeAsset.getSurrogate().get(0);
    if (surrogate != null) {
      lib.setId(SurrogateHelper.getTag(surrogate.getArtifactId()) + ":" + SurrogateHelper.getVersionTag(surrogate.getArtifactId()));
      lib.setUrl(surrogate.getArtifactId().getVersionId().toString());
    }
  }


  private CodeableConcept toCode(ConceptIdentifier cid) {
    return new CodeableConcept()
        .setCoding(Collections.singletonList(
            new Coding()
                .setCode(cid.getTag())
                .setDisplay(cid.getLabel())
                .setSystem(cid.getNamespace() != null
                    ? ((NamespaceIdentifier)cid.getNamespace()).getId().toString()
                    : null)
                .setVersion(cid.getNamespace() != null
                    ? ((NamespaceIdentifier)cid.getNamespace()).getVersion()
                    : null)));
  }


}
