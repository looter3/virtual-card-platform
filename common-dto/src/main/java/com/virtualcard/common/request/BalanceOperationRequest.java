package com.virtualcard.common.request;

import java.math.BigDecimal;

import com.virtualcard.common.enums.TransactionType;

/**
 * @author Lorenzo Leccese
 *
 *         28 ago 2025
 *
 */
public record BalanceOperationRequest(String senderCardNumber, String recipientCardNumber, BigDecimal amount, TransactionType type) {

}
