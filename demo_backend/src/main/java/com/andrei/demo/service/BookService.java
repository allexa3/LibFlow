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

    private void validateBorrowLimit(UUID personId, UUID excludeBookId) throws ValidationException {
        if (personId == null) return;
        long borrowedCount = bookRepository.findAll().stream()
                .filter(b -> b.getBorrowedBy() != null
                        && b.getBorrowedBy().getId().equals(personId)
                        && (excludeBookId == null || !b.getId().equals(excludeBookId)))
                .count();

        if (borrowedCount >= 3) {
            throw new ValidationException("You have reached the maximum limit of 3 borrowed books.");
        }
    }

    public Book create(BookCreateDTO dto) throws ValidationException {
        if (bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new ValidationException("A book with this ISBN already exists.");
        }

        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthorName(dto.getAuthorName());
        book.setIsbn(dto.getIsbn());

        // personId is optional
        if (dto.getPersonId() != null) {
            validateBorrowLimit(dto.getPersonId(), null);
            book.setBorrowedBy(personRepository.findById(dto.getPersonId())
                    .orElseThrow(() -> new ValidationException("Person not found")));
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            book.setGenres(genreRepository.findAllById(dto.getGenreIds()));
        }

        return bookRepository.save(book);
    }

    /**
     * Allows a customer to borrow a book. Enforces:
     * - book must not already be borrowed
     * - customer may borrow at most 3 books
     */
    public Book borrowBook(UUID bookId, UUID personId) throws ValidationException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ValidationException("Book not found"));

        if (book.getBorrowedBy() != null) {
            throw new ValidationException("This book is already borrowed.");
        }

        validateBorrowLimit(personId, bookId);

        book.setBorrowedBy(personRepository.findById(personId)
                .orElseThrow(() -> new ValidationException("Person not found")));

        return bookRepository.save(book);
    }

    @SuppressWarnings("unchecked")
    public Book patch(UUID id, Map<String, Object> updates) throws ValidationException {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Book not found"));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case "title" -> book.setTitle((String) value);
                case "authorName" -> book.setAuthorName((String) value);
                case "isbn" -> book.setIsbn((String) value);
                case "personId" -> {
                    if (value == null) {
                        book.setBorrowedBy(null);
                    } else {
                        UUID pId = UUID.fromString(value.toString());
                        validateBorrowLimit(pId, id);
                        book.setBorrowedBy(personRepository.findById(pId).orElse(null));
                    }
                }
                case "genreIds" -> {
                    List<UUID> genreIds = ((List<?>) value).stream()
                            .map(v -> UUID.fromString(v.toString()))
                            .collect(Collectors.toList());
                    book.setGenres(genreRepository.findAllById(genreIds));
                }
            }
        }
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

        // personId is optional
        if (dto.getPersonId() != null) {
            validateBorrowLimit(dto.getPersonId(), id);
            book.setBorrowedBy(personRepository.findById(dto.getPersonId())
                    .orElseThrow(() -> new ValidationException("Person not found")));
        } else {
            book.setBorrowedBy(null);
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            book.setGenres(genreRepository.findAllById(dto.getGenreIds()));
        }
        return bookRepository.save(book);
    }
}