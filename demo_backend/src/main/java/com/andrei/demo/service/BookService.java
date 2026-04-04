package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Book;
import com.andrei.demo.model.BookCreateDTO;
import com.andrei.demo.repository.BookRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;

    public List<Book> getAll() { return bookRepository.findAll(); }

    public Book create(BookCreateDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        return bookRepository.save(book);
    }

    // PATCH implementation for partial updates 
    public Book patch(UUID id, Map<String, Object> updates) throws ValidationException {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Book not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "title" -> book.setTitle((String) value);
                case "isbn" -> book.setIsbn((String) value);
                case "borrowedBy" -> {
                    // Logic for 1:n relationship [cite: 21]
                    if (value == null) book.setBorrowedBy(null);
                    else book.setBorrowedBy(personRepository.findById(UUID.fromString(value.toString())).orElse(null));
                }
            }
        });
        return bookRepository.save(book);
    }

    public void delete(UUID id) { bookRepository.deleteById(id); }
}