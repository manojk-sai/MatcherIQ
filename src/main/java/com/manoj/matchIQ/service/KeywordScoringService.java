package com.manoj.matchIQ.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class KeywordScoringService {
    private static final Logger log = LoggerFactory.getLogger(KeywordScoringService.class);
    
    public int calculateAtsScore(String resumeText, List<String> jobKeywords){
        log.info("Calculating ATS score - Resume length: {}, Keywords count: {}", 
                resumeText != null ? resumeText.length() : 0, jobKeywords.size());
        
        if(jobKeywords.isEmpty()) {
            log.warn("No keywords provided, returning score 0");
            return 0;
        }
        
        String normalizedResume = resumeText.toLowerCase(Locale.ROOT);
        long matches = jobKeywords.stream().filter(normalizedResume::contains).count();
        int score = (int) Math.round((matches*100.0)/jobKeywords.size());
        
        log.info("ATS Score calculated: {}% ({}/{} keywords matched)", score, matches, jobKeywords.size());
        return score;
    }
}
