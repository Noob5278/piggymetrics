package com.piggymetrics.account.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimePeriodTest {

	@Test
	public void shouldExposeAllExpectedTimePeriods() {
		List<TimePeriod> values = Arrays.asList(TimePeriod.values());

		assertEquals(5, values.size());
		assertTrue(values.contains(TimePeriod.YEAR));
		assertTrue(values.contains(TimePeriod.QUARTER));
		assertTrue(values.contains(TimePeriod.MONTH));
		assertTrue(values.contains(TimePeriod.DAY));
		assertTrue(values.contains(TimePeriod.HOUR));
	}

	@Test
	public void shouldResolveByName() {
		assertEquals(TimePeriod.YEAR, TimePeriod.valueOf("YEAR"));
		assertEquals(TimePeriod.QUARTER, TimePeriod.valueOf("QUARTER"));
		assertEquals(TimePeriod.MONTH, TimePeriod.valueOf("MONTH"));
		assertEquals(TimePeriod.DAY, TimePeriod.valueOf("DAY"));
		assertEquals(TimePeriod.HOUR, TimePeriod.valueOf("HOUR"));
	}
}
