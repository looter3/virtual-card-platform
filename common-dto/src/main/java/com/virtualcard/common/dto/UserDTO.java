package com.virtualcard.common.dto;

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
public class UserDTO implements DTO {

	// TODO we shouldn't expose password and role.
	// but if we delete the properties we break AbstractUserDetailsService
	private Long id;
	private String username;
//	private String password;
	private String cardholderName;
//	private String role;

}
