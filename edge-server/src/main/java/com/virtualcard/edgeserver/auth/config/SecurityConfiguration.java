package com.virtualcard.edgeserver.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import com.virtualcard.common.security.config.AbstractSecurityConfiguration;
import com.virtualcard.edgeserver.auth.component.AuthEntryPointImpl;
import com.virtualcard.edgeserver.auth.component.AuthenticationFilterImpl;
import com.virtualcard.edgeserver.auth.service.UserDetailsServiceImpl;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration extends AbstractSecurityConfiguration<AuthenticationFilterImpl, UserDetailsServiceImpl, AuthEntryPointImpl> {

	@Override
	protected void configureAuthorization(final ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
		exchanges
			.pathMatchers("/login").permitAll()
			.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
			.anyExchange().authenticated();
	}

}
