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
package org.operaton.bpm.engine.test.api.history.removaltime.cleanup;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration.START_DELAY;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.impl.history.event.HistoryEventTypes;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.runtime.Job;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineTestExtension;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupSchedulerVariableInstancesTest extends AbstractHistoryCleanupSchedulerTest {

  @RegisterExtension
  protected static ProcessEngineExtension engineRule = ProcessEngineExtension.builder()
    .cacheForConfigurationResource(false)
    .configurator(configuration ->
      configure(configuration, HistoryEventTypes.VARIABLE_INSTANCE_CREATE)
    ).build();
  @RegisterExtension
  protected static ProcessEngineTestExtension testRule = new ProcessEngineTestExtension(engineRule);

  @BeforeEach
  public void init() {
    initEngineConfiguration(engineRule, engineConfiguration);
  }

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  protected final String PROCESS_KEY = "process";
  protected final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_KEY)
    .operatonHistoryTimeToLive(5)
    .startEvent()
      .userTask()
    .endEvent().done();

  protected final Date END_DATE = new GregorianCalendar(2013, Calendar.MARCH, 18, 13, 0, 0).getTime();

  @Test
  public void shouldScheduleToNow() {
    // given
    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    for (int i = 0; i<5; i++) {
      runtimeService.setVariable(processInstanceId,"aVariableName"+i, "aVariableValue");
    }

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    engineConfiguration.setHistoryCleanupBatchSize(5);
    engineConfiguration.initHistoryCleanup();

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(removalTime);
  }

  @Test
  public void shouldScheduleToLater() {
    // given
    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    for (int i = 0; i<5; i++) {
      runtimeService.setVariable(processInstanceId,"aVariableName"+i, "aVariableValue");
    }

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    engineConfiguration.setHistoryCleanupBatchSize(6);
    engineConfiguration.initHistoryCleanup();

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(addSeconds(removalTime, START_DELAY));
  }

}
