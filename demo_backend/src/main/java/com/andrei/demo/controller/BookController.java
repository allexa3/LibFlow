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

@RestController // REQUIRED: Tells Spring this is a Web Controller
@RequestMapping("/books") // REQUIRED: Maps this class to the /books URL
@AllArgsConstructor // REQUIRED: Automatically creates a constructor to inject bookService
@CrossOrigin(origins = "http://localhost:4200") // REQUIRED: Fixes "Failed to load" errors in Frontend
public class BookController {

    // Declare the service so the methods below can "see" it
    private final BookService bookService;

    @GetMapping
    public List<Book> getAll() {
        return bookService.getAll();
    }

    @PostMapping
    // Added 'throws ValidationException' to satisfy the compiler
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
}