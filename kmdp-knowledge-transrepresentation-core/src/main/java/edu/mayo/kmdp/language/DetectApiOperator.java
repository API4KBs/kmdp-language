package edu.mayo.kmdp.language;

import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import java.util.List;
import org.omg.spec.api4kp._1_0.KnowledgePlatformOperator;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.DetectionOperator;

public interface DetectApiOperator
    extends DetectApiInternal.Operator, KnowledgePlatformOperator<DetectionOperator> {

  List<SyntacticRepresentation> getInto();

  default DetectionOperator getDescriptor() {
    return new DetectionOperator()
        .withInto(getInto())
        .withOperatorId(getOperatorId());
  }

}
