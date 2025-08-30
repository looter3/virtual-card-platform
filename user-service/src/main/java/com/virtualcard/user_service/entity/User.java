package com.virtualcard.user_service.entity;

import com.virtualcard.common.springdata.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements BaseEntity {

	@Id
	private Long id;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	// We want 1 user for 1 carholder
	@Column(name = "cardholder_name", nullable = false, unique = true)
	private String cardholderName;

	@Column(name = "role", nullable = false)
	private String role;

}
