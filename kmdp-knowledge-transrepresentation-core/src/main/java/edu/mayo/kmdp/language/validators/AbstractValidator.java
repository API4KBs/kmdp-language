package edu.mayo.kmdp.language.validators;

import edu.mayo.kmdp.language.ValidateApiOperator;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.ValidateApiInternal._applyNamedValidate;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.ValidateApiInternal._applyValidate;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;

public abstract class AbstractValidator
    implements ValidateApiOperator,
    _applyValidate,
    _applyNamedValidate {

  protected enum ValidationStatus {OK, ERR, WRN, INF}

  public Answer<Void> applyNamedValidate(UUID uuid, KnowledgeCarrier knowledgeCarrier, String config) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyValidate(knowledgeCarrier, config)
        : Answer.unsupported();
  }

  @Override
  public Answer<Void> applyValidate(KnowledgeCarrier knowledgeCarrier, String s) {
    Answer<Void> response = knowledgeCarrier.componentList().isEmpty()
        ? Answer.failed(new ServerSideException(ResponseCodeSeries.BadRequest, "Missing Artifact to Validate"))
        : Answer.succeed().withExplanation(introExplainValidation(knowledgeCarrier));
    if (response.isFailure()) {
      return response;
    }
    Answer<Void> tests = knowledgeCarrier.components()
        .map(this::validateComponent)
        .reduce(Answer::merge)
        .orElseGet(Answer::failed);
    return Answer.merge(response, tests);
  }

  protected String introExplainValidation(KnowledgeCarrier knowledgeCarrier) {
    StringBuilder sb = new StringBuilder()
        .append("Validating ").append(knowledgeCarrier.getAssetId().asKey());
    if (knowledgeCarrier.getLabel() != null) {
      sb.append(" - ").append(knowledgeCarrier.getLabel());
    }
    return sb.toString();
  }

  protected abstract Answer<Void> validateComponent(KnowledgeCarrier carrier);

  protected String format(KnowledgeCarrier carrier, ValidationStatus status, String ruleName, String message) {
    return String.format("%s... \t : %3s \t %20s \t %s",
        carrier.getAssetId().asKey().toString().substring(0,4),
        status,
        ruleName,
        message);
  }

  @Override
  public boolean can_applyNamedValidate() {
    return true;
  }

  @Override
  public boolean can_applyValidate() {
    return true;
  }
}
