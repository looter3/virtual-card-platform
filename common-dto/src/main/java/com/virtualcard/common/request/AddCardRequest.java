package com.virtualcard.common.request;

import java.time.YearMonth;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
public record AddCardRequest(String username, String cardNumber, YearMonth expiration, String cvc) {
}
