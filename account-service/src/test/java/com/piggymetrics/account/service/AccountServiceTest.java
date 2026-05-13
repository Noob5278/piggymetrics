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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AccountServiceTest {

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

	@Test
	public void shouldFindByName() {

		final Account account = new Account();
		account.setName("test");

		when(repository.findByName(account.getName())).thenReturn(account);
		Account found = accountService.findByName(account.getName());

		assertEquals(account, found);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNameIsEmpty() {
		accountService.findByName("");
	}

	@Test
	public void shouldCreateAccountWithGivenUser() {

		User user = new User();
		user.setUsername("test");

		Account account = accountService.create(user);

		assertEquals(user.getUsername(), account.getName());
		assertEquals(0, account.getSaving().getAmount().intValue());
		assertEquals(Currency.getDefault(), account.getSaving().getCurrency());
		assertEquals(0, account.getSaving().getInterest().intValue());
		assertEquals(false, account.getSaving().getDeposit());
		assertEquals(false, account.getSaving().getCapitalization());
		assertNotNull(account.getLastSeen());

		verify(authClient, times(1)).createUser(user);
		verify(repository, times(1)).save(account);
	}

	@Test
	public void shouldSaveChangesWhenUpdatedAccountGiven() {

		Item grocery = new Item();
		grocery.setTitle("Grocery");
		grocery.setAmount(new BigDecimal(10));
		grocery.setCurrency(Currency.USD);
		grocery.setPeriod(TimePeriod.DAY);
		grocery.setIcon("meal");

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal(9100));
		salary.setCurrency(Currency.USD);
		salary.setPeriod(TimePeriod.MONTH);
		salary.setIcon("wallet");

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(1500));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		final Account update = new Account();
		update.setName("test");
		update.setNote("test note");
		update.setIncomes(Arrays.asList(salary));
		update.setExpenses(Arrays.asList(grocery));
		update.setSaving(saving);

		final Account account = new Account();

		when(repository.findByName("test")).thenReturn(account);
		accountService.saveChanges("test", update);

		assertEquals(update.getNote(), account.getNote());
		assertNotNull(account.getLastSeen());

		assertEquals(update.getSaving().getAmount(), account.getSaving().getAmount());
		assertEquals(update.getSaving().getCurrency(), account.getSaving().getCurrency());
		assertEquals(update.getSaving().getInterest(), account.getSaving().getInterest());
		assertEquals(update.getSaving().getDeposit(), account.getSaving().getDeposit());
		assertEquals(update.getSaving().getCapitalization(), account.getSaving().getCapitalization());

		assertEquals(update.getExpenses().size(), account.getExpenses().size());
		assertEquals(update.getIncomes().size(), account.getIncomes().size());

		assertEquals(update.getExpenses().get(0).getTitle(), account.getExpenses().get(0).getTitle());
		assertEquals(0, update.getExpenses().get(0).getAmount().compareTo(account.getExpenses().get(0).getAmount()));
		assertEquals(update.getExpenses().get(0).getCurrency(), account.getExpenses().get(0).getCurrency());
		assertEquals(update.getExpenses().get(0).getPeriod(), account.getExpenses().get(0).getPeriod());
		assertEquals(update.getExpenses().get(0).getIcon(), account.getExpenses().get(0).getIcon());
		
		assertEquals(update.getIncomes().get(0).getTitle(), account.getIncomes().get(0).getTitle());
		assertEquals(0, update.getIncomes().get(0).getAmount().compareTo(account.getIncomes().get(0).getAmount()));
		assertEquals(update.getIncomes().get(0).getCurrency(), account.getIncomes().get(0).getCurrency());
		assertEquals(update.getIncomes().get(0).getPeriod(), account.getIncomes().get(0).getPeriod());
		assertEquals(update.getIncomes().get(0).getIcon(), account.getIncomes().get(0).getIcon());
		
		verify(repository, times(1)).save(account);
		verify(statisticsClient, times(1)).updateStatistics("test", account);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNoAccountsExistedWithGivenName() {
		final Account update = new Account();
		update.setIncomes(Arrays.asList(new Item()));
		update.setExpenses(Arrays.asList(new Item()));

		when(repository.findByName("test")).thenReturn(null);
		accountService.saveChanges("test", update);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenAccountAlreadyExists() {

		User user = new User();
		user.setUsername("test");

		final Account existing = new Account();
		existing.setName("test");

		when(repository.findByName(user.getUsername())).thenReturn(existing);

		accountService.create(user);
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenUserIsNull() {
		accountService.create(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenUsernameIsNull() {

		User user = new User();

		final Account existing = new Account();
		when(repository.findByName(user.getUsername())).thenReturn(existing);

		accountService.create(user);
	}

	@Test
	public void shouldFindByNameSuccessfully() {

		final Account account = new Account();
		account.setName("test");

		when(repository.findByName("test")).thenReturn(account);

		Account found = accountService.findByName("test");

		assertEquals(account, found);
		verify(repository, times(1)).findByName("test");
	}

	@Test
	public void shouldSaveChangesWithEmptyLists() {

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		final Account update = new Account();
		update.setName("test");
		update.setNote("empty lists");
		update.setIncomes(Collections.emptyList());
		update.setExpenses(Collections.emptyList());
		update.setSaving(saving);

		final Account account = new Account();

		when(repository.findByName("test")).thenReturn(account);
		accountService.saveChanges("test", update);

		assertEquals(update.getNote(), account.getNote());
		assertNotNull(account.getLastSeen());
		assertEquals(0, account.getIncomes().size());
		assertEquals(0, account.getExpenses().size());
		assertEquals(update.getSaving().getAmount(), account.getSaving().getAmount());

		verify(repository, times(1)).save(account);
		verify(statisticsClient, times(1)).updateStatistics("test", account);
	}

	@Test
	public void shouldSaveChangesWithNullNote() {

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		final Account update = new Account();
		update.setName("test");
		update.setNote(null);
		update.setIncomes(Collections.emptyList());
		update.setExpenses(Collections.emptyList());
		update.setSaving(saving);

		final Account account = new Account();
		account.setNote("previous note");

		when(repository.findByName("test")).thenReturn(account);
		accountService.saveChanges("test", update);

		assertNull(account.getNote());
		assertNotNull(account.getLastSeen());

		verify(repository, times(1)).save(account);
		verify(statisticsClient, times(1)).updateStatistics("test", account);
	}
}
