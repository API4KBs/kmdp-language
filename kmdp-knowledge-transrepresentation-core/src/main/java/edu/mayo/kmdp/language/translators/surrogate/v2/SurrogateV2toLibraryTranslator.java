package edu.mayo.kmdp.language.translators.surrogate.v2;

import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Syntactic_Translation_Task;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;

import edu.mayo.kmdp.language.translators.AbstractSimpleTranslator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.hl7.fhir.dstu3.model.Library;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;

@Named
@KPOperation(Syntactic_Translation_Task)
@KPSupport(Knowledge_Asset_Surrogate_2_0)
public class SurrogateV2toLibraryTranslator extends
    AbstractSimpleTranslator<org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset, Library> {

  public static final UUID id = UUID.fromString("e526d596-50d7-47b4-b435-879f6a31e622");
  public static final String version = "1.0.0";

  public SurrogateV2toLibraryTranslator() {
    setId(SemanticIdentifier.newId(id, version));
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return singletonList(rep(Knowledge_Asset_Surrogate_2_0));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return singletonList(rep(FHIR_STU3));
  }

  @Override
  protected Optional<Library> transformAst(ResourceIdentifier assetId,
      org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset expression, SyntacticRepresentation tgtRep,
      Properties config) {
    return Optional.ofNullable(new SurrogateV2ToLibrary().transform(expression));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return Knowledge_Asset_Surrogate_2_0;
  }

  @Override
  public KnowledgeRepresentationLanguage getTargetLanguage() {
    return FHIR_STU3;
  }
}
