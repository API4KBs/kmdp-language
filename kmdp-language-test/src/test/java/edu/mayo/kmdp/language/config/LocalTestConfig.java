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

import edu.mayo.kmdp.language.DeserializeApi;
import edu.mayo.kmdp.language.DetectApi;
import edu.mayo.kmdp.language.TransxionApi;
import edu.mayo.kmdp.language.server.DeserializeApiDelegate;
import edu.mayo.kmdp.language.server.DetectApiDelegate;
import edu.mayo.kmdp.language.server.TransxionApiDelegate;
import javax.inject.Inject;
import org.omg.spec.api4kp._1_0.services.KPComponent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(basePackages = {"edu.mayo.kmdp.language.server"})
@PropertySource(value={"classpath:application.test.properties"})
@Profile("test")
public class LocalTestConfig {

  @Inject
  DetectApiDelegate detector;

  @Bean
  @KPComponent
  public DetectApi detectApi() {
    return DetectApi.newInstance(detector);
  }


  @Inject
  TransxionApiDelegate txor;

  @Bean
  @KPComponent
  public TransxionApi executionApi() {
    return TransxionApi.newInstance(txor);
  }


  @Inject
  DeserializeApiDelegate deser;

  @Bean
  @KPComponent
  public DeserializeApi deserializeApi() {
    return DeserializeApi.newInstance(deser);
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }


}
