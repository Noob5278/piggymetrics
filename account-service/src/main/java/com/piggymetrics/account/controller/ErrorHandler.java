package com.piggymetrics.account.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void processValidationError(IllegalArgumentException e) {
		log.info("Returning HTTP 400 Bad Request", e);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String, Object> processMethodArgumentNotValid(MethodArgumentNotValidException e) {
		log.info("Returning HTTP 400 Bad Request for invalid method argument", e);

		List<Map<String, String>> fieldErrors = new ArrayList<>();
		for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
			Map<String, String> error = new HashMap<>();
			error.put("field", fieldError.getField());
			error.put("rejectedValue", String.valueOf(fieldError.getRejectedValue()));
			error.put("message", fieldError.getDefaultMessage());
			fieldErrors.add(error);
		}

		List<String> globalErrors = new ArrayList<>();
		for (ObjectError objectError : e.getBindingResult().getGlobalErrors()) {
			globalErrors.add(objectError.getDefaultMessage());
		}

		Map<String, Object> body = new HashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Bad Request");
		body.put("message", "Validation failed");
		body.put("fieldErrors", fieldErrors);
		body.put("globalErrors", globalErrors);

		return body;
	}
}
