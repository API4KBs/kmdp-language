package edu.mayo.kmdp.language.translators.fhir.stu3;

import java.net.URI;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

public final class FHIRTranslatorUtils {

  private FHIRTranslatorUtils() {
    // static functions only
  }

  public static CodeableConcept toCodeableConcept(Term trm) {
    return new CodeableConcept()
        .addCoding(toCoding(trm));
  }

  public static Coding toCoding(Term trm) {
    return new Coding()
        .setCode(trm.getTag())
        .setDisplay(trm.getLabel())
        .setSystem(trm.getNamespaceUri().toString())
        .setVersion(trm.getVersionTag());
  }

  public static Term toTerm(Coding c) {
    // TODO fixme add constructor that supports label
    ConceptIdentifier cid =
        (ConceptIdentifier) Term.newTerm(URI.create(c.getSystem()), c.getCode(), c.getVersion());
    cid.withName(c.getDisplay());
    return cid;
  }

}
