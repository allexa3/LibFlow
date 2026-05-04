package com.andrei.demo.controller;

import com.andrei.demo.model.Genre;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class BookControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String authToken;
    private UUID personId;
    private UUID genreId;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        bookRepository.flush();
        personRepository.deleteAll();
        personRepository.flush();
        genreRepository.deleteAll();
        genreRepository.flush();

        // Create a person to use as borrower
        Person person = new Person();
        person.setName("Test User");
        person.setEmail("test.user@example.com");
        person.setPassword(passwordUtil.hashPassword("Test@1234!"));
        person.setAge(30);
        person.setRole(UserRole.ADMIN);
        Person saved = personRepository.save(person);
        personId = saved.getId();

        // Create a genre
        Genre genre = new Genre();
        genre.setName("Test Genre");
        Genre savedGenre = genreRepository.save(genre);
        genreId = savedGenre.getId();

        authToken = jwtUtil.createToken(saved);
    }

    @Test
    void testGetAllBooks_EmptyInitially() throws Exception {
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateBook_ValidPayload() throws Exception {
        Map<String, Object> dto = Map.of(
                "title", "The Clean Coder",
                "authorName", "Robert Martin",
                "isbn", "9780137081073",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("The Clean Coder"))
                .andExpect(jsonPath("$.isbn").value("9780137081073"))
                .andExpect(jsonPath("$.borrowedBy.id").value(personId.toString()));
    }

    @Test
    void testCreateBook_BorrowLimitExceeded_ReturnsBadRequest() throws Exception {
        // Create 3 books for that person
        for (int i = 0; i < 3; i++) {
            Map<String, Object> dto = Map.of(
                    "title", "Borrowed Book " + i,
                    "authorName", "Author",
                    "isbn", "978000000000" + i,
                    "personId", personId.toString(),
                    "genreIds", new String[]{genreId.toString()}
            );
            mockMvc.perform(post("/books")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        // Now try to create a fourth book assigned to the same person
        Map<String, Object> fourth = Map.of(
                "title", "Borrowed Book 4",
                "authorName", "Author",
                "isbn", "9780000000004",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fourth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You have reached the maximum limit of 3 borrowed books."));
    }

    @Test
    void testUpdateBook_BorrowLimitExceeded_ReturnsBadRequest() throws Exception {
        // Create 3 books for that person
        for (int i = 0; i < 3; i++) {
            Map<String, Object> dto = Map.of(
                    "title", "Book " + i,
                    "authorName", "Author",
                    "isbn", "978111111111" + i,
                    "personId", personId.toString(),
                    "genreIds", new String[]{genreId.toString()}
            );
            mockMvc.perform(post("/books")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        // Create an unassigned book
        Map<String, Object> unassigned = Map.of(
                "title", "Unassigned Book",
                "authorName", "Author",
                "isbn", "9781111111114",
                "genreIds", new String[]{genreId.toString()}
        );

        String unassignedJson = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unassigned)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String unassignedId = objectMapper.readTree(unassignedJson).get("id").asText();

        // Update the unassigned book to be assigned to this person
        Map<String, Object> updatePayload = Map.of(
                "title", "Assigned now",
                "authorName", "Author",
                "isbn", "9781111111114",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        mockMvc.perform(put("/books/" + unassignedId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You have reached the maximum limit of 3 borrowed books."));
    }

    @Test
    void testCreateBook_DuplicateIsbn_ReturnsBadRequest() throws Exception {
        Map<String, Object> dto = Map.of(
                "title", "Book One",
                "authorName", "Author",
                "isbn", "9780137081073",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        // Create first time
        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Try to create again with same ISBN
        Map<String, Object> dto2 = Map.of(
                "title", "Book Two",
                "authorName", "Author",
                "isbn", "9780137081073",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateBook_MissingTitle_ReturnsBadRequest() throws Exception {
        Map<String, Object> dto = Map.of(
                "authorName", "Author",
                "isbn", "9780137081073",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteBook() throws Exception {
        // Create a book first
        Map<String, Object> dto = Map.of(
                "title", "Delete Me",
                "authorName", "Author",
                "isbn", "9780132350884",
                "personId", personId.toString(),
                "genreIds", new String[]{genreId.toString()}
        );

        String responseJson = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String bookId = objectMapper.readTree(responseJson).get("id").asText();

        // Delete it
        mockMvc.perform(delete("/books/" + bookId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify it's gone
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllBooks_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isUnauthorized());
    }
}