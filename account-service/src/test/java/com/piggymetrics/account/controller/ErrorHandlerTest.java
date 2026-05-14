package com.piggymetrics.account.controller;

import com.piggymetrics.account.service.AccountService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorHandlerTest {

	@InjectMocks
	private AccountController accountController;

	@Mock
	private AccountService accountService;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountController)
				.setControllerAdvice(new ErrorHandler())
				.build();
	}

	@Test
	public void shouldReturnBadRequestOnIllegalArgument() throws Exception {
		when(accountService.findByName("test")).thenThrow(new IllegalArgumentException("invalid argument"));

		mockMvc.perform(get("/test"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturnOkWhenNoException() throws Exception {
		when(accountService.findByName("test")).thenReturn(new com.piggymetrics.account.domain.Account());

		mockMvc.perform(get("/test"))
				.andExpect(status().isOk());
	}
}
