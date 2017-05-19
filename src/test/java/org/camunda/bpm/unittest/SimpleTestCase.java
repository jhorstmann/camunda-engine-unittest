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

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.historyService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SimpleTestCase {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldExecuteProcess() throws InterruptedException, ExecutionException {

        run("testProcess");
    }

    @Test
    @Deployment(resources = {"testProcess2.bpmn"})
    public void shouldExecuteProcess2() throws InterruptedException, ExecutionException {

        run("testProcess2");
    }

    private void run(String processDefinitionKey) throws InterruptedException, ExecutionException {
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

        final String businessKey = UUID.randomUUID().toString();

        final Future<?> sendPayment = scheduledExecutorService.schedule(new Runnable() {
            public void run() {
                System.out.println("correlate");
                runtimeService().correlateMessage("messagePayment", businessKey);
            }
        }, 200, TimeUnit.MILLISECONDS);

        final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(processDefinitionKey, businessKey);
        assertFalse(processInstance.isEnded());
        runtimeService().correlateMessage("messageContinue", businessKey);

        sendPayment.get();
        Thread.sleep(1000);

        final HistoricProcessInstance historicProcessInstance = historyService().createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
        assertNotNull(historicProcessInstance.getEndTime());

        scheduledExecutorService.shutdown();
    }

}
