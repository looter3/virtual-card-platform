package com.virtualcard.cardservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.virtualcard.user_integration.service.AbstractUserIntegrationService;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Service
public class UserIntegrationService extends AbstractUserIntegrationService {

	public UserIntegrationService(final WebClient webClient) {
		super(webClient);
	}

}
