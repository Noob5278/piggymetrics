package com.piggymetrics.account.domain;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

public class AccountTest {

	@Test
	public void shouldGetAndSetName() {
		Account account = new Account();
		account.setName("test");
		assertEquals("test", account.getName());
	}

	@Test
	public void shouldGetAndSetLastSeen() {
		Account account = new Account();
		Date now = new Date();
		account.setLastSeen(now);
		assertEquals(now, account.getLastSeen());
	}

	@Test
	public void shouldGetAndSetNote() {
		Account account = new Account();
		account.setNote("test note");
		assertEquals("test note", account.getNote());
	}

	@Test
	public void shouldGetAndSetSaving() {
		Account account = new Account();
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(1000));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.5"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		account.setSaving(saving);

		assertEquals(saving, account.getSaving());
		assertEquals(new BigDecimal(1000), account.getSaving().getAmount());
		assertEquals(Currency.USD, account.getSaving().getCurrency());
	}

	@Test
	public void shouldGetAndSetIncomes() {
		Account account = new Account();

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal(9100));
		salary.setCurrency(Currency.USD);
		salary.setPeriod(TimePeriod.MONTH);
		salary.setIcon("wallet");

		account.setIncomes(Arrays.asList(salary));

		assertEquals(1, account.getIncomes().size());
		assertEquals("Salary", account.getIncomes().get(0).getTitle());
	}

	@Test
	public void shouldGetAndSetExpenses() {
		Account account = new Account();

		Item grocery = new Item();
		grocery.setTitle("Grocery");
		grocery.setAmount(new BigDecimal(10));
		grocery.setCurrency(Currency.USD);
		grocery.setPeriod(TimePeriod.DAY);
		grocery.setIcon("meal");

		account.setExpenses(Arrays.asList(grocery));

		assertEquals(1, account.getExpenses().size());
		assertEquals("Grocery", account.getExpenses().get(0).getTitle());
	}

	@Test
	public void shouldReturnNullForUnsetFields() {
		Account account = new Account();
		assertNull(account.getName());
		assertNull(account.getLastSeen());
		assertNull(account.getNote());
		assertNull(account.getSaving());
		assertNull(account.getIncomes());
		assertNull(account.getExpenses());
	}
}
