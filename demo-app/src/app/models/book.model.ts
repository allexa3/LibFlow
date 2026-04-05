export interface Book {
  id: string;
  title: string;
  authorName: string;
  isbn: string;
}

export interface CreateBookDto {
  title: string;
  authorName: string;
  isbn: string;
}
export interface BookCreateDto {
  title: string;
  authorName: string; // Ensure this is exactly 'authorName'
  isbn: string;
  publisherId: string; // Added because it's required in your Java DTO
}