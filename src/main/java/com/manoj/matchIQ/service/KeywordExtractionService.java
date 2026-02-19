package com.manoj.matchIQ.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordExtractionService {
    private static final Logger log = LoggerFactory.getLogger(KeywordExtractionService.class);
    
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "with", "for", "that", "this", "from", "are", "was", "but", "not",
            "have", "has", "had", "by", "on", "in", "at", "to", "of", "a", "an", "role",
            "team", "years", "ability", "required"
    );

    public List<String> extractKeywords(String jobDescription) {
        log.info("Extracting keywords from job description - Length: {}", jobDescription != null ? jobDescription.length() : 0);
        
        List<String> keywords = Arrays.stream(jobDescription.toLowerCase(Locale.ROOT).split("[^a-z0-9+#.]+"))
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),LinkedHashSet::new))
                .stream()
                .limit(20)
                .toList();
        
        log.info("Extracted {} keywords: {}", keywords.size(), keywords);
        return keywords;
    }
}
