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

import static edu.mayo.kmdp.util.ws.ResponseHelper.get;
import static edu.mayo.kmdp.util.ws.ResponseHelper.getAll;
import static edu.mayo.kmdp.util.ws.ResponseHelper.map;
import static edu.mayo.kmdp.util.ws.ResponseHelper.succeed;
import static edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat.JSON;
import static edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate;
import static java.util.Collections.singletonList;

import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.server.DetectApiDelegate;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.lexicon._20190801.Lexicon;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.http.ResponseEntity;

@Named
@KPOperation(KnowledgeProcessingOperation.Detect_Language_Information_Task)
public class SurrogateDetector implements DetectApiDelegate {

  private XMLSurrogateDetector xmlDetector = new XMLSurrogateDetector();
  private JSNSurrogateDetector jsnDetector = new JSNSurrogateDetector();

  protected static final KnowledgeRepresentationLanguage theLanguage = Knowledge_Asset_Surrogate;


  @Override
  public ResponseEntity<List<SyntacticRepresentation>> getDetectableLanguages() {
    return succeed(Stream.concat(
        getAll(xmlDetector.getDetectableLanguages()).stream(),
        getAll(jsnDetector.getDetectableLanguages()).stream())
        .collect(Collectors.toList()));
  }

  @Override
  public ResponseEntity<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return map(getDetectedRepresentation(sourceArtifact),
        sourceArtifact::withRepresentation);
  }

  @Override
  public ResponseEntity<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return ResponseHelper.attempt(
        get(xmlDetector.getDetectedRepresentation(sourceArtifact))
            .orElse(get(jsnDetector.getDetectedRepresentation(sourceArtifact))
                .orElse(null)));
  }

  private static class XMLSurrogateDetector extends
      XMLBasedLanguageDetector<KnowledgeAsset> implements DetectApiDelegate {

    public XMLSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public ResponseEntity<List<SyntacticRepresentation>> getDetectableLanguages() {
      return succeed(
          singletonList(new org.omg.spec.api4kp._1_0.services.SyntacticRepresentation()
              .withLanguage(theLanguage)
              .withFormat(XML_1_1)
              .withLexicon(Lexicon.Asset_Relationships_Dependencies,
                  Lexicon.Asset_Relationships_Derivations, Lexicon.Asset_Relationships_Structural,
                  Lexicon.Asset_Relationships_Variants)));
    }
  }

  private static class JSNSurrogateDetector extends
      JSONBasedLanguageDetector<KnowledgeAsset> implements DetectApiDelegate {

    public JSNSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public ResponseEntity<List<SyntacticRepresentation>> getDetectableLanguages() {
      return succeed(
          singletonList(new org.omg.spec.api4kp._1_0.services.SyntacticRepresentation()
              .withLanguage(theLanguage)
              .withFormat(JSON)
              .withLexicon(Lexicon.Asset_Relationships_Dependencies,
                  Lexicon.Asset_Relationships_Derivations, Lexicon.Asset_Relationships_Structural,
                  Lexicon.Asset_Relationships_Variants)));
    }
  }


}
