package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ServiceTask implements JavaDelegate{
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        //throw new BpmnError("error", "An error occured in a service task");
    }
}
