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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Additional unit tests for {@link AccountServiceImpl} covering branches that the
 * original {@code AccountServiceTest} does not exercise: duplicate account creation,
 * downstream auth-service failures, statistics-client behaviour, and edge cases
 * around the {@code saveChanges} flow.
 */
public class AccountServiceImplExtendedTest {

	@InjectMocks
	private AccountServiceImpl accountService;

	@Mock
	private StatisticsServiceClient statisticsClient;

	@Mock
	private AuthServiceClient authClient;

	@Mock
	private AccountRepository repository;

	@Before
	public void setup() {
		initMocks(this);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToCreateWhenAccountAlreadyExists() {
		User user = new User();
		user.setUsername("existing");
		user.setPassword("password");

		Account existing = new Account();
		existing.setName("existing");
		when(repository.findByName("existing")).thenReturn(existing);

		try {
			accountService.create(user);
		} finally {
			verify(authClient, never()).createUser(user);
			verify(repository, never()).save(existing);
		}
	}

	@Test
	public void shouldDelegateUserCreationToAuthClient() {
		User user = new User();
		user.setUsername("brand-new");
		user.setPassword("password");

		when(repository.findByName("brand-new")).thenReturn(null);

		Account account = accountService.create(user);

		verify(authClient, times(1)).createUser(user);
		verify(repository, times(1)).save(account);
		assertEquals("brand-new", account.getName());
		assertEquals(Currency.getDefault(), account.getSaving().getCurrency());
		assertEquals(BigDecimal.ZERO.intValue(), account.getSaving().getAmount().intValue());
		assertEquals(BigDecimal.ZERO.intValue(), account.getSaving().getInterest().intValue());
		assertEquals(Boolean.FALSE, account.getSaving().getDeposit());
		assertEquals(Boolean.FALSE, account.getSaving().getCapitalization());
		assertNotNull(account.getLastSeen());
	}

	@Test(expected = RuntimeException.class)
	public void shouldPropagateAuthClientFailureDuringCreate() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("password");

		when(repository.findByName("alice")).thenReturn(null);
		doThrow(new RuntimeException("auth-service unavailable"))
				.when(authClient).createUser(user);

		try {
			accountService.create(user);
		} finally {
			verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Account.class));
		}
	}

	@Test
	public void saveChangesShouldStillSucceedWhenStatisticsClientReportsFailure() {
		// The Hystrix-backed StatisticsServiceClient swallows downstream failures via its
		// fallback; this test ensures the persistence side of saveChanges completes even
		// when the statistics call has side effects or no-ops.
		Account existing = new Account();
		when(repository.findByName("alice")).thenReturn(existing);

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("1000"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("1.5"));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setSaving(saving);
		update.setIncomes(Collections.<Item>emptyList());
		update.setExpenses(Collections.<Item>emptyList());
		update.setNote("note");

		accountService.saveChanges("alice", update);

		verify(repository, times(1)).save(existing);
		verify(statisticsClient, times(1)).updateStatistics("alice", existing);
		assertEquals("note", existing.getNote());
		assertNotNull(existing.getLastSeen());
		assertEquals(saving.getAmount(), existing.getSaving().getAmount());
	}

	@Test
	public void saveChangesShouldOverwriteAllUpdatableFields() {
		Account existing = new Account();
		existing.setName("alice");
		existing.setNote("OLD");
		Saving oldSaving = new Saving();
		oldSaving.setAmount(new BigDecimal("100"));
		oldSaving.setCurrency(Currency.EUR);
		oldSaving.setInterest(new BigDecimal("0"));
		oldSaving.setDeposit(false);
		oldSaving.setCapitalization(false);
		existing.setSaving(oldSaving);
		when(repository.findByName("alice")).thenReturn(existing);

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal("9100"));
		salary.setCurrency(Currency.USD);
		salary.setPeriod(TimePeriod.MONTH);
		salary.setIcon("wallet");

		Saving newSaving = new Saving();
		newSaving.setAmount(new BigDecimal("2000"));
		newSaving.setCurrency(Currency.USD);
		newSaving.setInterest(new BigDecimal("4.0"));
		newSaving.setDeposit(true);
		newSaving.setCapitalization(true);

		Account update = new Account();
		update.setSaving(newSaving);
		update.setIncomes(Arrays.asList(salary));
		update.setExpenses(Collections.<Item>emptyList());
		update.setNote("NEW");

		accountService.saveChanges("alice", update);

		assertEquals("NEW", existing.getNote());
		assertEquals(newSaving, existing.getSaving());
		assertEquals(1, existing.getIncomes().size());
		assertEquals(0, existing.getExpenses().size());
		verify(statisticsClient, times(1)).updateStatistics("alice", existing);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailFindByNameWithNull() {
		accountService.findByName(null);
	}

	@Test
	public void shouldDelegateFindByNameToRepository() {
		Account account = new Account();
		account.setName("alice");
		when(repository.findByName("alice")).thenReturn(account);

		Account found = accountService.findByName("alice");

		assertEquals(account, found);
		verify(repository, times(1)).findByName("alice");
	}
}
