package edu.mayo.kmdp.language;

import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import edu.mayo.kmdp.util.Util;
import java.util.List;
import org.omg.spec.api4kp._1_0.KnowledgePlatformOperator;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.DetectionOperator;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;

public interface DetectApiOperator
    extends DetectApiInternal.Operator, KnowledgePlatformOperator<DetectionOperator> {

  List<SyntacticRepresentation> getInto();

  default DetectionOperator getDescriptor() {
    return new DetectionOperator()
        .withInto(getInto())
        .withOperatorId(getOperatorId());
  }

  default boolean produces(String into) {
    return Util.isEmpty(into) ||
        ModelMIMECoder.decode(into)
        .map(intoRep -> getInto().stream()
            .anyMatch(supported -> theRepContrastor.isNarrowerOrEqual(supported, intoRep)))
        .orElse(false);
  }

}
