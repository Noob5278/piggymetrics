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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterClass
	public static void tearDown() {
		if (factory != null) {
			factory.close();
		}
	}

	@Test
	public void shouldValidateAccountWithValidSaving() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(validSaving());

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertTrue("Account with valid saving should pass validation", violations.isEmpty());
	}

	@Test
	public void shouldFailAccountValidationWhenSavingIsNull() {
		Account account = new Account();
		account.setName("test");
		account.setSaving(null);

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse("Account without saving should fail validation", violations.isEmpty());
		assertTrue(containsProperty(violations, "saving"));
	}

	@Test
	public void shouldFailAccountValidationWhenNoteIsTooLong() {
		char[] chars = new char[20_001];
		java.util.Arrays.fill(chars, 'a');

		Account account = new Account();
		account.setSaving(validSaving());
		account.setNote(new String(chars));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse("Note longer than 20000 should fail validation", violations.isEmpty());
		assertTrue(containsProperty(violations, "note"));
	}

	@Test
	public void shouldValidateUser() {
		User user = new User();
		user.setUsername("validuser");
		user.setPassword("strongpassword");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue("Valid user should pass validation", violations.isEmpty());
	}

	@Test
	public void shouldFailUserValidationWhenUsernameIsTooShort() {
		User user = new User();
		user.setUsername("a");
		user.setPassword("strongpassword");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "username"));
	}

	@Test
	public void shouldFailUserValidationWhenUsernameIsTooLong() {
		User user = new User();
		user.setUsername("aaaaaaaaaaaaaaaaaaaaaa");
		user.setPassword("strongpassword");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "username"));
	}

	@Test
	public void shouldFailUserValidationWhenUsernameIsNull() {
		User user = new User();
		user.setUsername(null);
		user.setPassword("strongpassword");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "username"));
	}

	@Test
	public void shouldFailUserValidationWhenPasswordIsTooShort() {
		User user = new User();
		user.setUsername("validuser");
		user.setPassword("12345");

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "password"));
	}

	@Test
	public void shouldFailUserValidationWhenPasswordIsNull() {
		User user = new User();
		user.setUsername("validuser");
		user.setPassword(null);

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "password"));
	}

	@Test
	public void shouldValidateSaving() {
		Saving saving = validSaving();

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailSavingValidationWhenAmountIsNull() {
		Saving saving = validSaving();
		saving.setAmount(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertEquals(1, violations.size());
		assertTrue(containsProperty(violations, "amount"));
	}

	@Test
	public void shouldFailSavingValidationWhenCurrencyIsNull() {
		Saving saving = validSaving();
		saving.setCurrency(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertEquals(1, violations.size());
		assertTrue(containsProperty(violations, "currency"));
	}

	@Test
	public void shouldFailSavingValidationWhenInterestIsNull() {
		Saving saving = validSaving();
		saving.setInterest(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertEquals(1, violations.size());
		assertTrue(containsProperty(violations, "interest"));
	}

	@Test
	public void shouldFailSavingValidationWhenDepositIsNull() {
		Saving saving = validSaving();
		saving.setDeposit(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertEquals(1, violations.size());
		assertTrue(containsProperty(violations, "deposit"));
	}

	@Test
	public void shouldFailSavingValidationWhenCapitalizationIsNull() {
		Saving saving = validSaving();
		saving.setCapitalization(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertEquals(1, violations.size());
		assertTrue(containsProperty(violations, "capitalization"));
	}

	@Test
	public void shouldValidateItem() {
		Item item = validItem();

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailItemValidationWhenTitleIsBlank() {
		Item item = validItem();
		item.setTitle("");

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "title"));
	}

	@Test
	public void shouldFailItemValidationWhenTitleIsTooLong() {
		Item item = validItem();
		item.setTitle("aaaaaaaaaaaaaaaaaaaaaaaaa");

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "title"));
	}

	@Test
	public void shouldFailItemValidationWhenAmountIsNull() {
		Item item = validItem();
		item.setAmount(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "amount"));
	}

	@Test
	public void shouldFailItemValidationWhenCurrencyIsNull() {
		Item item = validItem();
		item.setCurrency(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "currency"));
	}

	@Test
	public void shouldFailItemValidationWhenPeriodIsNull() {
		Item item = validItem();
		item.setPeriod(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "period"));
	}

	@Test
	public void shouldFailItemValidationWhenIconIsNull() {
		Item item = validItem();
		item.setIcon(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(containsProperty(violations, "icon"));
	}

	@Test
	public void shouldCascadeValidationFromAccountToSaving() {
		Account account = new Account();
		Saving invalid = new Saving(); // all required fields null
		account.setSaving(invalid);

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse("Cascaded saving violations should surface on account", violations.isEmpty());
	}

	@Test
	public void shouldCascadeValidationFromAccountToItems() {
		Account account = new Account();
		account.setSaving(validSaving());
		account.setIncomes(Collections.singletonList(new Item()));

		Set<ConstraintViolation<Account>> violations = validator.validate(account);
		assertFalse(violations.isEmpty());
	}

	private static Saving validSaving() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("100"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.5"));
		saving.setDeposit(false);
		saving.setCapitalization(false);
		return saving;
	}

	private static Item validItem() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal("1000"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("wallet");
		return item;
	}

	private static <T> boolean containsProperty(Set<ConstraintViolation<T>> violations, String property) {
		for (ConstraintViolation<T> v : violations) {
			if (v.getPropertyPath().toString().contains(property)) {
				return true;
			}
		}
		return false;
	}
}
