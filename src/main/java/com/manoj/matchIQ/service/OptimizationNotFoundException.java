package com.manoj.matchIQ.service;

public class OptimizationNotFoundException extends RuntimeException {
    public OptimizationNotFoundException(String jobId) {
        super("Optimization job not found with ID: " + jobId);
    }
}
