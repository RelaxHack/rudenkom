package com.company.ms;

/**
 * Exception identifies errors in MetricSender class
 */
public class MetricSenderException extends Exception {

    /**
     * Constructs a new exception
     *
     * @param message the detail message
     */
    public MetricSenderException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception
     *
     * @param message   the detail message
     * @param throwable thrown exception
     */
    public MetricSenderException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
