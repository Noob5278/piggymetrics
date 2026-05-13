package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Saving;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class StatisticsServiceClientTest {

	private StatisticsServiceClient statisticsServiceClient;

	@Before
	public void setup() {
		statisticsServiceClient = mock(StatisticsServiceClient.class);
	}

	@Test
	public void shouldInvokeUpdateStatisticsWithGivenArguments() {
		Account account = buildAccount("test");

		statisticsServiceClient.updateStatistics("test", account);

		verify(statisticsServiceClient, times(1)).updateStatistics("test", account);
		verifyNoMoreInteractions(statisticsServiceClient);
	}

	@Test
	public void shouldNotInvokeUpdateStatisticsWhenNotCalled() {
		verifyZeroInteractions(statisticsServiceClient);
	}

	@Test(expected = RuntimeException.class)
	public void shouldPropagateExceptionsRaisedByFeignClient() {
		Account account = buildAccount("test");

		doThrow(new RuntimeException("statistics service unavailable"))
				.when(statisticsServiceClient).updateStatistics("test", account);

		statisticsServiceClient.updateStatistics("test", account);
	}

	private static Account buildAccount(String name) {
		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.getDefault());
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account account = new Account();
		account.setName(name);
		account.setSaving(saving);
		return account;
	}
}
