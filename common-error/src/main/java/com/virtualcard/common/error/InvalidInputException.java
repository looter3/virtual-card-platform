package com.virtualcard.common.error;

@SuppressWarnings("serial")
public class InvalidInputException extends VirtualCardPlatformException {

	public InvalidInputException() {}

	public InvalidInputException(final String message) {
		super(message);
	}

	public InvalidInputException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidInputException(final Throwable cause) {
		super(cause);
	}

}
