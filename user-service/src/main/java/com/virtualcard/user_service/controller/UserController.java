package com.virtualcard.user_service.controller;

import static com.virtualcard.common.lang.EndpointConstants.GET_CREDENTIALS__BY_USER;
import static com.virtualcard.common.lang.EndpointConstants.USER;
import static com.virtualcard.common.lang.EndpointConstants.USERNAME;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualcard.common.dto.Credentials;
import com.virtualcard.common.dto.UserDTO;
import com.virtualcard.user_service.service.UserService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(USER)
public class UserController {

	private final UserService userService;

	@GetMapping(USERNAME)
	public Mono<UserDTO> getUserByUsername(@PathVariable final String username) {
		return userService.findUserByUsername(username);
	}

	@GetMapping(GET_CREDENTIALS__BY_USER)
	public Mono<Credentials> getCredentialsByUsername(@PathVariable final String username) {
		return userService.getCredentialsByUsername(username);
	}

}
