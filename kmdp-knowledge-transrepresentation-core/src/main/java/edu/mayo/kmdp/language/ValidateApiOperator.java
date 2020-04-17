package edu.mayo.kmdp.language;

import edu.mayo.kmdp.tranx.v4.server.ValidateApiInternal;
import java.util.List;
import org.omg.spec.api4kp._1_0.KnowledgePlatformOperator;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ValidationOperator;

public interface ValidateApiOperator
    extends ValidateApiInternal.Operator, KnowledgePlatformOperator<ValidationOperator> {

  List<SyntacticRepresentation> getFrom();

  default ValidationOperator getDescriptor() {
    return new ValidationOperator()
        .withFrom(getFrom())
        .withOperatorId(getOperatorId());
  }

}
