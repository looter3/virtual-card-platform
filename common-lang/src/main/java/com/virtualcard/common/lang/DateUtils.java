package com.virtualcard.common.lang;

import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * @author Lorenzo Leccese
 *
 *         2 ago 2025
 *
 */
public class DateUtils {

	public static Instant getFirstDayOfTheMonth() {
		// Get system default zone
		final ZoneId zone = ZoneId.systemDefault();

		// Get first day of current month at start of day in that zone, then to Instant
		return YearMonth.now()
			.atDay(1)
			.atStartOfDay(zone)
			.toInstant();
	}

	public static Instant getLastDayOfTheMonth() {
		// Get system default zone
		final ZoneId zone = ZoneId.systemDefault();

		// Get last day of current month at end of day in that zone, then to Instant
		return YearMonth.now()
			.atEndOfMonth()
			.atTime(LocalTime.MAX)
			.atZone(zone)
			.toInstant();
	}
}
