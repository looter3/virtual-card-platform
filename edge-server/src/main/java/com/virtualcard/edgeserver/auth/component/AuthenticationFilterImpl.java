package com.virtualcard.edgeserver.auth.component;

import org.springframework.stereotype.Component;

import com.virtualcard.common.security.component.AbstractAuthenticationFilter;
import com.virtualcard.edgeserver.auth.service.JwtServiceImpl;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Component
public class AuthenticationFilterImpl extends AbstractAuthenticationFilter {

	public AuthenticationFilterImpl(final JwtServiceImpl jwtService) {
		super(jwtService);
	}

}
