package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Book;
import com.andrei.demo.model.BookCreateDTO;
import com.andrei.demo.model.Genre;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.BookRepository;
import com.andrei.demo.repository.GenreRepository;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private BookService bookService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ── getAll ──────────────────────────────────────────────────────────────

    @Test
    void testGetAll_ReturnsList() {
        List<Book> books = List.of(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAll();

        assertEquals(2, result.size());
        verify(bookRepository).findAll();
    }

    // ── getById ─────────────────────────────────────────────────────────────

    @Test
    void testGetById_Found() throws ValidationException {
        UUID id = UUID.randomUUID();
        Book book = new Book();
        book.setId(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Book result = bookService.getById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void testGetById_NotFound_ThrowsValidationException() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> bookService.getById(id));
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    void testCreate_Success() throws ValidationException {
        UUID personId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();

        BookCreateDTO dto = new BookCreateDTO();
        dto.setTitle("Clean Code");
        dto.setAuthorName("Robert Martin");
        dto.setIsbn("9780132350884");
        dto.setPersonId(personId);
        dto.setGenreIds(List.of(genreId));

        Person person = new Person();
        person.setId(personId);

        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setName("Programming");

        when(bookRepository.findByIsbn(dto.getIsbn())).thenReturn(Optional.empty());
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(genreRepository.findAllById(List.of(genreId))).thenReturn(List.of(genre));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.create(dto);

        assertEquals("Clean Code", result.getTitle());
        assertEquals("9780132350884", result.getIsbn());
        assertEquals(person, result.getBorrowedBy());
        assertEquals(1, result.getGenres().size());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testCreate_DuplicateIsbn_ThrowsValidationException() {
        BookCreateDTO dto = new BookCreateDTO();
        dto.setIsbn("9780132350884");
        dto.setPersonId(UUID.randomUUID());

        when(bookRepository.findByIsbn(dto.getIsbn())).thenReturn(Optional.of(new Book()));

        assertThrows(ValidationException.class, () -> bookService.create(dto));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void testCreate_PersonNotFound_ThrowsValidationException() {
        UUID personId = UUID.randomUUID();
        BookCreateDTO dto = new BookCreateDTO();
        dto.setTitle("Test Book");
        dto.setIsbn("9780132350884");
        dto.setPersonId(personId);

        when(bookRepository.findByIsbn(dto.getIsbn())).thenReturn(Optional.empty());
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> bookService.create(dto));
        verify(bookRepository, never()).save(any());
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    void testDelete_CallsRepository() {
        UUID id = UUID.randomUUID();
        doNothing().when(bookRepository).deleteById(id);

        bookService.delete(id);

        verify(bookRepository).deleteById(id);
    }

    // ── patch ───────────────────────────────────────────────────────────────

    @Test
    void testPatch_UpdatesTitle() throws ValidationException {
        UUID id = UUID.randomUUID();
        Book book = new Book();
        book.setId(id);
        book.setTitle("Old Title");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.patch(id, Map.of("title", "New Title"));

        assertEquals("New Title", result.getTitle());
    }

    @Test
    void testPatch_BookNotFound_ThrowsValidationException() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> bookService.patch(id, Map.of("title", "X")));
    }

    @Test
    void testPatch_UpdatesGenres() throws ValidationException {
        UUID id = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Book book = new Book();
        book.setId(id);

        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setName("Fiction");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(genreRepository.findAllById(List.of(genreId))).thenReturn(List.of(genre));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.patch(id, Map.of("genreIds", List.of(genreId.toString())));

        assertEquals(1, result.getGenres().size());
        assertEquals("Fiction", result.getGenres().get(0).getName());
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    void testUpdate_Success() throws ValidationException {
        UUID id = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Book existing = new Book();
        existing.setId(id);
        existing.setIsbn("9780132350884");

        BookCreateDTO dto = new BookCreateDTO();
        dto.setTitle("Updated Title");
        dto.setAuthorName("New Author");
        dto.setIsbn("9780132350884"); // same ISBN, no conflict
        dto.setPersonId(personId);

        Person person = new Person();
        person.setId(personId);

        when(bookRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn(dto.getIsbn())).thenReturn(Optional.empty());
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.update(id, dto);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("New Author", result.getAuthorName());
    }

    @Test
    void testUpdate_NewIsbnAlreadyExists_ThrowsValidationException() throws ValidationException {
        UUID id = UUID.randomUUID();

        Book existing = new Book();
        existing.setId(id);
        existing.setIsbn("9780000000001");

        BookCreateDTO dto = new BookCreateDTO();
        dto.setIsbn("9780000000002"); // different ISBN that already belongs to another book
        dto.setPersonId(UUID.randomUUID());

        Book conflict = new Book();
        conflict.setId(UUID.randomUUID()); // different ID → it's a real conflict

        when(bookRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn(dto.getIsbn())).thenReturn(Optional.of(conflict));

        assertThrows(ValidationException.class, () -> bookService.update(id, dto));
        verify(bookRepository, never()).save(any());
    }
}