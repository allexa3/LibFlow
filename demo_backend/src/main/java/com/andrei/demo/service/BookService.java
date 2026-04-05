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

    public Book create(BookCreateDTO dto) throws ValidationException {
        // 1. Validation: Check for duplicate ISBN (Requirement 1.5p)
        if (bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new ValidationException("A book with this ISBN already exists.");
        }

        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthorName(dto.getAuthorName());
        book.setIsbn(dto.getIsbn());

        // 2. Handle 1:n Relationship (BorrowedBy / Person)
        if (dto.getPersonId() != null) {
            book.setBorrowedBy(personRepository.findById(dto.getPersonId())
                    .orElseThrow(() -> new ValidationException("Person not found")));
        }

        // 3. Handle n:m Relationship (Genres)
        // Assuming you have a genreRepository injected
        // List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
        // book.setGenres(genres);

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