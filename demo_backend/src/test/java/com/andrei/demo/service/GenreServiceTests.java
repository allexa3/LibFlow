package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Genre;
import com.andrei.demo.model.GenreCreateDTO;
import com.andrei.demo.repository.GenreRepository;
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

class GenreServiceTests {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

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
        List<Genre> genres = List.of(new Genre(), new Genre(), new Genre());
        when(genreRepository.findAll()).thenReturn(genres);

        List<Genre> result = genreService.getAll();

        assertEquals(3, result.size());
        verify(genreRepository).findAll();
    }

    // ── getById ─────────────────────────────────────────────────────────────

    @Test
    void testGetById_Found() throws ValidationException {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("Fiction");
        when(genreRepository.findById(id)).thenReturn(Optional.of(genre));

        Genre result = genreService.getById(id);

        assertEquals(id, result.getId());
        assertEquals("Fiction", result.getName());
    }

    @Test
    void testGetById_NotFound_ThrowsValidationException() {
        UUID id = UUID.randomUUID();
        when(genreRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> genreService.getById(id));
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    void testCreate_Success() throws ValidationException {
        GenreCreateDTO dto = new GenreCreateDTO();
        dto.setName("Science Fiction");

        when(genreRepository.findByName("Science Fiction")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> {
            Genre g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        Genre result = genreService.create(dto);

        assertNotNull(result.getId());
        assertEquals("Science Fiction", result.getName());
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void testCreate_DuplicateName_ThrowsValidationException() {
        GenreCreateDTO dto = new GenreCreateDTO();
        dto.setName("Fiction");

        when(genreRepository.findByName("Fiction")).thenReturn(Optional.of(new Genre()));

        assertThrows(ValidationException.class, () -> genreService.create(dto));
        verify(genreRepository, never()).save(any());
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    void testUpdate_Success() throws ValidationException {
        UUID id = UUID.randomUUID();
        Genre existing = new Genre();
        existing.setId(id);
        existing.setName("OldName");

        GenreCreateDTO dto = new GenreCreateDTO();
        dto.setName("NewName");

        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.findByName("NewName")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        Genre result = genreService.update(id, dto);

        assertEquals("NewName", result.getName());
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void testUpdate_SameNameSameId_NoConflict() throws ValidationException {
        UUID id = UUID.randomUUID();
        Genre existing = new Genre();
        existing.setId(id);
        existing.setName("Fiction");

        GenreCreateDTO dto = new GenreCreateDTO();
        dto.setName("Fiction");

        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        // findByName returns the same genre (same ID) → no conflict
        when(genreRepository.findByName("Fiction")).thenReturn(Optional.of(existing));
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> genreService.update(id, dto));
    }

    @Test
    void testUpdate_NameTakenByOther_ThrowsRuntimeException() {
        UUID id = UUID.randomUUID();
        Genre existing = new Genre();
        existing.setId(id);
        existing.setName("OldName");

        Genre conflicting = new Genre();
        conflicting.setId(UUID.randomUUID()); // different ID

        GenreCreateDTO dto = new GenreCreateDTO();
        dto.setName("Fiction");

        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.findByName("Fiction")).thenReturn(Optional.of(conflicting));

        assertThrows(RuntimeException.class, () -> genreService.update(id, dto));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotFound_ThrowsValidationException() {
        UUID id = UUID.randomUUID();
        when(genreRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> genreService.update(id, new GenreCreateDTO()));
    }

    // ── patch ───────────────────────────────────────────────────────────────

    @Test
    void testPatch_UpdatesName() throws ValidationException {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("OldName");

        when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
        when(genreRepository.findByName("NewName")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        Genre result = genreService.patch(id, Map.of("name", "NewName"));

        assertEquals("NewName", result.getName());
    }

    @Test
    void testPatch_NameConflict_ThrowsRuntimeException() {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("OldName");

        Genre conflicting = new Genre();
        conflicting.setId(UUID.randomUUID());

        when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
        when(genreRepository.findByName("Fiction")).thenReturn(Optional.of(conflicting));

        assertThrows(RuntimeException.class, () -> genreService.patch(id, Map.of("name", "Fiction")));
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    void testDelete_CallsRepository() {
        UUID id = UUID.randomUUID();
        doNothing().when(genreRepository).deleteById(id);

        genreService.delete(id);

        verify(genreRepository).deleteById(id);
    }
}