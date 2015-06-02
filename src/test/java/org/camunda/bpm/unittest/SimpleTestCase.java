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
import static org.junit.Assert.assertFalse;

import java.util.UUID;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
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
    public void shouldDeployWithoutRunningProcesses() {

        DeploymentBuilder deployment = rule.getRepositoryService().createDeployment();
        deployment.addClasspathResource("testProcess.bpmn");
        deployment.addClasspathResource("testProcess.png");
        deployment.name("testProcess_v2");
        deployment.deploy();

    }

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldDeployAfterFinishedProcess() {

        RuntimeService runtimeService = rule.getRuntimeService();

        final String businessKey = UUID.randomUUID().toString();

        ProcessInstance pi = runtimeService.startProcessInstanceByMessage("message", businessKey);
        assertFalse("Process instance should not be ended", pi.isEnded());
        assertEquals(1, runtimeService.createProcessInstanceQuery().count());

        runtimeService.createMessageCorrelation("message").processInstanceBusinessKey(businessKey).correlate();

        // now the process instance should be ended
        assertEquals(0, runtimeService.createProcessInstanceQuery().count());

        {
            DeploymentBuilder deployment = rule.getRepositoryService().createDeployment();
            deployment.addClasspathResource("testProcess.bpmn");
            deployment.addClasspathResource("testProcess.png");
            deployment.name("testProcess_v2");
            deployment.deploy();
        }
    }

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldDeployWithActiveRunningProcess() {

        RuntimeService runtimeService = rule.getRuntimeService();

        final String businessKey = UUID.randomUUID().toString();

        ProcessInstance pi = runtimeService.startProcessInstanceByMessage("message", businessKey);
        assertFalse("Process instance should not be ended", pi.isEnded());
        assertEquals(1, runtimeService.createProcessInstanceQuery().count());

        {
            DeploymentBuilder deployment = rule.getRepositoryService().createDeployment();
            deployment.addClasspathResource("testProcess.bpmn");
            deployment.addClasspathResource("testProcess.png");
            deployment.name("testProcess_v2");
            deployment.deploy();
        }

        runtimeService.createMessageCorrelation("message").processInstanceBusinessKey(businessKey).correlate();

        // now the process instance should be ended
        assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    }

}
