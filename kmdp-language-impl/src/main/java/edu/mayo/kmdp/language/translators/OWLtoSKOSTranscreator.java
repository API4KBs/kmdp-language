package edu.mayo.kmdp.language.translators;

import static edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations.Transcreation_Task;
import static org.omg.spec.api4kp.KnowledgeCarrierHelper.rep;

import edu.mayo.kmdp.language.TransxionApi;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTxConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import edu.mayo.kmdp.terms.lexicon._2018._08.Lexicon;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.inject.Named;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.language.TransrepresentationOperator;

@Named
@KPOperation(Transcreation_Task)
public class OWLtoSKOSTranscreator implements TransxionApi {

  @Override
  public KnowledgeCarrier applyTransrepresentation(String txionId, KnowledgeCarrier sourceArtifact,
      Properties params) {

    OWLtoSKOSTxConfig config = new OWLtoSKOSTxConfig(params);

    InputStream is;
    switch (sourceArtifact.getLevel()) {
      case Encoded_Knowledge_Expression:
        is = new ByteArrayInputStream(((BinaryCarrier) sourceArtifact).getEncodedExpression());
        break;
      case Concrete_Knowledge_Expression:
        is = new ByteArrayInputStream(
            ((ExpressionCarrier) sourceArtifact).getSerializedExpression().getBytes());
        break;
      case Parsed_Knowedge_Expression:
      case Abstract_Knowledge_Expression:
      default:
        return null;
    }

    Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model = model.read(is, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new Owl2SkosConverter(config.getTyped(OWLtoSKOSTxParams.TGT_NAMESPACE), Modes.SKOS)
        .run(model, false, false)
        .ifPresent((m) -> m.write(baos));

    String skos = new String(baos.toByteArray());
    return Util.isEmpty(skos)
        ? null
        : KnowledgeCarrier.of(skos)
            .withRepresentation(getTransrepresentationOutput(txionId));
  }

  @Override
  public TransrepresentationOperator getTransrepresentation(String txionId) {
    return new org.omg.spec.api4kp._1_0.services.language.resources.TransrepresentationOperator()
        .withInto(getTransrepresentationOutput(txionId));
  }

  @Override
  public Properties getTransrepresentationAcceptedParameters(String txionId) {
    return new OWLtoSKOSTxConfig();
  }

  @Override
  public SyntacticRepresentation getTransrepresentationOutput(String txionId) {
    return rep(KRLanguage.OWL_2, KRSerialization.RDF_XML_Syntax, KRFormat.XML_1_1)
        .withLexicon(Lexicon.SKOS);
  }

  @Override
  public List<Pointer> listOperators(SyntacticRepresentation from, SyntacticRepresentation into,
      String method) {
    return null;
  }


  @Override
  public KnowledgeCarrier transrepresent(KnowledgeCarrier sourceArtifact,
      SyntacticRepresentation from, SyntacticRepresentation into, String method) {
    return null;
  }
}
