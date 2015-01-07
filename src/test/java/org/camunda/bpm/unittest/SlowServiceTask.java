package org.camunda.bpm.unittest;

import java.util.concurrent.ConcurrentHashMap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SlowServiceTask implements JavaDelegate {

    private static final ConcurrentHashMap<String, Boolean> executions = new ConcurrentHashMap<String, Boolean>();

    @Override
    public void execute(final DelegateExecution delegateExecution) throws Exception {

        Boolean alreadyExecuted = executions.putIfAbsent(delegateExecution.getId(), Boolean.TRUE);

        if (alreadyExecuted != null) {
            throw new IllegalStateException("Service task was already executed for this execution!");
        }

        System.out.println("Begin service task");
        Thread.sleep(500L);
        System.out.println("End service task");
    }
}
