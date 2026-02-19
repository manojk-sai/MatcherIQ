package com.manoj.matchIQ.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KeywordServicesTests {
    private final KeywordExtractionService extractionService = new KeywordExtractionService();
    private final KeywordScoringService scoringService = new KeywordScoringService();

    @Test
    void extractsRelevantKeywords(){
        List<String> Keywords = extractionService.extractKeywords("We are looking for a software engineer with experience in Java, Spring Boot, and AWS. The ideal candidate should have strong problem-solving skills and the ability to work in a team environment.");
        assertThat(Keywords).contains("java", "spring", "boot");
    }

    @Test
    void calculatesScoring(){
        int score = scoringService.calculateAtsScore("Experienced software engineer with expertise in Java and Spring Boot.", List.of("software engineer", "java", "spring boot", "aws"));
        assertThat(score).isEqualTo(75);
    }
}
