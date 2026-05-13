package com.piggymetrics.account.domain;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TimePeriodTest {

	@Test
	public void shouldContainExpectedValuesInDeclaredOrder() {
		TimePeriod[] expected = new TimePeriod[]{
				TimePeriod.YEAR,
				TimePeriod.QUARTER,
				TimePeriod.MONTH,
				TimePeriod.DAY,
				TimePeriod.HOUR
		};
		assertArrayEquals(expected, TimePeriod.values());
	}

	@Test
	public void shouldResolveEachValueByName() {
		assertEquals(TimePeriod.YEAR, TimePeriod.valueOf("YEAR"));
		assertEquals(TimePeriod.QUARTER, TimePeriod.valueOf("QUARTER"));
		assertEquals(TimePeriod.MONTH, TimePeriod.valueOf("MONTH"));
		assertEquals(TimePeriod.DAY, TimePeriod.valueOf("DAY"));
		assertEquals(TimePeriod.HOUR, TimePeriod.valueOf("HOUR"));
	}
}
