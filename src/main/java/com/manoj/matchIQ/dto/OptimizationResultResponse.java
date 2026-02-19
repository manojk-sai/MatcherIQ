package com.manoj.matchIQ.dto;

import com.manoj.matchIQ.model.OptimizationStatus;
import lombok.Data;

import java.util.List;

public record OptimizationResultResponse (
    String id,
    OptimizationStatus status,
    Integer atsScore,
    List<String> extractedKeywords,
    String optimizedBulletPoints,
    String tailoredCoverLetter,
    String errorMessage){
}
