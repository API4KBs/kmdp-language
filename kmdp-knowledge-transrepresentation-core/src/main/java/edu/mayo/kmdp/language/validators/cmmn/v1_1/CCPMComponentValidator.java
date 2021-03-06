package edu.mayo.kmdp.language.validators.cmmn.v1_1;

import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Published;

import edu.mayo.kmdp.language.validators.AbstractValidator;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatus;

public abstract class CCPMComponentValidator extends AbstractValidator {


  protected Answer<Void> validateComponent(KnowledgeCarrier carrier) {
    if (carrier.is(KnowledgeAsset.class)) {
      return carrier.as(KnowledgeAsset.class)
          .map(ka -> this.validate(ka, carrier))
          .orElseGet(Answer::failed);
    } else if (carrier.is(org.omg.spec.dmn._20180521.model.TDefinitions.class)) {
      return carrier.as(org.omg.spec.dmn._20180521.model.TDefinitions.class)
          .map(kc -> this.validate(kc, carrier))
          .orElseGet(Answer::failed);
    } else if (carrier.is(org.omg.spec.cmmn._20151109.model.TDefinitions.class)) {
      return carrier.as(org.omg.spec.cmmn._20151109.model.TDefinitions.class)
          .map(kc -> this.validate(kc, carrier))
          .orElseGet(Answer::failed);
    } else {
      return Answer.failed();
    }
  }

  protected Answer<Void> validate(
      KnowledgeAsset knowledgeAsset, KnowledgeCarrier carrier) {
    return illegal();
  }

  protected Answer<Void> validate(
      org.omg.spec.dmn._20180521.model.TDefinitions decisionModel, KnowledgeCarrier carrier) {
    return illegal();
  }

  protected Answer<Void> validate(
      org.omg.spec.cmmn._20151109.model.TDefinitions caseModel, KnowledgeCarrier carrier) {
    return illegal();
  }

  private Answer<Void> illegal() {
    return Answer.failed(new IllegalStateException("This should not have been called"));
  }

  /**
   * Validates the presence of an Asset ID
   * (not having one at this point is a catastrophic illegal state)
   * @param knowledgeAsset
   * @param carrier
   * @return
   */
  protected Answer<Void> validateAssetId(KnowledgeAsset knowledgeAsset, KnowledgeCarrier carrier) {
    boolean success = knowledgeAsset.getAssetId() != null;
    return validationResponse(
        carrier,
        success,
        "Asset ID",
        () -> "found " + knowledgeAsset.getAssetId().asKey(),
        () -> "MISSING AssetId"
    );
  }

  /**
   * Validates that a model is in Published state
   * @param knowledgeAsset
   * @param carrier
   * @return
   */
  protected Answer<Void> validatePublicationStatus(KnowledgeAsset knowledgeAsset, KnowledgeCarrier carrier) {
    PublicationStatus status = knowledgeAsset.getLifecycle().getPublicationStatus();
    boolean success = Published.sameAs(status);
    return validationResponse(
        carrier,
        success,
        "Status",
        status::getLabel,
        () -> "NOT Published - " + status.getLabel().toUpperCase()
    );
  }

  /**
   * Checks for the present of a formal Asset Type term in the Surrogate
   * @param knowledgeAsset
   * @param carrier
   * @param types
   * @return
   */
  protected Answer<Void> validateAssetType(KnowledgeAsset knowledgeAsset, KnowledgeCarrier carrier,
      KnowledgeAssetType... types ) {
    List<KnowledgeAssetType> actualTypes = knowledgeAsset.getFormalType();
    List<KnowledgeAssetType> expectedTypes = Arrays.stream(types).collect(Collectors.toList());
    List<KnowledgeAssetType> missingTypes = expectedTypes.stream()
        .filter(type -> actualTypes.stream().noneMatch(t -> t.sameAs(type)))
        .collect(Collectors.toList());
    boolean success = missingTypes.isEmpty();

    return validationResponse(
        carrier,
        success,
        "Asset Type",
        () -> "found " + toString(actualTypes, Term::getLabel),
        () -> "MISSING " + toString(missingTypes, Term::getLabel)
            + " BUT FOUND  " + toString(actualTypes, Term::getLabel));
  }


  protected <T> String toString(Collection<T> list, Function<T,String> mapper) {
    return list.stream()
        .map(mapper)
        .collect(Collectors.joining(","));
  }

  protected Answer<Void> validationResponse(KnowledgeCarrier carrier, boolean outcome,
      String ruleName, Supplier<String> successMsg, Supplier<String> failMsg) {
    return Answer.succeed().withExplanation(
        format(
            carrier,
            outcome ? ValidationStatus.OK : ValidationStatus.ERR,
            ruleName,
            outcome ? successMsg.get() : failMsg.get()));
  }

  @SuppressWarnings("unchecked")
  protected Answer<Void> allOf(Answer<Void>... partials) {
    return Stream.of(partials)
        .reduce(Answer::merge)
        .orElseGet(Answer::failed);
  }

}
