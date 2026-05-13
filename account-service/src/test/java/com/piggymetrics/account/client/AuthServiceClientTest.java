package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AuthServiceClientTest {

	private AuthServiceClient authServiceClient;

	@Before
	public void setup() {
		authServiceClient = mock(AuthServiceClient.class);
	}

	@Test
	public void shouldInvokeCreateUserWithGivenUser() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("p@ssword");

		authServiceClient.createUser(user);

		verify(authServiceClient, times(1)).createUser(user);
		verifyNoMoreInteractions(authServiceClient);
	}

	@Test
	public void shouldNotInvokeCreateUserWhenNotCalled() {
		verifyZeroInteractions(authServiceClient);
	}

	@Test(expected = RuntimeException.class)
	public void shouldPropagateExceptionsRaisedByFeignClient() {
		User user = new User();
		user.setUsername("bob");
		user.setPassword("secret123");

		doThrow(new RuntimeException("auth service unavailable"))
				.when(authServiceClient).createUser(user);

		authServiceClient.createUser(user);
	}
}
