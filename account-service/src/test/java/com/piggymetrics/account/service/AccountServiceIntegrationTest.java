package com.piggymetrics.account.service;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClientFallback;
import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Item;
import com.piggymetrics.account.domain.Saving;
import com.piggymetrics.account.domain.TimePeriod;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.repository.AccountRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * End-to-end integration test wiring up the real {@link AccountService},
 * {@link AccountRepository} backed by the embedded MongoDB instance, the auth-service
 * Feign client (mocked), and the real Hystrix-backed
 * {@link StatisticsServiceClient}/{@link StatisticsServiceClientFallback} pair so that
 * the circuit-breaker path is exercised when the downstream service is unavailable.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"feign.hystrix.enabled=true"
})
public class AccountServiceIntegrationTest {

	@Autowired
	private AccountService accountService;

	@Autowired
	private AccountRepository repository;

	@MockBean
	private AuthServiceClient authServiceClient;

	@Rule
	public final OutputCapture outputCapture = new OutputCapture();

	@After
	public void cleanup() {
		repository.deleteAll();
	}

	@Test
	public void createShouldPersistAccountAndCallAuthService() {
		User user = new User();
		user.setUsername("alice");
		user.setPassword("password");

		Account created = accountService.create(user);

		assertNotNull(created);
		assertEquals("alice", created.getName());
		assertEquals(Currency.USD, created.getSaving().getCurrency());
		verify(authServiceClient, times(1)).createUser(user);

		Account stored = repository.findByName("alice");
		assertNotNull("account should be saved to mongo", stored);
		assertEquals(created.getName(), stored.getName());
		assertEquals(0, stored.getSaving().getAmount().compareTo(BigDecimal.ZERO));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createShouldRejectDuplicateUsernames() {
		User user = new User();
		user.setUsername("bob");
		user.setPassword("password");

		accountService.create(user);

		User dup = new User();
		dup.setUsername("bob");
		dup.setPassword("password");
		accountService.create(dup);
	}

	@Test
	public void saveChangesShouldPersistAccountAndTriggerStatisticsCircuitBreaker() {
		User user = new User();
		user.setUsername("carol");
		user.setPassword("password");
		accountService.create(user);

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal("9100"));
		salary.setCurrency(Currency.USD);
		salary.setPeriod(TimePeriod.MONTH);
		salary.setIcon("wallet");

		Saving newSaving = new Saving();
		newSaving.setAmount(new BigDecimal("2000"));
		newSaving.setCurrency(Currency.EUR);
		newSaving.setInterest(new BigDecimal("4.5"));
		newSaving.setDeposit(true);
		newSaving.setCapitalization(true);

		Account update = new Account();
		update.setSaving(newSaving);
		update.setIncomes(Arrays.asList(salary));
		update.setExpenses(Collections.<Item>emptyList());
		update.setNote("hello");

		outputCapture.reset();
		accountService.saveChanges("carol", update);

		Account stored = repository.findByName("carol");
		assertEquals("hello", stored.getNote());
		assertEquals(Currency.EUR, stored.getSaving().getCurrency());
		assertEquals(0, stored.getSaving().getAmount().compareTo(new BigDecimal("2000")));
		assertEquals(1, stored.getIncomes().size());
		assertEquals("Salary", stored.getIncomes().get(0).getTitle());

		// The real statistics-service is unreachable in the test environment, so the
		// Hystrix circuit breaker routes the call to StatisticsServiceClientFallback.
		outputCapture.expect(containsString("Error during update statistics for account: carol"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void saveChangesShouldFailForUnknownAccount() {
		Saving saving = new Saving();
		saving.setAmount(BigDecimal.ZERO);
		saving.setCurrency(Currency.USD);
		saving.setInterest(BigDecimal.ZERO);
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account update = new Account();
		update.setSaving(saving);

		accountService.saveChanges("ghost", update);
	}

	@Test
	public void findByNameShouldReturnNullForMissingAccount() {
		assertNull(accountService.findByName("nobody"));
	}

	@Test
	public void findByNameShouldReturnPersistedAccount() {
		User user = new User();
		user.setUsername("dan");
		user.setPassword("password");
		accountService.create(user);

		Account found = accountService.findByName("dan");
		assertNotNull(found);
		assertEquals("dan", found.getName());
	}
}
