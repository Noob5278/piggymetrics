package com.piggymetrics.account.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TimePeriodTest {

	@Test
	public void shouldExposeFiveEnumConstants() {
		TimePeriod[] values = TimePeriod.values();
		assertEquals(5, values.length);
		assertTrue(Arrays.asList(values).containsAll(EnumSet.allOf(TimePeriod.class)));
	}

	@Test
	public void shouldContainAllExpectedConstants() {
		EnumSet<TimePeriod> all = EnumSet.allOf(TimePeriod.class);
		assertTrue(all.contains(TimePeriod.YEAR));
		assertTrue(all.contains(TimePeriod.QUARTER));
		assertTrue(all.contains(TimePeriod.MONTH));
		assertTrue(all.contains(TimePeriod.DAY));
		assertTrue(all.contains(TimePeriod.HOUR));
	}

	@Test
	public void shouldResolveByValueOf() {
		assertSame(TimePeriod.YEAR, TimePeriod.valueOf("YEAR"));
		assertSame(TimePeriod.QUARTER, TimePeriod.valueOf("QUARTER"));
		assertSame(TimePeriod.MONTH, TimePeriod.valueOf("MONTH"));
		assertSame(TimePeriod.DAY, TimePeriod.valueOf("DAY"));
		assertSame(TimePeriod.HOUR, TimePeriod.valueOf("HOUR"));
	}

	@Test
	public void shouldThrowForUnknownValue() {
		try {
			TimePeriod.valueOf("WEEK");
			fail("Expected IllegalArgumentException for unknown TimePeriod");
		} catch (IllegalArgumentException expected) {
			assertNotNull(expected.getMessage());
		}
	}
}
