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

import edu.mayo.kmdp.language.DetectApi;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import edu.mayo.ontology.taxonomies.krformat._2018._08.KRFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KRLanguage;
import edu.mayo.ontology.taxonomies.lexicon._2018._08.Lexicon;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeOperations.Detect_Language_Information_Task)
public class SurrogateDetector implements DetectApi {

  private XMLSurrogateDetector xmlDetector = new XMLSurrogateDetector();
  private JSNSurrogateDetector jsnDetector = new JSNSurrogateDetector();

  protected static final KRLanguage theLanguage = KRLanguage.Asset_Surrogate;


  @Override
  public List<SyntacticRepresentation> getDetectableLanguages() {
    return Stream.concat(xmlDetector.getDetectableLanguages().stream(),
        jsnDetector.getDetectableLanguages().stream())
        .collect(Collectors.toList());
  }

  @Override
  public KnowledgeCarrier setDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    return sourceArtifact.withRepresentation(getDetectedRepresentation(sourceArtifact));
  }

  @Override
  public SyntacticRepresentation getDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    SyntacticRepresentation xml = xmlDetector.getDetectedRepresentation(sourceArtifact);
    return xml != null ? xml : jsnDetector.getDetectedRepresentation(sourceArtifact);
  }

  private static class XMLSurrogateDetector extends
      XMLBasedLanguageDetector<KnowledgeAsset> implements DetectApi {

    public XMLSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public List<SyntacticRepresentation> getDetectableLanguages() {
      return Collections
          .singletonList(new org.omg.spec.api4kp._1_0.services.SyntacticRepresentation()
              .withLanguage(theLanguage)
              .withFormat(KRFormat.XML_1_1)
              .withLexicon(Lexicon.API4KP_Rel_Jun18));
    }
  }

  private static class JSNSurrogateDetector extends
      JSONBasedLanguageDetector<KnowledgeAsset> implements DetectApi {

    public JSNSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public List<SyntacticRepresentation> getDetectableLanguages() {
      return Collections
          .singletonList(new org.omg.spec.api4kp._1_0.services.SyntacticRepresentation()
              .withLanguage(theLanguage)
              .withFormat(KRFormat.JSON)
              .withLexicon(Lexicon.API4KP_Rel_Jun18));
    }
  }


}
