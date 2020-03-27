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
package edu.mayo.kmdp.language.config;

import edu.mayo.kmdp.language.LanguageDeSerializer;
import edu.mayo.kmdp.language.LanguageDetector;
import edu.mayo.kmdp.language.LanguageValidator;
import edu.mayo.kmdp.language.TransrepresentationExecutor;
import edu.mayo.kmdp.tranx.v4.DeserializeApi;
import edu.mayo.kmdp.tranx.v4.DetectApi;
import edu.mayo.kmdp.tranx.v4.TransxionApi;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiDelegate;
import edu.mayo.kmdp.tranx.v4.server.DetectApiDelegate;
import edu.mayo.kmdp.tranx.v4.server.TransxionApiDelegate;
import edu.mayo.kmdp.tranx.v4.server.ValidateApiDelegate;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackageClasses = {
    TransxionApiDelegate.class,
    TransrepresentationExecutor.class,
    DeserializeApiDelegate.class,
    LanguageDeSerializer.class,
    DetectApiDelegate.class,
    LanguageDetector.class,
    ValidateApiDelegate.class,
    LanguageValidator.class})
@PropertySource(value={"classpath:application.test.properties"})
public class LocalTestConfig {


  @Bean
  @KPComponent
  public DetectApi detectApi(@Autowired @KPServer DetectApiDelegate detector) {
    return DetectApi.newInstance(detector);
  }

  @Bean
  @KPComponent
  public TransxionApi txionApi(@Autowired @KPServer TransxionApiDelegate txor) {
    return TransxionApi.newInstance(txor);
  }

  @Bean
  @KPComponent
  public DeserializeApi deserializeApi(@Autowired @KPServer DeserializeApiDelegate deser) {
    return DeserializeApi.newInstance(deser);
  }

}


