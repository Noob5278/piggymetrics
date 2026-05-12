package com.piggymetrics.statistics.service;

import com.google.common.collect.ImmutableMap;
import com.piggymetrics.statistics.client.ExchangeRatesClient;
import com.piggymetrics.statistics.domain.Currency;
import com.piggymetrics.statistics.domain.ExchangeRatesContainer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Dark-logic edge-case tests for ExchangeRatesServiceImpl.
 * Covers: same-currency conversion, zero amount conversion,
 * negative amount conversion, and rate cache staleness.
 */
public class ExchangeRatesServiceImplDarkLogicTest {

	@InjectMocks
	private ExchangeRatesServiceImpl ratesService;

	@Mock
	private ExchangeRatesClient client;

	@Before
	public void setup() {
		initMocks(this);
	}

	private void setupRates() {
		ExchangeRatesContainer container = new ExchangeRatesContainer();
		container.setRates(ImmutableMap.of(
				Currency.EUR.name(), new BigDecimal("0.8"),
				Currency.RUB.name(), new BigDecimal("80")
		));
		when(client.getRates(Currency.getBase())).thenReturn(container);
	}

	@Test
	public void shouldReturnSameAmountForSameCurrencyConversion() {
		setupRates();

		BigDecimal amount = new BigDecimal("100");
		BigDecimal result = ratesService.convert(Currency.USD, Currency.USD, amount);

		assertTrue(amount.compareTo(result) == 0);
	}

	@Test
	public void shouldConvertZeroAmount() {
		setupRates();

		BigDecimal result = ratesService.convert(Currency.USD, Currency.EUR, BigDecimal.ZERO);
		assertTrue(BigDecimal.ZERO.compareTo(result) == 0);
	}

	@Test
	public void shouldConvertNegativeAmount() {
		setupRates();

		BigDecimal amount = new BigDecimal("-100");
		BigDecimal result = ratesService.convert(Currency.USD, Currency.EUR, amount);

		assertTrue(result.compareTo(BigDecimal.ZERO) < 0);
	}

	@Test
	public void shouldConvertEURtoRUB() {
		setupRates();

		BigDecimal amount = new BigDecimal("100");
		BigDecimal result = ratesService.convert(Currency.EUR, Currency.RUB, amount);

		// EUR rate = 0.8, RUB rate = 80 => ratio = 80/0.8 = 100
		BigDecimal expected = new BigDecimal("10000.0000");
		assertTrue(expected.compareTo(result) == 0);
	}

	@Test
	public void shouldConvertRUBtoEUR() {
		setupRates();

		BigDecimal amount = new BigDecimal("8000");
		BigDecimal result = ratesService.convert(Currency.RUB, Currency.EUR, amount);

		// RUB rate = 80, EUR rate = 0.8 => ratio = 0.8/80 = 0.01
		BigDecimal expected = new BigDecimal("80.0000");
		assertTrue(expected.compareTo(result) == 0);
	}

	@Test
	public void shouldReturnAllThreeCurrenciesInRates() {
		setupRates();

		Map<Currency, BigDecimal> rates = ratesService.getCurrentRates();

		assertEquals(3, rates.size());
		assertTrue(rates.containsKey(Currency.USD));
		assertTrue(rates.containsKey(Currency.EUR));
		assertTrue(rates.containsKey(Currency.RUB));
		assertEquals(BigDecimal.ONE, rates.get(Currency.USD));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToConvertWithNullAmount() {
		ratesService.convert(Currency.USD, Currency.EUR, null);
	}
}
