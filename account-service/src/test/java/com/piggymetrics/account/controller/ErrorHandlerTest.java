package com.piggymetrics.account.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorHandlerTest {

	private MockMvc mockMvc;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new ThrowingController())
				.setControllerAdvice(new ErrorHandler())
				.build();
	}

	@Test
	public void shouldReturnBadRequestForIllegalArgumentException() throws Exception {
		mockMvc.perform(get("/throw"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldNotInterfereWithSuccessfulResponses() throws Exception {
		mockMvc.perform(get("/ok"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldHandleIllegalArgumentExceptionWithNullMessage() throws Exception {
		mockMvc.perform(get("/throwNull"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void processValidationErrorShouldReturnNormallyWhenInvokedDirectly() {
		new ErrorHandler().processValidationError(new IllegalArgumentException("boom"));
	}

	@RestController
	static class ThrowingController {

		@GetMapping("/throw")
		public void throwIllegalArgument() {
			throw new IllegalArgumentException("invalid argument");
		}

		@GetMapping("/throwNull")
		public void throwIllegalArgumentWithNullMessage() {
			throw new IllegalArgumentException();
		}

		@GetMapping("/ok")
		public ResponseEntity<String> ok() {
			return ResponseEntity.ok("ok");
		}
	}
}
