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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldExecuteProcess1() {
        run();
    }

    @Test
    @Deployment(resources = {"testProcess2.bpmn"})
    public void shouldExecuteProcess2() {
        run();
    }

    private void run() {
        RuntimeService runtimeService = rule.getRuntimeService();

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
        assertTrue(pi.isEnded());

        final HistoryService historyService = rule.getHistoryService();
        final String pid = pi.getProcessInstanceId();
        final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                                                                              .processInstanceId(pid).singleResult();
        assertEquals("endRollback", historicProcessInstance.getEndActivityId());

        final HistoricActivityInstance save = historyService.createHistoricActivityInstanceQuery()
                                                            .processInstanceId(pid).activityId("save").singleResult();

        assertNotNull("save service task should have been executed", save);

        final HistoricActivityInstance rollback = historyService.createHistoricActivityInstanceQuery()
                                                                .processInstanceId(pid).activityId("rollback")
                                                                .singleResult();

        assertNotNull("rollback service task should have been executed", rollback);
    }

}
