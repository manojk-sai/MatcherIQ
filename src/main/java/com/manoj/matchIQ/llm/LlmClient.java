package com.manoj.matchIQ.llm;

import java.util.List;

public interface LlmClient {
    String generateAtsBullets(String resumeText, String jobDescription, List<String> keywords);
    String generateTailoredCoverLetter(String resumeText, String jobDescription, List<String> keywords);
}
