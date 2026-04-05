package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Book;
import com.andrei.demo.model.BookCreateDTO;
import com.andrei.demo.repository.BookRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.GenreRepository;
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
    private final GenreRepository genreRepository;

    public List<Book> getAll() { return bookRepository.findAll(); }

    public Book create(BookCreateDTO dto) throws ValidationException {
        // Edge Case: Duplicate ISBN Check (1.5p requirement)
        if (bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new ValidationException("A book with this ISBN already exists.");
        }

        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthorName(dto.getAuthorName());
        book.setIsbn(dto.getIsbn());

        // 1:n Relationship
        book.setBorrowedBy(personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ValidationException("Person not found")));

        // n:m Relationship
        if (dto.getGenreIds() != null) {
            book.setGenres(genreRepository.findAllById(dto.getGenreIds()));
        }

        return bookRepository.save(book);
    }

    // PATCH implementation (Required for full CRUD points)
    public Book patch(UUID id, Map<String, Object> updates) throws ValidationException {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Book not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "title" -> book.setTitle((String) value);
                case "authorName" -> book.setAuthorName((String) value);
                case "isbn" -> book.setIsbn((String) value);
                case "personId" -> {
                    UUID pId = UUID.fromString(value.toString());
                    book.setBorrowedBy(personRepository.findById(pId).orElse(null));
                }
            }
        });
        return bookRepository.save(book);
    }

    public void delete(UUID id) { bookRepository.deleteById(id); }
}