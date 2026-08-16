package com.mahindra.api.model;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
