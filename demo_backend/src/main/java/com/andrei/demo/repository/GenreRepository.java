package com.andrei.demo.repository;

import com.andrei.demo.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {
    // This allows the service to check for existing genres by name
    Optional<Genre> findByName(String name);
}