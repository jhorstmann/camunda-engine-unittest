package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Servicetask implements JavaDelegate {
    private static final AtomicInteger counter = new AtomicInteger();

    public void execute(DelegateExecution delegateExecution) throws Exception {
        if (counter.getAndIncrement() <= 10) {
            Thread.sleep(2000L);
            throw new RuntimeException("Failed on first try");
        } else {
            System.out.println(new Date() + "\t" + getClass().getName());
        }
    }
}
