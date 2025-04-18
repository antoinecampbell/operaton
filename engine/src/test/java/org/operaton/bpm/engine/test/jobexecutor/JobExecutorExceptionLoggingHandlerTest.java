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
package org.operaton.bpm.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.operaton.bpm.engine.test.ProcessEngineRule;
import org.operaton.bpm.engine.test.util.ProcessEngineTestRule;
import org.operaton.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class JobExecutorExceptionLoggingHandlerTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ExecuteJobHelper.ExceptionLoggingHandler originalHandler;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    originalHandler = ExecuteJobHelper.loggingHandler;
  }

  @After
  public void tearDown() {
    // cleanup
    ExecuteJobHelper.loggingHandler = originalHandler;
  }

  @Test
  public void shouldBeAbleToReplaceLoggingHandler() {
 // given
    CollectingHandler collectingHandler = new CollectingHandler();
    ExecuteJobHelper.loggingHandler = collectingHandler;
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("failingDelegate")
        .startEvent()
        .serviceTask()
          .operatonClass("org.operaton.bpm.engine.test.jobexecutor.FailingDelegate")
          .operatonAsyncBefore()
        .done();
    testRule.deploy(modelInstance);

    // when
    engineRule.getRuntimeService().startProcessInstanceByKey("failingDelegate");
    final String jobId = engineRule.getManagementService().createJobQuery().singleResult().getId();
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs();
    processEngineConfiguration.getJobExecutor().shutdown();

    Throwable collectedException = collectingHandler.collectedExceptions.get(jobId);

    // then
    assertThat(collectedException).isInstanceOf(RuntimeException.class);
    assertThat(collectedException.getMessage()).isEqualTo("Expected Exception");
  }

  static class CollectingHandler implements ExecuteJobHelper.ExceptionLoggingHandler {

    Map<String, Throwable> collectedExceptions = new HashMap<>();

    @Override
    public void exceptionWhileExecutingJob(String jobId, Throwable exception) {
      collectedExceptions.put(jobId, exception);
    }

  }

}
