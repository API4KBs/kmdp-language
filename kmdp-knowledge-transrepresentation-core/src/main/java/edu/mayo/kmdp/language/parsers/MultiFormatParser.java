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

import edu.mayo.kmdp.tranx.server.DeserializeApiDelegate;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
@KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
public abstract class MultiFormatParser<T> extends AbstractDeSerializer implements
    DeserializeApiDelegate {

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
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.abstrakt(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }

  private boolean isCandidate(AbstractDeSerializer candidate, SyntacticRepresentation argumentRep) {
    return candidate.getSupportedRepresentations().stream()
        .anyMatch(supportedRep -> isNarrowerOrEqual(theRepContrastor.contrast(supportedRep,argumentRep)));
  }

  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.decode(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.deserialize(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    return parserSet.stream()
        .filter(l -> isCandidate(l, carrier.getRepresentation()))
        .map(l -> l.parse(carrier))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }





  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.concretize(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.concretize(carrier);
      case JSON:
        return jsonParser.concretize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.encode(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.encode(carrier);
      case JSON:
        return jsonParser.encode(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier, SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.externalize(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.externalize(carrier);
      case JSON:
        return jsonParser.externalize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier,
      SyntacticRepresentation into) {
    if (into == null) {
      return xmlParser.serialize(carrier);
    }
    switch (into.getFormat().asEnum()) {
      case XML_1_1:
        return xmlParser.serialize(carrier);
      case JSON:
        return jsonParser.serialize(carrier);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return null;
  }


}
