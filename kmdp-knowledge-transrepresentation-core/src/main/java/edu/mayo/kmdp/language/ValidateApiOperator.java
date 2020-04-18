package edu.mayo.kmdp.language;

import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.tranx.v4.server.ValidateApiInternal;
import edu.mayo.kmdp.util.Util;
import java.util.List;
import org.omg.spec.api4kp._1_0.KnowledgePlatformOperator;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;
import org.omg.spec.api4kp._1_0.services.tranx.ValidationOperator;

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
