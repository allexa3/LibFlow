package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Book;
import com.andrei.demo.model.BookCreateDTO;
import com.andrei.demo.service.BookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@AllArgsConstructor
// Removed @CrossOrigin - CORS is handled globally by SecurityConfig
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public Book getById(@PathVariable UUID id) throws ValidationException {
        return bookService.getById(id);
    }

    @GetMapping
    public List<Book> getAll() {
        return bookService.getAll();
    }

    @PostMapping
    public Book create(@RequestBody @Valid BookCreateDTO dto) throws ValidationException {
        return bookService.create(dto);
    }

    @PatchMapping("/{id}")
    public Book patch(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return bookService.patch(id, updates);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        bookService.delete(id);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable UUID id, @RequestBody @Valid BookCreateDTO dto) throws ValidationException {
        return bookService.update(id, dto);
    }
}