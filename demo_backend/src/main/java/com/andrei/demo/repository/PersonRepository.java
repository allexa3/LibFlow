package com.andrei.demo.repository;

import com.andrei.demo.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByEmail(String email);

    Optional<Person> findByEmailAndAge(String email, Integer age);

    @Query("SELECT p FROM Person p WHERE p.name LIKE ?1% OR p.name LIKE %?1")
    List<Person> findByNameApproximate(String name);

    List<Person> findByNameStartingWithOrNameEndingWith(String start, String end);
}
