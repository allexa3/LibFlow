package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private PersonService personService;

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
    void testGetPeople() {
        // given:
        List<Person> people = List.of(new Person(), new Person());

        // when:
        when(personRepository.findAll()).thenReturn(people);
        List<Person> result = personService.getPeople();

        // then:
        assertEquals(2, result.size());
        verify(personRepository, times(1)).findAll();
        assertEquals(people, result);
    }

    @Test
    void testAddPerson() {
        // given:
        PersonCreateDTO personDTO = new PersonCreateDTO();
        personDTO.setName("John");
        personDTO.setPassword("password");
        personDTO.setAge(30);
        personDTO.setEmail("john@example.com");

        when(passwordUtil.hashPassword("password")).thenReturn("hashed-password");
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Person result = personService.addPerson(personDTO);

        // then:
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPassword());
        verify(passwordUtil, times(1)).hashPassword("password");
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testUpdatePerson() throws ValidationException {
        // given:
        UUID uuid = UUID.randomUUID();
        Person existingPerson = new Person();
        existingPerson.setId(uuid);
        existingPerson.setName("John");
        existingPerson.setAge(30);
        existingPerson.setEmail("john@example.com");
        existingPerson.setPassword("old-hash");

        Person updatePayload = new Person();
        updatePayload.setId(uuid);
        updatePayload.setName("Jane");
        updatePayload.setAge(25);
        updatePayload.setEmail("jane@example.com");
        updatePayload.setPassword("newpassword");

        when(personRepository.findById(uuid)).thenReturn(Optional.of(existingPerson));
        when(passwordUtil.hashPassword("newpassword")).thenReturn("new-hash");
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Person result = personService.updatePerson(uuid, updatePayload);

        // then:
        assertEquals("Jane", result.getName());
        assertEquals(25, result.getAge());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("new-hash", result.getPassword());
        verify(personRepository, times(1)).findById(uuid);
        verify(passwordUtil, times(1)).hashPassword("newpassword");
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testUpdatePersonNotFound() {
        // given:
        UUID uuid = UUID.randomUUID();
        Person person = new Person();

        // when:
        when(personRepository.findById(uuid)).thenReturn(Optional.empty());

        // then:
        assertThrows(ValidationException.class, () -> personService.updatePerson(uuid, person));
        verify(personRepository, times(1)).findById(uuid);
        verify(personRepository, never()).save(any(Person.class));
        verifyNoInteractions(passwordUtil);
    }

    @Test
    void testDeletePerson() {
        // given:
        UUID uuid = UUID.randomUUID();

        // when:
        doNothing().when(personRepository).deleteById(uuid);
        personService.deletePerson(uuid);

        // then:
        verify(personRepository, times(1)).deleteById(uuid);
    }
}
