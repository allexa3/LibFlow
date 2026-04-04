package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Genre;
import com.andrei.demo.model.GenreCreateDTO;
import com.andrei.demo.service.GenreService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/genre")
@AllArgsConstructor
@CrossOrigin
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<Genre> getAll() { return genreService.getAll(); }

    @PostMapping
    public Genre create(@RequestBody GenreCreateDTO dto) throws ValidationException {
        return genreService.create(dto);
    }

    @PatchMapping("/{id}")
    public Genre patch(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return genreService.patch(id, updates);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) { genreService.delete(id); }
}