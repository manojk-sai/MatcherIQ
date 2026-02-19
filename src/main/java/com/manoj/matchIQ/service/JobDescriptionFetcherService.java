package com.manoj.matchIQ.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JobDescriptionFetcherService {
    private static final Logger log = LoggerFactory.getLogger(JobDescriptionFetcherService.class);
    
    private static final int TIMEOUT = 10000; // 10 seconds
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    /**
     * Fetches job description from a URL
     */
    public String fetchJobDescription(String url) throws IOException {
        log.info(">>> Fetching job description from URL: {}", url);
        
        // Validate URL
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Job URL cannot be empty");
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid URL format. URL must start with http:// or https://");
        }
        
        try {
            // Fetch the web page
            log.debug("    Connecting to URL...");
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();
            
            log.info("    Successfully fetched page - Title: {}", doc.title());
            
            // Extract job description using common selectors
            String jobDescription = extractJobDescription(doc, url);
            
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                log.warn("    Could not extract job description using selectors, falling back to body text");
                jobDescription = doc.body().text();
            }
            
            // Clean up the text
            jobDescription = cleanText(jobDescription);
            
            log.info("<<< Successfully extracted {} characters from job posting", jobDescription.length());
            log.debug("    First 100 chars: {}", jobDescription.substring(0, Math.min(100, jobDescription.length())));
            
            return jobDescription;
            
        } catch (IOException e) {
            log.error("    Failed to fetch job description from URL: {}", url, e);
            throw new IOException("Failed to fetch job description: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts job description using common selectors for job sites
     */
    private String extractJobDescription(Document doc, String url) {
        log.debug("    Attempting to extract job description using selectors...");
        
        // LinkedIn selectors
        if (url.contains("linkedin.com")) {
            Element description = doc.selectFirst("div.description__text");
            if (description != null) {
                log.debug("    Found LinkedIn job description");
                return description.text();
            }
            
            description = doc.selectFirst("div.show-more-less-html__markup");
            if (description != null) {
                log.debug("    Found LinkedIn job description (alternate selector)");
                return description.text();
            }
        }
        
        // Indeed selectors
        if (url.contains("indeed.com")) {
            Element description = doc.selectFirst("div#jobDescriptionText");
            if (description != null) {
                log.debug("    Found Indeed job description");
                return description.text();
            }
            
            description = doc.selectFirst("div.jobsearch-jobDescriptionText");
            if (description != null) {
                log.debug("    Found Indeed job description (alternate selector)");
                return description.text();
            }
        }
        
        // Glassdoor selectors
        if (url.contains("glassdoor.com")) {
            Element description = doc.selectFirst("div.jobDescriptionContent");
            if (description != null) {
                log.debug("    Found Glassdoor job description");
                return description.text();
            }
            
            description = doc.selectFirst("div[class*='JobDetails']");
            if (description != null) {
                log.debug("    Found Glassdoor job description (alternate selector)");
                return description.text();
            }
        }
        
        // Generic selectors for other job sites
        String[] genericSelectors = {
            "div[class*='job-description']",
            "div[class*='jobDescription']",
            "div[class*='job_description']",
            "div[id*='job-description']",
            "div[id*='jobDescription']",
            "section[class*='description']",
            "article[class*='description']",
            "div.description",
            "div.job-details",
            "div.posting-description"
        };
        
        for (String selector : genericSelectors) {
            Element description = doc.selectFirst(selector);
            if (description != null && description.text().length() > 100) {
                log.debug("    Found job description using generic selector: {}", selector);
                return description.text();
            }
        }
        
        log.debug("    No specific job description selector matched");
        return null;
    }
    
    /**
     * Cleans and normalizes extracted text
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        return text
                .replaceAll("\\s+", " ")  // Replace multiple spaces with single space
                .replaceAll("\\r\\n|\\r|\\n", " ")  // Replace newlines with space
                .trim();
    }
}
