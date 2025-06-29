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
package org.operaton.bpm.engine.rest.dto.runtime;

import org.operaton.bpm.engine.rest.dto.LinkableDto;
import org.operaton.bpm.engine.runtime.ProcessInstance;

public class ProcessInstanceDto extends LinkableDto {

  private String id;
  private String definitionId;
  private String businessKey;
  private String caseInstanceId;
  private boolean ended;
  private boolean suspended;
  private String tenantId;
  private String definitionKey;

  public ProcessInstanceDto() {
  }

  public ProcessInstanceDto(ProcessInstance instance) {
    this.id = instance.getId();
    this.definitionId = instance.getProcessDefinitionId();
    this.definitionKey = instance.getProcessDefinitionKey();
    this.businessKey = instance.getBusinessKey();
    this.caseInstanceId = instance.getCaseInstanceId();
    this.ended = instance.isEnded();
    this.suspended = instance.isSuspended();
    this.tenantId = instance.getTenantId();
  }

  public String getId() {
    return id;
  }

  public String getDefinitionId() {
    return definitionId;
  }

  public String getDefinitionKey() {
    return definitionKey;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public boolean isEnded() {
    return ended;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static ProcessInstanceDto fromProcessInstance(ProcessInstance instance) {
    return new ProcessInstanceDto(instance);
  }

}
