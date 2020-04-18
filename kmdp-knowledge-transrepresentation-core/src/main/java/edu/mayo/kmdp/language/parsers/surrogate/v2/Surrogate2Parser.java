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
package edu.mayo.kmdp.language.parsers.surrogate.v2;


import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.parsers.JSONBasedLanguageParser;
import edu.mayo.kmdp.language.parsers.MultiFormatParser;
import edu.mayo.kmdp.language.parsers.XMLBasedLanguageParser;
import edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.ObjectFactory;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(Lowering_Task)
@KPOperation(Lifting_Task)
@KPSupport(Knowledge_Asset_Surrogate_2_0)
public class Surrogate2Parser extends MultiFormatParser<KnowledgeAsset> {

  public static final UUID id = UUID.fromString("36a05122-1a29-47e1-b77c-53e1ad1bbba2");
  public static final String version = "1.0.0";

  private final List<SyntacticRepresentation> supportedRepresentations = Arrays.asList(
      rep(Knowledge_Asset_Surrogate_2_0),
      rep(Knowledge_Asset_Surrogate_2_0, XML_1_1, getDefaultCharset()),
      rep(Knowledge_Asset_Surrogate_2_0, JSON, getDefaultCharset()));

  public Surrogate2Parser() {
    super(new XMLSurrogateParser(), new JSONSurrogateParser());
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return supportedRepresentations;
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return Knowledge_Asset_Surrogate_2_0;
  }

  private static class XMLSurrogateParser extends XMLBasedLanguageParser<KnowledgeAsset> {

    public XMLSurrogateParser() {
      this.root = KnowledgeAsset.class;
      this.mapper = new ObjectFactory()::createKnowledgeAsset;
    }

    @Override
    public KnowledgeRepresentationLanguage getSupportedLanguage() {
      return Knowledge_Asset_Surrogate_2_0;
    }
  }

  private static class JSONSurrogateParser extends
      JSONBasedLanguageParser<KnowledgeAsset> {

    public JSONSurrogateParser() {
      this.root = KnowledgeAsset.class;
    }

    @Override
    public KnowledgeRepresentationLanguage getSupportedLanguage() {
      return Knowledge_Asset_Surrogate_2_0;
    }
  }


}
