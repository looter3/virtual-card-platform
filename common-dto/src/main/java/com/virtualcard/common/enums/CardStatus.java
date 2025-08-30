package com.virtualcard.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum CardStatus {

	ACTIVE,
	BLOCKED;

	public static CardStatus fromValue(final String value) {
		return switch (value.toUpperCase()) {
			case "ACTIVE" -> CardStatus.ACTIVE;
			case "BLOCKED" -> CardStatus.BLOCKED;
			default -> throw new IllegalArgumentException("Unknown transaction type: " + value);
		};
	}
}
