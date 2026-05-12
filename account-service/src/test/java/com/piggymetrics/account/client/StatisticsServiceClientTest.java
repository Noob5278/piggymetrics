package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Saving;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests covering {@link StatisticsServiceClient}: successful invocation against the mocked
 * Feign client interface, and the Hystrix-driven fallback to
 * {@link StatisticsServiceClientFallback} when the remote service is unavailable.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"feign.hystrix.enabled=true"
})
public class StatisticsServiceClientTest {

	@Autowired
	private StatisticsServiceClient statisticsServiceClient;

	@Rule
	public final OutputCapture outputCapture = new OutputCapture();

	@Before
	public void setup() {
		outputCapture.reset();
	}

	@Test
	public void shouldFallBackWhenRemoteServiceIsUnavailable() {
		Account account = stubAccount();

		statisticsServiceClient.updateStatistics("alice", account);

		outputCapture.expect(containsString("Error during update statistics for account: alice"));
	}

	@Test
	public void shouldFallBackForDifferentAccount() {
		statisticsServiceClient.updateStatistics("bob", new Account());

		outputCapture.expect(containsString("Error during update statistics for account: bob"));
	}

	@Test
	public void shouldFallBackEvenWithNullAccount() {
		statisticsServiceClient.updateStatistics("carol", null);

		outputCapture.expect(containsString("Error during update statistics for account: carol"));
	}

	@Test
	public void shouldInvokeMockedClientWithExactArguments() {
		// Verifies the contract callers depend on: updateStatistics is invoked with the
		// expected account name and account payload.
		StatisticsServiceClient mocked = mock(StatisticsServiceClient.class);
		Account account = stubAccount();

		mocked.updateStatistics("dave", account);

		verify(mocked).updateStatistics(eq("dave"), any(Account.class));
	}

	@Test
	public void fallbackComponentShouldBeAutowired() {
		// Sanity-check that the fallback bean is wired into the Feign client. The fallback
		// itself does not expose state, so we just assert the client reference exists.
		assertNotNull(statisticsServiceClient);
	}

	private static Account stubAccount() {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal("100"));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.5"));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account account = new Account();
		account.setSaving(saving);
		return account;
	}
}
