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
package org.operaton.bpm.model.dmn.impl.instance;

import static org.operaton.bpm.model.dmn.impl.DmnModelConstants.LATEST_DMN_NS;
import static org.operaton.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_BINDING;

import org.operaton.bpm.model.dmn.instance.Binding;
import org.operaton.bpm.model.dmn.instance.Expression;
import org.operaton.bpm.model.dmn.instance.Parameter;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.child.ChildElement;
import org.operaton.bpm.model.xml.type.child.SequenceBuilder;

public class BindingImpl extends DmnModelElementInstanceImpl implements Binding {

  protected static ChildElement<Parameter> parameterChild;
  protected static ChildElement<Expression> expressionChild;

  public BindingImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public Parameter getParameter() {
    return parameterChild.getChild(this);
  }

  @Override
  public void setParameter(Parameter parameter) {
    parameterChild.setChild(this, parameter);
  }

  @Override
  public Expression getExpression() {
    return expressionChild.getChild(this);
  }

  @Override
  public void setExpression(Expression expression) {
    expressionChild.setChild(this, expression);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Binding.class, DMN_ELEMENT_BINDING)
      .namespaceUri(LATEST_DMN_NS)
      .instanceProvider(BindingImpl::new);

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    parameterChild = sequenceBuilder.element(Parameter.class)
      .required()
      .build();

    expressionChild = sequenceBuilder.element(Expression.class)
      .build();

    typeBuilder.build();
  }

}
