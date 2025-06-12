package com.virtualcard.common.error;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class HttpErrorInfo {

	private final ZonedDateTime timestamp;
	private final String path;
	private final HttpStatus httpStatus;
	private final String message;

}
