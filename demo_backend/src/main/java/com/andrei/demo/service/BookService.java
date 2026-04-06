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
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;
    private final GenreRepository genreRepository;

    public List<Book> getAll() { return bookRepository.findAll(); }

    public Book getById(UUID id) throws ValidationException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Book not found"));
    }

    public Book create(BookCreateDTO dto) throws ValidationException {
        if (bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new ValidationException("A book with this ISBN already exists.");
        }

        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthorName(dto.getAuthorName());
        book.setIsbn(dto.getIsbn());

        book.setBorrowedBy(personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ValidationException("Person not found")));

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            book.setGenres(genreRepository.findAllById(dto.getGenreIds()));
        }

        return bookRepository.save(book);
    }

    @SuppressWarnings("unchecked")
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
                case "genreIds" -> {
                    // value arrives as List<String> from JSON
                    List<UUID> genreIds = ((List<?>) value).stream()
                            .map(v -> UUID.fromString(v.toString()))
                            .collect(Collectors.toList());
                    book.setGenres(genreRepository.findAllById(genreIds));
                }
            }
        });
        return bookRepository.save(book);
    }

    public void delete(UUID id) { bookRepository.deleteById(id); }

    public Book update(UUID id, BookCreateDTO dto) throws ValidationException {
        Book book = getById(id);
        if (!book.getIsbn().equals(dto.getIsbn()) &&
                bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new ValidationException("A book with this ISBN already exists.");
        }
        book.setTitle(dto.getTitle());
        book.setAuthorName(dto.getAuthorName());
        book.setIsbn(dto.getIsbn());
        book.setBorrowedBy(personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ValidationException("Person not found")));
        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            book.setGenres(genreRepository.findAllById(dto.getGenreIds()));
        }
        return bookRepository.save(book);
    }
}