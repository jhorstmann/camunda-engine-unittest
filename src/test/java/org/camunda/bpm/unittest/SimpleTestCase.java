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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Run this in a profiler with profiling root set to the benchmark method.
 *
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {

    RuntimeService runtimeService = rule.getRuntimeService();
    final ProcessDefinitionQuery query = rule.getRepositoryService().createProcessDefinitionQuery();
    ProcessDefinition pd = query.processDefinitionKey("testProcess").latestVersion().singleResult();
    String processDefinitionId = pd.getId();

    warmup(runtimeService, processDefinitionId);
    benchmark(runtimeService, processDefinitionId);
  }

  private static void warmup(RuntimeService runtimeService, String processDefinitionId) {
    runProcesses(runtimeService, processDefinitionId, 2);
    runProcesses(runtimeService, processDefinitionId, 2);
    runProcesses(runtimeService, processDefinitionId, 6);
  }

  private static void benchmark(RuntimeService runtimeService, String processDefinitionId) {
    runProcesses(runtimeService, processDefinitionId, 100);
  }

  private static void runProcesses(RuntimeService runtimeService, String processDefinitionId, int count) {
    for (int i = 0; i < count; i++) {
      ProcessInstance pi = runtimeService.startProcessInstanceById(processDefinitionId);
    }
  }

}
