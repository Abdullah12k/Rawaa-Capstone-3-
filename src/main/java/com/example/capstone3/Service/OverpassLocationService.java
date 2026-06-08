package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.ApartmentServicesDTOOut;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class OverpassLocationService {

    // This constant stores the Overpass endpoint used for nearby place searches.
    private static final String OVERPASS_URL = "https://gall.openstreetmap.de/api/interpreter";

    // RestTemplate sends the HTTP request to Overpass.
    private final RestTemplate restTemplate;
    // ObjectMapper converts the JSON response into nodes that can be read safely.
    private final ObjectMapper objectMapper;

    // Counts nearby services for location analysis and recommendation scoring.
    public ApartmentServicesDTOOut analyzeApartmentLocation(double latitude, double longitude, int radiusMetres) {

        String query = buildQuery(latitude, longitude, radiusMetres);
        JsonNode elements = fetchFromOverpass(query);

        int hospitals    = 0;
        int schools      = 0;
        int supermarkets = 0;
        int pharmacies   = 0;
        int gyms         = 0;
        int restaurants  = 0;

        for (JsonNode element : elements) {
            // path reads a JSON field without failing when the field is missing.
            JsonNode tags   = element.path("tags");
            String amenity  = tags.path("amenity").asText("");
            String shop     = tags.path("shop").asText("");
            String leisure  = tags.path("leisure").asText("");

            if (amenity.equals("hospital"))    hospitals++;
            if (amenity.equals("school"))      schools++;
            if (amenity.equals("pharmacy"))    pharmacies++;
            if (amenity.equals("restaurant") || amenity.equals("fast_food") || amenity.equals("cafe")) restaurants++;
            if (shop.equals("supermarket") || shop.equals("convenience")) supermarkets++;
            if (leisure.equals("fitness_centre") || leisure.equals("sports_centre")) gyms++;
        }

        ApartmentServicesDTOOut dto = new ApartmentServicesDTOOut();
        dto.setHospitalCount(hospitals);
        dto.setSchoolCount(schools);
        dto.setSupermarketCount(supermarkets);
        dto.setPharmacyCount(pharmacies);
        dto.setGymCount(gyms);
        dto.setRestaurantCount(restaurants);

        return dto;
    }

    // Build the OpenStreetMap query for service types used by the project.
    private String buildQuery(double lat, double lng, int radius) {
        String around = "around:" + radius + "," + lat + "," + lng;
        return "[out:json][timeout:25];"
             + "("
             + "node[\"amenity\"=\"hospital\"](" + around + ");"
             + "way[\"amenity\"=\"hospital\"](" + around + ");"
             + "node[\"amenity\"=\"school\"](" + around + ");"
             + "way[\"amenity\"=\"school\"](" + around + ");"
             + "node[\"shop\"=\"supermarket\"](" + around + ");"
             + "node[\"shop\"=\"convenience\"](" + around + ");"
             + "node[\"amenity\"=\"pharmacy\"](" + around + ");"
             + "node[\"leisure\"=\"fitness_centre\"](" + around + ");"
             + "node[\"leisure\"=\"sports_centre\"](" + around + ");"
             + "node[\"amenity\"=\"restaurant\"](" + around + ");"
             + "node[\"amenity\"=\"fast_food\"](" + around + ");"
             + "node[\"amenity\"=\"cafe\"](" + around + ");"
             + ");"
             + "out center;";
    }

    // Send the query to Overpass and return its map elements.
    private JsonNode fetchFromOverpass(String query) {
        try {
            // HttpHeaders stores request metadata such as the body format.
            HttpHeaders headers = new HttpHeaders();
            // Overpass expects its query as form data.
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Encode spaces and special characters safely using UTF-8.
            String body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            // HttpEntity combines the request body and headers.
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            // exchange sends a POST request and returns the full HTTP response.
            ResponseEntity<String> response = restTemplate.exchange(
                    OVERPASS_URL, HttpMethod.POST, request, String.class
            );

            // readTree converts the JSON text into a tree that can be navigated.
            JsonNode root = objectMapper.readTree(response.getBody());
            // elements is the JSON array containing the nearby places.
            return root.path("elements");

        } catch (Exception e) {
            // Convert external API or JSON failures into the project's API exception.
            throw new ApiException("Failed to fetch data from Overpass API: " + e.getMessage());
        }
    }
}
