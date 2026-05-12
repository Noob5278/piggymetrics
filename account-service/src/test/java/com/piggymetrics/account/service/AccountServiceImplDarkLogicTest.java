package com.piggymetrics.account.service;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.domain.*;
import com.piggymetrics.account.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Dark-logic edge-case tests for AccountServiceImpl.
 * Covers: null/blank account names, duplicate account creation,
 * null user input, and saveChanges boundary conditions.
 */
public class AccountServiceImplDarkLogicTest {

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

	// ── findByName dark-logic paths ──

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenFindByNameIsNull() {
		accountService.findByName(null);
	}

	@Test
	public void shouldReturnNullWhenAccountNotFound() {
		when(repository.findByName("nonexistent")).thenReturn(null);
		Account result = accountService.findByName("nonexistent");
		assertNull(result);
	}

	// ── create() dark-logic paths ──

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenCreatingDuplicateAccount() {
		Account existing = new Account();
		existing.setName("duplicate");

		when(repository.findByName("duplicate")).thenReturn(existing);

		User user = new User();
		user.setUsername("duplicate");
		user.setPassword("password");

		accountService.create(user);
	}

	@Test
	public void shouldNotCallAuthServiceWhenDuplicateAccountExists() {
		Account existing = new Account();
		existing.setName("duplicate");

		when(repository.findByName("duplicate")).thenReturn(existing);

		User user = new User();
		user.setUsername("duplicate");
		user.setPassword("password");

		try {
			accountService.create(user);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			verify(authClient, never()).createUser(any(User.class));
			verify(repository, never()).save(any(Account.class));
		}
	}

	@Test
	public void shouldSetDefaultSavingFieldsOnCreate() {
		User user = new User();
		user.setUsername("newuser");
		user.setPassword("password");

		when(repository.findByName("newuser")).thenReturn(null);

		Account account = accountService.create(user);

		assertNotNull(account);
		assertEquals("newuser", account.getName());
		assertNotNull(account.getSaving());
		assertEquals(Currency.getDefault(), account.getSaving().getCurrency());
		assertEquals(BigDecimal.ZERO.intValue(), account.getSaving().getAmount().intValue());
		assertEquals(BigDecimal.ZERO.intValue(), account.getSaving().getInterest().intValue());
		assertFalse(account.getSaving().getDeposit());
		assertFalse(account.getSaving().getCapitalization());
		assertNotNull(account.getLastSeen());
	}

	@Test
	public void shouldCallAuthClientAndRepositoryOnCreate() {
		User user = new User();
		user.setUsername("newuser");
		user.setPassword("password");

		when(repository.findByName("newuser")).thenReturn(null);

		accountService.create(user);

		verify(authClient, times(1)).createUser(user);
		verify(repository, times(1)).save(any(Account.class));
	}

	// ── saveChanges() dark-logic paths ──

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenSaveChangesWithNullName() {
		accountService.saveChanges(null, new Account());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenSaveChangesWithEmptyName() {
		accountService.saveChanges("", new Account());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowWhenSaveChangesAccountNotFound() {
		when(repository.findByName("missing")).thenReturn(null);
		accountService.saveChanges("missing", new Account());
	}

	@Test
	public void shouldSaveChangesWithEmptyItemLists() {
		Account existing = new Account();
		existing.setName("test");

		Account update = new Account();
		update.setIncomes(Collections.emptyList());
		update.setExpenses(Collections.emptyList());
		update.setSaving(createDefaultSaving());
		update.setNote("");

		when(repository.findByName("test")).thenReturn(existing);

		accountService.saveChanges("test", update);

		assertEquals(0, existing.getIncomes().size());
		assertEquals(0, existing.getExpenses().size());
		assertNotNull(existing.getLastSeen());
		verify(repository, times(1)).save(existing);
		verify(statisticsClient, times(1)).updateStatistics("test", existing);
	}

	@Test
	public void shouldSaveChangesWithNullItemLists() {
		Account existing = new Account();
		existing.setName("test");

		Account update = new Account();
		update.setIncomes(null);
		update.setExpenses(null);
		update.setSaving(createDefaultSaving());
		update.setNote(null);

		when(repository.findByName("test")).thenReturn(existing);

		accountService.saveChanges("test", update);

		assertNull(existing.getIncomes());
		assertNull(existing.getExpenses());
		assertNotNull(existing.getLastSeen());
		verify(repository, times(1)).save(existing);
	}

	@Test
	public void shouldOverwriteExistingFieldsOnSaveChanges() {
		Account existing = new Account();
		existing.setName("test");
		existing.setNote("old note");
		existing.setSaving(createDefaultSaving());
		existing.setIncomes(Arrays.asList(createItem("OldIncome", Currency.USD)));

		Saving newSaving = new Saving();
		newSaving.setAmount(new BigDecimal("5000"));
		newSaving.setCurrency(Currency.EUR);
		newSaving.setInterest(new BigDecimal("2.5"));
		newSaving.setDeposit(true);
		newSaving.setCapitalization(true);

		Account update = new Account();
		update.setIncomes(Arrays.asList(createItem("NewSalary", Currency.EUR)));
		update.setExpenses(Arrays.asList(createItem("NewExpense", Currency.RUB)));
		update.setSaving(newSaving);
		update.setNote("new note");

		when(repository.findByName("test")).thenReturn(existing);

		accountService.saveChanges("test", update);

		assertEquals("new note", existing.getNote());
		assertEquals(Currency.EUR, existing.getSaving().getCurrency());
		assertEquals(new BigDecimal("5000"), existing.getSaving().getAmount());
		assertTrue(existing.getSaving().getDeposit());
		assertTrue(existing.getSaving().getCapitalization());
		assertEquals(1, existing.getIncomes().size());
		assertEquals("NewSalary", existing.getIncomes().get(0).getTitle());
		assertEquals(1, existing.getExpenses().size());
		assertEquals("NewExpense", existing.getExpenses().get(0).getTitle());
	}

	// ── helpers ──

	private Saving createDefaultSaving() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.getDefault());
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);
		return saving;
	}

	private Item createItem(String title, Currency currency) {
		Item item = new Item();
		item.setTitle(title);
		item.setAmount(new BigDecimal("100"));
		item.setCurrency(currency);
		item.setPeriod(TimePeriod.MONTH);
		item.setIcon("icon");
		return item;
	}
}
