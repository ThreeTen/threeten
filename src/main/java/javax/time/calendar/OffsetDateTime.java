/*
 * Copyright (c) 2007-2011, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar;

import java.io.Serializable;

import javax.time.CalendricalException;
import javax.time.Duration;
import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.MathUtils;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

/**
 * A date-time with a zone offset from UTC in the ISO-8601 calendar system,
 * such as {@code 2007-12-03T10:15:30+01:00}.
 * <p>
 * {@code OffsetDateTime} is an immutable representation of a date-time with an offset.
 * This class stores all date and time fields, to a precision of nanoseconds,
 * as well as the offset from UTC. Thus, for example, the value
 * "2nd October 2007 at 13:45.30.123456789 +02:00" can be stored in an {@code OffsetDateTime}.
 * <p>
 * OffsetDateTime is immutable and thread-safe.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
public final class OffsetDateTime
        implements Calendrical, InstantProvider, DateTimeProvider,
        CalendricalMatcher, DateAdjuster, TimeAdjuster,
        Comparable<OffsetDateTime>, Serializable {

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = -456761901L;

    /**
     * The local date-time.
     */
    private final LocalDateTime dateTime;
    /**
     * The zone offset.
     */
    private final ZoneOffset offset;

    //-----------------------------------------------------------------------
    /**
     * Gets the rule for {@code OffsetDateTime}.
     *
     * @return the rule for the date-time, not null
     */
    public static CalendricalRule<OffsetDateTime> rule() {
        return ISOCalendricalRule.OFFSET_DATE_TIME;
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains the current date-time from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current date-time.
     * The offset will be calculated from the time-zone in the clock.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @return the current date-time using the system clock, not null
     */
    public static OffsetDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current date-time from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current date-time.
     * The offset will be calculated from the time-zone in the clock.
     * <p>
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current date-time, not null
     */
    public static OffsetDateTime now(Clock clock) {
        ISOChronology.checkNotNull(clock, "Clock must not be null");
        final Instant now = clock.instant();  // called once
        return ofInstant(now, clock.getZone().getRules().getOffset(now));
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month and
     * day with the time set to midnight at the start of day.
     * <p>
     * The time fields will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime ofMidnight(
            int year, MonthOfYear monthOfYear, int dayOfMonth, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.ofMidnight(year, monthOfYear, dayOfMonth);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month and
     * day with the time set to midnight at the start of day.
     * <p>
     * The time fields will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime ofMidnight(
            int year, int monthOfYear, int dayOfMonth, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.ofMidnight(year, monthOfYear, dayOfMonth);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from a date with the
     * time set to midnight at the start of day.
     * <p>
     * The time fields will be set to zero by this factory method.
     *
     * @param dateProvider  the date provider to use, not null
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     */
    public static OffsetDateTime ofMidnight(DateProvider dateProvider, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.ofMidnight(dateProvider);
        return new OffsetDateTime(dt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month,
     * day, hour and minute, setting the second and nanosecond to zero.
     * <p>
     * The second and nanosecond fields will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime of(
            int year, MonthOfYear monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, monthOfYear, dayOfMonth,
                hourOfDay, minuteOfHour);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month,
     * day, hour, minute and second, setting the nanosecond to zero.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     * <p>
     * The nanosecond field will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime of(
            int year, MonthOfYear monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, monthOfYear, dayOfMonth,
                hourOfDay, minuteOfHour, secondOfMinute);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month,
     * day, hour, minute, second and nanosecond.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime of(
            int year, MonthOfYear monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, monthOfYear, dayOfMonth,
                hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond);
        return new OffsetDateTime(dt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month,
     * day, hour and minute, setting the second and nanosecond to zero.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     * <p>
     * The second and nanosecond fields will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime of(
            int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, monthOfYear, dayOfMonth,
                hourOfDay, minuteOfHour);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month,
     * day, hour, minute and second, setting the nanosecond to zero.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     * <p>
     * The nanosecond field will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime of(
            int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, monthOfYear, dayOfMonth,
                hourOfDay, minuteOfHour, secondOfMinute);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from year, month,
     * day, hour, minute, second and nanosecond.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static OffsetDateTime of(
            int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, monthOfYear, dayOfMonth,
                hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond);
        return new OffsetDateTime(dt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from a date, time and zone offset.
     *
     * @param dateProvider  the date provider to use, not null
     * @param timeProvider  the time provider to use, not null
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     */
    public static OffsetDateTime of(DateProvider dateProvider, TimeProvider timeProvider, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(dateProvider, timeProvider);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from a date-time provider.
     *
     * @param dateTimeProvider  the date-time provider to use, not null
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     */
    public static OffsetDateTime of(DateTimeProvider dateTimeProvider, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(dateTimeProvider);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from a {@code DateProvider}
     * and {@code OffsetTime}.
     *
     * @param dateProvider  the date provider to use, not null
     * @param offsetTime  the offset time to use, not null
     * @return the offset date-time, not null
     */
    public static OffsetDateTime of(DateProvider dateProvider, OffsetTime offsetTime) {
        LocalDateTime dt = LocalDateTime.of(dateProvider, offsetTime);
        return new OffsetDateTime(dt, offsetTime.getOffset());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from an {@code InstantProvider}
     * using the UTC offset.
     *
     * @param instantProvider  the instant to convert, not null
     * @return the offset date-time in UTC, not null
     * @throws CalendricalException if the instant exceeds the supported date range
     */
    public static OffsetDateTime ofInstantUTC(InstantProvider instantProvider) {
        return ofInstant(instantProvider, ZoneOffset.UTC);
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from an {@code InstantProvider}.
     *
     * @param instantProvider  the instant to convert, not null
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     * @throws CalendricalException if the instant exceeds the supported date range
     */
    public static OffsetDateTime ofInstant(InstantProvider instantProvider, ZoneOffset offset) {
        Instant instant = Instant.of(instantProvider);
        ISOChronology.checkNotNull(offset, "ZoneOffset must not be null");
        long localSeconds = instant.getEpochSecond() + offset.getAmountSeconds();  // overflow caught later
        LocalDateTime ldt = LocalDateTime.create(localSeconds, instant.getNanoOfSecond());
        return new OffsetDateTime(ldt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} using seconds from the
     * epoch of 1970-01-01T00:00:00Z.
     * <p>
     * The nanosecond field is set to zero.
     *
     * @param epochSecond  the number of seconds from the epoch of 1970-01-01T00:00:00Z
     * @return the offset date-time, not null
     * @throws CalendricalException if the result exceeds the supported range
     */
    public static OffsetDateTime ofEpochSecond(long epochSecond, ZoneOffset offset) {
        ISOChronology.checkNotNull(offset, "ZoneOffset must not be null");
        long localSeconds = epochSecond + offset.getAmountSeconds();  // overflow caught later
        LocalDateTime ldt = LocalDateTime.create(localSeconds, 0);
        return new OffsetDateTime(ldt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from a set of calendricals.
     * <p>
     * A calendrical represents some form of date and time information.
     * This method combines the input calendricals into a date-time.
     *
     * @param calendricals  the calendricals to create a date-time from, no nulls, not null
     * @return the offset date-time, not null
     * @throws CalendricalException if unable to merge to an offset date-time
     */
    public static OffsetDateTime from(Calendrical... calendricals) {
        return CalendricalNormalizer.merge(calendricals).deriveChecked(rule());
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from the normalized form.
     * <p>
     * This internal method is used by the associated rule.
     *
     * @param normalized  the normalized calendrical, not null
     * @return the offset date-time, null if unable to obtain the date-time
     */
    static OffsetDateTime deriveFrom(CalendricalNormalizer normalized) {
        LocalDateTime dateTime = LocalDateTime.deriveFrom(normalized);
        ZoneOffset offset = normalized.getOffset(true);
        if (dateTime == null || offset == null) {
            return null;
        }
        return new OffsetDateTime(dateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code OffsetDateTime} from a text string such as {@code 2007-12-03T10:15:30+01:00}.
     * <p>
     * The following formats are accepted in ASCII:
     * <ul>
     * <li>{@code {Year}-{MonthOfYear}-{DayOfMonth}T{Hour}:{Minute}{OffsetID}}
     * <li>{@code {Year}-{MonthOfYear}-{DayOfMonth}T{Hour}:{Minute}:{Second}{OffsetID}}
     * <li>{@code {Year}-{MonthOfYear}-{DayOfMonth}T{Hour}:{Minute}:{Second}.{NanosecondFraction}{OffsetID}}
     * </ul>
     * <p>
     * The year has between 4 and 10 digits with values from MIN_YEAR to MAX_YEAR.
     * If there are more than 4 digits then the year must be prefixed with the plus symbol.
     * Negative years are allowed, but not negative zero.
     * <p>
     * The month-of-year has 2 digits with values from 1 to 12.
     * <p>
     * The day-of-month has 2 digits with values from 1 to 31 appropriate to the month.
     * <p>
     * The hour has 2 digits with values from 0 to 23.
     * The minute has 2 digits with values from 0 to 59.
     * The second has 2 digits with values from 0 to 59.
     * The nanosecond fraction has from 1 to 9 digits with values from 0 to 999,999,999.
     * <p>
     * The offset ID is the normalized form as defined in {@link ZoneOffset}.
     *
     * @param text  the text to parse such as "2007-12-03T10:15:30+01:00", not null
     * @return the parsed offset date-time, not null
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static OffsetDateTime parse(CharSequence text) {
        return DateTimeFormatters.isoOffsetDateTime().parse(text, rule());
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from a text string using a specific formatter.
     * <p>
     * The text is parsed using the formatter, returning a date-time.
     *
     * @param text  the text to parse, not null
     * @param formatter  the formatter to use, not null
     * @return the parsed offset date-time, not null
     * @throws UnsupportedOperationException if the formatter cannot parse
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static OffsetDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        ISOChronology.checkNotNull(formatter, "DateTimeFormatter must not be null");
        return formatter.parse(text, rule());
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param dateTime  the date-time, not null
     * @param offset  the zone offset, not null
     */
    private OffsetDateTime(LocalDateTime dateTime, ZoneOffset offset) {
        if (dateTime == null) {
            throw new NullPointerException("The date-time must not be null");
        }
        if (offset == null) {
            throw new NullPointerException("The zone offset must not be null");
        }
        this.dateTime = dateTime;
        this.offset = offset;
    }

    /**
     * Returns a new date-time based on this one, returning {@code this} where possible.
     *
     * @param dateTime  the date-time to create with, not null
     * @param offset  the zone offset to create with, not null
     */
    private OffsetDateTime with(LocalDateTime dateTime, ZoneOffset offset) {
        if (this.dateTime == dateTime && this.offset.equals(offset)) {
            return this;
        }
        return new OffsetDateTime(dateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the specified calendrical rule.
     * <p>
     * This method queries the value of the specified calendrical rule.
     * If the value cannot be returned for the rule from this date-time then
     * {@code null} will be returned.
     *
     * @param ruleToDerive  the rule to derive, not null
     * @return the value for the rule, null if the value cannot be returned
     */
    @SuppressWarnings("unchecked")
    public <T> T get(CalendricalRule<T> ruleToDerive) {
        // optimize, especially for LocalDateTime, OffsetDate and OffsetTime
        if (ruleToDerive instanceof ISOCalendricalRule<?>) {
            switch (((ISOCalendricalRule<?>) ruleToDerive).ordinal) {
                case ISOCalendricalRule.LOCAL_DATE_ORDINAL: return (T) toLocalDate();
                case ISOCalendricalRule.LOCAL_TIME_ORDINAL: return (T) toLocalTime();
                case ISOCalendricalRule.LOCAL_DATE_TIME_ORDINAL: return (T) dateTime;
                case ISOCalendricalRule.OFFSET_DATE_ORDINAL: return (T) toOffsetDate();
                case ISOCalendricalRule.OFFSET_TIME_ORDINAL: return (T) toOffsetTime();
                case ISOCalendricalRule.OFFSET_DATE_TIME_ORDINAL: return (T) this;
                case ISOCalendricalRule.ZONE_OFFSET_ORDINAL: return (T) offset;
                case ISOCalendricalRule.CHRONOLOGY_ORDINAL: return (T) ISOChronology.INSTANCE;
            }
            return null;
        }
        return CalendricalNormalizer.derive(ruleToDerive, rule(), toLocalDate(), toLocalTime(), offset, null, ISOChronology.INSTANCE, null);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the time altered and the offset retained.
     * <p>
     * This method returns an object with the same {@code ZoneOffset} and the specified {@code LocalDateTime}.
     * No calculation is needed or performed.
     *
     * @param dateTimeProvider  the local date-time to change to, not null
     * @return an {@code OffsetDateTime} based on this time with the requested time, not null
     */
    public OffsetDateTime withDateTime(DateTimeProvider dateTimeProvider) {
        LocalDateTime localDateTime = LocalDateTime.of(dateTimeProvider);
        return localDateTime.equals(this.dateTime) ? this : new OffsetDateTime(localDateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the zone offset.
     *
     * @return the zone offset, not null
     */
    public ZoneOffset getOffset() {
        return offset;
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified offset ensuring
     * that the result has the same local date-time.
     * <p>
     * This method returns an object with the same {@code LocalDateTime} and the specified {@code ZoneOffset}.
     * No calculation is needed or performed.
     * For example, if this time represents {@code 2007-12-03T10:30+02:00} and the offset specified is
     * {@code +03:00}, then this method will return {@code 2007-12-03T10:30+03:00}.
     * <p>
     * To take into account the difference between the offsets, and adjust the time fields,
     * use {@link #withOffsetSameInstant}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the zone offset to change to, not null
     * @return an {@code OffsetDateTime} based on this date-time with the requested offset, not null
     */
    public OffsetDateTime withOffsetSameLocal(ZoneOffset offset) {
        return with(dateTime, offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified offset ensuring
     * that the result is at the same instant.
     * <p>
     * This method returns an object with the the specified {@code ZoneOffset} and a {@code LocalDateTime}
     * adjusted by the difference between the two offsets.
     * This will result in the old and new objects representing the same instant.
     * This is useful for finding the local time in a different offset.
     * For example, if this time represents {@code 2007-12-03T10:30+02:00} and the offset specified is
     * {@code +03:00}, then this method will return {@code 2007-12-03T11:30+03:00}.
     * <p>
     * To change the offset without adjusting the local time use {@link #withOffsetSameLocal}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the zone offset to change to, not null
     * @return an {@code OffsetDateTime} based on this date-time with the requested offset, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime withOffsetSameInstant(ZoneOffset offset) {
        if (offset.equals(this.offset)) {
            return this;
        }
        int difference = offset.getAmountSeconds() - this.offset.getAmountSeconds();
        LocalDateTime adjusted = dateTime.plusSeconds(difference);
        return new OffsetDateTime(adjusted, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the year field.
     * <p>
     * This method returns the primitive {@code int} value for the year.
     * Additional information about the year can be obtained by creating a {@link Year}.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    public int getYear() {
        return dateTime.getYear();
    }

    /**
     * Gets the month-of-year field, which is an enum {@code MonthOfYear}.
     * <p>
     * This method returns the enum {@link MonthOfYear} for the month.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link MonthOfYear#getValue() int value}.
     * <p>
     * Additional information can be obtained from the {@code MonthOfYear}.
     * This includes month lengths, textual names and access to the quarter-of-year
     * and month-of-quarter values.
     *
     * @return the month-of-year, not null
     */
    public MonthOfYear getMonthOfYear() {
        return dateTime.getMonthOfYear();
    }

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    public int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    /**
     * Gets the day-of-year field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    public int getDayOfYear() {
        return dateTime.getDayOfYear();
    }

    /**
     * Gets the day-of-week field, which is an enum {@code DayOfWeek}.
     * <p>
     * This method returns the enum {@link DayOfWeek} for the day-of-week.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link DayOfWeek#getValue() int value}.
     * <p>
     * Additional information can be obtained from the {@code DayOfWeek}.
     * This includes textual names of the values.
     *
     * @return the day-of-week, not null
     */
    public DayOfWeek getDayOfWeek() {
        return dateTime.getDayOfWeek();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    public int getHourOfDay() {
        return dateTime.getHourOfDay();
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    public int getMinuteOfHour() {
        return dateTime.getMinuteOfHour();
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    public int getSecondOfMinute() {
        return dateTime.getSecondOfMinute();
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    public int getNanoOfSecond() {
        return dateTime.getNanoOfSecond();
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the year is a leap year, according to the ISO proleptic
     * calendar system rules.
     * <p>
     * This method applies the current rules for leap years across the whole time-line.
     * In general, a year is a leap year if it is divisible by four without
     * remainder. However, years divisible by 100, are not leap years, with
     * the exception of years divisible by 400 which are.
     * <p>
     * For example, 1904 is a leap year it is divisible by 4.
     * 1900 was not a leap year as it is divisible by 100, however 2000 was a
     * leap year as it is divisible by 400.
     * <p>
     * The calculation is proleptic - applying the same rules into the far future and far past.
     * This is historically inaccurate, but is correct for the ISO-8601 standard.
     *
     * @return true if the year is leap, false otherwise
     */
    public boolean isLeapYear() {
        return dateTime.isLeapYear();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this OffsetDateTime with the date altered using the adjuster.
     * <p>
     * Adjusters can be used to alter the date in various ways.
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date to the last day of the month.
     * <p>
     * The offset and time do not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster  the adjuster to use, not null
     * @return an {@code OffsetDateTime} based on this date-time with the date adjusted, not null
     * @throws NullPointerException if the adjuster returned null
     */
    public OffsetDateTime with(DateAdjuster adjuster) {
        return with(dateTime.with(adjuster), offset);
    }

    /**
     * Returns a copy of this OffsetDateTime with the time altered using the adjuster.
     * <p>
     * Adjusters can be used to alter the time in various ways.
     * A simple adjuster might simply set the one of the fields, such as the hour field.
     * A more complex adjuster might set the time to end of the working day.
     * <p>
     * The offset and date do not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster  the adjuster to use, not null
     * @return an {@code OffsetDateTime} based on this date-time with the time adjusted, not null
     * @throws IllegalArgumentException if the adjuster returned null
     */
    public OffsetDateTime with(TimeAdjuster adjuster) {
        return with(dateTime.with(adjuster), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the year altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@link DateResolvers#previousValid()}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This method does the same as {@code withYear(year, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @return an {@code OffsetDateTime} based on this date-time with the requested year, not null
     * @throws IllegalCalendarFieldValueException if the year value is invalid
     */
    public OffsetDateTime withYear(int year) {
        return with(dateTime.withYear(year), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the year altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@code dateResolver}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the requested year, not null
     * @throws IllegalCalendarFieldValueException if the year value is invalid
     */
    public OffsetDateTime withYear(int year, DateResolver dateResolver) {
        return with(dateTime.withYear(year, dateResolver), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the month-of-year altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@link DateResolvers#previousValid()}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This method does the same as {@code withMonthOfYear(monthOfYear, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @return an {@code OffsetDateTime} based on this date-time with the requested month, not null
     * @throws IllegalCalendarFieldValueException if the month-of-year value is invalid
     */
    public OffsetDateTime withMonthOfYear(int monthOfYear) {
        return with(dateTime.withMonthOfYear(monthOfYear), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the month-of-year altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@code dateResolver}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the requested month, not null
     * @throws IllegalCalendarFieldValueException if the month-of-year value is invalid
     */
    public OffsetDateTime withMonthOfYear(int monthOfYear, DateResolver dateResolver) {
        return with(dateTime.withMonthOfYear(monthOfYear, dateResolver), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the month-of-year altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@link DateResolvers#previousValid()}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This method does the same as {@code with(monthOfYear, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, not null
     * @return an {@code OffsetDateTime} based on this date-time with the requested month, not null
     */
    public OffsetDateTime with(MonthOfYear monthOfYear) {
        return with(dateTime.with(monthOfYear), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the month-of-year altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@code dateResolver}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, not null
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the requested month, not null
     */
    public OffsetDateTime with(MonthOfYear monthOfYear, DateResolver dateResolver) {
        return with(dateTime.with(monthOfYear, dateResolver), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the day-of-month altered.
     * If the resulting {@code OffsetDateTime} is invalid, an exception is thrown.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 28-31
     * @return an {@code OffsetDateTime} based on this date-time with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-month value is invalid
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public OffsetDateTime withDayOfMonth(int dayOfMonth) {
        return with(dateTime.withDayOfMonth(dayOfMonth), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the day-of-month altered.
     * If the resulting {@code OffsetDateTime} is invalid, it will be resolved using {@code dateResolver}.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 31
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-month value is invalid
     */
    public OffsetDateTime withDayOfMonth(int dayOfMonth, DateResolver dateResolver) {
        return with(dateTime.withDayOfMonth(dayOfMonth, dateResolver), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the day-of-year altered.
     * If the resulting {@code OffsetDateTime} is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 365-366
     * @return an {@code OffsetDateTime} based on this date with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-year value is invalid
     * @throws InvalidCalendarFieldException if the day-of-year is invalid for the year
     */
    public OffsetDateTime withDayOfYear(int dayOfYear) {
        return with(dateTime.withDayOfYear(dayOfYear), offset);
    }

//    //-----------------------------------------------------------------------
//    /**
//     * Returns a copy of this OffsetDateTime with a different local date.
//     * <p>
//     * This method changes the date stored to a different date.
//     * No calculation is performed. The result simply represents the same
//     * offset and the new date.
//     *
//     * @param dateProvider  the local date to change to, not null
//     * @return a new updated OffsetDateTime, not null
//     */
//    public OffsetDateTime withDate(DateProvider dateProvider) {
//        return with(dateTime.with(dateProvider), offset);
//    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the date values altered.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return an {@code OffsetDateTime} based on this date-time with the requested date, not null
     * @throws IllegalCalendarFieldValueException if any field value is invalid
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
      public OffsetDateTime withDate(int year, MonthOfYear monthOfYear, int dayOfMonth) {
          return with(dateTime.withDate(year, monthOfYear, dayOfMonth), offset);
      }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the date values altered.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_VALUE + 1 to MAX_VALUE
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return an {@code OffsetDateTime} based on this date-time with the requested date, not null
     * @throws IllegalCalendarFieldValueException if any field value is invalid
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public OffsetDateTime withDate(int year, int monthOfYear, int dayOfMonth) {
        return with(dateTime.withDate(year, monthOfYear, dayOfMonth), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the hour-of-day value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @return an {@code OffsetDateTime} based on this date-time with the requested hour, not null
     * @throws IllegalCalendarFieldValueException if the hour value is invalid
     */
    public OffsetDateTime withHourOfDay(int hourOfDay) {
        LocalDateTime newDT = dateTime.withHourOfDay(hourOfDay);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the minute-of-hour value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested minute, not null
     * @throws IllegalCalendarFieldValueException if the minute value is invalid
     */
    public OffsetDateTime withMinuteOfHour(int minuteOfHour) {
        LocalDateTime newDT = dateTime.withMinuteOfHour(minuteOfHour);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the second-of-minute value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested second, not null
     * @throws IllegalCalendarFieldValueException if the second value is invalid
     */
    public OffsetDateTime withSecondOfMinute(int secondOfMinute) {
        LocalDateTime newDT = dateTime.withSecondOfMinute(secondOfMinute);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the nano-of-second value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return an {@code OffsetDateTime} based on this date-time with the requested nanosecond, not null
     * @throws IllegalCalendarFieldValueException if the nanos value is invalid
     */
    public OffsetDateTime withNanoOfSecond(int nanoOfSecond) {
        LocalDateTime newDT = dateTime.withNanoOfSecond(nanoOfSecond);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the second and nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested time, not null
     * @throws IllegalCalendarFieldValueException if any field value is invalid
     */
    public OffsetDateTime withTime(int hourOfDay, int minuteOfHour) {
        LocalDateTime newDT = dateTime.withTime(hourOfDay, minuteOfHour);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested time, not null
     * @throws IllegalCalendarFieldValueException if any field value is invalid
     */
    public OffsetDateTime withTime(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        LocalDateTime newDT = dateTime.withTime(hourOfDay, minuteOfHour, secondOfMinute);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this OffsetDateTime with the time values altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return an {@code OffsetDateTime} based on this date-time with the requested time, not null
     * @throws IllegalCalendarFieldValueException if any field value is invalid
     */
    public OffsetDateTime withTime(int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond) {
        LocalDateTime newDT = dateTime.withTime(hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period added.
     * <p>
     * This adds the specified period to this date-time, returning a new date-time.
     * <p>
     * The detailed rules for the addition have some complexity due to variable length months.
     * See {@link LocalDateTime#plus(PeriodProvider)} for details.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param periodProvider  the period to add, not null
     * @return an {@code OffsetDateTime} based on this date-time with the period added, not null
     * @throws CalendricalException if the specified period cannot be converted to a {@code Period}
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plus(PeriodProvider periodProvider) {
        LocalDateTime newDT = dateTime.plus(periodProvider);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified duration added.
     * <p>
     * This adds the specified duration to this date-time, returning a new date-time.
     * <p>
     * The calculation is equivalent to using {@link #plusSeconds(long)} and
     * {@link #plusNanos(long)} on the two parts of the duration.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to add, not null
     * @return an {@code OffsetDateTime} based on this date-time with the duration added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plus(Duration duration) {
        LocalDateTime newDT = dateTime.plus(duration);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in years added.
     * <p>
     * This method adds the specified amount to the years field in three steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) plus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This method does the same as {@code plusYears(years, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the years added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #plusYears(long, javax.time.calendar.DateResolver)
     */
    public OffsetDateTime plusYears(long years) {
        LocalDateTime newDT = dateTime.plusYears(years);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in years added.
     * <p>
     * This method adds the specified amount to the years field in three steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the date using {@code dateResolver} if necessary</li>
     * </ol>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the years added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusYears(long years, DateResolver dateResolver) {
        LocalDateTime newDT = dateTime.plusYears(years, dateResolver);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in months added.
     * <p>
     * This method adds the specified amount to the months field in three steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 plus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This method does the same as {@code plusMonths(months, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the months added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #plusMonths(long, javax.time.calendar.DateResolver)
     */
    public OffsetDateTime plusMonths(long months) {
        LocalDateTime newDT = dateTime.plusMonths(months);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in months added.
     * <p>
     * This method adds the specified amount to the months field in three steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the date using {@code dateResolver} if necessary</li>
     * </ol>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the months added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusMonths(long months, DateResolver dateResolver) {
        LocalDateTime newDT = dateTime.plusMonths(months);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified period in weeks added.
     * <p>
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one week would result in the 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the weeks added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusWeeks(long weeks) {
        LocalDateTime newDT = dateTime.plusWeeks(weeks);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified period in days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one day would result in the 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the days added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusDays(long days) {
        LocalDateTime newDT = dateTime.plusDays(days);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in hours added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the hours added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusHours(long hours) {
        LocalDateTime newDT = dateTime.plusHours(hours);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in minutes added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the minutes added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusMinutes(long minutes) {
        LocalDateTime newDT = dateTime.plusMinutes(minutes);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the seconds added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusSeconds(long seconds) {
        LocalDateTime newDT = dateTime.plusSeconds(seconds);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the nanoseconds added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime plusNanos(long nanos) {
        LocalDateTime newDT = dateTime.plusNanos(nanos);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period subtracted.
     * <p>
     * This subtracts the specified period from this date-time, returning a new date-time.
     * <p>
     * The detailed rules for the subtraction have some complexity due to variable length months.
     * See {@link LocalDateTime#minus(PeriodProvider)} for details.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param periodProvider  the period to subtract, not null
     * @return an {@code OffsetDateTime} based on this date-time with the period subtracted, not null
     * @throws CalendricalException if the specified period cannot be converted to a {@code Period}
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minus(PeriodProvider periodProvider) {
        LocalDateTime newDT = dateTime.minus(periodProvider);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified duration subtracted.
     * <p>
     * This subtracts the specified duration from this date-time, returning a new date-time.
     * <p>
     * The calculation is equivalent to using {@link #minusSeconds(long)} and
     * {@link #minusNanos(long)} on the two parts of the duration.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to subtract, not null
     * @return an {@code OffsetDateTime} based on this date-time with the duration subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minus(Duration duration) {
        LocalDateTime newDT = dateTime.minus(duration);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in years subtracted.
     * <p>
     * This method subtracts the specified amount from the years field in three steps:
     * <ol>
     * <li>Subtract the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This method does the same as {@code minusYears(years, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the years subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #minusYears(long, javax.time.calendar.DateResolver)
     */
    public OffsetDateTime minusYears(long years) {
        LocalDateTime newDT = dateTime.minusYears(years);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in years subtracted.
     * <p>
     * This method subtracts the specified amount from the years field in three steps:
     * <ol>
     * <li>Subtract the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the date using {@code dateResolver} if necessary</li>
     * </ol>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the years subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusYears(long years, DateResolver dateResolver) {
        LocalDateTime newDT = dateTime.minusYears(years, dateResolver);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in months subtracted.
     * <p>
     * This method subtracts the specified amount from the months field in three steps:
     * <ol>
     * <li>Subtract the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This method does the same as {@code minusMonts(months, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the months subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #minusMonths(long, javax.time.calendar.DateResolver)
     */
    public OffsetDateTime minusMonths(long months) {
        LocalDateTime newDT = dateTime.minusMonths(months);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in months subtracted.
     * <p>
     * This method subtracts the specified amount from the months field in three steps:
     * <ol>
     * <li>Subtract the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the date using {@code dateResolver} if necessary</li>
     * </ol>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return an {@code OffsetDateTime} based on this date-time with the months subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusMonths(long months, DateResolver dateResolver) {
        LocalDateTime newDT = dateTime.minusMonths(months);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in weeks subtracted.
     * <p>
     * This method subtracts the specified amount in weeks from the days field decrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one week would result in the 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the weeks subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusWeeks(long weeks) {
        LocalDateTime newDT = dateTime.minusWeeks(weeks);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in days subtracted.
     * <p>
     * This method subtracts the specified amount from the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one day would result in the 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the days subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusDays(long days) {
        LocalDateTime newDT = dateTime.minusDays(days);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in hours subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the hours subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusHours(long hours) {
        LocalDateTime newDT = dateTime.minusHours(hours);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in minutes subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the minutes subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusMinutes(long minutes) {
        LocalDateTime newDT = dateTime.minusMinutes(minutes);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in seconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the seconds subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusSeconds(long seconds) {
        LocalDateTime newDT = dateTime.minusSeconds(seconds);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in nanoseconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the nanoseconds subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public OffsetDateTime minusNanos(long nanos) {
        LocalDateTime newDT = dateTime.minusNanos(nanos);
        return (newDT == dateTime ? this : new OffsetDateTime(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether this {@code OffsetDateTime} matches the specified matcher.
     * <p>
     * Matchers can be used to query the date-time.
     * A simple matcher might simply compare one of the fields, such as the year field.
     * A more complex matcher might check if the date is the last day of the month.
     *
     * @param matcher  the matcher to use, not null
     * @return true if this date-time matches the matcher, false otherwise
     */
    public boolean matches(CalendricalMatcher matcher) {
        return matcher.matchesCalendrical(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the date-time extracted from the calendrical matches this.
     * <p>
     * This method implements the {@code CalendricalMatcher} interface.
     * It is intended that applications use {@link #matches} rather than this method.
     *
     * @param calendrical  the calendrical to match, not null
     * @return true if the calendrical matches, false otherwise
     */
    public boolean matchesCalendrical(Calendrical calendrical) {
        return this.equals(calendrical.get(rule()));
    }

    /**
     * Adjusts a date to have the value of the date part of this object.
     * <p>
     * This method implements the {@code DateAdjuster} interface.
     * It is intended that applications use {@link #with(DateAdjuster)} rather than this method.
     *
     * @param date  the date to be adjusted, not null
     * @return the adjusted date, not null
     */
    public LocalDate adjustDate(LocalDate date) {
        return dateTime.adjustDate(date);
    }

    /**
     * Adjusts a time to have the value of the time part of this object.
     * <p>
     * This method implements the {@code TimeAdjuster} interface.
     * It is intended that applications use {@link #with(TimeAdjuster)} rather than this method.
     *
     * @param time  the time to be adjusted, not null
     * @return the adjusted time, not null
     */
    public LocalTime adjustTime(LocalTime time) {
        return dateTime.adjustTime(time);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a zoned date-time formed from the instant represented by this
     * date-time and the specified time-zone.
     * <p>
     * This conversion will ignore the visible local date-time and use the underlying instant instead.
     * This avoids any problems with local time-line gaps or overlaps.
     * The result might have different values for fields such as hour, minute an even day.
     * <p>
     * To attempt to retain the values of the fields, use {@link #atZoneSimilarLocal(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    public ZonedDateTime atZoneSameInstant(ZoneId zone) {
        return ZonedDateTime.ofInstant(this, zone);
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. As a result, this method can only convert the date-time
     * to the same time if the time-zone rules permit it. If not then a similar time is returned.
     * <p>
     * This method uses the {@link ZoneResolvers#postTransition() post transition} rules
     * to determine what to do when a gap or overlap occurs. These rules select the
     * date-time immediately after a gap and the later offset in overlaps.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the earlier offset at overlaps then call
     * {@link ZonedDateTime#withEarlierOffsetAtOverlap()} immediately after this method.
     * Alternately, pass a specific resolver to {@link #atZoneSimilarLocal(ZoneId, ZoneResolver)}.
     * <p>
     * To create a zoned date-time at the same instant irrespective of the local time-line,
     * use {@link #atZoneSameInstant(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date and the earliest valid time for the zone, not null
     */
    public ZonedDateTime atZoneSimilarLocal(ZoneId zone) {
        return ZonedDateTime.of(this, zone, ZoneResolvers.postTransition());
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone
     * taking control of what occurs in time-line gaps and overlaps.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. As a result, this method can only convert the date-time
     * to the same time if the time-zone rules permit it. If not then a similar time is returned.
     * <p>
     * This method uses the specified resolver to determine what to do when a gap or overlap occurs.
     * <p>
     * To create a zoned date-time at the same instant irrespective of the local time-line,
     * use {@link #atZoneSameInstant(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @param resolver  the zone resolver to use for gaps and overlaps, not null
     * @return the zoned date-time formed from this date and the earliest valid time for the zone, not null
     * @throws CalendricalException if the date-time cannot be resolved
     */
    public ZonedDateTime atZoneSimilarLocal(ZoneId zone, ZoneResolver resolver) {
        return ZonedDateTime.of(this, zone, resolver);
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date-time to an {@code Instant}.
     *
     * @return an Instant representing the same instant, not null
     */
    public Instant toInstant() {
        return Instant.ofEpochSecond(toEpochSecond(), getNanoOfSecond());
    }

    /**
     * Converts this date-time to a {@code LocalDate}.
     *
     * @return a LocalDate representing the date fields of this date-time, not null
     */
    public LocalDate toLocalDate() {
        return dateTime.toLocalDate();
    }

    /**
     * Converts this date-time to a {@code LocalTime}.
     *
     * @return a LocalTime representing the time fields of this date-time, not null
     */
    public LocalTime toLocalTime() {
        return dateTime.toLocalTime();
    }

    /**
     * Converts this date-time to a {@code LocalDateTime}.
     *
     * @return a LocalDateTime representing the fields of this date-time, not null
     */
    public LocalDateTime toLocalDateTime() {
        return dateTime;
    }

    /**
     * Converts this date-time to an {@code OffsetDate}.
     *
     * @return an OffsetDate representing the date and offset, not null
     */
    public OffsetDate toOffsetDate() {
        return OffsetDate.of(dateTime, offset);
    }

    /**
     * Converts this date-time to an {@code OffsetTime}.
     *
     * @return an OffsetTime representing the time and offset, not null
     */
    public OffsetTime toOffsetTime() {
        return OffsetTime.of(dateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date-time to the number of seconds from the epoch
     * of 1970-01-01T00:00:00Z.
     * <p>
     * Instants on the time-line after the epoch are positive, earlier are negative.
     *
     * @return the number of seconds from the epoch of 1970-01-01T00:00:00Z
     */
    public long toEpochSecond() {
        long epochDay = dateTime.toLocalDate().toEpochDay();
        long secs = epochDay * ISOChronology.SECONDS_PER_DAY + dateTime.toLocalTime().toSecondOfDay();
        secs -= offset.getAmountSeconds();
        return secs;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this {@code OffsetDateTime} to another date-time.
     * <p>
     * The comparison is based on the instant then local date-time.
     * This ordering is consistent with {@code equals()}.
     * For example, the following is the comparator order:
     * <ol>
     * <li>{@code 2008-12-03T10:30+01:00}</li>
     * <li>{@code 2008-12-03T11:00+01:00}</li>
     * <li>{@code 2008-12-03T12:00+02:00}</li>
     * <li>{@code 2008-12-03T11:30+01:00}</li>
     * <li>{@code 2008-12-03T12:00+01:00}</li>
     * <li>{@code 2008-12-03T12:30+01:00}</li>
     * </ol>
     * Values #2 and #3 represent the same instant on the time-line.
     * When two values represent the same instant, the local date-time is compared
     * to distinguish them. This step is needed to make the ordering
     * consistent with {@code equals()}.
     *
     * @param other  the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    public int compareTo(OffsetDateTime other) {
        if (offset.equals(other.offset)) {
            return dateTime.compareTo(other.dateTime);
        }
        int compare = MathUtils.safeCompare(toEpochSecond(), other.toEpochSecond());
        if (compare == 0) {
            compare = MathUtils.safeCompare(getNanoOfSecond(), other.getNanoOfSecond());
            if (compare == 0) {
                compare = dateTime.compareTo(other.dateTime);
            }
        }
        return compare;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the instant of this {@code OffsetDateTime} is after that of the specified date-time.
     * <p>
     * This method differs from the comparison in {@link #compareTo} and {@link #equals} in that it
     * only compares the instant of the date-time. This is equivalent to using
     * {@code dateTime1.toInstant().isAfter(dateTime2.toInstant());}.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is after the instant of the specified date-time
     */
    public boolean isAfter(OffsetDateTime other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec > otherEpochSec ||
            (thisEpochSec == otherEpochSec && getNanoOfSecond() > other.getNanoOfSecond());
    }

    /**
     * Checks if the instant of this {@code OffsetDateTime} is before that of the specified date-time.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the instant of the date-time. This is equivalent to using
     * {@code dateTime1.toInstant().isBefore(dateTime2.toInstant());}.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is before the instant of the specified date-time
     */
    public boolean isBefore(OffsetDateTime other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec < otherEpochSec ||
            (thisEpochSec == otherEpochSec && getNanoOfSecond() < other.getNanoOfSecond());
    }

    /**
     * Checks if the instant of this {@code OffsetDateTime} is equal to that of the specified date-time.
     * <p>
     * This method differs from the comparison in {@link #compareTo} and {@link #equals}
     * in that it only compares the instant of the date-time. This is equivalent to using
     * {@code dateTime1.toInstant().equals(dateTime2.toInstant());}.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if the instant equals the instant of the specified date-time
     */
    public boolean equalInstant(OffsetDateTime other) {
        return toEpochSecond() == other.toEpochSecond() &&
            getNanoOfSecond() == other.getNanoOfSecond();
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this date-time is equal to another date-time.
     * <p>
     * The comparison is based on the local date-time and the offset.
     * To compare for the same instant on the time-line, use {@link #equalInstant}.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other date-time
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof OffsetDateTime) {
            OffsetDateTime other = (OffsetDateTime) obj;
            return dateTime.equals(other.dateTime) && offset.equals(other.offset);
        }
        return false;
    }

    /**
     * A hash code for this date-time.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return dateTime.hashCode() ^ offset.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this date-time as a {@code String}, such as {@code 2007-12-03T10:15:30+01:00}.
     * <p>
     * The output will be one of the following ISO-8601 formats:
     * <ul>
     * <li>{@code yyyy-MM-dd'T'HH:mmXXXXX}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssXXXXX}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssfnnnXXXXX}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssfnnnnnnXXXXX}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssfnnnnnnnnnXXXXX}</li>
     * </ul>
     * The format used will be the shortest that outputs the full value of
     * the time where the omitted parts are implied to be zero.
     *
     * @return a string representation of this date-time, not null
     */
    @Override
    public String toString() {
        return dateTime.toString() + offset.toString();
    }

    /**
     * Outputs this date-time as a {@code String} using the formatter.
     *
     * @param formatter  the formatter to use, not null
     * @return the formatted date-time string, not null
     * @throws UnsupportedOperationException if the formatter cannot print
     * @throws CalendricalException if an error occurs during printing
     */
    public String toString(DateTimeFormatter formatter) {
        ISOChronology.checkNotNull(formatter, "DateTimeFormatter must not be null");
        return formatter.print(this);
    }

}
