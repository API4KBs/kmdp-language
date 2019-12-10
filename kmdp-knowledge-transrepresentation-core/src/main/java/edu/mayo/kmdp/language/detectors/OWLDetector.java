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

import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.detectors.OWLDetectorConfig.DetectorParams;
import edu.mayo.kmdp.tranx.v3.server.DetectApiInternal;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Named;
import org.apache.jena.vocabulary.SKOS;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Detect_Language_Information_Task)
public class OWLDetector implements DetectApiInternal {

  protected static final Logger logger = LoggerFactory.getLogger(OWLDetector.class);

  @Override
  public Answer<List<SyntacticRepresentation>> getDetectableLanguages() {
    return Answer.of(Collections.singletonList(rep(OWL_2)));
  }

  @Override
  public Answer<SyntacticRepresentation> getDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    Optional<OWLOntology> owl = asOWL(sourceArtifact);
    return Answer.of(
        owl.map(o -> new SyntacticRepresentation()
            .withLanguage(OWL_2)
            .withProfile(detectProfile(o))
            .withSerialization(detectSerialization(o))
            .withFormat(detectFormat(o))
            .withLexicon(detectLexicon(o))));
  }

  private SerializationFormat detectFormat(OWLOntology o) {
    OWLDocumentFormat format = o.getFormat();
    if (format instanceof RDFXMLDocumentFormat) {
      return SerializationFormatSeries.XML_1_1;
    } else if (format instanceof ManchesterSyntaxDocumentFormat) {
      return SerializationFormatSeries.TXT;
    } else if (format instanceof OWLXMLDocumentFormat) {
      return SerializationFormatSeries.XML_1_1;
    } else if (format instanceof TurtleDocumentFormat) {
      return SerializationFormatSeries.TXT;
    } else if (format instanceof FunctionalSyntaxDocumentFormat) {
      return SerializationFormatSeries.TXT;
    }
    return null;
  }

  private KnowledgeRepresentationLanguageSerialization detectSerialization(OWLOntology o) {
    OWLDocumentFormat format = o.getFormat();
    if (format instanceof RDFXMLDocumentFormat) {
      return KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
    } else if (format instanceof ManchesterSyntaxDocumentFormat) {
      return KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
    } else if (format instanceof OWLXMLDocumentFormat) {
      return KnowledgeRepresentationLanguageSerializationSeries.OWL_XML_Serialization;
    } else if (format instanceof TurtleDocumentFormat) {
      return KnowledgeRepresentationLanguageSerializationSeries.Turtle;
    } else if (format instanceof FunctionalSyntaxDocumentFormat) {
      return KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax;
    }
    return null;
  }

  private Collection<Lexicon> detectLexicon(OWLOntology o) {
    Set<Lexicon> lexica = new HashSet<>();
    if (o.importsDeclarations().anyMatch(decl -> decl.getIRI().equals(IRI.create(SKOS.uri)))) {
      lexica.add(LexiconSeries.SKOS);
    }
    // eventually check for more...
    return lexica;
  }

  protected KnowledgeRepresentationLanguageProfile detectProfile(OWLOntology o) {
    if (new OWL2RLProfile().checkOntology(o).isInProfile()) {
      return KnowledgeRepresentationLanguageProfileSeries.OWL2_RL;
    }
    if (new OWL2QLProfile().checkOntology(o).isInProfile()) {
      return KnowledgeRepresentationLanguageProfileSeries.OWL2_QL;
    }
    if (new OWL2ELProfile().checkOntology(o).isInProfile()) {
      return KnowledgeRepresentationLanguageProfileSeries.OWL2_EL;
    }
    if (new OWL2DLProfile().checkOntology(o).isInProfile()) {
      return KnowledgeRepresentationLanguageProfileSeries.OWL2_DL;
    }
    return KnowledgeRepresentationLanguageProfileSeries.OWL2_Full;
  }

  @Override
  public Answer<KnowledgeCarrier> setDetectedRepresentation(
      KnowledgeCarrier sourceArtifact) {
    return getDetectedRepresentation(sourceArtifact)
        .map(sourceArtifact::withRepresentation);
  }

  protected Optional<OWLOntology> asOWL(KnowledgeCarrier sourceArtifact) {
    switch (sourceArtifact.getLevel().asEnum()) {
      case Abstract_Knowledge_Expression:
        Object ast = ((ASTCarrier) sourceArtifact).getParsedExpression();
        return ast instanceof OWLOntology ? Optional.of((OWLOntology) ast) : Optional.empty();

      case Encoded_Knowledge_Expression:
        return loadOntology(
            new ByteArrayInputStream(((BinaryCarrier) sourceArtifact).getEncodedExpression()));
      case Concrete_Knowledge_Expression:
        return loadOntology(
            new ByteArrayInputStream(
                ((ExpressionCarrier) sourceArtifact).getSerializedExpression().getBytes()));

      case Parsed_Knowedge_Expression:

      default:
        return Optional.empty();
    }
  }


  protected Optional<OWLOntology> loadOntology(InputStream is) {
    return loadOntology(is, new OWLDetectorConfig());
  }

  protected Optional<OWLOntology> loadOntology(InputStream is, OWLDetectorConfig params) {
    try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

      configureCatalog(manager, params);

      if (is.markSupported()) {
        is.mark(Integer.MAX_VALUE);
      }

      Optional<OWLOntology> onto = Optional
          .ofNullable(manager.loadOntologyFromOntologyDocument(is))
          .filter(o -> o.getOntologyID().getOntologyIRI().isPresent());

      if (is.markSupported()) {
        is.reset();
      }
      return onto;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private void configureCatalog(OWLOntologyManager manager, OWLDetectorConfig params) {
    String catalog = params.getTyped(DetectorParams.CATALOG);
    if (!Util.isEmpty(catalog)) {
      CatalogResolver resolver = catalogResolver(catalog);
      manager.setIRIMappers(Collections.singleton(
          (OWLOntologyIRIMapper) iri -> {
            try {
              String resolved = resolver.getCatalog().resolveURI(iri.toURI().toString());
              return IRI.create(resolved);
            } catch (IOException e) {
              logger.error(e.getMessage(), e);
            }
            return null;
          }
      ));
    }

  }

}
