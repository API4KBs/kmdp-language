package edu.mayo.kmdp.language;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.junit.jupiter.api.Assertions.*;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.encode;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateV1ToSurrogateV2Translator;
import edu.mayo.kmdp.language.translators.surrogate.v2.SurrogateV2toSurrogateV1Translator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class SurrogateTranslatorTest {

  KnowledgeAsset meta = new MockSurrogateKnowledgeAsset().buildMetadata();
  TransionApiOperator v1ToV2Translator = new SurrogateV1ToSurrogateV2Translator();

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
  void TestSurrogateV1toV2Translation_noLifecycle_ThrowsException() {

    meta.setLifecycle(null);

    SurrogateV1ToSurrogateV2Translator v1ToV2Translator = new SurrogateV1ToSurrogateV2Translator();
    KnowledgeCarrier kc = AbstractCarrier.ofAst(meta)
            .withAssetId(DatatypeHelper.toSemanticIdentifier(meta.getAssetId()))
            .withRepresentation(rep(Knowledge_Asset_Surrogate));

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      v1ToV2Translator.applyTransrepresent(kc, encode(rep(Knowledge_Asset_Surrogate_2_0)), null);
    });
    assertEquals("Source surrogate must have lifecycle - none found", exception.getMessage());

  }

  Answer<KnowledgeCarrier> translateKnowledgeAssetToSurrogateV2() {
    return Answer.of(AbstractCarrier.ofAst(meta)
        .withAssetId(DatatypeHelper.toSemanticIdentifier(meta.getAssetId()))
        .withRepresentation(rep(Knowledge_Asset_Surrogate)))
        .flatMap(kc -> v1ToV2Translator
            .try_applyTransrepresent(kc, encode(rep(Knowledge_Asset_Surrogate_2_0)), null));

  }
}
