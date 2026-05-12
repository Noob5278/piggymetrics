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

public class SavingTest {

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
		saving.setAmount(new BigDecimal("1500.00"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);
		return saving;
	}

	@Test
	public void shouldExposeGettersAndSetters() {
		Saving saving = validSaving();

		assertEquals(new BigDecimal("1500.00"), saving.getAmount());
		assertEquals(Currency.USD, saving.getCurrency());
		assertEquals(new BigDecimal("3.32"), saving.getInterest());
		assertTrue(saving.getDeposit());
		assertFalse(saving.getCapitalization());
	}

	@Test
	public void shouldPassValidationWhenAllFieldsAreSet() {
		Set<ConstraintViolation<Saving>> violations = validator.validate(validSaving());
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenAmountIsNull() {
		Saving saving = validSaving();
		saving.setAmount(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenCurrencyIsNull() {
		Saving saving = validSaving();
		saving.setCurrency(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenInterestIsNull() {
		Saving saving = validSaving();
		saving.setInterest(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenDepositIsNull() {
		Saving saving = validSaving();
		saving.setDeposit(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenCapitalizationIsNull() {
		Saving saving = validSaving();
		saving.setCapitalization(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationOnEmptyInstance() {
		Set<ConstraintViolation<Saving>> violations = validator.validate(new Saving());
		assertEquals(5, violations.size());
	}
}
