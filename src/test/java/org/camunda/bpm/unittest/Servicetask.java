package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Servicetask implements JavaDelegate {
    private static final AtomicInteger counter = new AtomicInteger();

    public void execute(DelegateExecution delegateExecution) throws Exception {
        int i = counter.getAndIncrement();
        if (i <= 10) {
            throw new RuntimeException("Failed on try" + i);
        } else {
            System.out.println(new Date() + "\t" + getClass().getName());
        }
    }
}
