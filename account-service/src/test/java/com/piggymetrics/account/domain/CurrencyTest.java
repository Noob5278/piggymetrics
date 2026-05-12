package com.piggymetrics.account.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CurrencyTest {

	@Test
	public void shouldExposeAllExpectedCurrencies() {
		List<Currency> values = Arrays.asList(Currency.values());

		assertEquals(3, values.size());
		assertTrue(values.contains(Currency.USD));
		assertTrue(values.contains(Currency.EUR));
		assertTrue(values.contains(Currency.RUB));
	}

	@Test
	public void shouldReturnUsdAsDefault() {
		assertEquals(Currency.USD, Currency.getDefault());
	}

	@Test
	public void shouldResolveByName() {
		assertEquals(Currency.USD, Currency.valueOf("USD"));
		assertEquals(Currency.EUR, Currency.valueOf("EUR"));
		assertEquals(Currency.RUB, Currency.valueOf("RUB"));
		assertNotNull(Currency.valueOf("USD"));
	}
}
