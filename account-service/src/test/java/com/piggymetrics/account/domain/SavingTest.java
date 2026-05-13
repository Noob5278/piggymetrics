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

public class SavingTest {

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

	@Test
	public void shouldGetAndSetAllFields() {
		Saving saving = new Saving();
		BigDecimal amount = new BigDecimal("9999.99");
		BigDecimal interest = new BigDecimal("0.05");

		saving.setAmount(amount);
		saving.setCurrency(Currency.EUR);
		saving.setInterest(interest);
		saving.setDeposit(true);
		saving.setCapitalization(true);

		assertEquals(amount, saving.getAmount());
		assertEquals(Currency.EUR, saving.getCurrency());
		assertEquals(interest, saving.getInterest());
		assertTrue(saving.getDeposit());
		assertTrue(saving.getCapitalization());
	}

	@Test
	public void shouldDefaultToNullValues() {
		Saving saving = new Saving();
		assertNull(saving.getAmount());
		assertNull(saving.getCurrency());
		assertNull(saving.getInterest());
		assertNull(saving.getDeposit());
		assertNull(saving.getCapitalization());
	}

	@Test
	public void shouldSupportBothBooleanValuesForDepositAndCapitalization() {
		Saving saving = validSaving();
		saving.setDeposit(false);
		saving.setCapitalization(false);
		assertFalse(saving.getDeposit());
		assertFalse(saving.getCapitalization());

		saving.setDeposit(true);
		saving.setCapitalization(true);
		assertTrue(saving.getDeposit());
		assertTrue(saving.getCapitalization());
	}

	@Test
	public void shouldPassValidationOnFullyPopulatedSaving() {
		Set<ConstraintViolation<Saving>> violations = validator.validate(validSaving());
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenAllFieldsAreNull() {
		Set<ConstraintViolation<Saving>> violations = validator.validate(new Saving());
		assertFalse(violations.isEmpty());
		assertEquals(5, violations.size());
	}

	@Test
	public void shouldFailValidationWhenAmountIsNull() {
		Saving saving = validSaving();
		saving.setAmount(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
	}

	@Test
	public void shouldFailValidationWhenCurrencyIsNull() {
		Saving saving = validSaving();
		saving.setCurrency(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
	}

	@Test
	public void shouldFailValidationWhenInterestIsNull() {
		Saving saving = validSaving();
		saving.setInterest(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("interest")));
	}

	@Test
	public void shouldFailValidationWhenDepositIsNull() {
		Saving saving = validSaving();
		saving.setDeposit(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("deposit")));
	}

	@Test
	public void shouldFailValidationWhenCapitalizationIsNull() {
		Saving saving = validSaving();
		saving.setCapitalization(null);

		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("capitalization")));
	}
}
