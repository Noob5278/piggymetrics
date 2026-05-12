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
import static org.junit.Assert.assertTrue;

public class ItemTest {

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
		Item item = validItem();

		assertEquals("Salary", item.getTitle());
		assertEquals(new BigDecimal("9100"), item.getAmount());
		assertEquals(Currency.USD, item.getCurrency());
		assertEquals(TimePeriod.MONTH, item.getPeriod());
		assertEquals("wallet", item.getIcon());
	}

	@Test
	public void shouldPassValidationWhenAllFieldsAreValid() {
		Set<ConstraintViolation<Item>> violations = validator.validate(validItem());
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleIsNull() {
		Item item = validItem();
		item.setTitle(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleIsEmpty() {
		Item item = validItem();
		item.setTitle("");

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenTitleIsTooLong() {
		Item item = validItem();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 21; i++) {
			sb.append("a");
		}
		item.setTitle(sb.toString());

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenAmountIsNull() {
		Item item = validItem();
		item.setAmount(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenCurrencyIsNull() {
		Item item = validItem();
		item.setCurrency(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenPeriodIsNull() {
		Item item = validItem();
		item.setPeriod(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenIconIsNull() {
		Item item = validItem();
		item.setIcon(null);

		Set<ConstraintViolation<Item>> violations = validator.validate(item);
		assertFalse(violations.isEmpty());
	}
}
