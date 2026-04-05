package com.andrei.demo.repository;

import com.andrei.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    // Helpful for checking "Duplicate Entry" edge cases (1.5p section)
    Optional<Book> findByIsbn(String isbn);
}