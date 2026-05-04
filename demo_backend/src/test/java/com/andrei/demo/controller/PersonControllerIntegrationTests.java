package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.UserRole;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String authToken;

    private String loadFixture(String filename) throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/fixtures/" + filename)) {
            if (is == null) {
                throw new IllegalArgumentException("Fixture file not found: " + filename);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        personRepository.flush();

        // Create initial test users for getPeople
        Person person1 = new Person();
        person1.setName("John Doe");
        person1.setEmail("john.doe@example.com");
        person1.setPassword(passwordUtil.hashPassword("ValidPass@1"));
        person1.setAge(30);
        person1.setRole(UserRole.CUSTOMER);
        personRepository.save(person1);

        Person person2 = new Person();
        person2.setName("Jane Doe");
        person2.setEmail("jane.doe@example.com");
        person2.setPassword(passwordUtil.hashPassword("ValidPass@1"));
        person2.setAge(25);
        person2.setRole(UserRole.CUSTOMER);
        personRepository.save(person2);

        // Admin person for token authorization
        Person admin = new Person();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordUtil.hashPassword("AdminPass@1"));
        admin.setAge(35);
        admin.setRole(UserRole.ADMIN);
        Person savedAdmin = personRepository.save(admin);

        authToken = jwtUtil.createToken(savedAdmin);
    }

    @Test
    void testGetPeople() throws Exception {
        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].name", Matchers.hasItems("John Doe", "Jane Doe", "Admin User")))
                .andExpect(jsonPath("$[*].email", Matchers.hasItems("john.doe@example.com", "jane.doe@example.com", "admin@example.com")));
    }

    @Test
    void testAddPerson_ValidPayload() throws Exception {
        String validPersonJson = loadFixture("valid_person.json");

        mockMvc.perform(post("/person")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"));
    }

    @Test
    void testAddPerson_InvalidPayload() throws Exception {
        String invalidPersonJson = loadFixture("invalid_person.json");

        mockMvc.perform(post("/person")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPersonJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name should be between 2 and 100 characters"))
                .andExpect(jsonPath("$.password").value("Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character"))
                .andExpect(jsonPath("$.age").value("Age is required"))
                .andExpect(jsonPath("$.email").value("Email is required"));
    }
}