package com.piggymetrics.account.domain;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AccountTest {

	private static Validator validator;

	@BeforeClass
	public static void setupValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void shouldGetAndSetAllFields() {
		Date now = new Date();
		Saving saving = buildValidSaving();
		List<Item> incomes = Collections.singletonList(buildValidItem("Salary", "wallet"));
		List<Item> expenses = Arrays.asList(buildValidItem("Grocery", "meal"), buildValidItem("Rent", "home"));

		Account account = new Account();
		account.setName("test");
		account.setLastSeen(now);
		account.setIncomes(incomes);
		account.setExpenses(expenses);
		account.setSaving(saving);
		account.setNote("a note");

		assertEquals("test", account.getName());
		assertSame(now, account.getLastSeen());
		assertSame(incomes, account.getIncomes());
		assertSame(expenses, account.getExpenses());
		assertSame(saving, account.getSaving());
		assertEquals("a note", account.getNote());
	}

	@Test
	public void shouldAllowNullCollectionsAndNote() {
		Account account = new Account();
		account.setSaving(buildValidSaving());

		assertNull(account.getIncomes());
		assertNull(account.getExpenses());
		assertNull(account.getNote());

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationForFullyPopulatedAccount() {
		Account account = new Account();
		account.setName("test");
		account.setLastSeen(new Date());
		account.setSaving(buildValidSaving());
		account.setIncomes(Collections.singletonList(buildValidItem("Salary", "wallet")));
		account.setExpenses(Collections.singletonList(buildValidItem("Grocery", "meal")));
		account.setNote("hello");

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenSavingIsNull() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(null);

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenNoteExceedsMaxLength() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(buildValidSaving());
		account.setNote(repeat('x', 20_001));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtNoteMaxLength() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(buildValidSaving());
		account.setNote(repeat('x', 20_000));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldCascadeValidationToSaving() {
		Account account = new Account();
		account.setName("test");
		Saving saving = buildValidSaving();
		saving.setAmount(null);
		account.setSaving(saving);

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldCascadeValidationToIncomes() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(buildValidSaving());

		Item invalid = buildValidItem("Salary", "wallet");
		invalid.setTitle(null);
		account.setIncomes(Collections.singletonList(invalid));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldCascadeValidationToExpenses() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(buildValidSaving());

		Item invalid = buildValidItem("Grocery", "meal");
		invalid.setAmount(null);
		account.setExpenses(Collections.singletonList(invalid));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	private static Saving buildValidSaving() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1500"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);
		return saving;
	}

	private static Item buildValidItem(String title, String icon) {
		Item item = new Item();
		item.setTitle(title);
		item.setAmount(new BigDecimal("100"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon(icon);
		return item;
	}

	private static String repeat(char c, int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
