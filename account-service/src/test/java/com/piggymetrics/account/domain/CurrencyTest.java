package com.piggymetrics.account.domain;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CurrencyTest {

	@Test
	public void shouldContainExpectedValues() {
		Currency[] expected = new Currency[]{Currency.USD, Currency.EUR, Currency.RUB};
		assertArrayEquals(expected, Currency.values());
	}

	@Test
	public void shouldResolveEachValueByName() {
		assertEquals(Currency.USD, Currency.valueOf("USD"));
		assertEquals(Currency.EUR, Currency.valueOf("EUR"));
		assertEquals(Currency.RUB, Currency.valueOf("RUB"));
	}

	@Test
	public void shouldReturnUsdAsDefaultCurrency() {
		Currency defaultCurrency = Currency.getDefault();
		assertNotNull(defaultCurrency);
		assertEquals(Currency.USD, defaultCurrency);
	}
}
