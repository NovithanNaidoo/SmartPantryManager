package com.example.smartpantrymanager.logic;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Works out how close a pantry item is to its expiry date.
 *
 * Dates are stored as text in "yyyy-MM-dd" form, so they need parsing back into
 * a real date before they can be compared with today.
 */
public final class ExpiryHelper {

    /** Returned when an item has no expiry date, or the date cannot be read. */
    public static final long NO_EXPIRY = Long.MAX_VALUE;

    private ExpiryHelper() {
        // Utility class, never instantiated.
    }

    /**
     * How many days from today until the given date.
     *
     * 0 means it expires today, 2 means in two days, and a negative number means
     * it has already passed.
     */
    public static long daysUntil(@Nullable String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return NO_EXPIRY;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        Date expiry;
        try {
            expiry = format.parse(expiryDate.trim());
        } catch (ParseException e) {
            // Bad data should not crash the list, so treat it as having no expiry.
            return NO_EXPIRY;
        }

        if (expiry == null) {
            return NO_EXPIRY;
        }

        // Both dates are moved to midnight so the comparison counts whole days
        // rather than being thrown off by the time of day.
        Calendar today = atMidnight(Calendar.getInstance());

        Calendar target = Calendar.getInstance();
        target.setTime(expiry);
        target = atMidnight(target);

        long millisecondsApart = target.getTimeInMillis() - today.getTimeInMillis();
        return millisecondsApart / (1000 * 60 * 60 * 24);
    }

    /** True if the item expires within the given number of days, or already has. */
    public static boolean isExpiringSoon(@Nullable String expiryDate, int withinDays) {
        long days = daysUntil(expiryDate);

        if (days == NO_EXPIRY) {
            return false;
        }

        return days <= withinDays;
    }

    /** True if the date has already passed. */
    public static boolean isExpired(@Nullable String expiryDate) {
        long days = daysUntil(expiryDate);
        return days != NO_EXPIRY && days < 0;
    }

    private static Calendar atMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
