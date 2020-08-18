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
package edu.mayo.kmdp.language.detectors.surrogate.v1;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Language_Information_Detection_Task;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.omg.spec.api4kp.taxonomy.lexicon.LexiconSeries.Asset_Relationships_Dependencies;
import static org.omg.spec.api4kp.taxonomy.lexicon.LexiconSeries.Asset_Relationships_Derivations;
import static org.omg.spec.api4kp.taxonomy.lexicon.LexiconSeries.Asset_Relationships_Structural;
import static org.omg.spec.api4kp.taxonomy.lexicon.LexiconSeries.Asset_Relationships_Variants;

import edu.mayo.kmdp.language.detectors.JsonBasedLanguageDetector;
import edu.mayo.kmdp.language.detectors.MultiFormatLanguageDetector;
import edu.mayo.kmdp.language.detectors.XMLBasedLanguageDetector;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.DetectApiInternal;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;

@Named
@KPOperation(Language_Information_Detection_Task)
@KPSupport(Knowledge_Asset_Surrogate)
public class SurrogateDetector
    extends MultiFormatLanguageDetector<KnowledgeAsset>
    implements DetectApiInternal._applyDetect, DetectApiInternal._applyNamedDetect {

  static UUID id = UUID.randomUUID();
  static String version = "1.0.0";

  protected static final KnowledgeRepresentationLanguage theLanguage = Knowledge_Asset_Surrogate;

  protected SurrogateDetector() {
    super(new XMLSurrogateDetector(), new JSNSurrogateDetector());
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return theLanguage;
  }

  private static class XMLSurrogateDetector extends
      XMLBasedLanguageDetector<KnowledgeAsset>  {

    public XMLSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public List<SyntacticRepresentation> getSupportedRepresentations() {
      return Arrays.asList(
          rep(theLanguage, XML_1_1,
              Asset_Relationships_Dependencies,
              Asset_Relationships_Derivations, Asset_Relationships_Structural,
              Asset_Relationships_Variants),
          rep(theLanguage,
              Asset_Relationships_Dependencies,
              Asset_Relationships_Derivations, Asset_Relationships_Structural,
              Asset_Relationships_Variants)
          );
    }

    @Override
    public KnowledgeRepresentationLanguage getSupportedLanguage() {
      return theLanguage;
    }
  }

  private static class JSNSurrogateDetector extends
      JsonBasedLanguageDetector<KnowledgeAsset> implements DetectApiInternal {

    public JSNSurrogateDetector() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public List<SyntacticRepresentation> getSupportedRepresentations() {
      return Arrays.asList(
          rep(theLanguage, JSON,
              Asset_Relationships_Dependencies,
              Asset_Relationships_Derivations, Asset_Relationships_Structural,
              Asset_Relationships_Variants),
          rep(theLanguage,
              Asset_Relationships_Dependencies,
              Asset_Relationships_Derivations, Asset_Relationships_Structural,
              Asset_Relationships_Variants)
          );
    }

    @Override
    public KnowledgeRepresentationLanguage getSupportedLanguage() {
      return theLanguage;
    }
  }


}
