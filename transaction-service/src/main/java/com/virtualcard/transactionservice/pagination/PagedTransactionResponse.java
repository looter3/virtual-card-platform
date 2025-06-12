package com.virtualcard.transactionservice.pagination;

import com.jooq.generated.tables.pojos.TransactionDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedTransactionResponse {
    private List<TransactionDTO> transactions;
    private PaginationMetadata metadata;
}
