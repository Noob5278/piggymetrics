package com.piggymetrics.statistics.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.piggymetrics.statistics.domain.*;
import com.piggymetrics.statistics.domain.timeseries.DataPoint;
import com.piggymetrics.statistics.repository.DataPointRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Dark-logic edge-case tests for StatisticsServiceImpl.
 * Covers: empty income/expense lists, whitespace account names,
 * and saving-only accounts.
 */
public class StatisticsServiceImplDarkLogicTest {

	@InjectMocks
	private StatisticsServiceImpl statisticsService;

	@Mock
	private ExchangeRatesServiceImpl ratesService;

	@Mock
	private DataPointRepository repository;

	@Before
	public void setup() {
		initMocks(this);
	}

	private void setupRatesMock() {
		Map<Currency, BigDecimal> rates = ImmutableMap.of(
				Currency.EUR, new BigDecimal("0.8"),
				Currency.RUB, new BigDecimal("80"),
				Currency.USD, BigDecimal.ONE
		);

		when(ratesService.convert(any(Currency.class), any(Currency.class), any(BigDecimal.class)))
				.then(i -> ((BigDecimal) i.getArgument(2))
						.divide(rates.get(i.getArgument(0)), 4, RoundingMode.HALF_UP));
		when(ratesService.getCurrentRates()).thenReturn(rates);
		when(repository.save(any(DataPoint.class))).then(returnsFirstArg());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenFindByAccountNameIsNull() {
		statisticsService.findByAccountName(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenFindByAccountNameIsEmpty() {
		statisticsService.findByAccountName("");
	}

	/**
	 * Documents that whitespace-only account names pass Assert.hasLength()
	 * validation and reach the repository layer. This is a gap — the service
	 * does not trim or reject whitespace-only strings.
	 */
	@Test
	public void shouldAcceptWhitespaceOnlyAccountNameDueToMissingTrimValidation() {
		when(repository.findByIdAccount("   ")).thenReturn(Collections.emptyList());
		assertTrue(statisticsService.findByAccountName("   ").isEmpty());
	}

	@Test
	public void shouldReturnEmptyListWhenNoDataPoints() {
		when(repository.findByIdAccount("test")).thenReturn(Collections.emptyList());

		assertTrue(statisticsService.findByAccountName("test").isEmpty());
	}

	@Test
	public void shouldSaveDataPointWithEmptyIncomesAndExpenses() {
		setupRatesMock();

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("500"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("1.5"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		Account account = new Account();
		account.setIncomes(Collections.emptyList());
		account.setExpenses(Collections.emptyList());
		account.setSaving(saving);

		DataPoint result = statisticsService.save("test", account);

		assertNotNull(result);
		assertEquals("test", result.getId().getAccount());
		assertTrue(result.getIncomes().isEmpty());
		assertTrue(result.getExpenses().isEmpty());
		verify(repository, times(1)).save(any(DataPoint.class));
	}

	@Test
	public void shouldSaveDataPointWithSingleIncomeAndNoExpenses() {
		setupRatesMock();

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal("5000"));
		salary.setCurrency(Currency.USD);
		salary.setPeriod(TimePeriod.MONTH);

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1000"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("2"));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account account = new Account();
		account.setIncomes(ImmutableList.of(salary));
		account.setExpenses(Collections.emptyList());
		account.setSaving(saving);

		DataPoint result = statisticsService.save("test", account);

		assertNotNull(result);
		assertEquals(1, result.getIncomes().size());
		assertTrue(result.getExpenses().isEmpty());
	}

	@Test
	public void shouldSaveDataPointWithMultipleCurrencies() {
		setupRatesMock();

		Item salaryUSD = new Item();
		salaryUSD.setTitle("Salary");
		salaryUSD.setAmount(new BigDecimal("5000"));
		salaryUSD.setCurrency(Currency.USD);
		salaryUSD.setPeriod(TimePeriod.MONTH);

		Item freelanceEUR = new Item();
		freelanceEUR.setTitle("Freelance");
		freelanceEUR.setAmount(new BigDecimal("1000"));
		freelanceEUR.setCurrency(Currency.EUR);
		freelanceEUR.setPeriod(TimePeriod.MONTH);

		Item groceryRUB = new Item();
		groceryRUB.setTitle("Grocery");
		groceryRUB.setAmount(new BigDecimal("5000"));
		groceryRUB.setCurrency(Currency.RUB);
		groceryRUB.setPeriod(TimePeriod.DAY);

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("2000"));
		saving.setCurrency(Currency.EUR);
		saving.setInterest(new BigDecimal("3"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		Account account = new Account();
		account.setIncomes(ImmutableList.of(salaryUSD, freelanceEUR));
		account.setExpenses(ImmutableList.of(groceryRUB));
		account.setSaving(saving);

		DataPoint result = statisticsService.save("test", account);

		assertNotNull(result);
		assertEquals(2, result.getIncomes().size());
		assertEquals(1, result.getExpenses().size());
		assertNotNull(result.getRates());
		assertEquals(3, result.getRates().size());
	}
}
