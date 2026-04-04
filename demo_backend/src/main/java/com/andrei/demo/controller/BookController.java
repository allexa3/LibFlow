package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Book;
import com.andrei.demo.model.BookCreateDTO;
import com.andrei.demo.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@AllArgsConstructor
@CrossOrigin
public class BookController {
    private final BookService bookService;

    @GetMapping
    public List<Book> getAll() { return bookService.getAll(); }

    @PostMapping
    public Book create(@RequestBody BookCreateDTO dto) { return bookService.create(dto); }

    @PatchMapping("/{id}")
    public Book patch(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return bookService.patch(id, updates);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) { bookService.delete(id); }
}