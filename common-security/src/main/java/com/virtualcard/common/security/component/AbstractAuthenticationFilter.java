package com.virtualcard.common.security.component;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.virtualcard.common.security.service.AbstractJwtService;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
public abstract class AbstractAuthenticationFilter implements WebFilter {

	protected final AbstractJwtService jwtService;

	public AbstractAuthenticationFilter(final AbstractJwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
		final String token = resolveToken(exchange.getRequest());
		if ((StringUtils.isBlank(token) || !jwtService.validateToken(token))) {
			return chain.filter(exchange);
		}
		final Authentication authentication = this.jwtService.getAuthentication(token);
		return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
	}

	private String resolveToken(final ServerHttpRequest request) {
		final String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return StringUtils.EMPTY; // No JWT provided
		}

		final String token = authHeader.substring(7); // Remove "Bearer " prefix

		return token;

	}

}
