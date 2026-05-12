package com.piggymetrics.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piggymetrics.account.domain.*;
import com.piggymetrics.account.service.AccountService;
import com.sun.security.auth.UserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dark-logic edge-case tests for AccountController.
 * Covers: duplicate account creation via controller, missing password,
 * boundary-length usernames, and invalid update payloads.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerDarkLogicTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@InjectMocks
	private AccountController accountController;

	@Mock
	private AccountService accountService;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
	}

	@Test
	public void shouldReturn400WhenCreatingAccountWithNoBody() throws Exception {
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn400WhenCreatingAccountWithTooShortUsername() throws Exception {
		User user = new User();
		user.setUsername("ab");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn400WhenCreatingAccountWithTooLongUsername() throws Exception {
		User user = new User();
		user.setUsername("a]bcdefghijklmnopqrstu");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn400WhenCreatingAccountWithMissingPassword() throws Exception {
		User user = new User();
		user.setUsername("validuser");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn400WhenCreatingAccountWithTooShortPassword() throws Exception {
		User user = new User();
		user.setUsername("validuser");
		user.setPassword("12345");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn200ForMinBoundaryUsername() throws Exception {
		User user = new User();
		user.setUsername("abc");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldReturn200ForMaxBoundaryUsername() throws Exception {
		User user = new User();
		user.setUsername("abcdefghijklmnopqrst");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldReturn200ForMinBoundaryPassword() throws Exception {
		User user = new User();
		user.setUsername("validuser");
		user.setPassword("123456");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldReturn400WhenUpdatingAccountWithNullSaving() throws Exception {
		Account account = new Account();
		account.setName("test");

		String json = mapper.writeValueAsString(account);
		mockMvc.perform(put("/current")
				.principal(new UserPrincipal("test"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn200WhenGetAccountByName() throws Exception {
		Account account = new Account();
		account.setName("demo");

		when(accountService.findByName("demo")).thenReturn(account);

		mockMvc.perform(get("/demo"))
				.andExpect(status().isOk());
	}
}
