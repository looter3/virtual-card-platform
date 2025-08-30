package com.virtualcard.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.virtualcard.common.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
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
public class TransactionDTO implements DTO {

	private Long id;
	private Long senderCardId;
	private Long recipientCardId;
	private String code;
	private TransactionType type;
	private BigDecimal amount;
	private Instant createdAt;

}
