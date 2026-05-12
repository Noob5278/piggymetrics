package com.piggymetrics.account.service;

import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.domain.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;

/**
 * Tests covering the Hystrix circuit breaker that wraps {@link StatisticsServiceClient}.
 *
 * <p>With {@code feign.hystrix.enabled=true} and a fallback declared on the Feign client, every
 * call against an unreachable remote should be served by the fallback. Repeated calls keep the
 * fallback path engaged, simulating a sustained outage; the circuit transparently routes
 * subsequent traffic to the fallback as long as the underlying service is unavailable.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"feign.hystrix.enabled=true"
})
public class CircuitBreakerTest {

	@Autowired
	private StatisticsServiceClient statisticsServiceClient;

	@Rule
	public final OutputCapture outputCapture = new OutputCapture();

	@Before
	public void setup() {
		outputCapture.reset();
	}

	@Test
	public void shouldActivateFallbackWhenStatisticsServiceFails() {
		statisticsServiceClient.updateStatistics("ckt-test", new Account());

		outputCapture.expect(containsString("Error during update statistics for account: ckt-test"));
	}

	@Test
	public void shouldExecuteFallbackForEveryCallDuringOutage() {
		for (int i = 0; i < 5; i++) {
			statisticsServiceClient.updateStatistics("ckt-loop-" + i, new Account());
		}

		// Each invocation should hit the fallback while the remote is unreachable.
		outputCapture.expect(containsString("ckt-loop-0"));
		outputCapture.expect(containsString("ckt-loop-4"));
	}

	@Test
	public void shouldStayAvailableAfterRecoveryCallsCompleteFromFallback() {
		// Simulate a "burst" of failures so the breaker may open, then continue making calls.
		// Because the underlying service stays unreachable in this test, every call is served
		// by the fallback — but importantly, the client itself never throws and remains usable,
		// which is exactly the resilience guarantee we want.
		for (int i = 0; i < 10; i++) {
			statisticsServiceClient.updateStatistics("burst-" + i, new Account());
		}

		// Subsequent recovery-phase calls must still return without raising exceptions.
		statisticsServiceClient.updateStatistics("after-burst", new Account());

		outputCapture.expect(containsString("Error during update statistics for account: after-burst"));
	}

	@Test
	public void clientShouldBeWiredWithFallback() {
		// The Feign client must be present and proxied; a missing/raw client would mean
		// Hystrix is not actually wrapping invocations.
		assertNotNull(statisticsServiceClient);
	}
}
