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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.historyService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SimpleTestCase {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldExecuteProcess() throws Exception {

        final String businessKey = UUID.randomUUID().toString();

        final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess", businessKey);
        assertFalse(processInstance.isEnded());

        synchronized (CreateOrder.MONITOR) {
            CreateOrder.MONITOR.wait(10000);
            Thread.sleep(1000);
        }

        final HistoricProcessInstance historicProcessInstance = historyService().createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
        assertNotNull(historicProcessInstance.getEndTime());

    }


}
