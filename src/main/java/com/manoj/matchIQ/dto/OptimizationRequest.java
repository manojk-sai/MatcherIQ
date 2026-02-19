package com.manoj.matchIQ.dto;

import jakarta.validation.constraints.NotBlank;

public record OptimizationRequest (
        @NotBlank(message = "resumeText is required") String resumeText,
        @NotBlank(message = "jobDescription is required") String jobDescription
){ }

