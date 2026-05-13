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

public class SavingTest {

	private static Validator validator;

	@BeforeClass
	public static void setupValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void shouldGetAndSetAllFields() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1500.00"));
		saving.setCurrency(Currency.EUR);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		assertEquals(new BigDecimal("1500.00"), saving.getAmount());
		assertEquals(Currency.EUR, saving.getCurrency());
		assertEquals(new BigDecimal("3.32"), saving.getInterest());
		assertEquals(Boolean.TRUE, saving.getDeposit());
		assertEquals(Boolean.FALSE, saving.getCapitalization());
	}

	@Test
	public void shouldSupportBigDecimalArithmeticOnAmount() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1000.50"));

		BigDecimal increased = saving.getAmount().add(new BigDecimal("500.50"));
		assertEquals(0, increased.compareTo(new BigDecimal("1501.00")));

		BigDecimal scaled = saving.getAmount().multiply(new BigDecimal(2));
		assertEquals(0, scaled.compareTo(new BigDecimal("2001.00")));
	}

	@Test
	public void shouldPassValidationForValidSaving() {
		Saving saving = buildValidSaving();
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenAmountIsNull() {
		Saving saving = buildValidSaving();
		saving.setAmount(null);
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenCurrencyIsNull() {
		Saving saving = buildValidSaving();
		saving.setCurrency(null);
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenInterestIsNull() {
		Saving saving = buildValidSaving();
		saving.setInterest(null);
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenDepositIsNull() {
		Saving saving = buildValidSaving();
		saving.setDeposit(null);
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWhenCapitalizationIsNull() {
		Saving saving = buildValidSaving();
		saving.setCapitalization(null);
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertFalse(violations.isEmpty());
	}

	@Test
	public void shouldFailValidationWithMultipleNullFields() {
		Saving saving = new Saving();
		Set<ConstraintViolation<Saving>> violations = validator.validate(saving);
		assertEquals(5, violations.size());
	}

	private static Saving buildValidSaving() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1500.00"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);
		return saving;
	}
}
