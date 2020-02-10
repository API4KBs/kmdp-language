package edu.mayo.kmdp.language;

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.CMMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.SPARQL_1_1;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.CMMN_1_1_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.SurrogateBuilder;
import edu.mayo.kmdp.language.parsers.cmmn.v1_1.CMMN11Parser;
import edu.mayo.kmdp.language.parsers.dmn.v1_2.DMN12Parser;
import edu.mayo.kmdp.language.parsers.dmn.v1_1.DMN11Parser;
import edu.mayo.kmdp.language.parsers.owl2.OWLParser;
import edu.mayo.kmdp.language.parsers.owl2.JenaOwlRdfLifter;
import edu.mayo.kmdp.language.parsers.sparql.SparqlLifter;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import java.util.UUID;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.semanticweb.owlapi.model.OWLOntology;

class ParserTest {

  static final String VER = "1.0.0";

  @Test
  void testParseDMN11() {
    testVerticalLift(
        new DMN11Parser(),
        "/dmn11example.dmn",
        DMN_1_1, DMN_1_1_XML_Syntax, XML_1_1,
        org.omg.spec.dmn._20151101.dmn.TDefinitions.class,
        Abstract_Knowledge_Expression);
  }

  @Test
  void testParseDMN12() {
    testVerticalLift(
        new DMN12Parser(),
        "/dmn12example.dmn",
        DMN_1_2, DMN_1_1_XML_Syntax, XML_1_1,
        org.omg.spec.dmn._20180521.model.TDefinitions.class,
        Abstract_Knowledge_Expression);
  }

  @Test
  void testParseCMMN() {
    testVerticalLift(
        new CMMN11Parser(),
        "/cmmn11Example.cmmn",
        CMMN_1_1, CMMN_1_1_XML_Syntax, XML_1_1,
        org.omg.spec.cmmn._20151109.model.TDefinitions.class,
        Abstract_Knowledge_Expression);
  }

  @Test
  void testParseOWL() {
    testVerticalLift(
        new OWLParser(),
        "/owlExample.owl",
        OWL_2, OWL_Manchester_Syntax, TXT,
        OWLOntology.class,
        Abstract_Knowledge_Expression);
  }

  @Test
  void testParseOWLWithJena() {
    testVerticalLift(
        new JenaOwlRdfLifter(),
        "/owlExample.rdf",
        OWL_2, RDF_XML_Syntax, XML_1_1,
        Model.class,
        ParsingLevelSeries.Parsed_Knowedge_Expression);
  }

  @Test
  void testParseSparql() {
    testVerticalLift(
        new SparqlLifter(),
        "/sparqlTest.sparql",
        SPARQL_1_1, null, TXT,
        Query.class,
        Abstract_Knowledge_Expression);
  }

  private void testVerticalLift(
      DeserializeApiInternal parser,
      String sourcePath,
      KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization ser,
      SerializationFormat fmt,
      Class<?> astRootClass,
      ParsingLevel level) {
    testVerticalLift(parser::lift,
        sourcePath,
        language,
        ser,
        fmt,
        astRootClass,
        level);
  }

  private void testVerticalLift(
      DeserializeApiInternal._lift parser,
      String sourcePath,
      KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization ser,
      SerializationFormat fmt,
      Class<?> astRootClass,
      ParsingLevel level) {

    URIIdentifier assetId = SurrogateBuilder.assetId(UUID.randomUUID(), VER);

    KnowledgeCarrier carrier =
        AbstractCarrier.of(ParserTest.class.getResourceAsStream(sourcePath))
            .withRepresentation(rep(language, ser, fmt))
            .withAssetId(assetId);

    assertNotNull(carrier.getAssetId());
    assertEquals(Encoded_Knowledge_Expression, carrier.getLevel().asEnum());
    assertEquals(language, carrier.getRepresentation().getLanguage());

    Answer<KnowledgeCarrier> parsed =
        parser.lift(carrier, level);

    assertTrue(parsed.isSuccess());
    KnowledgeCarrier astCarrier = parsed.get();
    assertEquals(carrier.getAssetId(), astCarrier.getAssetId());
    assertEquals(level, astCarrier.getLevel().asEnum());

    if (level.sameAs(Abstract_Knowledge_Expression)) {
      assertTrue(astCarrier.as(astRootClass).isPresent());
    } else if (level.sameAs(Encoded_Knowledge_Expression)) {
      assertTrue(astCarrier instanceof DocumentCarrier);
      assertTrue(astRootClass.isAssignableFrom(((DocumentCarrier)astCarrier).getStructuredExpression().getClass()));
    }
  }


}
