package com.cognizant.springlearn;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.cognizant.springlearn.controller.CountryController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SpringLearnApplicationTests {

    @Autowired
    private CountryController countryController;

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads() {
        assertNotNull(countryController);
    }

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        ResultActions actions = mvc.perform(get("/country"));
        actions.andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuthenticate() throws Exception {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:pwd".getBytes());
        ResultActions actions = mvc.perform(get("/authenticate").header("Authorization", basicAuth));
        actions.andExpect(status().isOk());
        actions.andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testAuthenticatedCountryAccessWithJwt() throws Exception {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:pwd".getBytes());
        MvcResult authResult = mvc.perform(get("/authenticate").header("Authorization", basicAuth))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = authResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseStr);
        String token = root.path("token").asText();

        ResultActions actions = mvc.perform(get("/country").header("Authorization", "Bearer " + token));
        actions.andExpect(status().isOk());
        actions.andExpect(jsonPath("$.code").value("IN"));
        actions.andExpect(jsonPath("$.name").value("India"));
    }

    @Test
    public void testGetAllEmployeesWithJwt() throws Exception {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:pwd".getBytes());
        MvcResult authResult = mvc.perform(get("/authenticate").header("Authorization", basicAuth))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        String token = mapper.readTree(authResult.getResponse().getContentAsString()).path("token").asText();

        ResultActions actions = mvc.perform(get("/employees").header("Authorization", "Bearer " + token));
        actions.andExpect(status().isOk());
        actions.andExpect(jsonPath("$[0].id").value(101));
    }

    @Test
    public void testPostCountryValidWithJwt() throws Exception {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:pwd".getBytes());
        MvcResult authResult = mvc.perform(get("/authenticate").header("Authorization", basicAuth))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        String token = mapper.readTree(authResult.getResponse().getContentAsString()).path("token").asText();

        String countryJson = "{\"code\":\"FR\",\"name\":\"France\"}";
        ResultActions actions = mvc.perform(post("/countries")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(countryJson));
        actions.andExpect(status().isOk());
        actions.andExpect(jsonPath("$.code").value("FR"));
    }

    @Test
    public void testDeleteEmployeeNotFoundWithJwt() throws Exception {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:pwd".getBytes());
        MvcResult authResult = mvc.perform(get("/authenticate").header("Authorization", basicAuth))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        String token = mapper.readTree(authResult.getResponse().getContentAsString()).path("token").asText();

        ResultActions actions = mvc.perform(delete("/employees/999").header("Authorization", "Bearer " + token));
        actions.andExpect(status().isNotFound());
        actions.andExpect(status().reason("Employee not found"));
    }
}
