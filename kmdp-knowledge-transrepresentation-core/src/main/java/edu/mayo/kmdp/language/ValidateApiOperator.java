package edu.mayo.kmdp.language;

import static org.omg.spec.api4kp._20200801.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.util.Util;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.KnowledgePlatformOperator;
import org.omg.spec.api4kp._20200801.api.transrepresentation.v4.server.ValidateApiInternal;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ValidationOperator;

public interface ValidateApiOperator
    extends ValidateApiInternal.Operator, KnowledgePlatformOperator<ValidationOperator> {

  List<SyntacticRepresentation> getFrom();

  default ValidationOperator getDescriptor() {
    return new ValidationOperator()
        .withFrom(getFrom())
        .withOperatorId(getOperatorId());
  }

  default boolean consumes(String from) {
    return Util.isEmpty(from) ||
        ModelMIMECoder.decode(from)
        .map(fromRep -> getFrom().stream()
            .anyMatch(supported -> theRepContrastor.isBroaderOrEqual(supported, fromRep)))
        .orElse(false);
  }

}
