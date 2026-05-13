package com.piggymetrics.account;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Item;
import com.piggymetrics.account.domain.Saving;
import com.piggymetrics.account.domain.TimePeriod;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.repository.AccountRepository;
import com.piggymetrics.account.service.AccountService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * End-to-end integration test that exercises the account-service through the
 * real Spring context and embedded MongoDB. Outbound Feign clients are
 * swapped at runtime with Mockito-managed mocks via {@link ReflectionTestUtils}
 * so the test does not depend on the auth-service / statistics-service. This
 * approach avoids ambiguity between the Feign-generated proxy and the
 * registered {@code statisticsServiceClientFallback} component bean.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceIntegrationTest {

	@Autowired
	private AccountService accountService;

	@Autowired
	private AccountRepository repository;

	private AuthServiceClient authClient;
	private StatisticsServiceClient statisticsClient;

	@Before
	public void setup() {
		repository.deleteAll();

		authClient = Mockito.mock(AuthServiceClient.class);
		statisticsClient = Mockito.mock(StatisticsServiceClient.class);

		ReflectionTestUtils.setField(accountService, "authClient", authClient);
		ReflectionTestUtils.setField(accountService, "statisticsClient", statisticsClient);
	}

	@After
	public void teardown() {
		repository.deleteAll();
	}

	@Test
	public void shouldCreateAccountAndPersistInMongo() {
		User user = new User();
		user.setUsername("inttest-create");
		user.setPassword("p@ssword");

		Account created = accountService.create(user);

		assertNotNull(created);
		assertEquals(user.getUsername(), created.getName());
		assertNotNull(created.getLastSeen());
		assertEquals(Currency.getDefault(), created.getSaving().getCurrency());
		assertEquals(0, created.getSaving().getAmount().intValue());

		Account fromDb = repository.findByName("inttest-create");
		assertNotNull(fromDb);
		assertEquals(user.getUsername(), fromDb.getName());

		verify(authClient, times(1)).createUser(user);
	}

	@Test
	public void shouldRetrieveCreatedAccountByName() {
		User user = new User();
		user.setUsername("inttest-find");
		user.setPassword("p@ssword");
		accountService.create(user);

		Account found = accountService.findByName("inttest-find");

		assertNotNull(found);
		assertEquals("inttest-find", found.getName());
	}

	@Test
	public void shouldReturnNullForUnknownAccount() {
		Account found = accountService.findByName("inttest-missing");
		assertNull(found);
	}

	@Test
	public void shouldSaveChangesThroughFullStack() {
		User user = new User();
		user.setUsername("inttest-update");
		user.setPassword("p@ssword");
		accountService.create(user);

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("2500.00"));
		saving.setCurrency(Currency.EUR);
		saving.setInterest(new BigDecimal("4.50"));
		saving.setDeposit(true);
		saving.setCapitalization(true);

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal("8000"));
		salary.setCurrency(Currency.EUR);
		salary.setPeriod(TimePeriod.MONTH);
		salary.setIcon("wallet");

		Item rent = new Item();
		rent.setTitle("Rent");
		rent.setAmount(new BigDecimal("1500"));
		rent.setCurrency(Currency.EUR);
		rent.setPeriod(TimePeriod.MONTH);
		rent.setIcon("home");

		Account update = new Account();
		update.setName("inttest-update");
		update.setNote("monthly update");
		update.setSaving(saving);
		update.setIncomes(Collections.singletonList(salary));
		update.setExpenses(Arrays.asList(rent));

		accountService.saveChanges("inttest-update", update);

		Account persisted = repository.findByName("inttest-update");
		assertNotNull(persisted);
		assertEquals("monthly update", persisted.getNote());
		assertEquals(0, persisted.getSaving().getAmount().compareTo(new BigDecimal("2500.00")));
		assertEquals(Currency.EUR, persisted.getSaving().getCurrency());
		assertEquals(Boolean.TRUE, persisted.getSaving().getDeposit());
		assertEquals(Boolean.TRUE, persisted.getSaving().getCapitalization());
		assertEquals(1, persisted.getIncomes().size());
		assertEquals(1, persisted.getExpenses().size());
		assertEquals("Salary", persisted.getIncomes().get(0).getTitle());
		assertEquals("Rent", persisted.getExpenses().get(0).getTitle());

		// Account doesn't override equals/hashCode, so verify the statistics call
		// captured the right account by inspecting field values.
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		verify(statisticsClient, times(1))
				.updateStatistics(eq("inttest-update"), captor.capture());
		Account sentToStats = captor.getValue();
		assertEquals("monthly update", sentToStats.getNote());
		assertEquals(1, sentToStats.getIncomes().size());
		assertEquals(1, sentToStats.getExpenses().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToCreateDuplicateAccount() {
		User user = new User();
		user.setUsername("inttest-dup");
		user.setPassword("p@ssword");

		accountService.create(user);
		accountService.create(user);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSaveChangesForUnknownAccount() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setName("inttest-noexist");
		update.setSaving(saving);

		accountService.saveChanges("inttest-noexist", update);
	}

	@Test
	public void shouldPersistEmptyIncomesAndExpenses() {
		User user = new User();
		user.setUsername("inttest-empty");
		user.setPassword("p@ssword");
		accountService.create(user);

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setName("inttest-empty");
		update.setSaving(saving);
		update.setIncomes(Collections.<Item>emptyList());
		update.setExpenses(Collections.<Item>emptyList());

		accountService.saveChanges("inttest-empty", update);

		Account persisted = repository.findByName("inttest-empty");
		assertNotNull(persisted);
		assertTrue(persisted.getIncomes().isEmpty());
		assertTrue(persisted.getExpenses().isEmpty());
	}
}
