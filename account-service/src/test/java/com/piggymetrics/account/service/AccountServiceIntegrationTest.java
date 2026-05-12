package com.piggymetrics.account.service;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Item;
import com.piggymetrics.account.domain.Saving;
import com.piggymetrics.account.domain.TimePeriod;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.repository.AccountRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for {@link AccountServiceImpl} that exercise real repository
 * operations against an embedded MongoDB instance. The Feign clients
 * ({@link AuthServiceClient}, {@link StatisticsServiceClient}) are stubbed with Mockito
 * and wired into a hand-instantiated {@link AccountServiceImpl} so the tests focus on
 * the persistence behaviour end-to-end without dragging in the full Spring Boot stack.
 *
 * <p>This complements the unit-level {@link AccountServiceTest} (which uses pure mocks)
 * and the repository-only {@link com.piggymetrics.account.repository.AccountRepositoryTest}
 * (which exercises only Mongo).
 */
@RunWith(SpringRunner.class)
@DataMongoTest
public class AccountServiceIntegrationTest {

	@Autowired
	private AccountRepository accountRepository;

	private AuthServiceClient authServiceClient;
	private StatisticsServiceClient statisticsServiceClient;
	private AccountServiceImpl accountService;

	@Before
	public void setUp() {
		accountRepository.deleteAll();

		authServiceClient = Mockito.mock(AuthServiceClient.class);
		statisticsServiceClient = Mockito.mock(StatisticsServiceClient.class);

		accountService = new AccountServiceImpl();
		ReflectionTestUtils.setField(accountService, "repository", accountRepository);
		ReflectionTestUtils.setField(accountService, "authClient", authServiceClient);
		ReflectionTestUtils.setField(accountService, "statisticsClient", statisticsServiceClient);
	}

	@After
	public void tearDown() {
		accountRepository.deleteAll();
	}

	@Test
	public void shouldCreateAccountEndToEnd() {

		User user = new User();
		user.setUsername("integration-user");
		user.setPassword("strongpassword");

		Account created = accountService.create(user);

		assertNotNull(created);
		assertEquals("integration-user", created.getName());
		assertEquals(Currency.getDefault(), created.getSaving().getCurrency());
		assertEquals(0, created.getSaving().getAmount().intValue());
		assertFalse(created.getSaving().getDeposit());
		assertFalse(created.getSaving().getCapitalization());

		Account persisted = accountRepository.findByName("integration-user");
		assertNotNull("Account should be persisted to MongoDB", persisted);
		assertEquals("integration-user", persisted.getName());

		verify(authServiceClient).createUser(user);
	}

	@Test
	public void shouldUpdateAccountEndToEnd() {

		User user = new User();
		user.setUsername("update-user");
		user.setPassword("strongpassword");
		accountService.create(user);

		Saving newSaving = new Saving();
		newSaving.setAmount(new BigDecimal("9999"));
		newSaving.setCurrency(Currency.EUR);
		newSaving.setInterest(new BigDecimal("4.25"));
		newSaving.setDeposit(true);
		newSaving.setCapitalization(true);

		Item income = new Item();
		income.setTitle("Salary");
		income.setAmount(new BigDecimal("5000"));
		income.setCurrency(Currency.EUR);
		income.setPeriod(TimePeriod.MONTH);
		income.setIcon("wallet");

		Item expense = new Item();
		expense.setTitle("Rent");
		expense.setAmount(new BigDecimal("1500"));
		expense.setCurrency(Currency.EUR);
		expense.setPeriod(TimePeriod.MONTH);
		expense.setIcon("home");

		Account update = new Account();
		update.setSaving(newSaving);
		update.setIncomes(Collections.singletonList(income));
		update.setExpenses(Collections.singletonList(expense));
		update.setNote("integration update");

		accountService.saveChanges("update-user", update);

		Account loaded = accountRepository.findByName("update-user");
		assertNotNull(loaded);
		assertEquals(new BigDecimal("9999"), loaded.getSaving().getAmount());
		assertEquals(Currency.EUR, loaded.getSaving().getCurrency());
		assertTrue(loaded.getSaving().getDeposit());
		assertTrue(loaded.getSaving().getCapitalization());
		assertEquals(1, loaded.getIncomes().size());
		assertEquals(1, loaded.getExpenses().size());
		assertEquals("Salary", loaded.getIncomes().get(0).getTitle());
		assertEquals("Rent", loaded.getExpenses().get(0).getTitle());
		assertEquals("integration update", loaded.getNote());

		verify(statisticsServiceClient).updateStatistics(eq("update-user"), any(Account.class));
	}

	@Test
	public void shouldRetrieveAccountByNameWithPersistence() {

		User user = new User();
		user.setUsername("retrieve-user");
		user.setPassword("strongpassword");
		accountService.create(user);

		Account found = accountService.findByName("retrieve-user");

		assertNotNull(found);
		assertEquals("retrieve-user", found.getName());
		assertNotNull(found.getSaving());
		assertEquals(Currency.getDefault(), found.getSaving().getCurrency());
	}

	@Test
	public void shouldReturnNullWhenRetrievingMissingAccount() {
		Account found = accountService.findByName("never-saved");
		assertNull(found);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectDuplicateAccountAtDatabaseLevel() {

		User first = new User();
		first.setUsername("duplicate-user");
		first.setPassword("strongpassword");
		accountService.create(first);

		try {
			User second = new User();
			second.setUsername("duplicate-user");
			second.setPassword("anotherpassword");
			accountService.create(second);
		} finally {
			long count = accountRepository.count();
			assertEquals("Only the first account should be persisted", 1L, count);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectSaveChangesForMissingAccount() {

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("1"));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setSaving(saving);
		update.setIncomes(Arrays.<Item>asList());
		update.setExpenses(Arrays.<Item>asList());

		try {
			accountService.saveChanges("missing-user", update);
		} finally {
			verify(statisticsServiceClient, never()).updateStatistics(any(String.class), any(Account.class));
		}
	}
}
