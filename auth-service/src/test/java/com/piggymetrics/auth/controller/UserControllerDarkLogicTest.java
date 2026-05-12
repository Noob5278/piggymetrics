package com.piggymetrics.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piggymetrics.auth.domain.User;
import com.piggymetrics.auth.service.UserService;
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

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dark-logic edge-case tests for UserController (auth-service).
 *
 * KEY FINDING: The auth-service User domain class lacks validation annotations
 * (@NotNull, @Length), unlike the account-service User class. This means
 * @Valid on the controller endpoint does NOT reject null/empty usernames
 * or passwords — they pass through to the service layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerDarkLogicTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@InjectMocks
	private UserController userController;

	@Mock
	private UserService userService;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
	}

	/**
	 * Documents that an empty JSON body is accepted (200) because
	 * the auth-service User entity has no validation annotations.
	 * This is a gap: null username/password reach the service layer.
	 */
	@Test
	public void shouldAcceptEmptyBodyDueToMissingValidationAnnotations() throws Exception {
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isOk());

		verify(userService, times(1)).create(any(User.class));
	}

	/**
	 * Documents that null username is accepted at controller level
	 * because auth User class lacks @NotNull on username field.
	 */
	@Test
	public void shouldAcceptNullUsernameDueToMissingValidation() throws Exception {
		User user = new User();
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());

		verify(userService, times(1)).create(any(User.class));
	}

	/**
	 * Documents that null password is accepted at controller level
	 * because auth User class lacks @NotNull on password field.
	 */
	@Test
	public void shouldAcceptNullPasswordDueToMissingValidation() throws Exception {
		User user = new User();
		user.setUsername("validuser");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());

		verify(userService, times(1)).create(any(User.class));
	}

	@Test
	public void shouldReturn200ForValidUser() throws Exception {
		User user = new User();
		user.setUsername("validuser");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());
	}

	/**
	 * When the service layer throws IllegalArgumentException for a duplicate user,
	 * the auth-service controller (unlike account-service) has no @ControllerAdvice
	 * ErrorHandler to map it to 400. The exception propagates as a NestedServletException.
	 */
	@Test(expected = org.springframework.web.util.NestedServletException.class)
	public void shouldPropagateExceptionWhenServiceThrowsDuplicateUser() throws Exception {
		doThrow(new IllegalArgumentException("user already exists: duplicate"))
				.when(userService).create(any(User.class));

		User user = new User();
		user.setUsername("duplicate");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}
}
