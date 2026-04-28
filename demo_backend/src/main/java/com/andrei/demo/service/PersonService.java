package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.UserRole;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.PasswordUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PasswordUtil passwordUtil;

    public List<Person> getPeople() {
        return personRepository.findAll();
    }


    public Person addPerson(PersonCreateDTO personDTO) throws ValidationException {
        if (personRepository.findByEmail(personDTO.getEmail()).isPresent()) {
            throw new ValidationException("Email " + personDTO.getEmail() + " is already in use.");
        }
        Person person = new Person();

        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setRole(personDTO.getRole());
        String hashedPassword = passwordUtil.hashPassword(personDTO.getPassword());
        person.setPassword(hashedPassword);

        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) throws ValidationException {
        Person existingPerson = personRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Person with id " + uuid + " not found"));

        // Improved email uniqueness check
        personRepository.findByEmail(person.getEmail()).ifPresent(p -> {
            if (!p.getId().equals(uuid)) {
                throw new RuntimeException("Email " + person.getEmail() + " is already in use by another account.");
            }
        });

        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        existingPerson.setRole(person.getRole());

        // Only re-hash if a new password is provided
        if (person.getPassword() != null && !person.getPassword().isEmpty()) {
            existingPerson.setPassword(passwordUtil.hashPassword(person.getPassword()));
        }
        return personRepository.save(existingPerson);
    }

    public Person updatePerson2(UUID uuid, Person person) throws ValidationException{
        return personRepository
                        .findById(uuid)
                        .map(existingPerson -> {
                            existingPerson.setName(person.getName());
                            existingPerson.setAge(person.getAge());
                            existingPerson.setEmail(person.getEmail());
                            existingPerson.setPassword(person.getPassword());
                            existingPerson.setRole(person.getRole());
                            return personRepository.save(existingPerson);
                        })
                        .orElseThrow(
                                () -> new ValidationException("Person with id " + uuid + " not found")
                        );
    }

    public void deletePerson(UUID uuid) {
        personRepository.deleteById(uuid);
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Person with id " + uuid + " not found"));
    }

    public Person patchPerson(UUID uuid, Map<String, Object> updates) throws ValidationException {
        Person existingPerson = personRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Person with id " + uuid + " not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    existingPerson.setName((String) value);
                    break;
                case "age":
                    existingPerson.setAge((Integer) value);
                    break;
                case "email":
                    // Basic check for duplicate email during patch
                    String newEmail = (String) value;
                    if (!newEmail.equals(existingPerson.getEmail()) &&
                            personRepository.findByEmail(newEmail).isPresent()) {
                        throw new RuntimeException("Email " + newEmail + " is already in use.");
                    }
                    existingPerson.setEmail(newEmail);
                    break;
                case "password":
                    existingPerson.setPassword((String) value);
                    break;
                case "role":
                    existingPerson.setRole(UserRole.valueOf((String) value));
                    break;
            }
        });

        return personRepository.save(existingPerson);
    }
}
