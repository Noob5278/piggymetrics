package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Saving;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class StatisticsServiceClientTest {

	@Mock
	private StatisticsServiceClient statisticsClient;

	@Before
	public void setup() {
		initMocks(this);
	}

	@Test
	public void shouldUpdateStatistics() {
		Account account = new Account();
		account.setName("test");

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(100));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("1.5"));
		saving.setDeposit(true);
		saving.setCapitalization(false);
		account.setSaving(saving);

		statisticsClient.updateStatistics("test", account);

		verify(statisticsClient, times(1)).updateStatistics("test", account);
	}

	@Test
	public void shouldUpdateStatisticsWithDifferentAccountNames() {
		Account first = new Account();
		first.setName("first");

		Account second = new Account();
		second.setName("second");

		statisticsClient.updateStatistics("first", first);
		statisticsClient.updateStatistics("second", second);

		verify(statisticsClient, times(1)).updateStatistics("first", first);
		verify(statisticsClient, times(1)).updateStatistics("second", second);
	}

	@Test(expected = RuntimeException.class)
	public void shouldThrowOnFailure() {
		Account account = new Account();
		account.setName("test");

		doThrow(new RuntimeException("Statistics service unavailable"))
				.when(statisticsClient).updateStatistics("test", account);

		statisticsClient.updateStatistics("test", account);
	}
}
