export interface Genre {
  /** The unique identifier for the genre */
  id: string;

  /** The unique name of the genre (e.g., "Fantasy", "Sci-Fi") */
  name: string;

  /** * Optional list of books associated with this genre.
   * This represents the inverse side of the n:m relationship.
   */
  books?: any[]; 
}

/**
 * Data Transfer Object for creating a new Genre.
 * Matches the backend GenreCreateDTO.
 */
export interface CreateGenreDto {
  name: string;
}