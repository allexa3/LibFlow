package com.andrei.demo.service;

import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.UserRole;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTests {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private SecurityService securityService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }


    @Test
    void testLoginSuccess() {
        String email = "test@example.com";
        String password = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu";
        String token = "token-123";
        Person person = new Person();
        person.setEmail(email);
        // Valid BCrypt hash for "Password123!"
        person.setPassword(password);
        person.setRole(UserRole.ADMIN);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordUtil.checkPassword(password, person.getPassword())).thenReturn(true);
        when(jwtUtil.createToken(person)).thenReturn(token);

        LoginResponse result = securityService.login(email, password);

        assertTrue(result.success());
        assertEquals("ADMIN", result.role());
        assertEquals(token, result.token());
        verify(personRepository, times(1)).findByEmail(email);
        verify(passwordUtil, times(1)).checkPassword(password, person.getPassword());
        verify(jwtUtil, times(1)).createToken(person);
    }

    @Test
    void testLoginIncorrectPassword() {
        String email = "test@example.com";
        String password = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu";
        Person person = new Person();
        person.setEmail(email);
// Valid BCrypt hash for "Password123!"
        person.setPassword(password);
        person.setRole(UserRole.ADMIN);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordUtil.checkPassword(password, person.getPassword())).thenReturn(false);

        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertEquals("Incorrect password", result.errorMessage());
        verify(personRepository, times(1)).findByEmail(email);
        verify(passwordUtil, times(1)).checkPassword(password, person.getPassword());
        verify(jwtUtil, never()).createToken(any(Person.class));
    }

    @Test
    void testLoginEmailNotFound() {
        String email = "john@example.com";
        String password = "password";

        when(personRepository.findByEmail(email)).thenReturn(Optional.empty());
        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertEquals("Person with email " + email + " not found", result.errorMessage());
        verify(personRepository, times(1)).findByEmail(email);
        verifyNoInteractions(passwordUtil, jwtUtil);
    }
}
