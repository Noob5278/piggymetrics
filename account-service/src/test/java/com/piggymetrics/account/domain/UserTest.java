package com.piggymetrics.account.domain;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserTest {

	private static Validator validator;

	@BeforeClass
	public static void setupValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void shouldGetAndSetUsernameAndPassword() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("p@ssword");

		assertEquals("alice", user.getUsername());
		assertEquals("p@ssword", user.getPassword());
	}

	@Test
	public void shouldPassValidationForValidUser() {
		User user = buildUser("alice", "p@ssword");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameIsNull() {
		User user = buildUser(null, "p@ssword");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameTooShort() {
		User user = buildUser("ab", "p@ssword");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameTooLong() {
		User user = buildUser(repeat('a', 21), "p@ssword");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtUsernameMinLength() {
		User user = buildUser("abc", "p@ssword");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtUsernameMaxLength() {
		User user = buildUser(repeat('a', 20), "p@ssword");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordIsNull() {
		User user = buildUser("alice", null);
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordTooShort() {
		User user = buildUser("alice", "12345");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordTooLong() {
		User user = buildUser("alice", repeat('x', 41));
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtPasswordMinLength() {
		User user = buildUser("alice", repeat('x', 6));
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtPasswordMaxLength() {
		User user = buildUser("alice", repeat('x', 40));
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	private static User buildUser(String username, String password) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		return user;
	}

	private static String repeat(char c, int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
