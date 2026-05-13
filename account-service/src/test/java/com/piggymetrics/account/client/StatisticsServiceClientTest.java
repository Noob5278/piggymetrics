package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import org.junit.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StatisticsServiceClientTest {

	@Test
	public void shouldBeAnnotatedAsFeignClientWithStatisticsServiceNameAndFallback() {
		FeignClient annotation = StatisticsServiceClient.class.getAnnotation(FeignClient.class);
		assertNotNull("StatisticsServiceClient should be annotated with @FeignClient", annotation);
		assertEquals("statistics-service", annotation.name());
		assertEquals(StatisticsServiceClientFallback.class, annotation.fallback());
	}

	@Test
	public void shouldBeAnInterface() {
		assertTrue("StatisticsServiceClient must be an interface", StatisticsServiceClient.class.isInterface());
	}

	@Test
	public void shouldDeclareUpdateStatisticsMethodWithExpectedSignature() throws NoSuchMethodException {
		Method method = StatisticsServiceClient.class.getMethod("updateStatistics", String.class, Account.class);
		assertEquals(void.class, method.getReturnType());
		assertArrayEquals(new Class<?>[]{String.class, Account.class}, method.getParameterTypes());
	}

	@Test
	public void shouldMapUpdateStatisticsToPutOnStatistics() throws NoSuchMethodException {
		Method method = StatisticsServiceClient.class.getMethod("updateStatistics", String.class, Account.class);
		RequestMapping mapping = method.getAnnotation(RequestMapping.class);

		assertNotNull("updateStatistics should be annotated with @RequestMapping", mapping);
		assertTrue(Arrays.asList(mapping.method()).contains(RequestMethod.PUT));
		assertArrayEquals(new String[]{"/statistics/{accountName}"}, mapping.value());
		assertArrayEquals(new String[]{MediaType.APPLICATION_JSON_UTF8_VALUE}, mapping.consumes());
	}

	@Test
	public void shouldAnnotateAccountNameWithPathVariable() throws NoSuchMethodException {
		Method method = StatisticsServiceClient.class.getMethod("updateStatistics", String.class, Account.class);
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		assertEquals(2, parameterAnnotations.length);
		assertTrue("first parameter should carry annotations", parameterAnnotations[0].length > 0);
	}

	@Test
	public void shouldDeclareExactlyOneMethod() {
		Method[] methods = StatisticsServiceClient.class.getDeclaredMethods();
		assertEquals(1, methods.length);
		assertEquals("updateStatistics", methods[0].getName());
	}
}
