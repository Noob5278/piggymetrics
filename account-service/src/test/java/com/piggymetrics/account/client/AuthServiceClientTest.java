package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import org.junit.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AuthServiceClientTest {

	@Test
	public void shouldBeAnnotatedAsFeignClientWithAuthServiceName() {
		FeignClient annotation = AuthServiceClient.class.getAnnotation(FeignClient.class);
		assertNotNull("AuthServiceClient should be annotated with @FeignClient", annotation);
		assertEquals("auth-service", annotation.name());
	}

	@Test
	public void shouldBeAnInterface() {
		assertTrue("AuthServiceClient must be an interface", AuthServiceClient.class.isInterface());
	}

	@Test
	public void shouldDeclareCreateUserMethodWithExpectedSignature() throws NoSuchMethodException {
		Method method = AuthServiceClient.class.getMethod("createUser", User.class);
		assertEquals(void.class, method.getReturnType());
		assertArrayEquals(new Class<?>[]{User.class}, method.getParameterTypes());
	}

	@Test
	public void shouldMapCreateUserToPostOnUaaUsers() throws NoSuchMethodException {
		Method method = AuthServiceClient.class.getMethod("createUser", User.class);
		RequestMapping mapping = method.getAnnotation(RequestMapping.class);

		assertNotNull("createUser should be annotated with @RequestMapping", mapping);
		assertTrue(Arrays.asList(mapping.method()).contains(RequestMethod.POST));
		assertArrayEquals(new String[]{"/uaa/users"}, mapping.value());
		assertArrayEquals(new String[]{MediaType.APPLICATION_JSON_UTF8_VALUE}, mapping.consumes());
	}

	@Test
	public void shouldDeclareExactlyOneMethod() {
		Method[] methods = AuthServiceClient.class.getDeclaredMethods();
		assertEquals(1, methods.length);
		assertEquals("createUser", methods[0].getName());
	}
}
