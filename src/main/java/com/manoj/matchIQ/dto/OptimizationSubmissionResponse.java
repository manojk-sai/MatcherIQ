package com.manoj.matchIQ.dto;

import com.manoj.matchIQ.model.OptimizationStatus;

public record OptimizationSubmissionResponse(String jobId, OptimizationStatus status) {
}
