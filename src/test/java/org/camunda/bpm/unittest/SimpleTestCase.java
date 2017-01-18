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
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() throws InterruptedException {
    final RuntimeService runtimeService = rule.getRuntimeService();
    final ManagementService managementService = rule.getManagementService();
    final HistoryService historyService = rule.getHistoryService();

    for (int i = 0; i < 100; i++) {

      System.out.println(i);

      final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", "process" + i, Collections.<String, Object>singletonMap("waitForMessage", false));

      final AtomicBoolean correlated = new AtomicBoolean();

      final ExecutorService executorService = Executors.newFixedThreadPool(2);

      executorService.submit(() -> {

        while (true) {
          final List<Job> jobs = managementService.createJobQuery().processInstanceId(processInstance.getId()).messages().active().list();
          if (correlated.get() && jobs.isEmpty()) {
            return;
          }
          for (Job job : jobs) {
            runJob(job);
          }
        }
      });

      executorService.submit(() -> {

        runtimeService.createMessageCorrelation("message1").processInstanceId(processInstance.getId()).correlate();
        correlated.set(true);
      });

      executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);


      final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

      Assert.assertNotNull("process should be ended", historicProcessInstance.getEndTime());
    }


  }

  private void runJob(Job job) {
    final ManagementService managementService = rule.getManagementService();
    do {
      try {
        //System.out.println("executing " + job.getId());
        managementService.executeJob(job.getId());
      } catch (OptimisticLockingException e) {
        e.printStackTrace();
        continue;
      }
    } while (false);
  }

}
