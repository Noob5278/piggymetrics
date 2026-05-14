package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AuthServiceClientTest {

	@Mock
	private AuthServiceClient authClient;

	@Before
	public void setup() {
		initMocks(this);
	}

	@Test
	public void shouldCreateUser() {
		User user = new User();
		user.setUsername("test");
		user.setPassword("password");

		authClient.createUser(user);

		verify(authClient, times(1)).createUser(user);
	}

	@Test
	public void shouldNotThrowWhenCreateUserCalledMultipleTimes() {
		User first = new User();
		first.setUsername("first");
		first.setPassword("password1");

		User second = new User();
		second.setUsername("second");
		second.setPassword("password2");

		authClient.createUser(first);
		authClient.createUser(second);

		verify(authClient, times(1)).createUser(first);
		verify(authClient, times(1)).createUser(second);
	}

	@Test(expected = RuntimeException.class)
	public void shouldThrowOnFailure() {
		User user = new User();
		user.setUsername("test");
		user.setPassword("password");

		doThrow(new RuntimeException("Auth service unavailable"))
				.when(authClient).createUser(user);

		authClient.createUser(user);
	}
}
