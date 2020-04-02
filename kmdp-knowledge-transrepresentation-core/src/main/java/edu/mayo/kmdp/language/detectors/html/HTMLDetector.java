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
package edu.mayo.kmdp.language.detectors.html;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Detect_Language_Information_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@KPOperation(Detect_Language_Information_Task)
@KPSupport(HTML)
public class HTMLDetector implements DetectApiInternal {

  protected static final Logger logger = LoggerFactory.getLogger(HTMLDetector.class);

  @Override
  public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
    return Answer.of(Collections.singletonList(rep(HTML)));
  }

  @Override
  public Answer<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    Optional<String> str = getSerializedContent(sourceArtifact);
    if (str.isPresent() && isHtml(str.get())) {
      return Answer.of(new SyntacticRepresentation()
          .withLanguage(HTML)
          .withFormat(TXT));
    } else {
      return Answer.failed();
    }
  }

  private boolean isHtml(String s) {
    Document dirtyDoc = Jsoup.parse(s);
    Document dox = new Cleaner(Whitelist.relaxed()).clean(dirtyDoc);
    return ! (dox.body().children().isEmpty() && dox.head().children().isEmpty());
  }

  private Optional<String> getSerializedContent(KnowledgeCarrier sourceArtifact) {
    return sourceArtifact.asString();
  }


  @Override
  public Answer<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return getDetectedRepresentation(sourceArtifact)
        .map(sourceArtifact::withRepresentation);
  }


}
