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

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void timerShouldNotBeActive() {

        RuntimeService runtimeService = rule.getRuntimeService();
        ManagementService managementService = rule.getManagementService();

        Map<String, Object> variables = singletonMap("isTimerActive", (Object) false);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);
        assertFalse("Process instance should not be ended", pi.isEnded());

        String id = pi.getProcessInstanceId();
        Job timer = managementService.createJobQuery().processInstanceId(id).timers().active().singleResult();

        Assert.assertNull("There should be no active timer", timer);

        runtimeService.createMessageCorrelation("message1").processInstanceId(id).correlate();

        // now the process instance should be ended
        assertEquals("Process instance should be finished", 0, runtimeService.createProcessInstanceQuery().count());

    }

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void timerShouldBeSuspended() {

        RuntimeService runtimeService = rule.getRuntimeService();
        ManagementService managementService = rule.getManagementService();

        Map<String, Object> variables = singletonMap("isTimerActive", (Object) true);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);
        assertFalse("Process instance should not be ended", pi.isEnded());

        String id = pi.getProcessInstanceId();
        Job timer = managementService.createJobQuery().processInstanceId(id).timers().singleResult();

        Assert.assertNotNull("There should be a suspended timer", timer);
        assertNotNull(timer.getDuedate());
        assertTrue(timer.isSuspended());

    }

}
