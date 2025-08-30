package com.virtualcard.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Lorenzo Leccese
 *
 *         3 ago 2025
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TransactionType {

	TRANSFER;
//  ...
//	Add more...

	public static TransactionType fromString(final String value) {
		return switch (value.toUpperCase()) {
			case "TRANSFER" -> TransactionType.TRANSFER;
			default -> throw new IllegalArgumentException("Unknown transaction type: " + value);
		};
	}

}
