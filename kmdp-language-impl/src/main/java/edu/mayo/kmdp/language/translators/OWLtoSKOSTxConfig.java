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
package edu.mayo.kmdp.language.translators;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import edu.mayo.kmdp.language.detectors.OWLDetectorConfig;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTxConfig.OWLtoSKOSTxParams;
import java.util.Properties;

public class OWLtoSKOSTxConfig extends ConfigProperties<OWLtoSKOSTxConfig, OWLtoSKOSTxParams> {

  private static final Properties defaults = defaulted( OWLDetectorConfig.DetectorParams.class );

  public OWLtoSKOSTxConfig() {
    super( defaults );
  }

  public OWLtoSKOSTxConfig(Properties defaults) {
    super(defaults);
  }

  @Override
  public OWLtoSKOSTxConfig.OWLtoSKOSTxParams[] properties() {
    return OWLtoSKOSTxConfig.OWLtoSKOSTxParams.values();
  }


  public enum OWLtoSKOSTxParams implements Option<OWLtoSKOSTxConfig.OWLtoSKOSTxParams> {

    TGT_NAMESPACE( Opt.of(
        "targetNamespace",
        null,
        "The base URI of the generated SKOS vocabulary",
        String.class,
        true) );

    private Opt opt;

    OWLtoSKOSTxParams( Opt opt ) {
      this.opt = opt;
    }

    @Override
    public Opt getOption() {
      return opt;
    }

  }
}
