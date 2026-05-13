package com.piggymetrics.account.domain;

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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AccountTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeClass
	public static void buildValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterClass
	public static void closeValidator() {
		factory.close();
	}

	private Saving validSaving() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1500.00"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);
		return saving;
	}

	private Item validItem() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal("100.00"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("wallet");
		return item;
	}

	@Test
	public void shouldGetAndSetAllFields() {
		Account account = new Account();
		Date lastSeen = new Date();
		Saving saving = validSaving();
		Item income = validItem();
		Item expense = validItem();

		account.setName("john");
		account.setLastSeen(lastSeen);
		account.setSaving(saving);
		account.setIncomes(Collections.singletonList(income));
		account.setExpenses(Collections.singletonList(expense));
		account.setNote("a note");

		assertEquals("john", account.getName());
		assertSame(lastSeen, account.getLastSeen());
		assertSame(saving, account.getSaving());
		assertEquals(1, account.getIncomes().size());
		assertSame(income, account.getIncomes().get(0));
		assertEquals(1, account.getExpenses().size());
		assertSame(expense, account.getExpenses().get(0));
		assertEquals("a note", account.getNote());
	}

	@Test
	public void shouldAllowNullCollectionsAndNullNote() {
		Account account = new Account();
		account.setSaving(validSaving());

		assertNull(account.getIncomes());
		assertNull(account.getExpenses());
		assertNull(account.getNote());
		assertNull(account.getLastSeen());
		assertNull(account.getName());

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue("Account with valid saving and null note should validate", violations.isEmpty());
	}

	@Test
	public void shouldPassValidationOnFullyPopulatedAccount() {
		Account account = new Account();
		account.setName("john");
		account.setLastSeen(new Date());
		account.setSaving(validSaving());
		account.setIncomes(Collections.singletonList(validItem()));
		account.setExpenses(Collections.singletonList(validItem()));
		account.setNote("note");

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenSavingIsNull() {
		Account account = new Account();
		account.setSaving(null);

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("saving")));
	}

	@Test
	public void shouldAllowEmptyNote() {
		Account account = new Account();
		account.setSaving(validSaving());
		account.setNote("");

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldAllowNoteOfMaximumLength() {
		Account account = new Account();
		account.setSaving(validSaving());
		char[] buffer = new char[20_000];
		java.util.Arrays.fill(buffer, 'x');
		account.setNote(new String(buffer));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenNoteExceedsMaxLength() {
		Account account = new Account();
		account.setSaving(validSaving());
		char[] buffer = new char[20_001];
		java.util.Arrays.fill(buffer, 'x');
		account.setNote(new String(buffer));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("note")));
	}

	@Test
	public void shouldCascadeValidationToNestedItems() {
		Item invalidIncome = new Item();
		Account account = new Account();
		account.setSaving(validSaving());
		account.setIncomes(Collections.singletonList(invalidIncome));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
		assertNotNull(violations.iterator().next().getPropertyPath().toString());
	}
}
