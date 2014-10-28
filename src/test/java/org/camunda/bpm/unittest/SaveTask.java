package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SaveTask implements JavaDelegate {
    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        System.out.println("Save");
    }
}
