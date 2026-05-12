package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import org.junit.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuthServiceClientTest {

	@Test
	public void shouldBeAnnotatedWithFeignClientForAuthService() {
		FeignClient annotation = AuthServiceClient.class.getAnnotation(FeignClient.class);

		assertNotNull("AuthServiceClient must be annotated with @FeignClient", annotation);
		assertEquals("auth-service", annotation.name());
	}

	@Test
	public void shouldDeclareCreateUserPostMapping() throws NoSuchMethodException {
		Method createUser = AuthServiceClient.class.getMethod("createUser", User.class);
		RequestMapping mapping = createUser.getAnnotation(RequestMapping.class);

		assertNotNull("createUser must be annotated with @RequestMapping", mapping);
		assertArrayEquals(new RequestMethod[] { RequestMethod.POST }, mapping.method());
		assertArrayEquals(new String[] { "/uaa/users" }, mapping.value());
		assertArrayEquals(new String[] { MediaType.APPLICATION_JSON_UTF8_VALUE }, mapping.consumes());
	}

	@Test
	public void shouldReturnVoidFromCreateUser() throws NoSuchMethodException {
		Method createUser = AuthServiceClient.class.getMethod("createUser", User.class);
		assertEquals(void.class, createUser.getReturnType());
	}

	@Test
	public void shouldInteractWithCreateUserOnMockedClient() {
		AuthServiceClient client = mock(AuthServiceClient.class);

		User user = new User();
		user.setUsername("alice");
		user.setPassword("secret123");

		client.createUser(user);

		verify(client, times(1)).createUser(user);
	}

	@Test
	public void shouldOnlyExposeCreateUserMethod() {
		Method[] declaredMethods = AuthServiceClient.class.getDeclaredMethods();
		assertEquals(1, declaredMethods.length);
		assertEquals("createUser", declaredMethods[0].getName());
		assertTrue(AuthServiceClient.class.isInterface());
	}
}
