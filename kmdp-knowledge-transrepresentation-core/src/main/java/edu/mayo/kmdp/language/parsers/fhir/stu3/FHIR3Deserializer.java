package edu.mayo.kmdp.language.parsers.fhir.stu3;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;

import ca.uhn.fhir.context.FhirContext;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.language.parsers.AbstractDeSerializer;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
@KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
@KPSupport(FHIR_STU3)
public class FHIR3Deserializer extends AbstractDeSerializer {

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
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.of(
        (ExpressionCarrier) AbstractCarrier.of(new String(carrier.getEncodedExpression()))
    );
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    switch (carrier.getRepresentation().getFormat().asEnum()) {
      case JSON:
        return Optional.of((ASTCarrier) AbstractCarrier.ofAst(FhirContext.forDstu3().newJsonParser()
            .parseResource(carrier.getSerializedExpression())));
      case XML_1_1:
        return Optional.of((ASTCarrier) AbstractCarrier.ofAst(FhirContext.forDstu3().newXmlParser()
            .parseResource(carrier.getSerializedExpression())));
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    return Optional.empty();
  }



  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into) {
    return Optional.empty();
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier, SyntacticRepresentation into) {
    return Optional.empty();
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier,
      SyntacticRepresentation into) {
    return Optional.empty();
  }

  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    return Optional.empty();
  }
}
