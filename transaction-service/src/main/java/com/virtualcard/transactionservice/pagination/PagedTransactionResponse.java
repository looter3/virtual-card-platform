package com.virtualcard.transactionservice.pagination;

import java.util.List;

import com.virtualcard.common.dto.TransactionDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagedTransactionResponse {
	private List<TransactionDTO> transactions;
	private PaginationMetadata metadata;
}
