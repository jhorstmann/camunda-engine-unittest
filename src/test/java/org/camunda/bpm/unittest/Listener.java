package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class Listener implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        throw new BpmnError("error", "An error occured in a listener");
    }
}
