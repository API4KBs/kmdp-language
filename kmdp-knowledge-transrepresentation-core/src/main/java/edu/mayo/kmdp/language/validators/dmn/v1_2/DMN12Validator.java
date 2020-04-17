package edu.mayo.kmdp.language.validators.dmn.v1_2;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Well_Formedness_Check_Task;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.ValidateApiOperator;
import edu.mayo.kmdp.tranx.v4.server.ValidateApiInternal._applyNamedValidate;
import edu.mayo.kmdp.tranx.v4.server.ValidateApiInternal._applyValidate;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.inject.Named;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.dmn._20180521.model.TDefinitions;
import org.w3c.dom.Document;

@Named
@KPOperation(Well_Formedness_Check_Task)
@KPSupport(DMN_1_2)
public class DMN12Validator
    implements ValidateApiOperator,
    _applyValidate,
    _applyNamedValidate {

  static UUID id = UUID.randomUUID();
  static String version = "1.0.0";

  private ResourceIdentifier operatorId;
  
  public DMN12Validator() {
    this.operatorId = SemanticIdentifier.newId(id,version);
  }
  
  @Override
  public Answer<Void> applyNamedValidate(UUID uuid, KnowledgeCarrier knowledgeCarrier) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyValidate(knowledgeCarrier)
        : Answer.unsupported();
  }

  @Override
  public Answer<Void> applyValidate(KnowledgeCarrier sourceArtifact) {
    try {
      return validate(sourceArtifact)
          ? Answer.succeed()
          : Answer.failed();
    } catch (Exception e) {
      return Answer.failed(e);
    }
  }


  protected boolean validate(KnowledgeCarrier sourceArtifact) {
    Schema dmn12Schema = checkSchema(sourceArtifact);

    switch (sourceArtifact.getLevel().asEnum()) {
      case Abstract_Knowledge_Expression:
        return validateAST(sourceArtifact.getExpression(),dmn12Schema);
      case Parsed_Knowedge_Expression:
        return validateTree(sourceArtifact.getExpression(),dmn12Schema);
      case Concrete_Knowledge_Expression:
        return sourceArtifact.asString()
            .map(str -> validateString(str, dmn12Schema))
            .orElse(false);
      case Encoded_Knowledge_Expression:
        return sourceArtifact.asBinary()
            .map(bytes -> validateBinary(bytes, dmn12Schema))
            .orElse(false);
      default:
        return false;
    }
  }

  private boolean validateBinary(byte[] bytes, Schema schema) {
    return XMLUtil.validate(
        new StreamSource(new ByteArrayInputStream(bytes)),
        schema);
  }

  private boolean validateString(String expr, Schema schema) {
    return XMLUtil.validate(expr,schema);
  }

  private boolean validateTree(Object tree, Schema schema) {
    return tree instanceof Document
        && XMLUtil.validate((Document) tree,schema);
  }

  private boolean validateAST(Object ast, Schema dmn12Schema) {
    return ast instanceof TDefinitions;
  }

  private Schema checkSchema(KnowledgeCarrier sourceArtifact) {
    if (!DMN_1_2.sameAs(sourceArtifact.getRepresentation().getLanguage())) {
      throw new UnsupportedOperationException("DMN12 Validator unable to validate "
          + sourceArtifact.getRepresentation().getLanguage());
    }
    return XMLUtil.getSchemas(DMN_1_2.getRef())
        .orElseThrow(() -> new UnsupportedOperationException("Unable to retrieve schema for DMN12"));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return Arrays.asList(
        rep(DMN_1_2),
        rep(DMN_1_2, XML_1_1)
    );
  }

  @Override
  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return DMN_1_2;
  }
}
