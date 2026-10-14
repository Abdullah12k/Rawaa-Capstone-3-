package com.example.capstone3;

import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.BuildingRepository;
import com.example.capstone3.Repository.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(properties = "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
class ApiAuthorizationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired BuildingRepository buildings;
    @Autowired ApartmentRepository apartments;
    @Autowired MessageRepository messages;

    private record Account(int id, String token) {}

    @Test
    void jwtRolesAndResourceOwnership() throws Exception {
        Account renter1 = register("user", "u401", "0500040101", null);
        Account renter2 = register("user", "u402", "0500040102", null);
        Account owner1 = register("owner", "o401", "0590040101", "1000000401");
        Account owner2 = register("owner", "o402", "0590040102", "1000000402");

        mvc.perform(post("/api/v1/auth/register/user").contentType(MediaType.APPLICATION_JSON)
                .content("{\"profile\":{\"fullName\":\"Duplicate\",\"email\":\"new401@example.test\",\"phoneNumber\":\"0500040101\",\"gender\":\"MALE\",\"childrenCount\":0},\"password\":\"ExamplePass123!\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/auth/register/owner").contentType(MediaType.APPLICATION_JSON)
                .content("{\"profile\":{\"fullName\":\"Duplicate\",\"email\":\"newowner401@example.test\",\"phoneNumber\":\"0590040199\",\"commercialRegistrationNumber\":\"1000000401\"},\"password\":\"ExamplePass123!\"}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/apartment/get")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/user/get/" + renter1.id())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/user/get/" + renter1.id()).header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/user/get/" + renter1.id()).header("Authorization", bearer(renter1)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/user/get/" + renter2.id()).header("Authorization", bearer(renter1)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/user/get").header("Authorization", bearer(renter1)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/owner/get").header("Authorization", bearer(owner1)))
                .andExpect(status().isForbidden());

        String building = "{\"name\":\"Authorization Test\",\"city\":\"Riyadh\",\"district\":\"Olaya\",\"street\":\"Main\",\"latitude\":24.7,\"longitude\":46.6}";
        mvc.perform(post("/api/v1/building/add/" + owner1.id()).header("Authorization", bearer(owner2))
                .contentType(MediaType.APPLICATION_JSON).content(building)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/building/add/" + owner1.id()).header("Authorization", bearer(owner1))
                .contentType(MediaType.APPLICATION_JSON).content(building)).andExpect(status().isOk());
        int buildingId = buildings.findAll().stream().filter(b -> b.getName().equals("Authorization Test")).findFirst().orElseThrow().getId();
        mvc.perform(delete("/api/v1/building/delete/" + buildingId).header("Authorization", bearer(owner2)))
                .andExpect(status().isForbidden());

        String apartment = "{\"apartmentNumber\":\"A401\",\"monthlyRent\":4200,\"negotiable\":false,\"bedrooms\":2,\"bathrooms\":1,\"area\":80}";
        mvc.perform(post("/api/v1/apartment/add/" + owner1.id() + "/" + buildingId).header("Authorization", bearer(owner1))
                .contentType(MediaType.APPLICATION_JSON).content(apartment)).andExpect(status().isOk());
        int apartmentId = apartments.findAll().stream().filter(a -> a.getApartmentNumber().equals("A401")).findFirst().orElseThrow().getId();
        mvc.perform(delete("/api/v1/apartment/delete/" + apartmentId).header("Authorization", bearer(owner2)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/apartment/delete/" + apartmentId).header("Authorization", bearer(renter1)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/reservation/add/" + renter1.id() + "/" + apartmentId)
                .header("Authorization", bearer(renter2)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        String messagePath = "/api/v1/message/add/user/" + renter1.id() + "/" + owner1.id() + "/" + apartmentId;
        mvc.perform(post(messagePath).header("Authorization", bearer(renter1))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Hello\"}"))
                .andExpect(status().isOk());
        int messageId = messages.findAll().stream().filter(m -> m.getContent().equals("Hello")).findFirst().orElseThrow().getId();
        mvc.perform(get("/api/v1/message/get/" + messageId).header("Authorization", bearer(renter2)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/message/get/" + messageId).header("Authorization", bearer(owner2)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/message/get/" + messageId).header("Authorization", bearer(owner1)))
                .andExpect(status().isOk());
    }

    private Account register(String role, String name, String phone, String registration) throws Exception {
        String profile = registration == null
                ? "{\"fullName\":\"Test\",\"email\":\"" + name + "@example.test\",\"phoneNumber\":\"" + phone + "\",\"gender\":\"MALE\",\"childrenCount\":0}"
                : "{\"fullName\":\"Test\",\"email\":\"" + name + "@example.test\",\"phoneNumber\":\"" + phone + "\",\"commercialRegistrationNumber\":\"" + registration + "\"}";
        String result = mvc.perform(post("/api/v1/auth/register/" + role).contentType(MediaType.APPLICATION_JSON)
                .content("{\"profile\":" + profile + ",\"password\":\"ExamplePass123!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(result);
        return new Account(node.get("id").asInt(), node.get("token").asText());
    }

    private String bearer(Account account) { return "Bearer " + account.token(); }
}
