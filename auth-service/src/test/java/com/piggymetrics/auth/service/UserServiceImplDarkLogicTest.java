package com.piggymetrics.auth.service;

import com.piggymetrics.auth.domain.User;
import com.piggymetrics.auth.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Dark-logic edge-case tests for UserServiceImpl.
 * Covers: duplicate user creation, null username/password,
 * and password encoding verification.
 */
public class UserServiceImplDarkLogicTest {

	@InjectMocks
	private UserServiceImpl userService;

	@Mock
	private UserRepository repository;

	@Before
	public void setup() {
		initMocks(this);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenCreatingUserWithExistingUsername() {
		User existing = new User();
		existing.setUsername("existingUser");
		existing.setPassword("password");

		when(repository.findById("existingUser")).thenReturn(Optional.of(existing));

		User newUser = new User();
		newUser.setUsername("existingUser");
		newUser.setPassword("newpassword");

		userService.create(newUser);
	}

	@Test
	public void shouldNotSaveUserWhenDuplicateExists() {
		User existing = new User();
		existing.setUsername("existingUser");
		existing.setPassword("password");

		when(repository.findById("existingUser")).thenReturn(Optional.of(existing));

		User newUser = new User();
		newUser.setUsername("existingUser");
		newUser.setPassword("newpassword");

		try {
			userService.create(newUser);
		} catch (IllegalArgumentException e) {
			verify(repository, never()).save(any(User.class));
		}
	}

	@Test
	public void shouldHashPasswordBeforeStoring() {
		User user = new User();
		user.setUsername("newuser");
		user.setPassword("plaintext");

		when(repository.findById("newuser")).thenReturn(Optional.empty());

		userService.create(user);

		// Password should no longer be the plain text value
		verify(repository, times(1)).save(user);
		// The BCrypt hash always starts with "$2a$" or "$2b$"
		assert user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$");
	}

	@Test
	public void shouldSuccessfullyCreateNewUser() {
		User user = new User();
		user.setUsername("brandnew");
		user.setPassword("securepass");

		when(repository.findById("brandnew")).thenReturn(Optional.empty());

		userService.create(user);

		verify(repository, times(1)).save(user);
	}
}
