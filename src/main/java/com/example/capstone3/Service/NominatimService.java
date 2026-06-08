package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.NominatimLocationDTOOut;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NominatimService {

    // This constant stores the Nominatim endpoint that converts place names to coordinates.
    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    // Nominatim requires a user agent that identifies the calling application.
    private static final String USER_AGENT = "Capstone3ApartmentRecommendation/1.0";

    // RestTemplate sends workplace search requests to Nominatim.
    private final RestTemplate restTemplate;
    // ObjectMapper reads the location results from the JSON response.
    private final ObjectMapper objectMapper;

    // Search OpenStreetMap and keep only display name and coordinates.
    private List<NominatimLocationDTOOut> searchLocations(String query) {
        try {
            // Query parameters tell Nominatim what to search and how to format the result.
            URI uri = UriComponentsBuilder.fromUriString(NOMINATIM_SEARCH_URL)
                    .queryParam("q", query)
                    .queryParam("format", "jsonv2")
                    .queryParam("limit", 5)
                    .build()
                    .encode()
                    .toUri();

            // HttpHeaders stores metadata sent with the request.
            HttpHeaders headers = new HttpHeaders();
            // Ask Nominatim to return JSON data.
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.USER_AGENT, USER_AGENT);

            // exchange sends a GET request with the headers and returns the full response.
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    // HttpEntity carries the request headers; this GET request has no body.
                    new HttpEntity<>(headers),
                    String.class
            );

            // The Nominatim response is a JSON array of possible location matches.
            JsonNode results = objectMapper.readTree(response.getBody());
            List<NominatimLocationDTOOut> locations = new ArrayList<>();

            for (JsonNode result : results) {
                NominatimLocationDTOOut location = new NominatimLocationDTOOut();
                // path reads each JSON field without failing if it is missing.
                location.setDisplayName(result.path("display_name").asText());
                location.setLatitude(result.path("lat").asDouble());
                location.setLongitude(result.path("lon").asDouble());
                locations.add(location);
            }

            return locations;
        } catch (Exception e) {
            // Convert HTTP and JSON failures into the project's API exception.
            throw new ApiException("Failed to search locations using Nominatim: " + e.getMessage());
        }
    }

    // Nominatim is spelling-sensitive, so a Riyadh-specific query is used as fallback.
    public NominatimLocationDTOOut resolveWorkplaceCoordinates(String workplaceName) {
        String normalizedWorkplaceName = workplaceName.trim();
        List<NominatimLocationDTOOut> locations = searchLocations(normalizedWorkplaceName);
        if (locations.isEmpty()) {
            // Add local context when the spelling-sensitive original search finds no result.
            locations = searchLocations(normalizedWorkplaceName + " Riyadh Saudi Arabia");
        }

        if (locations.isEmpty()) {
            throw new ApiException("No location found for workplace: " + normalizedWorkplaceName);
        }
        return locations.get(0);
    }
}
