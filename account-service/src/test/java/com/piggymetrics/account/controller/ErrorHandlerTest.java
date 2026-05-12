package com.piggymetrics.account.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorHandlerTest {

	private MockMvc mockMvc;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
				.setControllerAdvice(new ErrorHandler())
				.build();
	}

	@Test
	public void shouldReturnBadRequestForIllegalArgumentException() throws Exception {
		mockMvc.perform(get("/boom"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturnBadRequestForIllegalArgumentSubclass() throws Exception {
		mockMvc.perform(get("/numberFormat"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void processValidationErrorShouldNotRethrow() {
		ErrorHandler handler = new ErrorHandler();
		handler.processValidationError(new IllegalArgumentException("invalid"));
	}

	@Test
	public void illegalArgumentHandlerShouldBeMarkedAsBadRequest() throws NoSuchMethodException {
		assertEquals(HttpStatus.BAD_REQUEST,
				ErrorHandler.class
						.getMethod("processValidationError", IllegalArgumentException.class)
						.getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class)
						.value());
	}

	@RestController
	static class ThrowingController {

		@GetMapping("/boom")
		public void boom() {
			throw new IllegalArgumentException("boom");
		}

		@GetMapping("/numberFormat")
		public void numberFormat() {
			throw new NumberFormatException("not a number");
		}
	}
}
