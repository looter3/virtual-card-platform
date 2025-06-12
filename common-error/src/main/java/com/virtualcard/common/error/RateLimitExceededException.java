package com.virtualcard.common.error;

/**
 * @author Lorenzo Leccese
 *
 *         12 giu 2025
 *
 */
public class RateLimitExceededException extends RuntimeException {
	private static final long serialVersionUID = 6969736442932695997L;

	public RateLimitExceededException(final String message) {
		super(message);
	}
}
