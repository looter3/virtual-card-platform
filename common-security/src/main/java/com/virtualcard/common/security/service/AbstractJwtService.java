package com.virtualcard.common.security.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.extern.log4j.Log4j2;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Log4j2
public abstract class AbstractJwtService {

	static final long EXPIRATIONTIME = 86_400_000; // 1 day in ms
	static final String PREFIX = "Bearer ";
//	private static final String AUTHORITIES_KEY = "roles";

	static final SecretKey key = Jwts.SIG.HS256.key().build();

	// Generate signed JWT token
	public String getToken(final String username) {
		return Jwts.builder()
			.subject(username)
			.expiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
			.signWith(key)
			.compact();
	}

	public Mono<String> getAuthUser(final String token) {
		return Mono.fromCallable(() -> {
			try {
				final JwtParser jwtParser = Jwts.parser().verifyWith(key).build();
				final String username = jwtParser.parseSignedClaims(token).getPayload().getSubject();
				log.debug("Parsed username: {}", username);
				return username;
			} catch (final JwtException e) {
				log.debug("Invalid token: {}", e.getMessage());
				return null;
			}
		});
	}

	public Authentication getAuthentication(final String token) {
		final Claims claims = Jwts.parser().verifyWith(key).build()
			.parseSignedClaims(token)
			.getPayload();
		final Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
		final User principal = new User(claims.getSubject(), StringUtils.EMPTY, authorities);
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public boolean validateToken(final String token) {
		try {
			final Jws<Claims> claims = Jwts.parser().verifyWith(key).build()
				.parseSignedClaims(token);
			if (claims.getPayload().getExpiration().before(new Date())) {
				return false;
			}
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.info("Invalid JWT token.");
		}
		return false;
	}

}
