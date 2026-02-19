package com.manoj.matchIQ.controller;

import com.manoj.matchIQ.dto.OptimizationRequest;
import com.manoj.matchIQ.dto.OptimizationResultResponse;
import com.manoj.matchIQ.dto.OptimizationSubmissionResponse;
import com.manoj.matchIQ.model.OptimizationJob;
import com.manoj.matchIQ.service.DocumentParsingService;
import com.manoj.matchIQ.service.JobDescriptionFetcherService;
import com.manoj.matchIQ.service.ResumeOptimizationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/optimizations")
public class OptimizationController {
    private static final Logger log = LoggerFactory.getLogger(OptimizationController.class);
    
    private final ResumeOptimizationService optimizationService;
    private final DocumentParsingService documentParsingService;
    private final JobDescriptionFetcherService jobFetcherService;

    public OptimizationController(
            ResumeOptimizationService optimizationService,
            DocumentParsingService documentParsingService,
            JobDescriptionFetcherService jobFetcherService) {
        this.optimizationService = optimizationService;
        this.documentParsingService = documentParsingService;
        this.jobFetcherService = jobFetcherService;
        log.info("OptimizationController initialized");
    }

    /**
     * Original endpoint - accepts plain text
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OptimizationSubmissionResponse submitOptimization(@Valid @RequestBody OptimizationRequest request) {
        log.info("POST /api/optimizations - Received optimization request (text-based)");
        log.debug("Request details - Resume length: {}, Job description length: {}", 
                request.resumeText() != null ? request.resumeText().length() : 0,
                request.jobDescription() != null ? request.jobDescription().length() : 0);
        
        OptimizationJob saved = optimizationService.submit(request);
        
        log.info("Optimization submitted successfully - Job ID: {}, Status: {}", saved.getId(), saved.getStatus());
        return new OptimizationSubmissionResponse(saved.getId(), saved.getStatus());
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OptimizationSubmissionResponse submitOptimizationWithFile(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("jobUrl") String jobUrl) throws IOException {
        
        log.info("========================================");
        log.info("POST /api/optimizations/upload - NEW endpoint");
        log.info("    Resume file: {}", resumeFile.getOriginalFilename());
        log.info("    Job URL: {}", jobUrl);
        log.info("========================================");
        
        log.info("Step 1: Extracting text from resume file...");
        String resumeText = documentParsingService.extractTextFromResume(resumeFile);
        log.info("✓ Resume text extracted - {} characters", resumeText.length());
        
        log.info("Step 2: Fetching job description from URL...");
        String jobDescription = jobFetcherService.fetchJobDescription(jobUrl);
        log.info("✓ Job description fetched - {} characters", jobDescription.length());
        
        log.info("Step 3: Submitting optimization job...");
        OptimizationRequest request = new OptimizationRequest(resumeText, jobDescription);
        OptimizationJob saved = optimizationService.submit(request);
        
        log.info("========================================");
        log.info("✓ Optimization submitted successfully");
        log.info("    Job ID: {}", saved.getId());
        log.info("    Status: {}", saved.getStatus());
        log.info("========================================");
        
        return new OptimizationSubmissionResponse(saved.getId(), saved.getStatus());
    }

    @PostMapping("/upload-resume")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OptimizationSubmissionResponse submitOptimizationWithResumeFile(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) throws IOException {
        
        log.info("POST /api/optimizations/upload-resume");
        log.info("    Resume file: {}, Job description length: {}", 
                resumeFile.getOriginalFilename(), jobDescription.length());
        
        // Extract text from resume file
        String resumeText = documentParsingService.extractTextFromResume(resumeFile);
        log.info("✓ Resume text extracted - {} characters", resumeText.length());
        
        // Submit optimization
        OptimizationRequest request = new OptimizationRequest(resumeText, jobDescription);
        OptimizationJob saved = optimizationService.submit(request);
        
        log.info("Optimization submitted - Job ID: {}", saved.getId());
        return new OptimizationSubmissionResponse(saved.getId(), saved.getStatus());
    }

    @PostMapping("/fetch-job")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OptimizationSubmissionResponse submitOptimizationWithJobUrl(
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobUrl") String jobUrl) throws IOException {
        
        log.info("POST /api/optimizations/fetch-job");
        log.info("    Resume length: {}, Job URL: {}", resumeText.length(), jobUrl);
        
        // Fetch job description from URL
        String jobDescription = jobFetcherService.fetchJobDescription(jobUrl);
        log.info("✓ Job description fetched - {} characters", jobDescription.length());
        
        // Submit optimization
        OptimizationRequest request = new OptimizationRequest(resumeText, jobDescription);
        OptimizationJob saved = optimizationService.submit(request);
        
        log.info("Optimization submitted - Job ID: {}", saved.getId());
        return new OptimizationSubmissionResponse(saved.getId(), saved.getStatus());
    }

    @GetMapping("/{id}")
    public OptimizationResultResponse getOptimizationResult(@PathVariable String id) {
        log.info("GET /api/optimizations/{} - Fetching optimization result", id);
        
        OptimizationJob job = optimizationService.getById(id);
        
        log.info("Retrieved job - ID: {}, Status: {}, ATS Score: {}", 
                job.getId(), job.getStatus(), job.getAtsScore());
        
        return new OptimizationResultResponse(
                job.getId(),
                job.getStatus(),
                job.getAtsScore(),
                job.getExtractedKeywords(),
                job.getOptimizedBulletPoints(),
                job.getTailoredCoverLetter(),
                job.getErrorMessage());
    }
}
