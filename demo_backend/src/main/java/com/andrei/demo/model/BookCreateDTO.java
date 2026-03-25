package com.andrei.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class BookCreateDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(978|979)[0-9]{10}$", message = "Invalid ISBN-13 format")
    private String isbn;

    @NotNull(message = "Publisher ID is required")
    private UUID publisherId; // For the 1:n relationship

    private List<UUID> authorIds; // For the n:m relationship
    private List<UUID> genreIds;  // For the n:m relationship
}