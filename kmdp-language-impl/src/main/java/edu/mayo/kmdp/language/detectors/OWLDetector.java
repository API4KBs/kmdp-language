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
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.DetectApi;
import edu.mayo.kmdp.language.detectors.OWLDetectorConfig.DetectorParams;
import edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krprofile._2018._08.KRProfile;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import edu.mayo.kmdp.terms.lexicon._2018._08.Lexicon;
import edu.mayo.kmdp.util.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Named;
import org.apache.jena.vocabulary.SKOS;
import org.apache.xerces.util.XMLCatalogResolver;
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

@Named
@KPOperation(KnowledgeOperations.Detect_Language_Information_Task)
public class OWLDetector implements DetectApi {

  @Override
  public List<SyntacticRepresentation> getDetectableLanguages() {
    return Collections.singletonList(rep(KRLanguage.OWL_2));
  }

  @Override
  public SyntacticRepresentation getDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    Optional<OWLOntology> owl = asOWL(sourceArtifact);
    if (owl.isPresent() && !owl.get().isEmpty()) {
      return owl.map((o) -> new SyntacticRepresentation()
          .withLanguage(KRLanguage.OWL_2)
          .withProfile(detectProfile(o))
          .withSerialization(detectSerialization(o))
          .withFormat(detectFormat(o))
          .withLexicon(detectLexicon(o)))
          .get();
    } else {
      return null;
    }
  }

  private KRFormat detectFormat(OWLOntology o) {
    OWLDocumentFormat format = o.getFormat();
    if (format instanceof RDFXMLDocumentFormat) {
      return KRFormat.XML_1_1;
    } else if (format instanceof ManchesterSyntaxDocumentFormat) {
      return KRFormat.TXT;
    } else if (format instanceof OWLXMLDocumentFormat) {
      return KRFormat.XML_1_1;
    } else if (format instanceof TurtleDocumentFormat) {
      return KRFormat.TXT;
    } else if (format instanceof FunctionalSyntaxDocumentFormat) {
      return KRFormat.TXT;
    }
    return null;
  }

  private KRSerialization detectSerialization(OWLOntology o) {
    OWLDocumentFormat format = o.getFormat();
    if (format instanceof RDFXMLDocumentFormat) {
      return KRSerialization.RDF_XML_Syntax;
    } else if (format instanceof ManchesterSyntaxDocumentFormat) {
      return KRSerialization.OWL_Manchester_Syntax;
    } else if (format instanceof OWLXMLDocumentFormat) {
      return KRSerialization.OWL_XML_Serialization;
    } else if (format instanceof TurtleDocumentFormat) {
      return KRSerialization.Turtle;
    } else if (format instanceof FunctionalSyntaxDocumentFormat) {
      return KRSerialization.OWL_Functional_Syntax;
    }
    return null;
  }

  private Collection<Lexicon> detectLexicon(OWLOntology o) {
    Set<Lexicon> lexica = new HashSet<>();
    if (o.importsDeclarations().anyMatch((decl) -> decl.getIRI().equals(IRI.create(SKOS.uri)))) {
      lexica.add(Lexicon.SKOS);
    }
    // eventually check for more...
    return lexica;
  }

  protected KRProfile detectProfile(OWLOntology o) {
    if (new OWL2RLProfile().checkOntology(o).isInProfile()) {
      return KRProfile.OWL_2_RL;
    }
    if (new OWL2QLProfile().checkOntology(o).isInProfile()) {
      return KRProfile.OWL_2_QL;
    }
    if (new OWL2ELProfile().checkOntology(o).isInProfile()) {
      return KRProfile.OWL_2_EL;
    }
    if (new OWL2DLProfile().checkOntology(o).isInProfile()) {
      return KRProfile.OWL_2_DL;
    }
    return KRProfile.OWL_2_Full;
  }

  @Override
  public KnowledgeCarrier setDetectedRepresentation(KnowledgeCarrier sourceArtifact) {
    return sourceArtifact.withRepresentation(getDetectedRepresentation(sourceArtifact));
  }

  protected Optional<OWLOntology> asOWL(KnowledgeCarrier sourceArtifact) {
    switch (sourceArtifact.getLevel()) {
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
          .ofNullable(manager.loadOntologyFromOntologyDocument(is));

      if (is.markSupported()) {
        is.reset();
      }
      return onto;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private void configureCatalog(OWLOntologyManager manager, OWLDetectorConfig params) {
    String catalog = params.get(DetectorParams.CATALOG).orElse(null);
    if (!Util.isEmpty(catalog)) {
      XMLCatalogResolver resolver = catalogResolver(catalog);
      manager.setIRIMappers(Collections.singleton(
          new OWLOntologyIRIMapper() {
            @Nullable
            @Override
            public IRI getDocumentIRI(IRI iri) {
              try {
                String resolved = resolver.resolveURI(iri.toURI().toString());
                return IRI.create(resolved);
              } catch (IOException e) {
                e.printStackTrace();
              }
              return null;
            }
          }
      ));
    }

  }

}