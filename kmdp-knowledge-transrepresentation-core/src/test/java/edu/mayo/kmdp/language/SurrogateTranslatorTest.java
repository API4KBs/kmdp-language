package edu.mayo.kmdp.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder.encode;
import static org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyTypeSeries.Imports;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;

import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateV1ToSurrogateV2;
import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateV1ToSurrogateV2Translator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.SubLanguage;
import edu.mayo.kmdp.terms.VersionableTerm;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Dependency;
import org.omg.spec.api4kp._20200801.surrogate.Link;

class SurrogateTranslatorTest {

  KnowledgeAsset meta = new MockSurrogateKnowledgeAsset().buildMetadata();
  SurrogateV1ToSurrogateV2Translator v1ToV2Translator = new SurrogateV1ToSurrogateV2Translator();
  SurrogateV1ToSurrogateV2 v1ToV2 = new SurrogateV1ToSurrogateV2();

  @Test
  void TestSurrogateV1toV2Translation() {
    KnowledgeCarrier knowledgeCarrier =
        translateKnowledgeAssetToSurrogateV2().orElseGet(Assertions::fail);
    assertTrue(knowledgeCarrier instanceof CompositeKnowledgeCarrier);
    org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogateV2 =
        knowledgeCarrier
            .mainComponent()
            .as(org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset.class)
            .orElseGet(Assertions::fail);

    assertNotNull(surrogateV2.getAssetId());
    assertFalse(surrogateV2.getFormalCategory().isEmpty());
    assertFalse(surrogateV2.getFormalType().isEmpty());

    assertEquals(1, surrogateV2.getCarriers().size());
  }

  @Test
  void TestMapRepresentation() {
    Representation representation = createRepresentation(null);
    SubLanguage subLanguage =
        new SubLanguage()
            .withRole(KnowledgeRepresentationLanguageRoleSeries.Expression_Language)
            .withSubLanguage(representation);
    Representation representation1 = createRepresentation(Arrays.asList(subLanguage));
    SubLanguage subLanguage1 =
        new SubLanguage()
            .withRole(KnowledgeRepresentationLanguageRoleSeries.Annotation_Language)
            .withSubLanguage(representation1);
    Representation representation2 = createRepresentation(Arrays.asList(subLanguage1));
    SubLanguage subLanguage2 =
        new SubLanguage()
            .withRole(KnowledgeRepresentationLanguageRoleSeries.Annotation_Language)
            .withSubLanguage(representation2);
    Representation representation3 = createRepresentation(null);
    SubLanguage subLanguage3 =
        new SubLanguage()
            .withRole(KnowledgeRepresentationLanguageRoleSeries.Annotation_Language)
            .withSubLanguage(representation3);
    Representation representation4 =
        createRepresentation(Arrays.asList(subLanguage3, subLanguage2));

    SyntacticRepresentation result = v1ToV2.mapRepresentation(representation4);
    assertTrue(assertRepresentationsEqual(representation4, result));
  }

  private boolean assertRepresentationsEqual(
      Representation representation, SyntacticRepresentation syntacticRepresentation) {
    boolean formatEqual =
        assertUUIDsSame(representation.getFormat(), syntacticRepresentation.getFormat());
    boolean languageEqual =
        assertUUIDsSame(representation.getLanguage(), syntacticRepresentation.getLanguage());
    boolean serializationEqual =
        assertUUIDsSame(
            representation.getSerialization(), syntacticRepresentation.getSerialization());
    List<Representation> matching =
        representation.getWith().stream()
            .map(sl -> sl.getSubLanguage())
            .filter(
                os ->
                    syntacticRepresentation.getSubLanguage().stream()
                        .anyMatch(ns -> assertRepresentationsEqual(os, ns)))
            .collect(Collectors.toList());
    boolean childrenMatching = matching.size() == representation.getWith().size();
    return formatEqual && languageEqual && serializationEqual && childrenMatching;
  }

  private boolean assertUUIDsSame(
      VersionableTerm repTerm,
      org.omg.spec.api4kp._20200801.terms.VersionableTerm syntacticRepTerm) {
    return repTerm.getUuid().equals(syntacticRepTerm.getUuid());
  }

  private Representation createRepresentation(List<SubLanguage> subLanguages) {
    ConceptIdentifier conceptIdentifier =
        new ConceptIdentifier().withTag(UUID.randomUUID().toString());
    Random random = new Random();
    Representation representation =
        new Representation()
            .withLanguage(
                KnowledgeRepresentationLanguageSeries.values()[
                    random.nextInt(KnowledgeRepresentationLanguageSeries.values().length - 1)])
            .withFormat(
                SerializationFormatSeries.values()[
                    random.nextInt(SerializationFormatSeries.values().length - 1)])
            .withSerialization(
                KnowledgeRepresentationLanguageSerializationSeries.values()[
                    random.nextInt(
                        KnowledgeRepresentationLanguageSerializationSeries.values().length - 1)])
            .withComplexity(conceptIdentifier);
    if (subLanguages != null) {
      representation.withWith(subLanguages);
    }
    return representation;
  }

  @Test
  void TestGetImmediateChildrenFunction() {
    Dependency dependency = new Dependency();
    dependency.setRel(Imports);
    UUID uuid = UUID.randomUUID();
    dependency.setHref(new ResourceIdentifier().withUuid(uuid).withVersionTag("0.0.0"));
    Dependency dependency1 = new Dependency();
    UUID uuid1 = UUID.randomUUID();
    dependency1.setHref(new ResourceIdentifier().withUuid(uuid1).withVersionTag("0.0.0"));
    Function<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset, List<Link>> getImmediateChildren =
        org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset::getLinks;
    org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset surrogate =
        new org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset()
            .withLinks(Arrays.asList(dependency, dependency1));
    List<Link> links = getImmediateChildren.apply(surrogate);
    assertEquals(links.get(0).getHref().getUuid(), uuid);
    assertEquals(links.get(1).getHref().getUuid(), uuid1);
  }

  Answer<KnowledgeCarrier> translateKnowledgeAssetToSurrogateV2() {

    return Answer.of(AbstractCarrier.ofAst(meta)
        .withAssetId(fromURIIdentifier(meta.getAssetId()))
        .withRepresentation(rep(Knowledge_Asset_Surrogate)))
        .flatMap(kc -> v1ToV2Translator
            .try_applyTransrepresent(kc, encode(rep(Knowledge_Asset_Surrogate_2_0)), null));
  }

  private ResourceIdentifier fromURIIdentifier(URIIdentifier assetId) {
    return assetId.getVersionId() != null
        ? SemanticIdentifier.newVersionId(assetId.getVersionId())
        : SemanticIdentifier.newId(assetId.getUri());
  }
}
