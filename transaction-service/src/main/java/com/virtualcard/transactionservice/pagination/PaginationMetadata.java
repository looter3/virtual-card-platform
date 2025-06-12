package com.virtualcard.transactionservice.pagination;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationMetadata {
    private int currentPage;
    private int pageSize;
    private int totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
