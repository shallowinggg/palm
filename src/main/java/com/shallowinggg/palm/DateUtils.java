package com.shallowinggg.palm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.shallowinggg.palm.PreConditions.checkArgument;
import static com.shallowinggg.palm.PreConditions.checkNotNull;

/**
 * Convenient utility class for {@code Date}.
 *
 * @author ding shimin
 */
public final class DateUtils {

    private static final String FORMATTER_PROPERTY = "io.github.shallowinggg.palm.defaultDateTimeFormatter";

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER;

    static {
        String formatter = SystemPropertyUtils.get(FORMATTER_PROPERTY, "yyyy-MM-dd HH:mm:ss");
        DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(formatter);
    }

    /**
     * Return now time.
     *
     * @return now time
     */
    public static Date now() {
        return new Date();
    }

    /**
     * Return date that before now in n days.
     *
     * @param days days to calc
     * @return date that before now in n days
     */
    public static Date nDaysBeforeNow(int days) {
        return dateBeforeNow(0, days);
    }

    /**
     * Return date that before now in n months.
     *
     * @param months months to calc
     * @return date that before now in n months
     */
    public static Date nMonthsBeforeNow(int months) {
        return dateBeforeNow(months, 0);
    }

    /**
     * Return date that before now in n months and n days.
     *
     * @param months months to calc
     * @param days   days to calc
     * @return date that before now in n months and n days
     */
    public static Date dateBeforeNow(int months, int days) {
        checkArgument((months & days) >= 0, "months or days must be positive or 0");

        return toDate(LocalDateTime.now().minusMonths(months).minusDays(days));
    }

    /**
     * Return date that after now in n days.
     *
     * @param days days to calc
     * @return date that after now in n days
     */
    public static Date nDaysAfterNow(int days) {
        checkArgument(days >= 0, "days must be positive or 0");

        return toDate(LocalDateTime.now().plusDays(days));
    }


    /**
     * Convert the given {@link LocalDateTime} instance to the millis representation.
     *
     * @param dateTime the {@link LocalDateTime} instance to convert
     * @return the millis representation
     */
    public static long toMillis(LocalDateTime dateTime) {
        checkNotNull(dateTime, "dateTime must not be null");
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Convert the given {@link LocalDate} instance to the millis representation.
     *
     * @param date the {@link LocalDate} instance to convert
     * @return the millis representation
     */
    public static long toMillis(LocalDate date) {
        checkNotNull(date, "date must not be null");
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Format the given {@link LocalDateTime} with the default formatter
     * {@code "yyyy-MM-dd HH:mm:ss"}.
     *
     * @param dateTime the {@link LocalDateTime} instance to convert
     * @return the millis representation
     * @see #DEFAULT_DATE_TIME_FORMATTER
     */
    public static String format(LocalDateTime dateTime) {
        checkNotNull(dateTime, "dateTime must not be null");
        return dateTime.format(DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * Convert the given {@link LocalDateTime} instance to the {@link Date} representation.
     *
     * @param dateTime the {@link LocalDateTime} instance to convert
     * @return the {@link Date} representation
     */
    public static Date toDate(LocalDateTime dateTime) {
        checkNotNull(dateTime, "dateTime must not be null");
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private DateUtils() {
    }
}
