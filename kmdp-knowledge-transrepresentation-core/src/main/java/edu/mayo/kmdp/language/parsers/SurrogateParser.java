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
package edu.mayo.kmdp.language.parsers;


import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperation.Lowering_Task)
@KPOperation(KnowledgeProcessingOperation.Lifting_Task)
public class SurrogateParser extends MultiFormatParser<KnowledgeAsset> implements DeserializeApiDelegate {

  private final List<SyntacticRepresentation> supportedRepresentations = Arrays.asList(
      rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.XML_1_1, getDefaultCharset()),
      rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.JSON, getDefaultCharset()));

  public SurrogateParser() {
    super(new XMLSurrogateParser(), new JSONSurrogateParser());
  }


  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return supportedRepresentations;
  }

  private static class XMLSurrogateParser extends XMLBasedLanguageParser<KnowledgeAsset> {

    public XMLSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    protected List<SyntacticRepresentation> getSupportedRepresentations() {
      return Collections
          .singletonList(rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.XML_1_1, getDefaultCharset()));
    }
  }

  private static class JSONSurrogateParser extends
      JSONBasedLanguageParser<KnowledgeAsset> {

    public JSONSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    protected List<SyntacticRepresentation> getSupportedRepresentations() {
      return Collections
          .singletonList(rep(KnowledgeRepresentationLanguage.Knowledge_Asset_Surrogate, SerializationFormat.JSON, getDefaultCharset()));
    }
  }


}
