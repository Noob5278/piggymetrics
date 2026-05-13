package com.piggymetrics.account.domain;

import org.junit.AfterClass;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ItemTest {

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

	private Item validItem() {
		Item item = new Item();
		item.setTitle("Salary");
		item.setAmount(new BigDecimal("9100.00"));
		item.setCurrency(Currency.USD);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("wallet");
		return item;
	}

	@Test
	public void shouldGetAndSetAllFields() {
		Item item = new Item();
		BigDecimal amount = new BigDecimal("12.34");

		item.setTitle("Lunch");
		item.setAmount(amount);
		item.setCurrency(Currency.EUR);
		item.setPeriod(TimePeriod.DAY);
		item.setIcon("meal");

		assertEquals("Lunch", item.getTitle());
		assertEquals(amount, item.getAmount());
		assertEquals(Currency.EUR, item.getCurrency());
		assertEquals(TimePeriod.DAY, item.getPeriod());
		assertEquals("meal", item.getIcon());
	}

	@Test
	public void shouldDefaultToNullValues() {
		Item item = new Item();
		assertNull(item.getTitle());
		assertNull(item.getAmount());
		assertNull(item.getCurrency());
		assertNull(item.getPeriod());
		assertNull(item.getIcon());
	}

	@Test
	public void shouldPassValidationOnFullyPopulatedItem() {
		Set<ConstraintViolation<Item>> violations = validator.validate(validItem());
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenAllFieldsAreNull() {
		Set<ConstraintViolation<Item>> violations = validator.validate(new Item());
		assertFalse(violations.isEmpty());
		assertTrue(violations.size() >= 5);
	}

	@Test
	public void shouldFailValidationWhenTitleIsEmpty() {
		Item item = validItem();
		item.setTitle("");

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
	}

	@Test
	public void shouldPassValidationAtMinimumTitleLength() {
		Item item = validItem();
		item.setTitle("A");

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldPassValidationAtMaximumTitleLength() {
		Item item = validItem();
		item.setTitle(repeat("x", 20));

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleExceedsMaxLength() {
		Item item = validItem();
		item.setTitle(repeat("x", 21));

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
	}

	@Test
	public void shouldFailValidationWhenAmountIsNull() {
		Item item = validItem();
		item.setAmount(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
	}

	@Test
	public void shouldFailValidationWhenCurrencyIsNull() {
		Item item = validItem();
		item.setCurrency(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
	}

	@Test
	public void shouldFailValidationWhenPeriodIsNull() {
		Item item = validItem();
		item.setPeriod(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("period")));
	}

	@Test
	public void shouldFailValidationWhenIconIsNull() {
		Item item = validItem();
		item.setIcon(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("icon")));
	}

	private static String repeat(String s, int times) {
		StringBuilder sb = new StringBuilder(s.length() * times);
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		return sb.toString();
	}
}
