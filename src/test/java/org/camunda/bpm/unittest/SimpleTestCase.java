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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
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
    public void shouldExecuteProcess() throws InterruptedException, ExecutionException {

        final RuntimeService runtimeService = rule.getRuntimeService();

        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

        final String businessKey = UUID.randomUUID().toString();

        final ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", businessKey);

        assertFalse("Process instance should not be ended", pi.isEnded());
        assertEquals(1, runtimeService.createProcessInstanceQuery().count());

        List<ScheduledFuture<Exception>> futures = new ArrayList<ScheduledFuture<Exception>>();
        for (int i = 0; i < 2; i++) {
            final int j = i;
            futures.add(executorService.schedule(new Callable<Exception>() {
                        @Override
                        public Exception call() {
                            try {
                                runtimeService.correlateMessage("message1", businessKey);
                                return null;
                            } catch (Exception ex) {
                                return ex;
                            }
                        }
                    }, 100 * i, TimeUnit.MILLISECONDS));
        }

        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(2, futures.size());

        ScheduledFuture<Exception> firstMessage = futures.get(0);
        Exception ex1 = firstMessage.get();
        assertNull("First message correlation should have succeeeded", ex1);

        ScheduledFuture<Exception> secondMessage = futures.get(1);
        Exception ex2 = secondMessage.get();
        assertNotNull("Second message correlation should lead to an exception", ex2);

        assertFalse("Second message correlation should have been prevented by the engine",
            ex2 instanceof AlreadyExecutedException);
        assertFalse("Second message correlation should have been prevented by the engine",
            ex2.getMessage().contains("already executed"));

        // now the process instance should be ended
        assertEquals(0, runtimeService.createProcessInstanceQuery().count());

        List<HistoricActivityInstance> tasks = rule.getHistoryService().createHistoricActivityInstanceQuery()
                                                   .processInstanceId(pi.getProcessInstanceId())
                                                   .activityId("slowServiceTask").list();

        assertEquals("Task should be executed once", 1, tasks.size());

        HistoricActivityInstance task = tasks.get(0);
        assertNotNull("Service task should have finished", task.getEndTime());
    }

}
