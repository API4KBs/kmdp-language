package edu.mayo.kmdp.language.validators.dmn.v1_2;

import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;

import edu.mayo.kmdp.language.parsers.dmn.v1_2.DMN12Parser;
import edu.mayo.kmdp.tranx.v3.server.ValidateApiInternal;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCodeSeries;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import javax.inject.Named;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Well_Formedness_Check_Task)
public class DMN12Validator implements ValidateApiInternal {

  @Override
  public Answer<Void> validate(KnowledgeCarrier sourceArtifact, SyntacticRepresentation into) {
    if (!DMN_1_2.sameAs(sourceArtifact.getRepresentation().getLanguage())
        || !DMN_1_2.sameAs(into.getLanguage())) {
      return Answer.unsupported();
    }

    Optional<Schema> dmn12SchemaOpt = XMLUtil.getSchemas(DMN_1_2.getRef());
    if (!dmn12SchemaOpt.isPresent()) {
      return Answer.unsupported();
    }
    Schema dmn12Schema = dmn12SchemaOpt.get();

    boolean isValid = false;
    switch (sourceArtifact.getLevel().asEnum()) {
      case Abstract_Knowledge_Expression:
        isValid = new DMN12Parser()
            .lower(sourceArtifact, ParsingLevelSeries.Parsed_Knowedge_Expression)
            .flatOpt(kc ->
                kc.asParseTree(Document.class))
            .map(dox -> XMLUtil.validate(dox, dmn12Schema))
            .orElse(false);
        break;
      case Parsed_Knowedge_Expression:
        isValid = sourceArtifact.asParseTree(Document.class)
            .map(dox -> XMLUtil.validate(dox,dmn12Schema))
            .orElse(false);
        break;
      case Concrete_Knowledge_Expression:
        isValid = sourceArtifact.asString()
            .map(str -> XMLUtil.validate(str,dmn12Schema))
            .orElse(false);
        break;
      case Encoded_Knowledge_Expression:
        isValid = XMLUtil.validate(
            new StreamSource(new ByteArrayInputStream(((BinaryCarrier)sourceArtifact).getEncodedExpression())),
            dmn12Schema);
        break;
      default:
    }

    return isValid ? Answer.succeed() : Answer.of(ResponseCodeSeries.BadRequest);
  }

  @Override
  public Answer<Void> validate() {
    return Answer.unsupported();
  }
}
