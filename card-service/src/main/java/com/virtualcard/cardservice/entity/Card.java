package com.virtualcard.cardservice.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

import com.virtualcard.common.enums.CardStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Entity
@Table(name = "cards")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card implements BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "code", nullable = false, unique = true)
	private String code;

	@Column(name = "balance", nullable = false)
	private BigDecimal balance;

	@Column(name = "created_at")
	private Instant createdAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private CardStatus status;

	@Version
	private int version;

	@Column(name = "cvc", length = 4)
	private String cvc;

	@Column(name = "expiration_date")
	private YearMonth expirationDate;

	@PrePersist
	public void prePersist() {
		if (status == null) {
			status = CardStatus.BLOCKED; // default
		}
	}

}
