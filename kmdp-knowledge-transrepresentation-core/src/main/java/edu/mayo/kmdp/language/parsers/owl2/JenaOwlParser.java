/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.language.parsers.owl2;

import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lifting_Task;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Lowering_Task;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Parsed_Knowedge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.language.DeserializeApiOperator;
import edu.mayo.kmdp.language.parsers.AbstractDeSerializeOperator;
import edu.mayo.kmdp.language.parsers.Lifter;
import edu.mayo.kmdp.language.parsers.Lowerer;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(Lowering_Task)
@KPOperation(Lifting_Task)
@KPSupport(OWL_2)
public class JenaOwlParser extends AbstractDeSerializeOperator {

  public static final UUID id = UUID.fromString("1e912b84-f08f-4d9c-a2ae-30f6f09a27a4");
  public static final String version = "1.0.0";

  public JenaOwlParser() {
    setId(SemanticIdentifier.newId(id,version));
  }

  @Override
  protected List<SyntacticRepresentation> getSupportedRepresentations() {
    return Arrays.asList(
        rep(OWL_2, Turtle, TXT),
        rep(OWL_2, RDF_XML_Syntax, XML_1_1)
    );
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return TXT;
  }

  /**
   * Lifts a binary-encoded expression (byte[]) into a concrete expression (String)
   *
   * @param carrier A binary carrier
   * @return A string carrier
   * @see Lowerer#innerEncode(KnowledgeCarrier, Properties)
   */
  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier, Properties properties) {
    SyntacticRepresentation tgtRep =
        getTargetLiftRepresentation(carrier.getRepresentation(), Concrete_Knowledge_Expression);
    return Optional.of(
        DeserializeApiOperator.newVerticalCarrier(carrier,
            Concrete_Knowledge_Expression,
            tgtRep,
            carrier.asString().orElseThrow(UnsupportedOperationException::new)));
  }

  /**
   * Lifts a concrete expression (String) into a parsed expression (parse tree)
   *
   * @param carrier A string carrier
   * @return A parse tree carrier
   */
  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier, Properties properties) {
    SyntacticRepresentation tgtRep =
        getTargetLiftRepresentation(carrier.getRepresentation(), Parsed_Knowedge_Expression);
    return Optional.of(
        DeserializeApiOperator.newVerticalCarrier(carrier,
            Parsed_Knowedge_Expression,
            tgtRep,
            readModel(carrier.asString().orElseThrow(UnsupportedOperationException::new))));
  }

  /**
   * Lifts a concrete expression (String) into an abstract expression (abstract syntax tree)
   *
   * @param carrier A string carrier
   * @return An abstract syntax tree carrier
   */
  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier, Properties properties) {
    return Optional.empty();
  }

  /**
   * Lifts a parsed expression (parse tree) into an abstract expression (abstract syntax tree)
   *
   * @param carrier A parse tree carrier
   * @return An abstract syntax tree carrier
   */
  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier, Properties properties) {
    return Optional.empty();
  }

  /**
   * Lowers a concrete expression (String) into a binary-encoded expression (byte[])
   *
   * @param carrier A binary carrier
   * @param into    A representation that provides the details of encoding Must be compatible with
   *                the source representation at the language/syntax/serialization level
   * @return A string carrier
   * @see Lifter#innerDecode(KnowledgeCarrier, Properties)
   */
  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    SyntacticRepresentation tgtRep =
        getTargetLowerRepresentation(carrier.getRepresentation(), into, Encoded_Knowledge_Expression);
    return Optional.of(
        DeserializeApiOperator.newVerticalCarrier(carrier,
            Encoded_Knowledge_Expression,
            tgtRep,
            carrier.asBinary().orElseThrow(UnsupportedOperationException::new)));
  }

  /**
   * Lowers an abstract expression (abstract syntax tree) into a concrete expression (string)
   *
   * @param carrier A parse tree carrier
   * @param into    A representation that defines how to derive the serialized, concrete expression
   *                Must be compatible with the source representation at the language level (Note:
   *                only String is currently supported)
   * @return A string carrier
   * @see Lifter#innerParse(KnowledgeCarrier, Properties)
   */
  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    SyntacticRepresentation tgtRep =
        getTargetLowerRepresentation(carrier.getRepresentation(), into,
            Concrete_Knowledge_Expression);
    return carrier.as(Model.class)
        .map(model -> writeModel(model, tgtRep))
        .map(str -> DeserializeApiOperator.newVerticalCarrier(carrier,
            Concrete_Knowledge_Expression,
            tgtRep,
            str));
  }

  /**
   * Lowers a parsed expression (parse tree) into a concrete expression (string)
   *
   * @param carrier A parse tree carrier
   * @param into    A representation that defines how to derive the serialized, concrete expression
   *                Must be compatible with the source representation at the language/syntax level
   *                (Note: only String is currently supported)
   * @return A string carrier
   * @see Lifter#innerDeserialize(KnowledgeCarrier, Properties)
   */
  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    SyntacticRepresentation tgtRep =
        getTargetLowerRepresentation(carrier.getRepresentation(), into,
            Concrete_Knowledge_Expression);
    return carrier.as(Model.class)
        .map(model -> writeModel(model, tgtRep))
        .map(str -> DeserializeApiOperator.newVerticalCarrier(carrier,
            Concrete_Knowledge_Expression,
            tgtRep,
            str));
  }

  /**
   * Lowers an abstract expression (abstract syntax tree) into a parsed expression for a given
   * syntax (parse tree)
   *
   * @param carrier A parse tree carrier
   * @param into    A representation that defines how to derive the concrete expression in a given
   *                syntax Must be compatible with the source representation at the language level
   * @return A string carrier
   * @see Lifter#innerAbstract(KnowledgeCarrier, Properties)
   */
  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier,
      SyntacticRepresentation into, Properties properties) {
    return Optional.empty();
  }



  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return OWL_2;
  }

  private Model readModel(String str) {
    Model m = ModelFactory.createOntologyModel();
    return m.read(new ByteArrayInputStream(str.getBytes()), null);
  }

  private String writeModel(Model model, SyntacticRepresentation tgtRep) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String lang = "RDF/XML";
    if (tgtRep.getSerialization() != null) {
      switch (tgtRep.getSerialization().asEnum()) {
        case Turtle:
          lang = "TTL";
          break;
        case RDF_XML_Syntax:
        default:
          lang = "RDF/XML";
      }
    }
    model.write(baos, lang);
    return new String(baos.toByteArray());
  }

  private SyntacticRepresentation getTargetLowerRepresentation(
      SyntacticRepresentation srcRep,
      SyntacticRepresentation into,
      ParsingLevelSeries tgtLevel) {
    // TODO improve...
    return into;
  }

  private SyntacticRepresentation getTargetLiftRepresentation(SyntacticRepresentation srcRep,
      ParsingLevel tgtLevel) {
    if (srcRep == null) {
      if (tgtLevel.sameAs(Encoded_Knowledge_Expression)
          || tgtLevel.sameAs(Concrete_Knowledge_Expression)) {
        return rep(OWL_2, TXT);
      } else {
        return rep(OWL_2);
      }
    } else {
      switch (tgtLevel.asEnum()) {
        case Encoded_Knowledge_Expression:
          return rep(
              srcRep.getLanguage(), srcRep.getProfile(),
              srcRep.getSerialization(), srcRep.getFormat(),
              Charset.forName(srcRep.getCharset()),
              srcRep.getEncoding());
        case Concrete_Knowledge_Expression:
          return rep(srcRep.getLanguage(), srcRep.getProfile(),
              srcRep.getSerialization(), srcRep.getFormat());
        case Parsed_Knowedge_Expression:
          return rep(srcRep.getLanguage(), srcRep.getProfile(),
              srcRep.getSerialization());
        case Abstract_Knowledge_Expression:
          return rep(srcRep.getLanguage(), srcRep.getProfile());
        default:
          throw new UnsupportedOperationException();
      }
    }
  }
}
