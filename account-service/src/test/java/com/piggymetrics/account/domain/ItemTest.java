package com.piggymetrics.account.domain;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemTest {

	private static Validator validator;

	@BeforeClass
	public static void setupValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void shouldGetAndSetAllFields() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal("9100.00"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("wallet");

		assertEquals("Salary", item.getTitle());
		assertEquals(new BigDecimal("9100.00"), item.getAmount());
		assertEquals(Currency.USD, item.getCurrency());
		assertEquals(TimePeriod.MONTH, item.getPeriod());
		assertEquals("wallet", item.getIcon());
	}

	@Test
	public void shouldPassValidationForValidItem() {
		Item item = buildValidItem();
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleIsNull() {
		Item item = buildValidItem();
		item.setTitle(null);
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleIsEmpty() {
		Item item = buildValidItem();
		item.setTitle("");
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleTooLong() {
		Item item = buildValidItem();
		item.setTitle(repeat('a', 21));
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtTitleMinLength() {
		Item item = buildValidItem();
		item.setTitle("a");
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtTitleMaxLength() {
		Item item = buildValidItem();
		item.setTitle(repeat('a', 20));
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenAmountIsNull() {
		Item item = buildValidItem();
		item.setAmount(null);
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenCurrencyIsNull() {
		Item item = buildValidItem();
		item.setCurrency(null);
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPeriodIsNull() {
		Item item = buildValidItem();
		item.setPeriod(null);
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenIconIsNull() {
		Item item = buildValidItem();
		item.setIcon(null);
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWithAllNullFields() {
		Item item = new Item();
		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		// title is @NotNull + @Length(min=1), amount, currency, period, and icon are @NotNull
		assertFalse(violations.isEmpty());
	}

	private static Item buildValidItem() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal("9100"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("wallet");
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
