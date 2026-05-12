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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AccountServiceImplTest {

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

	// ---------------------------------------------------------------------
	// 1. Duplicate account name scenario
	// ---------------------------------------------------------------------

	@Test
	public void shouldFailWhenAccountAlreadyExists() {

		User user = new User();
		user.setUsername("existing");

		Account existing = new Account();
		existing.setName("existing");

		when(repository.findByName("existing")).thenReturn(existing);

		try {
			accountService.create(user);
		} catch (IllegalArgumentException e) {
			assertEquals("account already exists: existing", e.getMessage());
			verify(authClient, never()).createUser(any(User.class));
			verify(repository, never()).save(any(Account.class));
			return;
		}

		throw new AssertionError("Expected IllegalArgumentException for duplicate account name");
	}

	// ---------------------------------------------------------------------
	// 2. Null user scenario - create(null)
	// ---------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenUserIsNull() {
		accountService.create(null);
	}

	// ---------------------------------------------------------------------
	// 3. User with a null username
	// ---------------------------------------------------------------------

	@Test
	public void shouldCreateAccountWhenUsernameIsNull() {

		User user = new User();
		user.setUsername(null);

		when(repository.findByName(null)).thenReturn(null);

		Account account = accountService.create(user);

		assertNull(account.getName());
		assertNotNull(account.getLastSeen());
		assertNotNull(account.getSaving());
		assertEquals(0, account.getSaving().getAmount().intValue());
		assertEquals(Currency.getDefault(), account.getSaving().getCurrency());

		verify(authClient, times(1)).createUser(user);
		verify(repository, times(1)).save(account);
	}

	// ---------------------------------------------------------------------
	// 4. Null currency inside Saving when calling saveChanges()
	// ---------------------------------------------------------------------

	@Test
	public void shouldSaveChangesWhenSavingCurrencyIsNull() {

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(100));
		saving.setCurrency(null);
		saving.setInterest(new BigDecimal("1.5"));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setNote("no currency");
		update.setIncomes(Collections.<Item>emptyList());
		update.setExpenses(Collections.<Item>emptyList());
		update.setSaving(saving);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertNull(stored.getSaving().getCurrency());
		assertEquals(new BigDecimal(100), stored.getSaving().getAmount());
		assertEquals("no currency", stored.getNote());
		assertNotNull(stored.getLastSeen());

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	// ---------------------------------------------------------------------
	// 5. Null incomes / expenses / saving / note in saveChanges()
	// ---------------------------------------------------------------------

	@Test
	public void shouldSaveChangesWhenIncomesAreNull() {

		Account update = buildUpdate();
		update.setIncomes(null);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertNull(stored.getIncomes());
		assertNotNull(stored.getExpenses());
		assertNotNull(stored.getSaving());
		assertEquals(update.getNote(), stored.getNote());
		assertNotNull(stored.getLastSeen());

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	@Test
	public void shouldSaveChangesWhenExpensesAreNull() {

		Account update = buildUpdate();
		update.setExpenses(null);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertNull(stored.getExpenses());
		assertNotNull(stored.getIncomes());
		assertNotNull(stored.getSaving());
		assertEquals(update.getNote(), stored.getNote());
		assertNotNull(stored.getLastSeen());

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	@Test
	public void shouldSaveChangesWhenSavingIsNull() {

		Account update = buildUpdate();
		update.setSaving(null);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertNull(stored.getSaving());
		assertNotNull(stored.getIncomes());
		assertNotNull(stored.getExpenses());
		assertEquals(update.getNote(), stored.getNote());
		assertNotNull(stored.getLastSeen());

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	@Test
	public void shouldSaveChangesWhenNoteIsNull() {

		Account update = buildUpdate();
		update.setNote(null);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertNull(stored.getNote());
		assertNotNull(stored.getIncomes());
		assertNotNull(stored.getExpenses());
		assertNotNull(stored.getSaving());
		assertNotNull(stored.getLastSeen());

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	// ---------------------------------------------------------------------
	// 6. Negative BigDecimal amounts in Saving / income / expense
	// ---------------------------------------------------------------------

	@Test
	public void shouldSaveChangesWhenAmountsAreNegative() {

		BigDecimal negative = new BigDecimal("-100");

		Item expense = new Item();
		expense.setTitle("Negative expense");
		expense.setAmount(negative);
		expense.setCurrency(Currency.USD);
		expense.setPeriod(TimePeriod.DAY);
		expense.setIcon("meal");

		Item income = new Item();
		income.setTitle("Negative income");
		income.setAmount(negative);
		income.setCurrency(Currency.USD);
		income.setPeriod(TimePeriod.MONTH);
		income.setIcon("wallet");

		Saving saving = new Saving();
		saving.setAmount(negative);
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("-1.5"));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setNote("negative");
		update.setIncomes(Arrays.asList(income));
		update.setExpenses(Arrays.asList(expense));
		update.setSaving(saving);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertEquals(0, negative.compareTo(stored.getSaving().getAmount()));
		assertEquals(0, new BigDecimal("-1.5").compareTo(stored.getSaving().getInterest()));
		assertEquals(0, negative.compareTo(stored.getIncomes().get(0).getAmount()));
		assertEquals(0, negative.compareTo(stored.getExpenses().get(0).getAmount()));

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	// ---------------------------------------------------------------------
	// 7. Zero BigDecimal amounts
	// ---------------------------------------------------------------------

	@Test
	public void shouldSaveChangesWhenAmountsAreZero() {

		BigDecimal zero = BigDecimal.ZERO;

		Item expense = new Item();
		expense.setTitle("Zero expense");
		expense.setAmount(zero);
		expense.setCurrency(Currency.USD);
		expense.setPeriod(TimePeriod.DAY);
		expense.setIcon("meal");

		Item income = new Item();
		income.setTitle("Zero income");
		income.setAmount(zero);
		income.setCurrency(Currency.USD);
		income.setPeriod(TimePeriod.MONTH);
		income.setIcon("wallet");

		Saving saving = new Saving();
		saving.setAmount(zero);
		saving.setCurrency(Currency.USD);
		saving.setInterest(zero);
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setNote("zero");
		update.setIncomes(Arrays.asList(income));
		update.setExpenses(Arrays.asList(expense));
		update.setSaving(saving);

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		accountService.saveChanges("test", update);

		assertEquals(0, zero.compareTo(stored.getSaving().getAmount()));
		assertEquals(0, zero.compareTo(stored.getSaving().getInterest()));
		assertEquals(0, zero.compareTo(stored.getIncomes().get(0).getAmount()));
		assertEquals(0, zero.compareTo(stored.getExpenses().get(0).getAmount()));

		verify(repository, times(1)).save(stored);
		verify(statisticsClient, times(1)).updateStatistics("test", stored);
	}

	// ---------------------------------------------------------------------
	// 8. Statistics service failure propagates
	// ---------------------------------------------------------------------

	@Test
	public void shouldPropagateExceptionWhenStatisticsServiceFails() {

		Account update = buildUpdate();

		Account stored = new Account();
		when(repository.findByName("test")).thenReturn(stored);

		RuntimeException boom = new RuntimeException("statistics down");
		doThrow(boom).when(statisticsClient).updateStatistics(anyString(), any(Account.class));

		try {
			accountService.saveChanges("test", update);
		} catch (RuntimeException thrown) {
			assertSame(boom, thrown);
			// repository.save() runs before statisticsClient.updateStatistics(), so it should have been invoked
			verify(repository, times(1)).save(stored);
			verify(statisticsClient, times(1)).updateStatistics("test", stored);
			return;
		}

		throw new AssertionError("Expected RuntimeException from statistics service to propagate");
	}

	// ---------------------------------------------------------------------
	// helpers
	// ---------------------------------------------------------------------

	private static Account buildUpdate() {

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

		Account update = new Account();
		update.setName("test");
		update.setNote("test note");
		update.setIncomes(Arrays.asList(salary));
		update.setExpenses(Arrays.asList(grocery));
		update.setSaving(saving);

		return update;
	}
}
