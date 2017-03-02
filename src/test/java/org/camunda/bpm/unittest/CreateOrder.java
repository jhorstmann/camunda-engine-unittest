package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Job;

public class CreateOrder implements JavaDelegate {
    static final Object MONITOR = new Object();

    private int getRemainingRetries(DelegateExecution execution) {
        final ManagementService managementService = execution.getProcessEngineServices().getManagementService();
        final Job job = managementService.createJobQuery()
                .processInstanceId(execution.getProcessInstanceId())
                .executionId(execution.getId())
                .active()
                .singleResult();

        return job == null ? 0 : job.getRetries();
    }

    public void execute(DelegateExecution execution) throws Exception {
        int remainingRetries = getRemainingRetries(execution);
        System.out.println(getClass().getSimpleName() + " remaining retries " + remainingRetries);
        if (remainingRetries > 1) {
            throw new Exception("Exception with " + remainingRetries + " remaining retries");
        }
        System.out.println("Continueing with " + remainingRetries + " remaining retries");

        synchronized (MONITOR) {
            MONITOR.notifyAll();
        }
    }
}
