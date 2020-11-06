package edu.mayo.kmdp.language.translators.surrogate.v2;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelType;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.krprofile._20190801.KnowledgeRepresentationLanguageProfile;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.surrogate.Applicability;
import org.omg.spec.api4kp._20200801.surrogate.Component;
import org.omg.spec.api4kp._20200801.surrogate.Dependency;
import org.omg.spec.api4kp._20200801.surrogate.Derivative;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.Link;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.surrogate.Version;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.snapshot.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;


public class SurrogateV2ToLibrary {
  public Library transform(KnowledgeAsset knowledgeAsset) {
    throw new UnsupportedOperationException();
  }

//  //TODO: map surrogate v2 in a similar manner to surrogate v1 into the library object.
//  //Consolidate like mappings from the SurrogateToLibrary class and this one together to cut duplication.
//  //not needed currently.
//  public Library transform(KnowledgeAsset knowledgeAsset) {
//    Library lib = new Library();
//
//    mapSurrogateId(knowledgeAsset, lib);
//    mapIdentifier(knowledgeAsset, lib);
//    mapDescriptive(knowledgeAsset, lib);
//
//    mapType(knowledgeAsset, lib);
//
//    mapPublicationStatus(knowledgeAsset, lib);
//    mapLifecycle(knowledgeAsset, lib);
//
//    // FHIR and KMDP deal with applicability / domain semantics
//    // in a similar but not directly compatible way.
//    // Differences in granularity and pre/post-coordination make the mapping complex
//    mapIntentAndApplicability(knowledgeAsset, lib);
//    mapUseContext(knowledgeAsset, lib);
//
//    mapPolicies(knowledgeAsset, lib);
//
//    mapTopics(knowledgeAsset, lib);
//
//    mapContributors(knowledgeAsset, lib);
//
//    mapRelateds(knowledgeAsset, lib);
//    mapBiblio(knowledgeAsset, lib);
//
//    // in KMDP data dependencies are 'semantic', i.e. defined just in terms of a concept
//    // The service "Concept Glossary Library' allows to map concepts to data definitions,
//    // including FHIR profiles, etc..
//    // This 'weaving' happens at a later stage, after the Library has been translated into
//    mapDataRequirements(knowledgeAsset, lib);
//
//    // For each artifact, map it..
//    knowledgeAsset.getCarriers().stream()
//        .flatMap(StreamUtil.filterAs(KnowledgeArtifact.class))
//        .forEach(cka -> lib.addContent(mapKnowledgeArtifact(cka)));
//
//    return lib;
//  }
//
//  private Attachment mapKnowledgeArtifact(KnowledgeArtifact cka) {
//    Attachment artifact = new Attachment();
//
//    // (Version) URI are supposed to be dereferenceable
//    artifact.setUrl(cka.getArtifactId().getVersionId().toString());
//
//    if (!cka.getLocalization().isEmpty()) {
//      artifact.setLanguage(cka.getLocalization().get(0).getTag());
//    }
//
//    artifact.setTitle(cka.getTitle());
//
//    if (cka.getRepresentation() != null) {
//      SyntacticRepresentation r = cka.getRepresentation();
//      artifact.setContentType(ModelMIMECoder.encode(
//          rep(
//              mapLanguage(r.getLanguage()),
//              mapProfile(r.getProfile()),
//              mapSerialization(r.getSerialization()),
//              mapFormat(r.getFormat()),
//              null,
//              null,
//              mapLexicon(r.getLexicon())
//          )));
//    }
//
//    if (cka.getLifecycle() != null) {
//      artifact.setCreation(cka.getLifecycle().getCreatedOn());
//    }
//
//    if (cka.getInlined() != null) {
//      artifact.setData(cka.getInlined().getExpr().getBytes());
//    }
//
//    return artifact;
//  }
//
//  private KnowledgeRepresentationLanguage mapLanguage(
//      KnowledgeRepresentationLanguage language) {
//    if (language == null) {
//      return null;
//    }
//    return KnowledgeRepresentationLanguageSeries
//        .resolveUUID(language.getUuid()).orElse(null);
//  }
//
//  private org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile mapProfile(
//      KnowledgeRepresentationLanguageProfile profile) {
//    if (profile == null) {
//      return null;
//    }
//    return org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries
//        .resolveUUID(profile.getUuid()).orElse(null);
//  }
//
//  private org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization mapSerialization(
//      KnowledgeRepresentationLanguageSerialization serialization) {
//    if (serialization == null) {
//      return null;
//    }
//    return org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries
//        .resolveUUID(serialization.getUuid()).orElse(null);
//  }
//
//  private org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat mapFormat(
//      SerializationFormat format) {
//    if (format == null) {
//      return null;
//    }
//    return org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries
//        .resolveUUID(format.getUuid()).orElse(null);
//  }
//
//  private List<Lexicon> mapLexicon(
//      Collection<edu.mayo.ontology.taxonomies.lexicon.Lexicon> lexicaList) {
//    if (lexicaList == null || lexicaList.isEmpty()) {
//      return Collections.emptyList();
//    }
//    return lexicaList.stream()
//        .map(lexicon -> org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries
//            .resolveUUID(lexicon.getUuid()).orElse(null))
//        .filter(Objects::nonNull)
//        .collect(Collectors.toList());
//  }
//
//
//  private void mapDataRequirements(KnowledgeAsset knowledgeAsset, Library lib) {
//    knowledgeAsset.getSubject().stream()
//        .flatMap(StreamUtil.filterAs(SimpleAnnotation.class))
//        .forEach(ann -> mapInputAnnotations(ann, lib));
//  }
//
//  private void mapInputAnnotations(SimpleAnnotation ann, Library lib) {
//    Optional<AnnotationRelType> rel = AnnotationRelTypeSeries
//        .resolveUUID(ann.getRel().getConceptUUID());
//    if (rel.isPresent() &&
//        AnnotationRelTypeSeries.In_Terms_Of.sameAs(rel.get().asEnum())) {
//      lib.getDataRequirement().add(
//          new DataRequirement()
//              .setCodeFilter(Collections.singletonList(
//                  new DataRequirementCodeFilterComponent()
//                      .addValueCodeableConcept(toCode(ann.getExpr()))
//              ))
//      );
//    }
//  }
//
//  private void mapBiblio(KnowledgeAsset knowledgeAsset, Library lib) {
//    knowledgeAsset.getCitations()
//        .forEach(cit -> lib.getRelatedArtifact().add(
//            new RelatedArtifact()
//                .setType(RelatedArtifactType.CITATION)
//                .setCitation(cit.getBibliography())
//        ));
//  }
//
//  private void mapRelateds(KnowledgeAsset knowledgeAsset, Library lib) {
//    knowledgeAsset.getRelated().stream()
//        .filter(assoc -> assoc.getTgt() instanceof KnowledgeAsset) // simplified in v2
//        .forEach(assoc -> lib.getRelatedArtifact().add(
//            new RelatedArtifact()
//                .setType(mapRelatedType(assoc))
//                .setDisplay(assoc.getTgt().getName())
//                .setUrl(((KnowledgeAsset) assoc.getTgt()).getAssetId().getVersionId().toString())
//        ));
//  }
//
//  private RelatedArtifactType mapRelatedType(Link assoc) {
//    // KMDP Relationship will be simplified in v2
//    if (assoc instanceof Dependency) {
//      return RelatedArtifactType.DEPENDSON;
//    }
//    if (assoc instanceof Derivative) {
//      return RelatedArtifactType.DERIVEDFROM;
//    }
//    if (assoc instanceof Component) {
//      return RelatedArtifactType.COMPOSEDOF;
//    }
//    if (assoc instanceof Version) {
//      Version v = (Version) assoc;
//      if (RelatedVersionTypeSeries.Has_Prior_Version.getUuid().equals(v.getRel().getUuid())
//          || RelatedVersionTypeSeries.Has_Previous_Version.getUuid().equals(v.getRel().getUuid())
//          || RelatedVersionTypeSeries.Has_Original.getUuid().equals(v.getRel().getUuid())
//      ) {
//        return RelatedArtifactType.SUCCESSOR;
//      } else {
//        return RelatedArtifactType.PREDECESSOR;
//      }
//    }
//    return RelatedArtifactType.DOCUMENTATION;
//  }
//
//  private void mapContributors(KnowledgeAsset knowledgeAsset, Library lib) {
//    Publication pub = knowledgeAsset.getLifecycle();
//
//    // name or reference?
//    // what about all the other various roles?
//    pub.getAssociatedTo().stream()
//        .filter(party -> PublishingRoleSeries.Contributor.getUuid()
//            .equals(party.getPublishingRole().getUuid()))
//        .forEach(contri -> lib.getContributor().add(
//            new Contributor()
//                .setName(contri.getIdentifier().getUri().toString())));
//  }
//
//  private void mapTopics(KnowledgeAsset knowledgeAsset, Library lib) {
//    knowledgeAsset.getSubject().stream()
//        .flatMap(StreamUtil.filterAs(Annotation.class))
//        .forEach(ann -> mapSubjectAnnotation(ann, lib));
//  }
//
//  private void mapSubjectAnnotation(Annotation ann, Library lib) {
//    Optional<SemanticAnnotationRelType> rel = SemanticAnnotationRelTypeSeries
//        .resolveUUID(ann.getRel().getUuid());
//    if (rel.isPresent()) {
//      switch (rel.get().asEnum()) {
//        case Has_Primary_Subject:
//        case Secondary_Subject:
//        case Has_Focus:
//          lib.getTopic().add(toCode(ann.getExpr()));
//          break;
//        default:
//      }
//    }
//  }
//
//  private void mapPolicies(KnowledgeAsset knowledgeAsset, Library lib) {
//    // KMDP does not yet implement policies in general, nor jurisdictions in particular
//    lib.setCopyright("TODO");
//  }
//
//  private void mapUseContext(KnowledgeAsset knowledgeAsset, Library lib) {
//    knowledgeAsset.getSubject().stream()
//        .flatMap(StreamUtil.filterAs(Annotation.class))
//        .forEach(ann -> mapUsageAnnotation(ann, lib));
//  }
//
//  private void mapUsageAnnotation(Annotation ann, Library lib) {
//    Optional<SemanticAnnotationRelType> rel = SemanticAnnotationRelTypeSeries
//        .resolveUUID(ann.getRel().getUuid());
//    if (rel.isPresent()) {
//      // Mayo has some annotations for 'Usage Context', but they are not yet implemented in KMDP
//    }
//    lib.setUseContext(Collections.emptyList());
//  }
//
//  private void mapIntentAndApplicability(
//      KnowledgeAsset knowledgeAsset, Library lib) {
//    // applicability is less formal in FHIR
//    if (knowledgeAsset.getApplicableIn() != null) {
//      Applicability app = knowledgeAsset.getApplicableIn();
//      if (app.getSituation() != null) {
//        lib.setUsage(app.getSituation().stream().map(ResourceIdentifier::getTag).collect(Collectors.joining(",")));
//      }
//    }
//
//    // but purpose is not modelled yet
//    lib.setPurpose("TODO");
//  }
//
//  private void mapLifecycle(KnowledgeAsset knowledgeAsset, Library lib) {
//    Publication pub = knowledgeAsset.getLifecycle();
//    // Assuming Knowledge Resources are immutable, and that the 'createdon' refers to the latest version
//    lib.setDate(pub.getCreatedOn());
//
//    // by name or identifier?
//    pub.getAssociatedTo().stream()
//        .filter(party -> PublishingRoleSeries.Publisher.sameAs()party.getPublishingRole().getUuid()))
//        .findFirst()
//        .ifPresent(publisher -> lib.setPublisher(publisher.getIdentifier().getUri().toString()));
//
//    // Are these the right mapping?
//    if (pub.getIssuedOn() != null) {
//      lib.setApprovalDate(pub.getIssuedOn());
//    }
//    if (pub.getLastReviewedOn() != null) {
//      lib.setLastReviewDate(pub.getLastReviewedOn());
//    }
//  }
//
//  private void mapType(KnowledgeAsset knowledgeAsset, Library lib) {
//    if (knowledgeAsset.getFormalType().isEmpty()) {
//      return;
//    }
//    // No more than one type is supported..
//    KnowledgeAssetType type0 = knowledgeAsset.getFormalType().get(0);
//    lib.setType(toCode(type0.asConcept()));
//  }
//
//
//  private void mapPublicationStatus(KnowledgeAsset knowledgeAsset, Library lib) {
//    lib.setExperimental(false);
//
//    // Need a more precise mapping of publication statuses
//    if (knowledgeAsset.getLifecycle() == null
//        || knowledgeAsset.getLifecycle().getPublicationStatus() == null) {
//      lib.setStatus(PublicationStatus.NULL);
//      return;
//    }
//    switch (knowledgeAsset.getLifecycle().getPublicationStatus().asEnum()) {
//      case Published:
//        lib.setStatus(PublicationStatus.ACTIVE);
//        break;
//      case Draft:
//        lib.setStatus(PublicationStatus.DRAFT);
//        break;
//      case Archived:
//        lib.setStatus(PublicationStatus.RETIRED);
//        break;
//      default:
//        lib.setStatus(PublicationStatus.UNKNOWN);
//    }
//  }
//
//  private void mapDescriptive(KnowledgeAsset knowledgeAsset, Library lib) {
//    lib.setName(knowledgeAsset.getName());
//    lib.setDescription(knowledgeAsset.getDescription());
//    lib.setTitle(knowledgeAsset.getName());
//  }
//
//  private void mapIdentifier(KnowledgeAsset knowledgeAsset, Library lib) {
//    if (knowledgeAsset.getAssetId() == null) {
//      throw new IllegalArgumentException("Surrogates MUST have an asset ID - none found");
//    }
//    ResourceIdentifier vid = knowledgeAsset.getAssetId();
//    Identifier assetId = new Identifier()
//        .setValue(getTag(vid))
//        .setSystem(Registry.MAYO_ASSETS_BASE_URI)
//        .setPeriod(new Period().setStart(knowledgeAsset.getAssetId().getEstablishedOn()))
//        .setUse(IdentifierUse.OFFICIAL);
//
//    lib.setIdentifier(Collections.singletonList(assetId));
//    lib.setVersion(getVersionTag(vid));
//  }
//
//  private void mapSurrogateId(KnowledgeAsset knowledgeAsset, Library lib) {
//    if (knowledgeAsset.getSurrogate().isEmpty()) {
//      return;
//    }
//    KnowledgeArtifact surrogate = knowledgeAsset.getSurrogate().get(0);
//    if (surrogate != null) {
//      lib.setId(getTag(surrogate.getArtifactId()) + ":" + getVersionTag(surrogate.getArtifactId()));
//      lib.setUrl(surrogate.getArtifactId().getVersionId().toString());
//    }
//  }
//
//
//  private CodeableConcept toCode(ConceptIdentifier cid) {
//    return new CodeableConcept()
//        .setCoding(Collections.singletonList(
//            new Coding()
//                .setCode(cid.getTag())
//                .setDisplay(cid.getLabel())
//                .setSystem(cid.getNamespace() != null
//                    ? cid.getNamespace().getId().toString()
//                    : null)
//                .setVersion(cid.getNamespace() != null
//                    ? cid.getNamespace().getVersion()
//                    : null)));
//  }
//
//
//  public static ConceptIdentifier toLegacyConceptIdentifier(
//      org.omg.spec.api4kp._20200801.id.ConceptIdentifier cId) {
//
//    org.omg.spec.api4kp._20200801.id.ResourceIdentifier nsId = org.omg.spec.api4kp._20200801.id.SemanticIdentifier
//        .newNamespaceId(cId.getNamespaceUri());
//
//    return new ConceptIdentifier()
//        .withConceptId(cId.getResourceId())
//        .withTag(cId.getTag())
//        .withLabel(cId.getName())
//        .withRef(cId.getReferentId())
//        .withConceptUUID(cId.getUuid())
//        .withNamespace(
//            new NamespaceIdentifier()
//                .withId(nsId.getResourceId())
//                .withLabel(nsId.getName())
//                .withTag(nsId.getTag())
//                .withVersion(cId.getVersionTag())
//                .withEstablishedOn(nsId.getEstablishedOn())
//        );
//  }
//
//
//  private static String getTag(ResourceIdentifier id) {
//    return URIUtil.detectLocalName(id.getResourceId());
//  }
//
//  private static String getVersionTag(ResourceIdentifier id) {
//    return DatatypeHelper.toVersionIdentifier(id.getVersionId()).getVersionTag();
//  }
//
}
