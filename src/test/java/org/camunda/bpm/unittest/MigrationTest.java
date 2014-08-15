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

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/camunda.cfg.xml")
public class MigrationTest {

  @Autowired
  private ProcessEngine processEngine;

  @Test
  public void shouldExecuteProcess() {

    RepositoryService repositoryService = processEngine.getRepositoryService();
    String deploymentV1 = repositoryService.createDeployment()
            .name("v1")
            .addClasspathResource("process-v2.bpmn")
            .deploy()
            .getId();
    String deploymentV2 = repositoryService.createDeployment()
            .name("v2")
            .addClasspathResource("process-v3.bpmn")
            .deploy()
            .getId();

    ProcessDefinition processV1 = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deploymentV1)
            .processDefinitionKey("testProcess")
            .singleResult();
    ProcessDefinition processV2 = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deploymentV2)
            .processDefinitionKey("testProcess")
            .singleResult();

    assertEquals(1, processV1.getVersion());
    assertEquals(2, processV2.getVersion());

    RuntimeService runtimeService = processEngine.getRuntimeService();
    ProcessInstance pi = runtimeService.startProcessInstanceById(processV1.getId());

    ManagementService managementService = processEngine.getManagementService();
    String processInstanceId = pi.getProcessInstanceId();

    Job migrateVersionJob = managementService.createJobQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
    assertNotNull("Version migration should be an async task", migrateVersionJob);
    managementService.executeJob(migrateVersionJob.getId());

    Job secondJob = managementService.createJobQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
    assertNull("After the version migration the second async task should be removed", secondJob);

    HistoryService historyService = processEngine.getHistoryService();
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(processInstanceId)
            .activityId("secondTask")
            .singleResult();
		
    assertNull("After the version migration the second async task should be removed", historicActivityInstance);
  }

}
