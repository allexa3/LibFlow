package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.UserRole;
import com.andrei.demo.repository.BookRepository;
import com.andrei.demo.repository.GenreRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class GenreControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String authToken;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        bookRepository.flush();
        genreRepository.deleteAll();
        genreRepository.flush();
        personRepository.deleteAll();
        personRepository.flush();

        Person person = new Person();
        person.setName("Admin");
        person.setEmail("admin@test.com");
        person.setPassword(passwordUtil.hashPassword("Admin@123!"));
        person.setAge(30);
        person.setRole(UserRole.ADMIN);
        Person saved = personRepository.save(person);
        authToken = jwtUtil.createToken(saved);
    }

    @Test
    void testGetAllGenres_EmptyInitially() throws Exception {
        mockMvc.perform(get("/genre")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateGenre_ValidPayload() throws Exception {
        Map<String, String> dto = Map.of("name", "Science Fiction");

        mockMvc.perform(post("/genre")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Science Fiction"));
    }

    @Test
    void testCreateGenre_DuplicateName_ReturnsBadRequest() throws Exception {
        Map<String, String> dto = Map.of("name", "Fantasy");

        mockMvc.perform(post("/genre")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/genre")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testUpdateGenre() throws Exception {
        // Create
        Map<String, String> createDto = Map.of("name", "OldName");
        String createResponse = mockMvc.perform(post("/genre")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String genreId = objectMapper.readTree(createResponse).get("id").asText();

        // Update
        Map<String, String> updateDto = Map.of("name", "NewName");
        mockMvc.perform(put("/genre/" + genreId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void testDeleteGenre() throws Exception {
        // Create
        Map<String, String> dto = Map.of("name", "ToDelete");
        String resp = mockMvc.perform(post("/genre")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        String genreId = objectMapper.readTree(resp).get("id").asText();

        // Delete
        mockMvc.perform(delete("/genre/" + genreId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify
        mockMvc.perform(get("/genre")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(jsonPath("$.length()").value(0));
    }
}