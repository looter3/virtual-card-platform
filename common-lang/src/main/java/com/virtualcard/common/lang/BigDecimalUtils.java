package com.virtualcard.common.lang;

/**
 * @author Lorenzo Leccese
 *
 * 7 giu 2025
 *
 */
import java.math.BigDecimal;

public class BigDecimalUtils {

	/**
	 * Checks if two BigDecimals are numerically equal (ignores scale).
	 */
	public static boolean equals(final BigDecimal a, final BigDecimal b) {
		if (a == null || b == null) {
			return a == b; // both null = true, else false
		}
		return a.compareTo(b) == 0;
	}

	/**
	 * Checks if 'a' is greater than 'b'.
	 */
	public static boolean greaterThan(final BigDecimal a, final BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) > 0;
	}

	/**
	 * Checks if 'a' is less than 'b'.
	 */
	public static boolean lessThan(final BigDecimal a, final BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) < 0;
	}

	/**
	 * Checks if 'a' is greater or equal to 'b'.
	 */
	public static boolean greaterOrEqual(final BigDecimal a, final BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) >= 0;
	}

	/**
	 * Checks if 'a' is less or equal to 'b'.
	 */
	public static boolean lessOrEqual(final BigDecimal a, final BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) <= 0;
	}
}
