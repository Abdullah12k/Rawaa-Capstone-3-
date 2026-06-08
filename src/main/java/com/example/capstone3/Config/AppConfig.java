package com.example.capstone3.Config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // This bean lets services send HTTP requests to OpenAI, OSRM, Overpass, and Nominatim.
    @Bean
    public RestTemplate restTemplate() {
        // Spring injects this shared client into external API services.
        return new RestTemplate();
    }

    // This bean converts JSON used by controllers and external API services.
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // Adds support for Java date and time classes.
                .registerModule(new JavaTimeModule())
                // Ignores extra JSON fields that are not present in project models.
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Accepts enum values such as "preferred" and "PREFERRED".
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                // Writes dates as readable text instead of numeric timestamps.
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
