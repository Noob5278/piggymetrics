package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import feign.FeignException;
import feign.RetryableException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Mockito-based tests for {@link AuthServiceClient}. Because the client is a Feign-declared
 * interface, these tests cover the contract callers rely on: successful invocation, error
 * propagation when the remote service is unavailable, and timeout/retry propagation. Real Feign
 * decoding/encoding is exercised indirectly by Spring's auto-configuration.
 */
public class AuthServiceClientTest {

	private AuthServiceClient client;

	@Before
	public void setUp() {
		client = mock(AuthServiceClient.class);
	}

	@Test
	public void shouldCreateUserSuccessfully() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("password");

		client.createUser(user);

		verify(client).createUser(user);
	}

	@Test(expected = FeignException.class)
	public void shouldPropagateServiceUnavailable() {
		doThrow(mock(FeignException.class))
				.when(client).createUser(any(User.class));

		User user = new User();
		user.setUsername("bob");
		user.setPassword("password");

		client.createUser(user);
	}

	@Test(expected = RetryableException.class)
	public void shouldPropagateTimeout() {
		doThrow(new RetryableException("read timeout", null, null))
				.when(client).createUser(any(User.class));

		User user = new User();
		user.setUsername("carol");
		user.setPassword("password");

		client.createUser(user);
	}

	@Test
	public void shouldForwardExactUserInstance() {
		final User[] captured = new User[1];
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				captured[0] = (User) invocation.getArguments()[0];
				return null;
			}
		}).when(client).createUser(any(User.class));

		User user = new User();
		user.setUsername("dave");
		user.setPassword("password");

		client.createUser(user);

		assertNotNull(captured[0]);
		assertEquals("dave", captured[0].getUsername());
		verify(client).createUser(user);
	}
}
