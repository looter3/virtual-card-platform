package com.virtualcard.common.error;

@SuppressWarnings("serial")
public class NotFoundException extends VirtualCardPlatformException {

	public NotFoundException() {}

	public NotFoundException(final String message) {
		super(message);
	}

	public NotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(final Throwable cause) {
		super(cause);
	}

}
