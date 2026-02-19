        package com.manoj.matchIQ.llm;

        import com.fasterxml.jackson.databind.JsonNode;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.boot.web.client.RestTemplateBuilder;
        import org.springframework.http.HttpEntity;
        import org.springframework.http.MediaType;
        import org.springframework.http.HttpHeaders;
        import org.springframework.http.ResponseEntity;
        import org.springframework.stereotype.Component;
        import org.springframework.util.StringUtils;
        import org.springframework.web.client.RestTemplate;

        import java.time.Duration;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.StringJoiner;

        @Component
        public class ApiLlmClient implements LlmClient{
            private static final Logger log = LoggerFactory.getLogger(ApiLlmClient.class);

            private final RestTemplate restTemplate;
            private final String apiUrl;
            private final String apiKey;
            private final String model;
            private final ObjectMapper objectMapper;

            public ApiLlmClient(RestTemplateBuilder builder,
                                @Value("${llm.api.url:}") String apiUrl,
                                @Value("${llm.api.key:}") String apiKey,
                                @Value("${llm.model:}") String model) {
                this.restTemplate = builder
                        .setConnectTimeout(Duration.ofSeconds(30))
                        .setReadTimeout(Duration.ofSeconds(60))
                        .build();
                this.apiUrl = apiUrl;
                this.apiKey = apiKey;
                this.model = model;
                this.objectMapper = new ObjectMapper();

                log.info("========================================");
                log.info("ApiLlmClient Configuration:");
                log.info("  URL configured: {}", StringUtils.hasText(apiUrl));
                log.info("  URL value: {}", apiUrl);
                log.info("  API Key configured: {}", StringUtils.hasText(apiKey));
                log.info("  API Key starts with: {}", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "N/A");
                log.info("  Model: {}", model);
                log.info("========================================");
            }

            @Override
            public String generateAtsBullets(String resumeText, String jobDescription, List<String> keywords) {
                log.info(">>> Generating ATS-optimized bullet points with {} keywords", keywords.size());

                String prompt = "You are an expert resume writer. Generate exactly 5 ATS-optimized resume bullet points based on the provided resume and job description. "
                        + "Each bullet point should:\n"
                        + "- Start with a strong action verb\n"
                        + "- Include quantifiable achievements when possible\n"
                        + "- Incorporate these keywords naturally: " + String.join(", ", keywords) + "\n"
                        + "- Be concise and impactful (1-2 lines each)\n\n"
                        + "Resume:\n" + resumeText + "\n\n"
                        + "Job Description:\n" + jobDescription + "\n\n"
                        + "Return ONLY the 5 bullet points, one per line, each starting with a hyphen (-).";

                String result = generateFromLlmOrFallback(prompt, buildBulletFallback(keywords));
                log.info("<<< ATS bullets generated - Length: {}", result != null ? result.length() : 0);
                return result;
            }

            @Override
            public String generateTailoredCoverLetter(String resumeText, String jobDescription, List<String> keywords) {
                log.info(">>> Generating tailored cover letter with {} keywords", keywords.size());

                String prompt = "You are an expert cover letter writer. Generate a professional, concise cover letter (3-4 paragraphs) for this job application. "
                        + "The cover letter should:\n"
                        + "- Demonstrate enthusiasm for the role\n"
                        + "- Highlight relevant experience from the resume\n"
                        + "- Naturally incorporate these keywords: " + String.join(", ", keywords) + "\n"
                        + "- Be professional yet personable\n\n"
                        + "Resume:\n" + resumeText + "\n\n"
                        + "Job Description:\n" + jobDescription + "\n\n"
                        + "Return ONLY the cover letter text, no additional commentary.";

                String result = generateFromLlmOrFallback(prompt, buildCoverLetterFallback(keywords));
                log.info("<<< Cover letter generated - Length: {}", result != null ? result.length() : 0);
                return result;
            }

            private String generateFromLlmOrFallback(String prompt, String fallback){
                if(!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)){
                    log.warn("⚠️ LLM API URL or Key not configured, using fallback content");
                    log.warn("   URL empty: {}, Key empty: {}", !StringUtils.hasText(apiUrl), !StringUtils.hasText(apiKey));
                    return fallback;
                }

                log.info("🔵 Calling OpenAI API at: {}", apiUrl);
                log.debug("   Prompt length: {} characters", prompt.length());

                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(apiKey);

                    // Build proper OpenAI request format
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("model", model);
                    requestBody.put("messages", List.of(
                        Map.of("role", "user", "content", prompt)
                    ));
                    requestBody.put("max_tokens", 1000);
                    requestBody.put("temperature", 0.7);

                    String requestJson = objectMapper.writeValueAsString(requestBody);
                    log.debug("   Request payload: {}", requestJson.substring(0, Math.min(200, requestJson.length())) + "...");

                    HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                    log.info("   📡 Sending request to OpenAI...");
                    ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

                    log.info("   ✅ OpenAI response status: {}", response.getStatusCode());

                    String body = response.getBody() == null ? "" : response.getBody();
                    MediaType contentType = response.getHeaders().getContentType();
                    String contentTypeStr = contentType == null ? "none" : contentType.toString();

                    // Validate status and content type before parsing JSON
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        log.warn("   ⚠️ OpenAI returned non-2xx status: {} - Body snippet: {}", response.getStatusCode(), trimForLog(body, 500));
                        return fallback;
                    }

                    if (contentType == null || !contentTypeStr.toLowerCase().contains("application/json")) {
                        // detect HTML in body
                        if (body.trim().startsWith("<")) {
                            log.error("   ❌ OpenAI API returned HTML (likely an error or redirect page). Content-Type: {}. Body snippet: {}", contentTypeStr, trimForLog(body, 1000));
                            return fallback;
                        } else {
                            log.error("   ❌ OpenAI API returned non-JSON Content-Type: {}. Body snippet: {}", contentTypeStr, trimForLog(body, 500));
                            return fallback;
                        }
                    }

                    log.debug("   Raw response: {}", body.length() > 200 ? body + "..." : body);

                    // Parse OpenAI response
                    JsonNode jsonResponse;
                    try {
                        jsonResponse = objectMapper.readTree(body);
                    } catch (Exception e) {
                        log.error("   ❌ Failed to parse JSON response. Body snippet: {}", trimForLog(body, 1000), e);
                        return fallback;
                    }

                    if (jsonResponse.has("choices") && jsonResponse.get("choices").size() > 0) {
                        JsonNode choice = jsonResponse.get("choices").get(0);
                        JsonNode messageNode = choice.has("message") ? choice.get("message") : choice;
                        String content = extractContentFromChoice(choice);

                        log.info("   ✅ Successfully extracted content from OpenAI response - Length: {}", content.length());
                        log.debug("   Content preview: {}", content.length() > 100 ? content.substring(0, 100) + "..." : content);

                        return content.trim();
                    } else {
                        log.error("   ❌ OpenAI response missing 'choices' field");
                        log.error("   Response body: {}", trimForLog(body, 1000));
                        return fallback;
                    }
                } catch (Exception e) {
                    log.error("   ❌ OpenAI API call failed!", e);
                    log.error("   Error type: {}", e.getClass().getName());
                    log.error("   Error message: {}", e.getMessage());
                    if (e.getCause() != null) {
                        log.error("   Caused by: {}", e.getCause().getMessage());
                    }
                }

                log.warn("   ⚠️ Falling back to default content");
                return fallback;
            }

            private String buildBulletFallback(List<String> keywords) {
                log.debug("   Building fallback bullet points");
                StringJoiner joiner = new StringJoiner("\n");
                int limit = Math.min(keywords.size(), 5);
                for( int i=0; i<limit; i++ ) {
                    joiner.add("- Delivered measurable impact with " + keywords.get(i)
                            + " through cross-functional execution and KPI-focused initiatives");
                }
                if (limit == 0) {
                    joiner.add("- Delivered measurable impact through cross-functional execution and KPI-focused initiatives");
                }
                return joiner.toString();
            }

            private String buildCoverLetterFallback(List<String> keywords){
                log.debug("   Building fallback cover letter");
                return "Dear Hiring Manager,\n\n"
                        + "I am excited to apply for this role. My experience and skills align well with the requirements, especially in areas like "
                        + String.join(", ", keywords)
                        + ". I am eager to contribute to your team and help drive success.\n\n"
                        + "Thank you for considering my application.\n\n"
                        + "Best regards,\n"
                        + "Candidate";
            }
            private String extractContentFromChoice(JsonNode choice) {
                // prefer message if present
                JsonNode messageNode = choice.has("message") ? choice.get("message") : choice;
                JsonNode contentNode = null;

                // common places providers put content
                if (messageNode.has("content")) contentNode = messageNode.get("content");
                else if (messageNode.has("output")) contentNode = messageNode.get("output");
                else if (choice.has("text")) contentNode = choice.get("text");
                else contentNode = messageNode;

                StringBuilder sb = new StringBuilder();
                appendTextFromNode(contentNode, sb);
                return sb.toString().trim();
            }

            private void appendTextFromNode(JsonNode node, StringBuilder sb) {
                if (node == null || node.isNull()) return;

                if (node.isTextual()) {
                    sb.append(node.asText());
                    return;
                }

                if (node.isArray()) {
                    for (JsonNode elem : node) {
                        // common element forms: plain text, { "text": "..." }, { "type":"output_text","text":"..." }
                        if (elem.isTextual()) {
                            sb.append(elem.asText());
                        } else if (elem.has("text")) {
                            sb.append(elem.get("text").asText());
                        } else if (elem.has("content")) {
                            appendTextFromNode(elem.get("content"), sb);
                        } else {
                            appendTextFromNode(elem, sb);
                        }
                        // preserve simple separation between array items
                        if (sb.length() > 0 && sb.charAt(sb.length()-1) != '\n') sb.append("\n");
                    }
                    return;
                }

                if (node.isObject()) {
                    // common object shapes
                    if (node.has("text") && node.get("text").isTextual()) {
                        sb.append(node.get("text").asText());
                        return;
                    }
                    if (node.has("content")) {
                        appendTextFromNode(node.get("content"), sb);
                        return;
                    }
                    // iterate fields for anything textual
                    node.fields().forEachRemaining(entry -> {
                        appendTextFromNode(entry.getValue(), sb);
                    });
                }
            }
            private static String trimForLog(String s, int max) {
                if (s == null || max <= 0) return "";
                if (s.length() <= max) return s;
                int end = Math.min(max, s.length());

                if(end>0 && end<s.length()){
                    char last = s.charAt(end-1);
                    char next = s.charAt(end);
                    if(Character.isHighSurrogate(last) && Character.isLowSurrogate(next)){
                        end = end - 1; // avoid cutting in the middle of a surrogate pair
                    }
                }
                return s.substring(0, end) + "...(truncated)";
            }
        }