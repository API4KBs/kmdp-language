package edu.mayo.kmdp.language;

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
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.encode;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateToLibraryTranslator;
import edu.mayo.kmdp.metadata.surrogate.Citation;
import edu.mayo.kmdp.metadata.surrogate.Component;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeResource;
import edu.mayo.kmdp.metadata.surrogate.Party;
import edu.mayo.kmdp.metadata.surrogate.Publication;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.SubLanguage;
import edu.mayo.kmdp.metadata.surrogate.Summary;
import edu.mayo.kmdp.metadata.surrogate.Variant;
import edu.mayo.kmdp.metadata.surrogate.Version;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes.LanguageSeries;
import java.util.UUID;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.RelatedArtifact.RelatedArtifactType;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class LibraryTranslatorTest {

  KnowledgeAsset meta = new MockSurrogateKnowledgeAsset().buildMetadata();
  TransionApiOperator translator = new SurrogateToLibraryTranslator();

  @Test
  void testLibraryTranslation() {
    Answer<KnowledgeCarrier> fhir =
        Answer.of(AbstractCarrier.ofAst(meta)
            .withRepresentation(rep(Knowledge_Asset_Surrogate)))
            .flatMap(kc -> translator.try_applyTransrepresent(kc, encode(rep(FHIR_STU3)), null));

    assertTrue(fhir.isSuccess());
    Library lib = fhir
        .flatOpt(kc -> kc.as(Library.class))
        .orElse(new Library());

    assertEquals(Clinical_Rule.asEnum().getTag(),
        lib.getType().getCodingFirstRep().getCode());
    assertEquals("0c36a4a3-7645-4276-baf5-be957112717b",
        lib.getIdentifierFirstRep().getValue());

    assertEquals(7, lib.getRelatedArtifact().size());
    assertTrue(lib.getRelatedArtifact().stream()
        .anyMatch(rel -> rel.getType().equals(RelatedArtifactType.DEPENDSON)));
    assertTrue(lib.getRelatedArtifact().stream()
        .anyMatch(rel -> rel.getType().equals(RelatedArtifactType.COMPOSEDOF)));
    assertTrue(lib.getRelatedArtifact().stream()
        .anyMatch(rel -> rel.getType().equals(RelatedArtifactType.DERIVEDFROM)));
    assertTrue(lib.getRelatedArtifact().stream()
        .anyMatch(rel -> rel.getType().equals(RelatedArtifactType.SUCCESSOR)));

    assertEquals(PublicationStatus.ACTIVE, lib.getStatus());

    assertEquals(1, lib.getContent().size());

    Attachment artifact = lib.getContentFirstRep();
    assertTrue(artifact.getUrl().contains("f2b9828d-f84c-4d09-9c88-413c7f1439a4"));
    assertEquals("it", artifact.getLanguage());
    assertEquals("model/dmn-v11[cql-exx]+xml;lex={sct}", artifact.getContentType());
    assertEquals("A mock example", artifact.getTitle());

  }
}
