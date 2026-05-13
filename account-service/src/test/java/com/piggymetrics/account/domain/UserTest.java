package com.piggymetrics.account.domain;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeClass
	public static void buildValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterClass
	public static void closeValidator() {
		factory.close();
	}

	private User user(String username, String password) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		return user;
	}

	@Test
	public void shouldGetAndSetUsernameAndPassword() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("password");

		assertEquals("alice", user.getUsername());
		assertEquals("password", user.getPassword());
	}

	@Test
	public void shouldDefaultToNullValues() {
		User user = new User();
		assertNull(user.getUsername());
		assertNull(user.getPassword());
	}

	@Test
	public void shouldPassValidationWithLegalUsernameAndPassword() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("alice", "secret123"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameIsNull() {
		Set<ConstraintViolation<User>> violations = validator.validate(user(null, "secret123"));
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
	}

	@Test
	public void shouldFailValidationWhenPasswordIsNull() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("alice", null));
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
	}

	@Test
	public void shouldFailValidationWhenUsernameIsTooShort() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("ab", "secret123"));
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
	}

	@Test
	public void shouldPassValidationAtMinimumUsernameLength() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("abc", "secret123"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameIsTooLong() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("a".concat(repeat("x", 20)), "secret123"));
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
	}

	@Test
	public void shouldPassValidationAtMaximumUsernameLength() {
		Set<ConstraintViolation<User>> violations = validator.validate(user(repeat("x", 20), "secret123"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordIsTooShort() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("alice", "short"));
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
	}

	@Test
	public void shouldPassValidationAtMinimumPasswordLength() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("alice", repeat("x", 6)));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordIsTooLong() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("alice", repeat("x", 41)));
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
	}

	@Test
	public void shouldPassValidationAtMaximumPasswordLength() {
		Set<ConstraintViolation<User>> violations = validator.validate(user("alice", repeat("x", 40)));
		assertTrue(violations.isEmpty());
	}

	private static String repeat(String s, int times) {
		StringBuilder sb = new StringBuilder(s.length() * times);
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		return sb.toString();
	}
}
