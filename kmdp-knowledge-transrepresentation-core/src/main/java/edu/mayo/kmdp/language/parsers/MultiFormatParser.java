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


import static edu.mayo.kmdp.comparator.Contrastor.isBroaderOrEqual;
import static edu.mayo.kmdp.comparator.Contrastor.isNarrowerOrEqual;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.theRepContrastor;

import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

@Named
@KPOperation(KnowledgeProcessingOperationSeries.Lowering_Task)
@KPOperation(KnowledgeProcessingOperationSeries.Lifting_Task)
public abstract class MultiFormatParser<T> extends AbstractDeSerializer {

  private List<AbstractDeSerializer> parserSet;

  protected MultiFormatParser(XMLBasedLanguageParser<T> xmlParser,
      JSONBasedLanguageParser<T> jsonParser) {
    this.parserSet = Arrays.asList(xmlParser,jsonParser);
  }

  @Override
  public Optional<KnowledgeCarrier> innerAbstract(KnowledgeCarrier carrier) {
    return processLift(
        carrier,
        Lifter::innerAbstract);
  }

  @Override
  public Optional<KnowledgeCarrier> innerDecode(KnowledgeCarrier carrier) {
    return processLift(
        carrier,
        Lifter::innerDecode);
  }

  @Override
  public Optional<KnowledgeCarrier> innerDeserialize(KnowledgeCarrier carrier) {
    return processLift(
        carrier,
        Lifter::innerDeserialize);
  }

  @Override
  public Optional<KnowledgeCarrier> innerParse(KnowledgeCarrier carrier) {
    return processLift(
        carrier,
        Lifter::innerParse);
  }


  private boolean isLiftCandidate(AbstractDeSerializer candidate, SyntacticRepresentation argumentRep) {
    return candidate.getSupportedRepresentations().stream()
        .anyMatch(supportedRep -> isNarrowerOrEqual(theRepContrastor.contrast(supportedRep,argumentRep)));
  }

  private boolean isLowerCandidate(AbstractDeSerializer candidate, SyntacticRepresentation argumentRep) {
    return candidate.getSupportedRepresentations().stream()
        .anyMatch(supportedRep -> isBroaderOrEqual(theRepContrastor.contrast(supportedRep,argumentRep)));
  }

  protected Optional<KnowledgeCarrier> processLift(KnowledgeCarrier source,
      BiFunction<AbstractDeSerializer,KnowledgeCarrier,Optional<KnowledgeCarrier>> mapper) {
    return process(
        source,
        mapper,
        parser -> isLiftCandidate(parser, source.getRepresentation()));
  }

  protected Optional<KnowledgeCarrier> processLower(KnowledgeCarrier source,
      BiFunction<AbstractDeSerializer,KnowledgeCarrier,Optional<KnowledgeCarrier>> mapper) {
    return process(
        source,
        mapper,
        parser -> isLowerCandidate(parser, source.getRepresentation()));
  }

  protected Optional<KnowledgeCarrier> process(KnowledgeCarrier source,
      BiFunction<AbstractDeSerializer,KnowledgeCarrier,Optional<KnowledgeCarrier>> mapper,
      Predicate<AbstractDeSerializer> test) {
    return parserSet.stream()
        .filter(test)
        .map(l -> mapper.apply(l,source))
        .flatMap(StreamUtil::trimStream)
        .findFirst();
  }


  @Override
  public Optional<KnowledgeCarrier> innerConcretize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    return processLower(
        carrier,
        Lowerer::innerConcretize);
  }

  @Override
  public Optional<KnowledgeCarrier> innerEncode(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    return processLower(
        carrier,
        Lowerer::innerEncode);
  }

  @Override
  public Optional<KnowledgeCarrier> innerExternalize(KnowledgeCarrier carrier, SyntacticRepresentation into) {
    return processLower(
        carrier,
        Lowerer::innerExternalize);
  }

  @Override
  public Optional<KnowledgeCarrier> innerSerialize(KnowledgeCarrier carrier,
      SyntacticRepresentation into) {
    return processLower(
        carrier,
        Lowerer::innerSerialize);
  }

  @Override
  protected SerializationFormat getDefaultFormat() {
    return null;
  }

}
