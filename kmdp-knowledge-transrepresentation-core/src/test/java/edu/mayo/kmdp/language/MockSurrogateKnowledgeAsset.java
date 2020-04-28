package edu.mayo.kmdp.language;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.*;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes.LanguageSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.Term;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;
import static edu.mayo.ontology.taxonomies.kao.knowledgeartifactcategory.KnowledgeArtifactCategorySeries.Software;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries.Operational_Concept_Definition;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries.Logic_Based_Technique;
import static edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries.Schema_Language;
import static edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries.Published;
import static edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRoleSeries.Author;
import static edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationTypeSeries.Cites_As_Authority;
import static edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries.Depends_On;
import static edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries.Inspired_By;
import static edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries.Has_Original;
import static edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartTypeSeries.Has_Part;
import static edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationTypeSeries.Compact_Representation_Of;
import static edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantTypeSeries.Adaptation_Of;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;

public class MockSurrogateKnowledgeAsset {

  public KnowledgeAsset buildMetadata() {
    return new KnowledgeAsset()
        .withAssetId(uri(BASE_UUID_URN + "0c36a4a3-7645-4276-baf5-be957112717b", "142412"))
        .withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Clinical_Rule)
        .withProcessingMethod(Logic_Based_Technique)
        .withRole(Operational_Concept_Definition)
        .withName("Diabetes Treatment Plan")
        .withDescription("Description of Diabetes...")
        .withSubject(new SimpleAnnotation().withExpr(
            DatatypeHelper.toConceptIdentifier(Term.mock("mock", "12345").asConceptIdentifier()))
            .withRel(
                AnnotationRelTypeSeries.Has_Focus.asConcept()))
        .withRelated(
            new Component()
                .withRel(Has_Part)
                .withTgt(ref(UUID.randomUUID(), "1")))
        .withRelated(
            new Derivative().withRel(Inspired_By)
                .withTgt(ref(UUID.randomUUID(), "99").withName("Sub Assett Test")))
        .withRelated(
            new Derivative().withRel(Inspired_By)
                .withTgt(ref(UUID.randomUUID(), "2")))
        .withRelated(
            new Dependency().withRel(Depends_On)
                .withTgt(ref(UUID.randomUUID(), "3")))
        .withRelated(
            new Variant().withRel(Adaptation_Of)
                .withTgt(ref(UUID.randomUUID(), "4")))
        .withRelated(
            new Version().withRel(Has_Original)
                .withTgt(ref(UUID.randomUUID(), "5")))
        .withCitations(
            new Citation().withRel(Cites_As_Authority)
                .withBibliography("F.F. Very Important Paper."))
        .withLifecycle(
            new Publication()
                .withPublicationStatus(Published)
                .withAssociatedTo(new Party().withPublishingRole(Author))
        )
        .withCarriers(
            new ComputableKnowledgeArtifact()
                .withArtifactId(uri(BASE_UUID_URN + "f2b9828d-f84c-4d09-9c88-413c7f1439a4", "000"))
                .withLocalization(LanguageSeries.Italian)
                .withExpressionCategory(Software)
                .withTitle("A mock example")
                .withSummary(
                    new Summary().withRel(Compact_Representation_Of))
                .withRepresentation(new Representation()
                    .withLanguage(DMN_1_1)
                    .withProfile(CQL_Essentials)
                    .withFormat(TXT)
                    .withLexicon(SNOMED_CT)
                    .withSerialization(DMN_1_1_XML_Syntax)
                    .withWith(
                        new SubLanguage().withRole(Schema_Language))
                )
        )
        .withSurrogate(
            new ComputableKnowledgeArtifact()
                .withArtifactId(
                    uri(BASE_UUID_URN + "a42e2cef-be7f-4aab-82ba-fe6f12495e3f", "65464"))
                .withRepresentation(new Representation()
                    .withLanguage(Knowledge_Asset_Surrogate)
                )
        );
  }

  private KnowledgeResource ref(UUID randomUUID, String version) {
    return new KnowledgeAsset()
        .withAssetId(DatatypeHelper.uri(BASE_UUID_URN, randomUUID.toString(), version));
  }
}
