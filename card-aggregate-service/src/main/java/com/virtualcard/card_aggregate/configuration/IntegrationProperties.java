package com.virtualcard.card_aggregate.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lex_looter
 *
 *         11 giu 2025
 *
 */
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {
	private String cardServiceBaseUrl;
	private String transactionServiceBaseUrl;

	public String getCardServiceBaseUrl() {
		return cardServiceBaseUrl;
	}

	public void setCardServiceBaseUrl(final String cardServiceBaseUrl) {
		this.cardServiceBaseUrl = cardServiceBaseUrl;
	}

	public String getTransactionServiceBaseUrl() {
		return transactionServiceBaseUrl;
	}

	public void setTransactionServiceBaseUrl(final String transactionServiceBaseUrl) {
		this.transactionServiceBaseUrl = transactionServiceBaseUrl;
	}
}
