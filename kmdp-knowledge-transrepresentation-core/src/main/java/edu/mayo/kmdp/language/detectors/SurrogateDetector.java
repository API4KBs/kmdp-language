/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.language.detectors;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.v3.server.DetectApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Detect_Language_Information_Task)
public class SurrogateDetector implements DetectApiInternal {

  private XMLSurrogateDetector xmlDetector = new XMLSurrogateDetector();
  private JSNSurrogateDetector jsnDetector = new JSNSurrogateDetector();

  protected static final KnowledgeRepresentationLanguage theLanguage = Knowledge_Asset_Surrogate;


  @Override
  public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
    return Answer.of(Stream.concat(
        xmlDetector.getDetectableLanguages().orElse(emptyList()).stream(),
        jsnDetector.getDetectableLanguages().orElse(emptyList()).stream())
        .collect(Collectors.toList()));
  }

  @Override
  public Answer<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return getDetectedRepresentation(sourceArtifact)
        .map(sourceArtifact::withRepresentation);
  }

  @Override
  public Answer<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    Answer<SyntacticRepresentation> xmlOpinion =
        xmlDetector.getDetectedRepresentation(sourceArtifact);
    if (xmlOpinion.isSuccess()) {
      return xmlOpinion;
    } else {
      return jsnDetector.getDetectedRepresentation(sourceArtifact);
    }
  }

  private static class XMLSurrogateDetector extends
      XMLBasedLanguageDetector<KnowledgeAsset> implements DetectApiInternal {

    public XMLSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
      return Answer.of((
          singletonList(new org.omg.spec.api4kp._1_0.services.SyntacticRepresentation()
              .withLanguage(theLanguage)
              .withFormat(SerializationFormatSeries.XML_1_1)
              .withLexicon(LexiconSeries.Asset_Relationships_Dependencies,
                  LexiconSeries.Asset_Relationships_Derivations, LexiconSeries.Asset_Relationships_Structural,
                  LexiconSeries.Asset_Relationships_Variants))));
    }
  }

  private static class JSNSurrogateDetector extends
      JSONBasedLanguageDetector<KnowledgeAsset> implements DetectApiInternal {

    public JSNSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
      return Answer.of(
          singletonList(new org.omg.spec.api4kp._1_0.services.SyntacticRepresentation()
              .withLanguage(theLanguage)
              .withFormat(SerializationFormatSeries.JSON)
              .withLexicon(LexiconSeries.Asset_Relationships_Dependencies,
                  LexiconSeries.Asset_Relationships_Derivations, LexiconSeries.Asset_Relationships_Structural,
                  LexiconSeries.Asset_Relationships_Variants)));
    }
  }


}
