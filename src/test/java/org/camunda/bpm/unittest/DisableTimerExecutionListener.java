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

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

/**
 * @author  Thorben Lindhauer
 */
public class DisableTimerExecutionListener implements ExecutionListener {

    @Override
    public void notify(final DelegateExecution execution) throws Exception {
        final String eventBasedGatewayExecutionId = execution.getId();

        // wird am Ende der Transaktion, vor dem Flush in die Datenbank aufgerufen
        Context.getCommandContext().registerCommandContextListener(new CommandContextListener() {
                @Override
                public void onCommandFailed(final CommandContext commandContext, final Throwable t) { }

                @Override
                public void onCommandContextClose(final CommandContext commandContext) {

                    DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
                    List<JobEntity> jobs = dbSqlSession.findInCache(JobEntity.class);
                    for (JobEntity job : jobs) {

                        // hier k�nnte man ggf noch weiter einschr�nken, falls es mehr als einen TimerJob f�r das
                        // exclusive Gateway gibt
                        if (eventBasedGatewayExecutionId.equals(job.getExecutionId())) {
                            job.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());

                            // falls der Job sofort (bzw. vor dem n�chsten Pollen durch den Job-Executor) due ist
                            // wird er direkt in dieser Queue abgelegt, sodass der Job-Executor ihn unmittelbar
                            // ausf�hrt;
                            // entsprechend m�ssen wir ihn wieder entfernen
                            // Context.getJobExecutorContext().getCurrentProcessorJobQueue().remove(job.getId());
                        }
                    }
                }
            });

    }

}
