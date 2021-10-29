package edu.mayo.kmdp.language.validators;

import static edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries.BadRequest;
import static edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries.PreconditionFailed;
import static edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries.UnprocessableEntity;

import edu.mayo.kmdp.language.ValidateApiOperator;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.Explainer;
import org.omg.spec.api4kp._20200801.Explainer.InfoProblem;
import org.omg.spec.api4kp._20200801.Explainer.IssueProblem;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.ValidateApiInternal._applyNamedValidate;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.ValidateApiInternal._applyValidate;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.zalando.problem.Problem;

public abstract class AbstractValidator
    implements ValidateApiOperator,
    _applyValidate,
    _applyNamedValidate {

  protected enum ValidationStatus {OK, ERR, WRN, INF, FATAL}

  public Answer<Void> applyNamedValidate(UUID uuid, KnowledgeCarrier knowledgeCarrier,
      String config) {
    return uuid.equals(getOperatorId().getUuid())
        ? applyValidate(knowledgeCarrier, config)
        : Answer.unsupported();
  }

  @Override
  public Answer<Void> applyValidate(KnowledgeCarrier knowledgeCarrier, String s) {
    Answer<Void> response = knowledgeCarrier.componentList().isEmpty()
        ? Answer.failed(new ServerSideException(BadRequest, "Missing Artifact to Validate"))
        : Answer.succeed().withExplanationDetail(introExplainValidation(knowledgeCarrier));
    if (response.isFailure()) {
      return response;
    }
    return Stream.concat(
            Stream.of(response),
            knowledgeCarrier.components().map(this::validateComponent))
        .reduce(Answer::merge)
        .orElseGet(Answer::failed);
  }

  protected Problem introExplainValidation(KnowledgeCarrier knowledgeCarrier) {
    return new InfoProblem(
        "Validation",
        "Validating " + knowledgeCarrier.getLabel(),
        knowledgeCarrier.getAssetId().getVersionId());
  }

  protected abstract Answer<Void> validateComponent(KnowledgeCarrier carrier);

  protected Problem format(String key, ValidationStatus status, String ruleName, String message) {
    switch (status) {
      case OK:
      case INF:
        return new InfoProblem(ruleName, message, URI.create(key));
      case WRN:
      case ERR:
        return new IssueProblem(ruleName, PreconditionFailed, message, URI.create(key));
      case FATAL:
        return new ServerSideException(
            Explainer.GENERIC_ERROR_TYPE,
            ruleName,
            UnprocessableEntity,
            message,
            URI.create(key));
      default:
        throw new IllegalStateException("This cannot happen");
    }
  }


  protected String mapAssetId(KnowledgeCarrier carrier) {
    return mapResourceId(carrier.getAssetId(), 4);
  }

  protected String mapResourceId(ResourceIdentifier rid, int len) {
    return rid.getVersionId() != null
        ? rid.getVersionId().toString()
        : rid.getResourceId().toString();
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
