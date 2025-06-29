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
package org.operaton.bpm.model.bpmn.instance;

import org.junit.jupiter.api.BeforeEach;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.GatewayDirection;
import org.operaton.bpm.model.xml.impl.util.ReflectUtil;
import org.operaton.bpm.model.xml.instance.ModelElementInstance;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.model.bpmn.impl.BpmnModelConstants.OPERATON_NS;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractGatewayTest<G extends Gateway> extends BpmnModelElementInstanceTest {

  protected G gateway;

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(Gateway.class, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption(OPERATON_NS, "asyncBefore", false, false, false),
      new AttributeAssumption(OPERATON_NS, "asyncAfter", false, false, false)
    );
  }

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void getGateway() {
    InputStream inputStream = ReflectUtil.getResourceAsStream("org/operaton/bpm/model/bpmn/GatewaysTest.xml");
    Collection<ModelElementInstance> elementInstances = Bpmn.readModelFromStream(inputStream).getModelElementsByType(modelElementType);
    assertThat(elementInstances).hasSize(1);
    gateway = (G) elementInstances.iterator().next();
    assertThat(gateway.getGatewayDirection()).isEqualTo(GatewayDirection.Mixed);
  }

}
