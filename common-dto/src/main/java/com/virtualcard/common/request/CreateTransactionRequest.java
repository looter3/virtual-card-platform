package com.virtualcard.common.request;

import java.math.BigDecimal;

import com.jooq.generated.enums.TransactionType;

/**
 * @author lex_looter
 *
 *         8 giu 2025
 *
 */
public record CreateTransactionRequest(String cardId, BigDecimal amount, TransactionType type) {
}
