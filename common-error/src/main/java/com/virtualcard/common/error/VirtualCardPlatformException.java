package com.virtualcard.common.error;


@SuppressWarnings("serial")
public class VirtualCardPlatformException extends RuntimeException {

	public VirtualCardPlatformException() {}

	public VirtualCardPlatformException(final String message) {
		super(message);
	}

	public VirtualCardPlatformException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public VirtualCardPlatformException(final Throwable cause) {
		super(cause);
	}

}
