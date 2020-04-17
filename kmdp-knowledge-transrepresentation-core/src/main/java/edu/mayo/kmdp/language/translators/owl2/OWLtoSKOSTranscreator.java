/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language.translators.owl2;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Transcreation_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(Transcreation_Task)
@KPSupport(OWL_2)
public class OWLtoSKOSTranscreator extends AbstractSimpleTranslator<String,String> {

  public static final UUID id = UUID.fromString("57869ee0-304c-40a4-8759-40ea667c328d");
  public static final String version = "1.0.0";

  public OWLtoSKOSTranscreator() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(OWL_2,
        RDF_XML_Syntax,
        XML_1_1));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(OWL_2,
        RDF_XML_Syntax,
        XML_1_1)
        .withLexicon(LexiconSeries.SKOS));
  }

  @Override
  protected Optional<String> transformString(ResourceIdentifier assetId, String str,
      SyntacticRepresentation tgtRep) {
    return doTransform(new ByteArrayInputStream(str.getBytes()), new Owl2SkosConfig());
  }

  @Override
  protected Optional<String> transformBinary(ResourceIdentifier assetId, byte[] bytes,
      SyntacticRepresentation tgtRep) {
    return doTransform(new ByteArrayInputStream(bytes), new Owl2SkosConfig());
  }

  protected Optional<String> doTransform(InputStream is, Properties params) {
    Owl2SkosConfig config = new Owl2SkosConfig(params);

    Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model = model.read(is, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new Owl2SkosConverter()
        .apply(model, config)
        .ifPresent(m -> m.write(baos));

    String skos = new String(baos.toByteArray());
    if (Util.isEmpty(skos)) {
      throw new UnsupportedOperationException();
    }
    return Optional.of(skos);
  }


  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return OWL_2;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return OWL_2;
  }

}
