package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Genre;
import com.andrei.demo.model.GenreCreateDTO;
import com.andrei.demo.repository.GenreRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    /**
     * Read All - Requirement 28
     */
    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    /**
     * Read by ID - Requirement 27
     */
    public Genre getById(UUID id) throws ValidationException {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Genre with id " + id + " not found"));
    }

    /**
     * Create a new entity - Requirement 26
     * Includes manual validation for duplicate names - Requirement 32
     */
    public Genre create(GenreCreateDTO dto) throws ValidationException {
        if (genreRepository.findByName(dto.getName()).isPresent()) {
            throw new ValidationException("Genre with name '" + dto.getName() + "' already exists.");
        }

        Genre genre = new Genre();
        genre.setName(dto.getName());
        return genreRepository.save(genre);
    }

    /**
     * Update an entity - Requirement 29
     */
    public Genre update(UUID id, GenreCreateDTO dto) throws ValidationException {
        Genre existingGenre = getById(id);
        existingGenre.setName(dto.getName());
        return genreRepository.save(existingGenre);
    }

    /**
     * Patch an entity - Requirement 30
     * Allows partial updates (e.g., just the name)
     */
    public Genre patch(UUID id, Map<String, Object> updates) throws ValidationException {
        Genre genre = getById(id);

        if (updates.containsKey("name")) {
            genre.setName((String) updates.get("name"));
        }

        return genreRepository.save(genre);
    }

    /**
     * Delete an entity - Requirement 31
     */
    public void delete(UUID id) {
        genreRepository.deleteById(id);
    }
}