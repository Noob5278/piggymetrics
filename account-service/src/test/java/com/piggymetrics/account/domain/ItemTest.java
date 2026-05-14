package com.piggymetrics.account.domain;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ItemTest {

	@Test
	public void shouldGetAndSetTitle() {
		Item item = new Item();
		item.setTitle("Grocery");
		assertEquals("Grocery", item.getTitle());
	}

	@Test
	public void shouldGetAndSetAmount() {
		Item item = new Item();
		item.setAmount(new BigDecimal(10));
		assertEquals(new BigDecimal(10), item.getAmount());
	}

	@Test
	public void shouldGetAndSetCurrency() {
		Item item = new Item();
		item.setCurrency(Currency.USD);
		assertEquals(Currency.USD, item.getCurrency());
	}

	@Test
	public void shouldGetAndSetPeriod() {
		Item item = new Item();
		item.setPeriod(TimePeriod.MONTH);
		assertEquals(TimePeriod.MONTH, item.getPeriod());
	}

	@Test
	public void shouldGetAndSetIcon() {
		Item item = new Item();
		item.setIcon("meal");
		assertEquals("meal", item.getIcon());
	}

	@Test
	public void shouldReturnNullForUnsetFields() {
		Item item = new Item();
		assertNull(item.getTitle());
		assertNull(item.getAmount());
		assertNull(item.getCurrency());
		assertNull(item.getPeriod());
		assertNull(item.getIcon());
	}

	@Test
	public void shouldSetAllFieldsTogether() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal(9100));
		item.setCurrency(Currency.EUR);
		item.setPeriod(TimePeriod.YEAR);
		item.setIcon("wallet");

		assertEquals("Salary", item.getTitle());
		assertEquals(new BigDecimal(9100), item.getAmount());
		assertEquals(Currency.EUR, item.getCurrency());
		assertEquals(TimePeriod.YEAR, item.getPeriod());
		assertEquals("wallet", item.getIcon());
	}
}
