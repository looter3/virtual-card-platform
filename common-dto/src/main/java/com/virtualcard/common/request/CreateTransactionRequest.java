package com.virtualcard.common.request;

import java.math.BigDecimal;

import com.virtualcard.common.enums.TransactionType;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
public record CreateTransactionRequest(Long senderCardId, Long recipientCardId, BigDecimal amount, TransactionType type) {
}
