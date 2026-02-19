package com.manoj.matchIQ.service;

import com.manoj.matchIQ.dto.OptimizationRequest;
import com.manoj.matchIQ.llm.LlmClient;
import com.manoj.matchIQ.model.OptimizationJob;
import com.manoj.matchIQ.model.OptimizationStatus;
import com.manoj.matchIQ.repository.OptimizationJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ResumeOptimizationService {
    private static final Logger log = LoggerFactory.getLogger(ResumeOptimizationService.class);
    
    private final OptimizationJobRepository repo;
    private final KeywordExtractionService extractionService;
    private final KeywordScoringService scoringService;
    private final LlmClient llmClient;

    public ResumeOptimizationService(
            OptimizationJobRepository repo,
            KeywordExtractionService extractionService,
            KeywordScoringService scoringService,
            LlmClient llmClient) {
        this.repo = repo;
        this.extractionService = extractionService;
        this.scoringService = scoringService;
        this.llmClient = llmClient;
        log.info("ResumeOptimizationService initialized");
    }

    public OptimizationJob submit(OptimizationRequest request){
        log.info("=== SUBMIT START ===");
        log.info("Received optimization request - Resume length: {}, Job description length: {}", 
                request.resumeText() != null ? request.resumeText().length() : 0,
                request.jobDescription() != null ? request.jobDescription().length() : 0);
        
        OptimizationJob job = new OptimizationJob();
        job.setJobDescription(request.jobDescription());
        job.setResumeText(request.resumeText());
        job.setStatus(OptimizationStatus.PENDING);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());

        log.info("Saving job to MongoDB...");
        OptimizationJob savedJob = repo.save(job);
        log.info("Job saved successfully with ID: {}", savedJob.getId());
        
        log.info("Triggering async processing for job ID: {}", savedJob.getId());
        processAsync(savedJob.getId());
        
        log.info("=== SUBMIT END - Returning job ID: {} ===", savedJob.getId());
        return savedJob;
    }

    @Async("optimizationExecutor")
    public void processAsync(String jobId) {
        log.info("=== ASYNC PROCESSING START for job ID: {} ===", jobId);
        log.info("Thread name: {}", Thread.currentThread().getName());
        
        OptimizationJob job = getById(jobId);
        log.info("Retrieved job from database - Status: {}", job.getStatus());
        
        try{
            log.info("Updating status to PROCESSING for job ID: {}", jobId);
            job.setStatus(OptimizationStatus.PROCESSING);
            repo.save(job);
            log.info("Status updated to PROCESSING");

            log.info("Step 1: Extracting keywords from job description...");
            List<String> keywords = extractionService.extractKeywords(job.getJobDescription());
            job.setExtractedKeywords(keywords);
            log.info("Extracted {} keywords: {}", keywords.size(), keywords);

            log.info("Step 2: Calculating ATS score...");
            int score = scoringService.calculateAtsScore(job.getResumeText(), keywords);
            job.setAtsScore(score);
            log.info("ATS Score calculated: {}", score);

            log.info("Step 3: Generating optimized bullet points...");
            String optimizedBullets = llmClient.generateAtsBullets(job.getResumeText(), job.getJobDescription(), keywords);
            job.setOptimizedBulletPoints(optimizedBullets);
            log.info("Optimized bullet points generated - Length: {}", optimizedBullets != null ? optimizedBullets.length() : 0);

            log.info("Step 4: Generating tailored cover letter...");
            String coverLetter = llmClient.generateTailoredCoverLetter(job.getResumeText(), job.getJobDescription(), keywords);
            job.setTailoredCoverLetter(coverLetter);
            log.info("Cover letter generated - Length: {}", coverLetter != null ? coverLetter.length() : 0);
            
            job.setUpdatedAt(Instant.now());
            job.setStatus(OptimizationStatus.COMPLETED);
            
            log.info("Saving completed job to database...");
            repo.save(job);
            log.info("=== ASYNC PROCESSING COMPLETED SUCCESSFULLY for job ID: {} ===", jobId);
            
        } catch (Exception e) {
            log.error("=== ASYNC PROCESSING FAILED for job ID: {} ===", jobId, e);
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            
            job.setStatus(OptimizationStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setUpdatedAt(Instant.now());
            repo.save(job);
            log.info("Job status updated to FAILED in database");
        }
    }

    public OptimizationJob getById(String id){
        log.info("Fetching job by ID: {}", id);
        OptimizationJob job = repo.findById(id)
                .orElseThrow(()-> {
                    log.error("Job not found with ID: {}", id);
                    return new NoSuchElementException("Optimization job not found with id: "+id);
                });
        log.info("Job found - ID: {}, Status: {}", job.getId(), job.getStatus());
        return job;
    }


}
