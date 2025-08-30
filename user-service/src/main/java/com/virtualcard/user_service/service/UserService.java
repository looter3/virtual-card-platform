package com.virtualcard.user_service.service;

import static com.virtualcard.common.converter.VertxWebFluxConverter.convertUniToMono;

import org.springframework.stereotype.Service;

import com.virtualcard.common.dto.Credentials;
import com.virtualcard.common.dto.UserDTO;
import com.virtualcard.user_service.mapper.UserMapper;
import com.virtualcard.user_service.repository.ReactiveUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

	private final ReactiveUserRepository repository;
	private final UserMapper mapper;

	public Mono<UserDTO> findUserByUsername(final String username) {

		return convertUniToMono(this.repository.findByUsername(username)
			.map(e -> {
				log.debug("Found user {}", e.getUsername());
				return e;
			})
			.map(mapper::entityToDTO));
	}

	public Mono<Credentials> getCredentialsByUsername(final String username) {

		return convertUniToMono(this.repository.findByUsername(username)
			.map(e -> {
				log.debug("Found user {}", e.getUsername());
				return e;
			})
			.map(e -> new Credentials(e.getUsername(), e.getPassword(), e.getRole())));
	}
}
