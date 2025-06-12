package com.virtualcard.common.request;

import java.math.BigDecimal;

/**
 * @author lex_looter
 *
 *         8 giu 2025
 *
 */
public record CardRequest(String cardholderName, BigDecimal initialBalance) {
}
