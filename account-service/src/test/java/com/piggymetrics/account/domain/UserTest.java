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
import static org.junit.Assert.assertTrue;

public class UserTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeClass
	public static void setupValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterClass
	public static void tearDownValidator() {
		factory.close();
	}

	@Test
	public void shouldExposeGettersAndSetters() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("secret123");

		assertEquals("alice", user.getUsername());
		assertEquals("secret123", user.getPassword());
	}

	@Test
	public void shouldPassValidationWhenAllFieldsAreValid() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("secret123");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameIsNull() {
		User user = new User();
		user.setPassword("secret123");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameIsTooShort() {
		User user = new User();
		user.setUsername("ab");
		user.setPassword("secret123");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenUsernameIsTooLong() {
		User user = new User();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 21; i++) {
			sb.append("a");
		}
		user.setUsername(sb.toString());
		user.setPassword("secret123");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordIsNull() {
		User user = new User();
		user.setUsername("alice");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPasswordIsTooShort() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("12345");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationOnUsernameBoundaries() {
		User minUser = new User();
		minUser.setUsername("abc");
		minUser.setPassword("secret123");
		assertTrue(validator.validate(minUser).isEmpty());

		User maxUser = new User();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			sb.append("a");
		}
		maxUser.setUsername(sb.toString());
		maxUser.setPassword("secret123");
		assertTrue(validator.validate(maxUser).isEmpty());
	}
}
