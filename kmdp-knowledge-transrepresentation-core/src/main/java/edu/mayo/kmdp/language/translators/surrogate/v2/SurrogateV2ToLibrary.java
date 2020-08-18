package edu.mayo.kmdp.language.translators.surrogate.v2;

import org.hl7.fhir.dstu3.model.Library;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;

public class SurrogateV2ToLibrary {

  //TODO: map surrogate v2 in a similar manner to surrogate v1 into the library object.
  //Consolidate like mappings from the SurrogateToLibrary class and this one together to cut duplication.
  //not needed currently.
  public Library transform(KnowledgeAsset knowledgeAsset) {
    throw new UnsupportedOperationException(
        "Surroate v2 to FHIR library object not supported yet within framework.");
  }
}
