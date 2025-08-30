package com.virtualcard.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.virtualcard.common.security.component.AbstractAuthEntryPoint;
import com.virtualcard.common.security.component.AbstractAuthenticationFilter;
import com.virtualcard.common.security.service.AbstractUserDetailsService;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
public abstract class AbstractSecurityConfiguration<AuthFI extends AbstractAuthenticationFilter,
		UserDS extends AbstractUserDetailsService,
		AuthEP extends AbstractAuthEntryPoint> {

	@Bean
	SecurityWebFilterChain securityWebFilterChain(
			final ServerHttpSecurity http,
			final AuthFI authFilter,
			final AuthEP authEntryPoint) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(this::configureAuthorization)
			.exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
			.addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
			.authenticationManager(noOpReactiveAuthenticationManager()) // no-op
			.build();
	}

	// TODO not production ready, see below for a more dynamic approach
	@Bean
	CorsWebFilter corsWebFilter() {
		final CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedOrigin("http://localhost:5173"); // your frontend URL
		corsConfig.addAllowedMethod("*"); // GET, POST, etc
		corsConfig.addAllowedHeader("*"); // all headers
		corsConfig.setAllowCredentials(true); // if you need cookies/auth headers

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return new CorsWebFilter(source);
	}

	/*-
	@Bean
	public CorsWebFilter corsWebFilter() {
	    CorsConfiguration config = new CorsConfiguration();

	    // Read comma separated origins from env var, e.g. "http://localhost:3000,http://myapp.com"
	    // ...or better read from configuration yaml and make spring parse it in a config object to inject
	    String origins = System.getenv("FRONTEND_URLS");
	    if (origins != null) {
	        for (String origin : origins.split(",")) {
	            config.addAllowedOrigin(origin.trim());
	        }
	    } else {
	        config.addAllowedOrigin("*"); // fallback, or empty to disallow
	    }

	    config.addAllowedMethod("*");
	    config.addAllowedHeader("*");
	    config.setAllowCredentials(true);

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return new CorsWebFilter(source);
	}
	*/

	protected abstract void configureAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges);

	@Bean
	ReactiveAuthenticationManager authenticationManager(final UserDS userDetailsService,
			final PasswordEncoder passwordEncoder) {
		final UserDetailsRepositoryReactiveAuthenticationManager authManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
		authManager.setPasswordEncoder(passwordEncoder);
		return authManager;
	}

	@Bean
	ReactiveAuthenticationManager noOpReactiveAuthenticationManager() {
		return authentication -> Mono.error(new BadCredentialsException("This AuthenticationManager should not be used."));
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
