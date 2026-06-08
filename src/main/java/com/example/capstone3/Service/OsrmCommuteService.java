package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.CommuteAnalysisDTOOut;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OsrmCommuteService {

    // Public OSRM demo server — no API key required
    // OSRM calculates driving time and distance between apartment and workplace coordinates.
    // This constant stores the OSRM endpoint for driving routes.
    private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving/";

    // RestTemplate sends route requests to OSRM.
    private final RestTemplate restTemplate;
    // ObjectMapper reads distance and duration from the JSON response.
    private final ObjectMapper objectMapper;

    // Returns rounded distance in kilometres and travel time in minutes.
    public CommuteAnalysisDTOOut analyzeCommute(double apartmentLatitude, double apartmentLongitude,
                                                double workLatitude, double workLongitude) {
        try {
            // OSRM expects: longitude,latitude (not lat,lng)
            String url = OSRM_URL
                    + apartmentLongitude + "," + apartmentLatitude
                    + ";"
                    + workLongitude + "," + workLatitude
                    + "?overview=false";

            // getForObject sends a GET request and returns the response body as text.
            String response = restTemplate.getForObject(url, String.class);

            // readTree converts the JSON text into a navigable node tree.
            JsonNode root = objectMapper.readTree(response);
            if (!"Ok".equals(root.path("code").asText()) || !root.path("routes").isArray()
                    || root.path("routes").isEmpty()) {
                throw new ApiException("OSRM did not return a route");
            }

            // routes is a JSON array; the first item is the selected route.
            JsonNode route = root.path("routes").path(0);
            if (!route.has("distance") || !route.path("distance").isNumber()
                    || !route.has("duration") || !route.path("duration").isNumber()) {
                throw new ApiException("OSRM route data is incomplete");
            }

            // OSRM returns distance in metres and duration in seconds.
            double distanceMetres = route.path("distance").asDouble();
            double durationSeconds = route.path("duration").asDouble();

            CommuteAnalysisDTOOut dto = new CommuteAnalysisDTOOut();
            dto.setDistanceKm(Math.round((distanceMetres / 1000.0) * 10.0) / 10.0);
            dto.setDurationMinutes((int) Math.ceil(durationSeconds / 60.0));

            return dto;

        } catch (Exception e) {
            // Report HTTP and JSON failures through the project's exception type.
            throw new ApiException("Failed to calculate commute from OSRM: " + e.getMessage());
        }
    }
}
