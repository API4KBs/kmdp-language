package edu.mayo.kmdp.language.translators;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.newRandomSurrogate;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Clinical_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;

import edu.mayo.kmdp.language.translators.surrogate.v2.SurrogateV2ToHTML;
import java.net.URI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;

public class SurrToHTMLTest {

  @Test
  void testTranslate() {
    String html = getHtml();
    assertNotNull(Jsoup.parse(html));
  }

  public static void main(String... args) {
    new TestHTMLViewer(getHtml());
  }

  static String getHtml() {
    KnowledgeAsset surr = newRandomSurrogate()
        .withName("Test Asset", "Some description...")
        .withFormalType(Rules_Policies_And_Guidelines, Clinical_Rule)
        .withCarriers(SurrogateBuilder.randomArtifactId(), URI.create("http://www.mayo.edu"))
        .get();
    Document doh = new SurrogateV2ToHTML().transform(surr);
    return doh.outerHtml();
  }



}
