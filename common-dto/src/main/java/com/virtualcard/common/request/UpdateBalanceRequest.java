package com.virtualcard.common.request;

import java.math.BigDecimal;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
public record UpdateBalanceRequest(BigDecimal newBalance) {
}
