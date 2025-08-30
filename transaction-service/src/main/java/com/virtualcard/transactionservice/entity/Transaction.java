package com.virtualcard.transactionservice.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.virtualcard.common.enums.TransactionType;
import com.virtualcard.common.springdata.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Lorenzo Leccese
 *
 *         3 ago 2025
 *
 */
@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, unique = true)
	private String code;

	@Column(name = "sender_card_id", nullable = false)
	private Long senderCardId;

	@Column(name = "recipient_card_id", nullable = false)
	private Long recipientCardId;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TransactionType type;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "created_at")
	private Instant createdAt;

}
