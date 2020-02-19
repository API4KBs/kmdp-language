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
package edu.mayo.kmdp.language.parsers.surrogate.v1;


import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.parsers.JSONBasedLanguageParser;
import edu.mayo.kmdp.language.parsers.MultiFormatParser;
import edu.mayo.kmdp.language.parsers.XMLBasedLanguageParser;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(Lowering_Task)
@KPOperation(Lifting_Task)
public class SurrogateParser extends MultiFormatParser<KnowledgeAsset> implements
    DeserializeApiInternal {

  private final List<SyntacticRepresentation> supportedRepresentations = Arrays.asList(
      rep(KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate, SerializationFormatSeries.XML_1_1, getDefaultCharset()),
      rep(KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate, SerializationFormatSeries.JSON, getDefaultCharset()));

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
          .singletonList(rep(KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate, SerializationFormatSeries.XML_1_1, getDefaultCharset()));
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
          .singletonList(rep(KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate, SerializationFormatSeries.JSON, getDefaultCharset()));
    }
  }


}
