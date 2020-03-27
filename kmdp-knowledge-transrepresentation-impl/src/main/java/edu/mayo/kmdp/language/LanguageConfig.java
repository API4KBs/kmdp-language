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
package edu.mayo.kmdp.language;


import edu.mayo.kmdp.tranx.v4.server.DeserializeApiDelegate;
import edu.mayo.kmdp.tranx.v4.server.DetectApiDelegate;
import edu.mayo.kmdp.tranx.v4.server.TransxionApiDelegate;
import edu.mayo.kmdp.tranx.v4.server.ValidateApiDelegate;
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
@PropertySource(value = {"classpath:application.properties"})
public class LanguageConfig {


}
