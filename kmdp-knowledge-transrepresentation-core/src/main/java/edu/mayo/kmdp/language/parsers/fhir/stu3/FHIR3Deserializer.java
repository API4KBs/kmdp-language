package edu.mayo.kmdp.language.parsers.fhir.stu3;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;

import ca.uhn.fhir.context.FhirContext;
import edu.mayo.kmdp.language.parsers.AbstractDeSerializeOperator;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(Lifting_Task)
@KPOperation(Lowering_Task)
@KPSupport(FHIR_STU3)
public class FHIR3Deserializer extends AbstractDeSerializeOperator {

  public static final UUID id = UUID.fromString("606717df-3f8d-49ee-9d1b-5d2cc1edff55");
  public static final String version = "1.0.0";

  public FHIR3Deserializer() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
        new SyntacticRepresentation().withLanguage(FHIR_STU3).withFormat(XML_1_1),
        new SyntacticRepresentation().withLanguage(FHIR_STU3).withFormat(JSON)
    );
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return JSON;
  }

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier,
      Properties properties) {
    return carrier.asString()
        .map(AbstractCarrier::of);
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier,
      Properties properties) {
    return Optional.empty();
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier,
      Properties properties) {
    switch (carrier.getRepresentation().getFormat().asEnum()) {
      case JSON:
        return carrier.asString()
            .map(str -> AbstractCarrier.ofAst(FhirContext.forDstu3().newJsonParser()
                .parseResource(str)));
      case XML_1_1:
        return carrier.asString()
            .map(str -> AbstractCarrier.ofAst(FhirContext.forDstu3().newXmlParser()
                .parseResource(str)));
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier,
      Properties properties) {
    return Optional.empty();
  }



  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    return Optional.empty();
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    return Optional.empty();
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    return Optional.empty();
  }

  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    return Optional.empty();
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return FHIR_STU3;
  }
}
