package com.virtualcard.edgeserver.auth.controller;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.web.bind.annotation.RestController;

import com.virtualcard.edgeserver.auth.service.JwtServiceImpl;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@RestController
public class LoginController extends AbstractLoginController<JwtServiceImpl> {

	public LoginController(final JwtServiceImpl jwtService, final ReactiveAuthenticationManager authenticationManager) {
		super(jwtService, authenticationManager);
	}

}
