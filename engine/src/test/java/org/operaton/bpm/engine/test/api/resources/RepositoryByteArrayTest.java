/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.operaton.bpm.engine.test.api.resources;

import static org.operaton.bpm.engine.repository.ResourceTypes.REPOSITORY;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.operaton.bpm.engine.IdentityService;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.identity.Picture;
import org.operaton.bpm.engine.identity.User;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.operaton.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.repository.Resource;
import org.operaton.bpm.engine.test.ProcessEngineRule;
import org.operaton.bpm.engine.test.util.ProcessEngineTestRule;
import org.operaton.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class RepositoryByteArrayTest {
  protected static final String USER_ID = "johndoe";
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;


  @Before
  public void initServices() {
    configuration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
  }

  @After
  public void cleanUp() {
    identityService.deleteUser(USER_ID);
  }

  @Test
  public void testResourceBinary() {
    Date fixedDate = new Date();
    ClockUtil.setCurrentTime(fixedDate);

    String bpmnDeploymentId = testRule.deploy("org/operaton/bpm/engine/test/repository/one.bpmn20.xml").getId();
    String dmnDeploymentId = testRule.deploy("org/operaton/bpm/engine/test/repository/one.dmn").getId();
    String cmmnDeplymentId = testRule.deploy("org/operaton/bpm/engine/test/repository/one.cmmn").getId();

    checkResource(fixedDate, bpmnDeploymentId);
    checkResource(fixedDate, dmnDeploymentId);
    checkResource(fixedDate, cmmnDeplymentId);
  }

  @Test
  public void testFormsBinaries() {
    Date fixedDate = new Date();
    ClockUtil.setCurrentTime(fixedDate);

    String deploymentId = testRule.deploy("org/operaton/bpm/engine/test/api/form/DeployedFormsProcess.bpmn20.xml",
        "org/operaton/bpm/engine/test/api/form/start.html",
        "org/operaton/bpm/engine/test/api/form/task.html",
        "org/operaton/bpm/engine/test/api/authorization/renderedFormProcess.bpmn20.xml",
        "org/operaton/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();

    List<Resource> deploymentResources = repositoryService.getDeploymentResources(deploymentId);
    assertThat(deploymentResources).hasSize(5);
    for (Resource resource : deploymentResources) {
      ResourceEntity entity = (ResourceEntity) resource;
      checkEntity(fixedDate, entity);
    }
  }

  @Test
  public void testUserPictureBinary() {
    // when
    Date fixedDate = new Date();
    ClockUtil.setCurrentTime(fixedDate);
    User user = identityService.newUser(USER_ID);
    identityService.saveUser(user);
    String userId = user.getId();

    Picture picture = new Picture("niceface".getBytes(), "image/string");
    identityService.setUserPicture(userId, picture);
    String userInfo = identityService.getUserInfo(USER_ID, "picture");

    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired()
        .execute(new GetByteArrayCommand(userInfo));

    // then
    assertThat(byteArrayEntity).isNotNull();
    assertThat(byteArrayEntity.getCreateTime()).hasToString(fixedDate.toString());
    assertThat(byteArrayEntity.getType()).isEqualTo(REPOSITORY.getValue());
  }


  protected void checkResource(Date expectedDate, String deploymentId) {
    List<Resource> deploymentResources = repositoryService.getDeploymentResources(deploymentId);
    assertThat(deploymentResources).hasSize(1);
    ResourceEntity resource = (ResourceEntity) deploymentResources.get(0);
    checkEntity(expectedDate, resource);
  }

  protected void checkEntity(Date expectedDate, ResourceEntity entity) {
    assertThat(entity).isNotNull();
    assertThat(entity.getCreateTime()).hasToString(expectedDate.toString());
    assertThat(entity.getType()).isEqualTo(REPOSITORY.getValue());
  }
}
