package com.virtualcard.card_aggregate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.virtualcard.common.error.RateLimitExceededException;

/**
 * @author lex_looter
 *
 *         12 giu 2025
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<String> handleRateLimitExceeded(final RateLimitExceededException ex) {
		return ResponseEntity
			.status(429)
			.body(ex.getMessage());
	}

}
