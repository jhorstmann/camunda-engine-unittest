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

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() throws InterruptedException {

    RuntimeService runtimeService = rule.getRuntimeService();
    ManagementService managementService = rule.getManagementService();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    assertFalse("Process instance should not be ended", pi.isEnded());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    for (int i = 0; i < 10; i++) {
      JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();
      if (job != null) {
        System.out.println("Exp : " + job.getLockExpirationTime());

        Thread.sleep(1000L);
      } else {
        break;
      }
    }

    // process instance should still be active and waiting for retry time cycle
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
  }

}
