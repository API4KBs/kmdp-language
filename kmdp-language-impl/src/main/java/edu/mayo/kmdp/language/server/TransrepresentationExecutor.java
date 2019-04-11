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
package edu.mayo.kmdp.language.server;

import edu.mayo.kmdp.language.TransxionApi;
import edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Named
public class TransrepresentationExecutor implements TransxionApiDelegate {


  private Map<SourceToTarget, edu.mayo.kmdp.language.TransxionApi> translatorBySourceTarget;
  private Map<String, edu.mayo.kmdp.language.TransxionApi> translatorById;


  public TransrepresentationExecutor(@Autowired(required = false)
  @KPOperation(KnowledgeOperations.Translation_Task)
      List<TransxionApi> translators) {
    super();
    if (translators != null) {
//      this.translatorBySourceTarget = Maps.uniqueIndex(translators,
//          translator -> new SourceToTarget(translator.getFrom(),
//              translator.getTo()));
//
//      this.translatorById = Maps.uniqueIndex(translators, Translator::getId);
    } else {
      this.translatorBySourceTarget = Collections.emptyMap();
      this.translatorById = Collections.emptyMap();
    }

  }

  @Override
  public ResponseEntity<Void> transrepresent(SyntacticRepresentation from,
      SyntacticRepresentation into, String method) {
    return null;
  }


  private static class SourceToTarget {

    public SyntacticRepresentation from;

    public SyntacticRepresentation to;

    public SourceToTarget(SyntacticRepresentation from, SyntacticRepresentation to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SourceToTarget that = (SourceToTarget) o;
      return Objects.equals(from, that.from) &&
          Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to);
    }
  }
}
