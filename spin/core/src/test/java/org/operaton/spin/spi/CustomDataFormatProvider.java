/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.spin.spi;


import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class CustomDataFormatProvider implements DataFormatProvider {

  public static final String NAME = "test-data-format";
  public static final ExampleCustomDataFormat DATA_FORMAT = new ExampleCustomDataFormat(NAME);

  @Override
  public String getDataFormatName() {
    return NAME;
  }

  @Override
  public DataFormat<?> createInstance() {
    return DATA_FORMAT;
  }

  @Override
  public DataFormat<?> createInstance(Map<String, Object> configurationProperties) {
    boolean conditionalProperty = (boolean) configurationProperties.getOrDefault("conditional-prop", false);
    DATA_FORMAT.setConditionalProperty(conditionalProperty);
    return DATA_FORMAT;
  }

}

