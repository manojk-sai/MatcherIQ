package com.manoj.matchIQ.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "optimization_jobs")
public class OptimizationJob {

    @Id
    private String id;
    private String resumeText;
    private String jobDescription;
    private List<String> extractedKeywords;
    private Integer atsScore;
    private String optimizedBulletPoints;
    private String tailoredCoverLetter;
    private OptimizationStatus status;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public List<String> getExtractedKeywords() {
        return extractedKeywords;
    }

    public void setExtractedKeywords(List<String> extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public String getOptimizedBulletPoints() {
        return optimizedBulletPoints;
    }

    public void setOptimizedBulletPoints(String optimizedBulletPoints) {
        this.optimizedBulletPoints = optimizedBulletPoints;
    }

    public String getTailoredCoverLetter() {
        return tailoredCoverLetter;
    }

    public void setTailoredCoverLetter(String tailoredCoverLetter) {
        this.tailoredCoverLetter = tailoredCoverLetter;
    }

    public OptimizationStatus getStatus() {
        return status;
    }

    public void setStatus(OptimizationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
