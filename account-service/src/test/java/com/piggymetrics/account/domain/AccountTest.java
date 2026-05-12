package com.piggymetrics.account.domain;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AccountTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeClass
	public static void setupValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterClass
	public static void tearDownValidator() {
		factory.close();
	}

	private Saving validSaving() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1500"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);
		return saving;
	}

	private Item validItem() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal("9100"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("wallet");
		return item;
	}

	@Test
	public void shouldExposeGettersAndSetters() {
		Date now = new Date();
		Saving saving = validSaving();
		List<Item> incomes = ImmutableList.of(validItem());
		List<Item> expenses = Collections.<Item>emptyList();

		Account account = new Account();
		account.setName("alice");
		account.setLastSeen(now);
		account.setSaving(saving);
		account.setIncomes(incomes);
		account.setExpenses(expenses);
		account.setNote("hello");

		assertEquals("alice", account.getName());
		assertEquals(now, account.getLastSeen());
		assertEquals(saving, account.getSaving());
		assertEquals(incomes, account.getIncomes());
		assertEquals(expenses, account.getExpenses());
		assertEquals("hello", account.getNote());
	}

	@Test
	public void shouldDefaultCollectionsAndOptionalFieldsToNull() {
		Account account = new Account();
		assertNull(account.getName());
		assertNull(account.getLastSeen());
		assertNull(account.getSaving());
		assertNull(account.getIncomes());
		assertNull(account.getExpenses());
		assertNull(account.getNote());
	}

	@Test
	public void shouldPassValidationWhenSavingIsPresent() {
		Account account = new Account();
		account.setName("alice");
		account.setSaving(validSaving());

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenSavingIsNull() {
		Account account = new Account();
		account.setName("alice");

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenSavingIsInvalid() {
		Account account = new Account();
		account.setName("alice");
		Saving incomplete = new Saving();
		account.setSaving(incomplete);

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenNestedItemIsInvalid() {
		Account account = new Account();
		account.setName("alice");
		account.setSaving(validSaving());
		account.setIncomes(ImmutableList.of(new Item()));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenNoteIsTooLong() {
		Account account = new Account();
		account.setName("alice");
		account.setSaving(validSaving());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20_001; i++) {
			sb.append("a");
		}
		account.setNote(sb.toString());

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldAllowEmptyNote() {
		Account account = new Account();
		account.setName("alice");
		account.setSaving(validSaving());
		account.setNote("");

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
		assertNotNull(account);
	}
}
