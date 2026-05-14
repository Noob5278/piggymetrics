package com.piggymetrics.account.domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

	@Test
	public void shouldGetAndSetUsername() {
		User user = new User();
		user.setUsername("testuser");
		assertEquals("testuser", user.getUsername());
	}

	@Test
	public void shouldGetAndSetPassword() {
		User user = new User();
		user.setPassword("password");
		assertEquals("password", user.getPassword());
	}

	@Test
	public void shouldReturnNullForUnsetFields() {
		User user = new User();
		assertNull(user.getUsername());
		assertNull(user.getPassword());
	}

	@Test
	public void shouldAllowSettingMultipleFields() {
		User user = new User();
		user.setUsername("testuser");
		user.setPassword("securepassword");

		assertEquals("testuser", user.getUsername());
		assertEquals("securepassword", user.getPassword());
	}
}
