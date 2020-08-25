package edu.mayo.kmdp.language;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.junit.jupiter.api.Assertions.*;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.encode;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateV1ToSurrogateV2Translator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.Dependency;
import edu.mayo.kmdp.metadata.v2.surrogate.Link;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class SurrogateTranslatorTest {

  KnowledgeAsset meta = new MockSurrogateKnowledgeAsset().buildMetadata();
  SurrogateV1ToSurrogateV2Translator v1ToV2Translator = new SurrogateV1ToSurrogateV2Translator();

  @Test
  void TestSurrogateV1toV2Translation() {
    KnowledgeCarrier knowledgeCarrier = translateKnowledgeAssetToSurrogateV2()
        .orElseGet(Assertions::fail);
    assertTrue(knowledgeCarrier instanceof CompositeKnowledgeCarrier);

    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2 =
        knowledgeCarrier.mainComponent()
            .as(edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset.class)
            .orElseGet(Assertions::fail);

    assertNotNull(surrogateV2.getAssetId());
    assertFalse(surrogateV2.getFormalCategory().isEmpty());
    assertFalse(surrogateV2.getFormalType().isEmpty());

    assertEquals(1,surrogateV2.getCarriers().size());

  }

  @Test
  void TestGetImmediateChildrenFunction() {
    Dependency dependency = new Dependency();
    dependency.setRel(DependencyTypeSeries.Imports);
    UUID uuid = UUID.randomUUID();
    dependency.setHref(new ResourceIdentifier().withUuid(uuid).withVersionTag("0.0.0"));
    Dependency dependency1 = new Dependency();
    UUID uuid1 = UUID.randomUUID();
    dependency1.setHref(new ResourceIdentifier().withUuid(uuid1).withVersionTag("0.0.0"));
    Function<edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset, List<Link>> getImmediateChildren = v1ToV2Translator.getImmediateChildrenFunction();
    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogate =
            new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset()
            .withLinks(Arrays.asList(dependency, dependency1));
    List<Link> links = getImmediateChildren.apply(surrogate);
    assertEquals(links.get(0).getHref().getUuid(), uuid);
    assertEquals(links.get(1).getHref().getUuid(), uuid1);

  }

  Answer<KnowledgeCarrier> translateKnowledgeAssetToSurrogateV2() {
    return Answer.of(AbstractCarrier.ofAst(meta)
        .withAssetId(DatatypeHelper.toSemanticIdentifier(meta.getAssetId()))
        .withRepresentation(rep(Knowledge_Asset_Surrogate)))
        .flatMap(kc -> v1ToV2Translator
            .try_applyTransrepresent(kc, encode(rep(Knowledge_Asset_Surrogate_2_0)), null));

  }
}