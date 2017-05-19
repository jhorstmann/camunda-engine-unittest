package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class CreateOrder implements JavaDelegate {
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println(getClass().getSimpleName() + " start");
        Thread.sleep(500L);
        System.out.println(getClass().getSimpleName() + " end");
    }
}
