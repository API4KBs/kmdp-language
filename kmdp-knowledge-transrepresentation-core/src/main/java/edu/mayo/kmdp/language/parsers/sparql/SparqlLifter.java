/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.language.parsers.sparql;


import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries.Lifting_Task;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.SPARQL_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.language.DeserializeApiOperator;
import edu.mayo.kmdp.language.parsers.Lifter;
import edu.mayo.kmdp.util.PropertiesUtil;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KPOperation;
import org.omg.spec.api4kp._20200801.services.KPSupport;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;

;

@Named
@KPOperation(Lifting_Task)
@KPSupport(SPARQL_1_1)
public class SparqlLifter
    implements DeserializeApiOperator, Lifter {

  public static final UUID id = UUID.fromString("21652a7d-c5c8-4c57-b483-2af561ba778e");
  public static final String version = "1.0.0";

  private final ResourceIdentifier operatorId;

  public SparqlLifter() {
    this.operatorId = SemanticIdentifier.newId(id,version);
  }

  @Override
  public Answer<KnowledgeCarrier> applyLift(KnowledgeCarrier knowledgeCarrier,
      ParsingLevel parsingLevel, String xAccept, String config) {
    Properties props = PropertiesUtil.parseProperties(config);
    //TODO should check for consistency between source level and targetLevel;

    switch (parsingLevel.asEnum()) {
      case Serialized_Knowledge_Expression:
        switch (knowledgeCarrier.getLevel().asEnum()) {
          case Encoded_Knowledge_Expression:
            return Answer.of(innerDecode(knowledgeCarrier, props));
          default:
            return Answer.unsupported();
        }
      case Concrete_Knowledge_Expression:
        switch (knowledgeCarrier.getLevel().asEnum()) {
          case Encoded_Knowledge_Expression:
            return Answer.of(
                innerDecode(knowledgeCarrier, props)
                .flatMap( str -> innerDeserialize(str, props)));
          case Serialized_Knowledge_Expression:
            return Answer.of(
                innerDeserialize(knowledgeCarrier, props));
          case Concrete_Knowledge_Expression:
            return Answer.of(knowledgeCarrier);
          default:
            return Answer.unsupported();
        }
      case Abstract_Knowledge_Expression:
        switch (knowledgeCarrier.getLevel().asEnum()) {
          case Encoded_Knowledge_Expression:
            return Answer.of(
                innerDecode(knowledgeCarrier, props)
                    .flatMap(str -> innerParse(str, props)));
          case Serialized_Knowledge_Expression:
            return Answer.of(
                innerParse(knowledgeCarrier, props));
          case Concrete_Knowledge_Expression:
            return Answer.of(innerAbstract(knowledgeCarrier, props));
          default:
            return Answer.of(knowledgeCarrier);
        }

      default:
        throw new UnsupportedOperationException();

    }
  }

  @Override
  public List<SyntacticRepresentation> getFrom() {
    return Collections.singletonList(rep(SPARQL_1_1,TXT));
  }

  @Override
  public List<SyntacticRepresentation> getInto() {
    return Collections.singletonList(rep(SPARQL_1_1));
  }

  @Override
  public ResourceIdentifier getOperatorId() {
    return operatorId;
  }

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier, Properties props) {
    String sparql = carrier.asString()
        .orElseThrow(UnsupportedOperationException::new);
    return Optional.of(
        DeserializeApiOperator
            .newVerticalCarrier(carrier, Serialized_Knowledge_Expression, rep(SPARQL_1_1,TXT), sparql));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier, Properties props) {
    ParameterizedSparqlString sparql = carrier.asString()
        .map(ParameterizedSparqlString::new)
        .orElseThrow(UnsupportedOperationException::new);
    return Optional.of(
        DeserializeApiOperator
            .newVerticalCarrier(carrier, Concrete_Knowledge_Expression, rep(SPARQL_1_1), sparql));
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier, Properties props) {
    Query sparql = carrier.asString()
        .map(ParameterizedSparqlString::new)
        .map(ParameterizedSparqlString::asQuery)
        .orElseThrow(UnsupportedOperationException::new);
    return Optional.of(
        DeserializeApiOperator
            .newVerticalCarrier(carrier, Abstract_Knowledge_Expression, rep(SPARQL_1_1), sparql));
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier, Properties props) {
    Query sparql = carrier.as(ParameterizedSparqlString.class)
        .map(ParameterizedSparqlString::asQuery)
        .orElseThrow(UnsupportedOperationException::new);
    return Optional.of(
        DeserializeApiOperator
            .newVerticalCarrier(carrier, Abstract_Knowledge_Expression, rep(SPARQL_1_1), sparql));
  }

  @Override
  public KnowledgeRepresentationLanguage getSupportedLanguage() {
    return SPARQL_1_1;
  }
}
