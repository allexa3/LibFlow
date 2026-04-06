export interface Book {
  id: string;
  title: string;
  authorName: string;
  isbn: string;
  genres?: { id: string; name: string }[];
  borrowedBy?: { id: string; name: string; email: string; };
}

export interface CreateBookDto {
  title: string;
  authorName: string;
  isbn: string;
}
export interface BookCreateDto {
  title: string;
  authorName: string;
  isbn: string;
  publisherId: string;
}
