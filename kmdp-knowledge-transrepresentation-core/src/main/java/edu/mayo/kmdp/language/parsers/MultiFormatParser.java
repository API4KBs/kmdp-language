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
package edu.mayo.kmdp.language.parsers;


import static edu.mayo.kmdp.comparator.Contrastor.isNarrowerOrEqual;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
@KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
public abstract class MultiFormatParser<T> extends AbstractDeSerializer implements
    DeserializeApiInternal {

  protected XMLBasedLanguageParser<T> xmlParser;
  protected JSONBasedLanguageParser<T> jsonParser;

  private List<AbstractDeSerializer> parserSet;

  protected MultiFormatParser(XMLBasedLanguageParser<T> xmlParser,
      JSONBasedLanguageParser<T> jsonParser) {
    this.xmlParser = xmlParser;
    this.jsonParser = jsonParser;
    this.parserSet = Arrays.asList(xmlParser,jsonParser);
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.innerAbstract(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }

  private boolean isCandidate(AbstractDeSerializer candidate, SyntacticRepresentation argumentRep) {
    return candidate.getSupportedRepresentations().stream()
        .anyMatch(supportedRep -> isNarrowerOrEqual(theRepContrastor.contrast(supportedRep,argumentRep)));
  }

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.innerDecode(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.innerDeserialize(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.innerParse(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }





  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.innerConcretize(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.innerConcretize(carrier);
      case JSON:
        return jsonParser.innerConcretize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.innerEncode(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.innerEncode(carrier);
      case JSON:
        return jsonParser.innerEncode(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.innerExternalize(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.innerExternalize(carrier);
      case JSON:
        return jsonParser.innerExternalize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.innerSerialize(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.innerSerialize(carrier);
      case JSON:
        return jsonParser.innerSerialize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return null;
  }


}
