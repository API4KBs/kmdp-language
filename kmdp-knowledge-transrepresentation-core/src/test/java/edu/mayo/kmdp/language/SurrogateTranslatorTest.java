package edu.mayo.kmdp.language;

import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateToLibraryTranslator;
import edu.mayo.kmdp.language.translators.surrogate.v1.SurrogateV1ToSurrogateV2Translator;
import edu.mayo.kmdp.language.translators.surrogate.v2.SurrogateV2toSurrogateV1Translator;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.RelatedArtifact.RelatedArtifactType;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

import static edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.encode;

public class SurrogateTranslatorTest {

  KnowledgeAsset meta = new MockSurrogateKnowledgeAsset().buildMetadata();
  TransionApiOperator v1ToV2Translator = new SurrogateV1ToSurrogateV2Translator();
  TransionApiOperator v2Translator = new SurrogateV2toSurrogateV1Translator();

  @Test
  void TestSurrogateV1toV2Translation() {
    Answer<KnowledgeCarrier> knowledgeCarrier = translateKnowledgeAssetToSurrogateV2();

    assertTrue(knowledgeCarrier.isSuccess());

    edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset surrogateV2 = knowledgeCarrier
        .flatOpt(kc -> kc.as(
            edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset.class))
            .orElse(new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset());

    //TODO: do any assertions needed on Knowledge Asset Surrogate V2
  }

  Answer<KnowledgeCarrier> translateKnowledgeAssetToSurrogateV2() {
    return Answer.of(AbstractCarrier.ofAst(meta)
        .withRepresentation(rep(Knowledge_Asset_Surrogate)))
        .flatMap(kc -> v1ToV2Translator
            .try_applyTransrepresent(kc, encode(rep(Knowledge_Asset_Surrogate_2_0)), null));

  }
}
