package com.virtualcard.card_aggregate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.virtualcard.common.error.RateLimitExceededException;

/**
 * @author Lorenzo Leccese
 *
 *         12 giu 2025
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles the exception thrown when a rate limit is exceeded.
	 *
	 * @param ex the {@link RateLimitExceededException} containing details of the rate-limiting error.
	 * @return a {@link ResponseEntity} with a status code of 429 (Too Many Requests) and a message explaining the error.
	 */
	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<String> handleRateLimitExceeded(final RateLimitExceededException ex) {
		return ResponseEntity
			.status(429)
			.body(ex.getMessage());
	}

}
