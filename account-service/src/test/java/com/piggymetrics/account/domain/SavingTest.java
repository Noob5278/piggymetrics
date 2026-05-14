package com.piggymetrics.account.domain;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class SavingTest {

	@Test
	public void shouldGetAndSetAmount() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(1500));
		assertEquals(new BigDecimal(1500), saving.getAmount());
	}

	@Test
	public void shouldGetAndSetCurrency() {
		Saving saving = new Saving();
		saving.setCurrency(Currency.USD);
		assertEquals(Currency.USD, saving.getCurrency());
	}

	@Test
	public void shouldGetAndSetInterest() {
		Saving saving = new Saving();
		saving.setInterest(new BigDecimal("3.32"));
		assertEquals(new BigDecimal("3.32"), saving.getInterest());
	}

	@Test
	public void shouldGetAndSetDeposit() {
		Saving saving = new Saving();
		saving.setDeposit(true);
		assertTrue(saving.getDeposit());
	}

	@Test
	public void shouldGetAndSetCapitalization() {
		Saving saving = new Saving();
		saving.setCapitalization(false);
		assertFalse(saving.getCapitalization());
	}

	@Test
	public void shouldReturnNullForUnsetFields() {
		Saving saving = new Saving();
		assertNull(saving.getAmount());
		assertNull(saving.getCurrency());
		assertNull(saving.getInterest());
		assertNull(saving.getDeposit());
		assertNull(saving.getCapitalization());
	}

	@Test
	public void shouldSetAllFieldsTogether() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(2000));
		saving.setCurrency(Currency.EUR);
		saving.setInterest(new BigDecimal("5.0"));
		saving.setDeposit(true);
		saving.setCapitalization(true);

		assertEquals(new BigDecimal(2000), saving.getAmount());
		assertEquals(Currency.EUR, saving.getCurrency());
		assertEquals(new BigDecimal("5.0"), saving.getInterest());
		assertTrue(saving.getDeposit());
		assertTrue(saving.getCapitalization());
	}
}
