export interface Book {
  id: string;
  title: string;
  isbn: string;
  borrowedBy?: string;
}

export interface CreateBookDto {
  title: string;
  isbn: string;
}