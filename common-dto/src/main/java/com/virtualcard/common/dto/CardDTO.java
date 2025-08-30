package com.virtualcard.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

import com.virtualcard.common.enums.CardStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CardDTO implements DTO {

	private Long id;
	private Long userId;
	private String code;
	private BigDecimal balance;
	private Instant createdAt;
	private CardStatus status;
	private Integer version;
	private String cvc;
	private YearMonth expirationDate;

}
