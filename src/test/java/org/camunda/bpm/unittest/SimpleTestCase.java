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

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.junit.Rule;
import org.junit.Test;


public class SimpleTestCase {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void canBeCanceledBeforeFirstServiceTask() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess");

        historyService().createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list();

        runtimeService().createMessageCorrelation("cancelation-requested").processInstanceId(processInstance.getId()).correlate();
    }

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void canBeCanceledBeforeSecondServiceTask() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess");

        historyService().createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list();

        Job job = managementService().createJobQuery().processInstanceId(processInstance.getId()).active().singleResult();
        managementService().executeJob(job.getId());

        historyService().createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list();

        runtimeService().createMessageCorrelation("cancelation-requested").processInstanceId(processInstance.getId()).correlate();
    }

}
