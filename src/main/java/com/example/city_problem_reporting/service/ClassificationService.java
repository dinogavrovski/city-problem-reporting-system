package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.ClassificationResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ClassificationService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model:meta-llama/llama-4-scout:free}")
    private String model;

    private final RestClient restClient;
    private final RestClient imageDownloader;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;

    private static final String PROMPT = """
        Analyze this city infrastructure photo and respond ONLY with a JSON object, no extra text, no markdown:
        {
          "category": one of ["road_damage", "traffic_signal", "lighting", "flooding", "waste", "sidewalk", "vandalism", "other"]
        }
        """;

    public ClassificationService() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.restClient = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .build();

        this.imageDownloader = RestClient.builder()
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .build();
    }

    public ClassificationResult classifyFromUrl(String imageUrl) {
        try {
            byte[] imageBytes = imageDownloader.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);

            if (imageBytes == null) throw new RuntimeException("Image download failed");

            System.out.println("Image downloaded, size: " + imageBytes.length + " bytes");

            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mediaType = detectMediaType(imageUrl);
            String dataUrl = "data:" + mediaType + ";base64," + base64Image;

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "image_url",
                                                    "image_url", Map.of("url", dataUrl)
                                            ),
                                            Map.of(
                                                    "type", "text",
                                                    "text", PROMPT
                                            )
                                    )
                            )
                    )
            );

            return callWithRetry(requestBody);

        } catch (Exception e) {
            System.err.println("Classification error: " + e.getMessage());
            return getFallback();
        }
    }

    private ClassificationResult callWithRetry(Map<String, Object> requestBody) throws Exception {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                System.out.println("OpenRouter API attempt " + attempt + " of " + MAX_RETRIES + " using model: " + model);

                String rawResponse = restClient.post()
                        .uri("/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "http://localhost:8080")
                        .header("X-Title", "City Problem Reporting")
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                System.out.println("Raw response: " + rawResponse);
                return parseResponse(rawResponse);

            } catch (HttpServerErrorException e) {
                if (attempt < MAX_RETRIES) {
                    long delay = RETRY_DELAY_MS * attempt;
                    System.err.println("Server error on attempt " + attempt + ", retrying in " + delay + "ms...");
                    Thread.sleep(delay);
                } else {
                    throw e;
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429 && attempt < MAX_RETRIES) {
                    long delay = RETRY_DELAY_MS * 2 * attempt;
                    System.err.println("Rate limited on attempt " + attempt + ", retrying in " + delay + "ms...");
                    Thread.sleep(delay);
                } else {
                    throw e;
                }
            }
        }

        return getFallback();
    }

    private ClassificationResult parseResponse(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String jsonText = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();

        System.out.println("Parsed content: " + jsonText);

        // Strip markdown code blocks if present
        if (jsonText.contains("```")) {
            jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();
        }

        return objectMapper.readValue(jsonText, ClassificationResult.class);
    }

    private String detectMediaType(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".png")) return "image/png";
        if (lower.contains(".gif")) return "image/gif";
        if (lower.contains(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private ClassificationResult getFallback() {
        ClassificationResult fallback = new ClassificationResult();
        fallback.setCategory("other");
        return fallback;
    }
}