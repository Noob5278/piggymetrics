package com.piggymetrics.account.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CurrencyTest {

	@Test
	public void shouldExposeThreeEnumConstants() {
		Currency[] values = Currency.values();
		assertEquals(3, values.length);

		Set<Currency> uniqueValues = new HashSet<>(Arrays.asList(values));
		assertTrue(uniqueValues.contains(Currency.USD));
		assertTrue(uniqueValues.contains(Currency.EUR));
		assertTrue(uniqueValues.contains(Currency.RUB));
	}

	@Test
	public void shouldResolveByValueOf() {
		assertSame(Currency.USD, Currency.valueOf("USD"));
		assertSame(Currency.EUR, Currency.valueOf("EUR"));
		assertSame(Currency.RUB, Currency.valueOf("RUB"));
	}

	@Test
	public void shouldThrowForUnknownValue() {
		try {
			Currency.valueOf("GBP");
			fail("Expected IllegalArgumentException for unknown currency");
		} catch (IllegalArgumentException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void getDefaultShouldReturnUsd() {
		assertSame(Currency.USD, Currency.getDefault());
	}
}
