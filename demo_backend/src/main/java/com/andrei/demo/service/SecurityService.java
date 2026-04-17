package com.andrei.demo.service;

import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.util.PasswordUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SecurityService {
    private final PersonRepository personRepository;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    public LoginResponse login(String email, String password) {
        Optional<Person> maybePerson = personRepository.findByEmail(email);
        if (maybePerson.isEmpty()) {
            return new LoginResponse(
                    "Person with email " + email + " not found"
            );
        }
        Person person = maybePerson.get();
        if (passwordUtil.checkPassword(password, person.getPassword())) {
            String token = jwtUtil.createToken(person);
            // Return the actual role from the person entity, not hardcoded "ADMIN"
            String role = person.getRole() != null ? person.getRole().name() : "CUSTOMER";
            return new LoginResponse(role, token);
        } else {
            return new LoginResponse("Incorrect password");
        }
    }
}