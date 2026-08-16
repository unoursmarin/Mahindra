export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}
