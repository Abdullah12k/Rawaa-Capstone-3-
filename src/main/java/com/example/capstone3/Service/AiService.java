package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.Models.Contract;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.Review;
import com.example.capstone3.Models.UserPreference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    // OpenAI Responses API endpoint used for all AI text generation.
    private static final String OPENAI_RESPONSES_URL =
            "https://api.openai.com/v1/responses";

    // Secrets stay outside source control and are supplied through environment variables.
    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    // RestTemplate sends prompts to OpenAI.
    private final RestTemplate restTemplate;
    // ObjectMapper builds OpenAI request JSON and reads response JSON.
    private final ObjectMapper objectMapper;

    // OpenAI only writes text from supplied facts; it does not read or update project data.
    public String generateText(String prompt, String language) {
        if (!"AR".equals(language) && !"EN".equals(language)) {
            throw new ApiException("Language must be AR or EN");
        }

        try {
            // Language changes OpenAI's response only; stored project data is unchanged.
            String languageInstruction = "AR".equals(language)
                    ? "Respond in Arabic.\n\n"
                    : "Respond in English.\n\n";
            String requestBody = buildRequestBody(languageInstruction + prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_RESPONSES_URL, HttpMethod.POST, request, String.class);

            return extractGeneratedText(response.getBody());

        } catch (HttpClientErrorException e) {
            throw new ApiException(
                    "OpenAI error " + e.getStatusCode() + ": "
                            + e.getResponseBodyAsString());

        } catch (Exception e) {
            throw new ApiException("OpenAI call failed: " + e.getMessage());
        }
    }

    // Sends owner details and tenant reviews to OpenAI for a written analysis.
    public String generateOwnerReviewAnalysis(Owner owner, List<Review> reviews, String language) {
        String prompt = buildOwnerAnalysisPrompt(owner, reviews);
        return generateText(prompt, language);
    }

    // Builds the owner-review facts and instructions OpenAI is allowed to analyze.
    private String buildOwnerAnalysisPrompt(Owner owner, List<Review> reviews) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following tenant reviews for a property owner and provide a concise professional summary.\n\n");
        prompt.append("Owner: ").append(owner.getFullName()).append("\n");
        prompt.append("Total Reviews: ").append(reviews.size()).append("\n\n");
        prompt.append("=== REVIEWS ===\n");
        for (int i = 0; i < reviews.size(); i++) {
            Review r = reviews.get(i);
            prompt.append("\nReview ").append(i + 1).append(":\n");
            prompt.append("Rating: ").append(r.getRating()).append("/5\n");
            prompt.append("Comment: ").append(r.getComment()).append("\n");
        }
        prompt.append("\n=== YOUR TASK ===\n");
        prompt.append("Write a plain text analysis of 4 to 8 sentences maximum.\n");
        prompt.append("Do not use headings, bullet points, numbered lists, bold text, or any Markdown formatting.\n");
        prompt.append("Cover only these four points in flowing prose: overall tenant satisfaction, common strengths, common complaints, and an overall conclusion.\n");
        prompt.append("Write in a clear and professional tone. Keep it concise and easy to read.");
        return prompt.toString();
    }

    // Creates the JSON structure required by the OpenAI Responses API.
    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("input", prompt);

        return objectMapper.writeValueAsString(root);
    }

    // Reads generated text from the OpenAI Responses API output items.
    private String extractGeneratedText(String responseBody) throws Exception {
        if (responseBody == null || responseBody.isBlank()) {
            throw new ApiException("OpenAI returned an empty response");
        }

        JsonNode root = objectMapper.readTree(responseBody);
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())
                        && !content.path("text").asText().isBlank()) {
                    return content.path("text").asText();
                }
            }
        }
        throw new ApiException("OpenAI returned no generated text");
    }

    public String cleanAiText(String text) {
        if (text == null) {
            return null;
        }
        // Strip markdown formatting before collapsing whitespace
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");   // **bold** → bold
        text = text.replaceAll("\\*(.+?)\\*", "$1");           // *italic* → italic
        text = text.replaceAll("(?m)^#{1,6}\\s*", "");        // ## heading → heading
        text = text.replaceAll("(?m)^\\d+\\.\\s+", "");       // 1. item → item
        text = text.replaceAll("(?m)^[-*]\\s+", "");           // - item → item
        // Collapse whitespace and newlines
        text = text.replace("\\n", " ")
                   .replace("\n", " ")
                   .replace("/n", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
        return text;
    }

    // CONTRACT ANALYSIS AI ENDPOINT!
    public String analyzeContract(Contract contract, String language) {
        String prompt = buildContractAnalysisPrompt(contract, language);
        String rawResponse = generateText(prompt, language);
        return cleanJsonResponse(rawResponse);
    }

    private String buildContractAnalysisPrompt(Contract contract, String language) {
        StringBuilder prompt = new StringBuilder();
        String responseLanguage = "AR".equals(language) ? "Arabic" : "English";
        prompt.append("You are an expert legal AI assistant specializing in the Saudi real estate market.\n");
        prompt.append("Analyze the following contract details.\n\n");
        prompt.append("Start Date: ").append(contract.getStartDate()).append("\n");
        prompt.append("End Date: ").append(contract.getEndDate()).append("\n");
        prompt.append("Monthly Rent: ").append(contract.getMonthlyRent()).append(" SAR\n");
        prompt.append("Security Deposit: ").append(contract.getSecurityDeposit()).append(" SAR\n\n");

        prompt.append("Task:\n");
        prompt.append("1. Provide a brief summary of the financial and time commitments.\n");
        prompt.append("2. Highlight any irregularities, financial risks, or unusual terms (e.g., if the security deposit is unusually high compared to the rent, or if the duration is irregular).\n");
        prompt.append("3. Give a final recommendation (Proceed, Negotiate, or Walk Away).\n\n");

        prompt.append("CRITICAL INSTRUCTIONS:\n");
        prompt.append("- You must output ONLY a valid JSON object. Do not add any conversational text.\n");
        prompt.append("- The JSON keys must be exactly in English: \"summary\", \"irregularities\" (a list of strings), and \"recommendation\".\n");
        prompt.append("- The actual VALUES inside the JSON must be written entirely in ").append(responseLanguage).append(".\n");

        return prompt.toString();
    }

    // Helper method to remove markdown formatting if the AI adds it.
    private String cleanJsonResponse(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return cleanAiText(response);
    }


    public String getRoommateMatches(UserPreference requester, List<UserPreference> candidates) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert AI roommate matchmaker. Analyze the 'Requester' and compare them to the 'Candidates' list.\n");
        prompt.append("Note: Work locations are provided as Latitude and Longitude coordinates. Candidates with work coordinates geographically closer to the requester's work coordinates should receive a higher match percentage.\n\n");

        prompt.append("--- REQUESTER ---\n");
        prompt.append("Work Coordinates: Lat ").append(requester.getWorkLatitude())
                .append(", Lon ").append(requester.getWorkLongitude()).append("\n");
        prompt.append("Roommate Budget: ").append(requester.getRoommateBudget()).append(" SAR\n");
        prompt.append("Lifestyle Preferences - Gym: ").append(requester.getGymPreference())
                .append(", Cafes: ").append(requester.getCafesPreference())
                .append(", Schools: ").append(requester.getSchoolPreference()).append("\n\n");

        prompt.append("--- CANDIDATES ---\n");
        for (UserPreference c : candidates) {
            prompt.append("Candidate ID: ").append(c.getUser().getId()).append("\n");
            prompt.append("Roommate Budget: ").append(c.getRoommateBudget()).append(" SAR\n");
            prompt.append("Work Coordinates: Lat ").append(c.getWorkLatitude())
                    .append(", Lon ").append(c.getWorkLongitude()).append("\n");
            prompt.append("Lifestyle Preferences - Gym: ").append(c.getGymPreference())
                    .append(", Cafes: ").append(c.getCafesPreference())
                    .append(", Schools: ").append(c.getSchoolPreference()).append("\n\n");
        }

        prompt.append("CRITICAL INSTRUCTIONS:\n");
        prompt.append("- Return ONLY a valid JSON array of objects. No markdown, no extra text.\n");
        prompt.append("- Each object must have exactly these keys: \"candidateId\" (integer), \"matchPercentage\" (integer out of 100), and \"reason\" (a 1-sentence explanation of why they are a good match based on budget, location, and lifestyle).\n");
        prompt.append("- Sort the array from highest matchPercentage to lowest.\n");

        String rawResponse = generateText(prompt.toString(), "EN");
        return cleanJsonResponse(rawResponse);
    }
}
