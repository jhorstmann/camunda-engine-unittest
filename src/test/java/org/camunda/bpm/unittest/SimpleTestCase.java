/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Meyer
 *
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {

    RuntimeService runtimeService = rule.getRuntimeService();
    ManagementService managementService = rule.getManagementService();
    HistoryService historyService = rule.getHistoryService();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertFalse("Process instance should not be ended", processInstance.isEnded());

    final String processInstanceId = processInstance.getId();

    final Job task1 = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    managementService.executeJob(task1.getId());

    runtimeService.createMessageCorrelation("cancelation-requested").processInstanceId(processInstanceId).correlate();

    final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

    assertNotNull("historic process should exist",historicProcessInstance);

    // now the process instance should be ended
    assertNotNull("process should have ended", historicProcessInstance.getEndTime());
    assertEquals("canceled", historicProcessInstance.getEndActivityId());

    final List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderPartiallyByOccurrence().asc().list();

    final List<String> activityIds = historicActivityInstances.stream().map(HistoricActivityInstance::getActivityId).collect(toList());

    System.out.println(activityIds);

    assertTrue("task1 should have been executed", activityIds.contains("task1"));
    assertTrue("compensation should have been executed", activityIds.contains("compensate"));
  }

}
