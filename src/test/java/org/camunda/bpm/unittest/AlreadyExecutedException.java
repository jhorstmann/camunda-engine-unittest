package org.camunda.bpm.unittest;

public class AlreadyExecutedException extends RuntimeException {
    public AlreadyExecutedException(final String message) {
        super(message);
    }
}
