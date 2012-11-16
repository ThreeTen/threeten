/*
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.chrono;

import static javax.time.DateTimeConstants.HOURS_PER_DAY;
import static javax.time.DateTimeConstants.MICROS_PER_DAY;
import static javax.time.DateTimeConstants.MILLIS_PER_DAY;
import static javax.time.DateTimeConstants.MINUTES_PER_DAY;
import static javax.time.DateTimeConstants.NANOS_PER_DAY;
import static javax.time.DateTimeConstants.NANOS_PER_HOUR;
import static javax.time.DateTimeConstants.NANOS_PER_MINUTE;
import static javax.time.DateTimeConstants.NANOS_PER_SECOND;
import static javax.time.DateTimeConstants.SECONDS_PER_DAY;
import static javax.time.calendrical.ChronoField.EPOCH_DAY;

import java.io.Serializable;
import java.util.Objects;

import javax.time.DateTimeException;
import javax.time.DayOfWeek;
import javax.time.LocalTime;
import javax.time.ZoneId;
import javax.time.ZoneOffset;
import javax.time.calendrical.ChronoField;
import javax.time.calendrical.ChronoUnit;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.PeriodUnit;
import javax.time.jdk8.DefaultInterfaceChronoLocalDateTime;
import javax.time.jdk8.Jdk8Methods;
import javax.time.zone.ZoneResolver;
import javax.time.zone.ZoneResolvers;

/**
 * A date-time without a time-zone for the calendar neutral API.
 * <p>
 * {@code ChronoLocalDateTime} is an immutable date-time object that represents a date-time, often
 * viewed as year-month-day-hour-minute-second. This object can also access other
 * fields such as day-of-year, day-of-week and week-of-year.
 * <p>
 * This class stores all date and time fields, to a precision of nanoseconds.
 * It does not store or represent a time-zone. For example, the value
 * "2nd October 2007 at 13:45.30.123456789" can be stored in an {@code ChronoLocalDateTime}.
 *
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 *
 * @param <C> the chronology of this date
 */
class ChronoDateTimeImpl<C extends Chrono<C>>
        extends DefaultInterfaceChronoLocalDateTime<C>
        implements  ChronoLocalDateTime<C>, DateTime, WithAdjuster, Serializable {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 4556003607393004514L;

    /**
     * The date part.
     */
    private final ChronoLocalDate<C> date;

    /**
     * The time part.
     */
    private final LocalTime time;

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code ChronoLocalDateTime} from a date and time.
     *
     * @param date  the local date, not null
     * @param time  the local time, not null
     * @return the local date-time, not null
     */
    static <R extends Chrono<R>> ChronoDateTimeImpl<R> of(ChronoLocalDate<R> date, LocalTime time) {
        return new ChronoDateTimeImpl<>(date, time);
    }

    /**
     * Constructor.
     *
     * @param date  the date part of the date-time, not null
     * @param time  the time part of the date-time, not null
     */
    protected ChronoDateTimeImpl(ChronoLocalDate<C> date, LocalTime time) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(time, "time");
        this.date = date;
        this.time = time;
    }

    /**
     * Returns a copy of this date-time with the new date and time, checking
     * to see if a new object is in fact required.
     *
     * @param newDate  the date of the new date-time, not null
     * @param newTime  the time of the new date-time, not null
     * @return the date-time, not null
     */
    @SuppressWarnings("unchecked")
    private ChronoDateTimeImpl<C> with(DateTime newDate, LocalTime newTime) {
        if (date == newDate && time == newTime) {
            return (ChronoDateTimeImpl<C>) this;
        }
        // Validate that the new DateTime is a ChronoLocalDate (and not something else)
        ChronoLocalDate<C> cd = date.getChrono().ensureChronoLocalDate(newDate);
        return new ChronoDateTimeImpl<>(cd, newTime);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(DateTimeField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return f.isDateField() || f.isTimeField();
        }
        return field != null && field.doIsSupported(this);
    }

    @Override
    public DateTimeValueRange range(DateTimeField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return (f.isTimeField() ? time.range(field) : date.range(field));
        }
        return field.doRange(this);
    }

    @Override
    public int get(DateTimeField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return (f.isTimeField() ? time.get(field) : date.get(field));
        }
        return range(field).checkValidIntValue(getLong(field), field);
    }

    @Override
    public long getLong(DateTimeField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return (f.isTimeField() ? time.getLong(field) : date.getLong(field));
        }
        return field.doGet(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the year field.
     * <p>
     * This method returns the primitive {@code int} value for the year.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    int getYear() {
        return date.get(ChronoField.YEAR_OF_ERA);
    }

    /**
     * Gets the month-of-year field from 1 to 12 or 13 depending on the chronology.
     * *
     * @return the month-of-year, from 1 to 12 or 13
     */
    int getMonthValue() {
        return date.get(ChronoField.MONTH_OF_YEAR);
    }

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    int getDayOfMonth() {
        return date.get(ChronoField.DAY_OF_MONTH);
    }

    /**
     * Gets the day-of-year field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    int getDayOfYear() {
        return date.get(ChronoField.DAY_OF_YEAR);
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
    DayOfWeek getDayOfWeek() {
        return DayOfWeek.of(date.get(ChronoField.DAY_OF_WEEK));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    int getHour() {
        return time.getHour();
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    int getMinute() {
        return time.getMinute();
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    int getSecond() {
        return time.getSecond();
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    int getNano() {
        return time.getNano();
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public ChronoDateTimeImpl<C> with(WithAdjuster adjuster) {
        if (adjuster instanceof ChronoLocalDate) {
            // The Chrono is checked in with(date,time)
            return with((ChronoLocalDate<C>) adjuster, time);
        } else if (adjuster instanceof LocalTime) {
            return with(date, (LocalTime) adjuster);
        } else if (adjuster instanceof ChronoDateTimeImpl) {
            return date.getChrono().ensureChronoLocalDateTime((ChronoDateTimeImpl<?>) adjuster);
        }
        return date.getChrono().ensureChronoLocalDateTime((ChronoDateTimeImpl<?>) adjuster.doWithAdjustment(this));
    }

    @Override
    public ChronoDateTimeImpl<C> with(DateTimeField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (f.isTimeField()) {
                return with(date, time.with(field, newValue));
            } else {
                return with(date.with(field, newValue), time);
            }
        }
        return date.getChrono().ensureChronoLocalDateTime(field.doSet(this, newValue));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the year altered.
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    ChronoDateTimeImpl<C> withYear(int year) {
        return with(date.with(ChronoField.YEAR_OF_ERA, year), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the month-of-year altered.
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    ChronoDateTimeImpl<C> withMonth(int month) {
        return with(date.with(ChronoField.MONTH_OF_YEAR, month), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the day-of-month altered.
     * If the resulting {@code ChronoLocalDateTime} is invalid, an exception is thrown.
     * The time does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 28-31
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     */
    ChronoDateTimeImpl<C> withDayOfMonth(int dayOfMonth) {
        return with(date.with(ChronoField.DAY_OF_MONTH, dayOfMonth), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the day-of-year altered.
     * If the resulting {@code ChronoLocalDateTime} is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 365-366
     * @return a {@code ChronoLocalDateTime} based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid
     * @throws DateTimeException if the day-of-year is invalid for the year
     */
    ChronoDateTimeImpl<C> withDayOfYear(int dayOfYear) {
        return with(date.with(ChronoField.DAY_OF_YEAR, dayOfYear), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the date values altered.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested date, not null
     * @throws DateTimeException if any field value is invalid
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     */
    ChronoDateTimeImpl<C> withDate(int year, int month, int dayOfMonth) {
        if (year == getYear() &&
                month == getMonthValue() &&
                dayOfMonth == getDayOfMonth()) {
            return this;
        }
        return withYear(year).withMonth(month).withDayOfMonth(dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the hour-of-day value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    ChronoDateTimeImpl<C> withHour(int hour) {
        LocalTime newTime = time.withHour(hour);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the minute-of-hour value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    ChronoDateTimeImpl<C> withMinute(int minute) {
        LocalTime newTime = time.withMinute(minute);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the second-of-minute value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    ChronoDateTimeImpl<C> withSecond(int second) {
        LocalTime newTime = time.withSecond(second);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the nano-of-second value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nano value is invalid
     */
    ChronoDateTimeImpl<C> withNano(int nanoOfSecond) {
        LocalTime newTime = time.withNano(nanoOfSecond);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the second and nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    ChronoDateTimeImpl<C> withTime(int hour, int minute) {
        return withTime(hour, minute, 0, 0);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    ChronoDateTimeImpl<C> withTime(int hour, int minute, int second) {
        return withTime(hour, minute, second, 0);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return a {@code ChronoLocalDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    ChronoDateTimeImpl<C> withTime(int hour, int minute, int second, int nanoOfSecond) {
        if (hour == getHour() && minute == getMinute() &&
                second == getSecond() && nanoOfSecond == getNano()) {
            return this;
        }
        LocalTime newTime = LocalTime.of(hour, minute, second, nanoOfSecond);
        return with(date, newTime);
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoDateTimeImpl<C> plus(long amountToAdd, PeriodUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case NANOS: return plusNanos(amountToAdd);
                case MICROS: return plusDays(amountToAdd / MICROS_PER_DAY).plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
                case MILLIS: return plusDays(amountToAdd / MILLIS_PER_DAY).plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000000);
                case SECONDS: return plusSeconds(amountToAdd);
                case MINUTES: return plusMinutes(amountToAdd);
                case HOURS: return plusHours(amountToAdd);
                case HALF_DAYS: return plusDays(amountToAdd / 256).plusHours((amountToAdd % 256) * 12);  // no overflow (256 is multiple of 2)
            }
            return with(date.plus(amountToAdd, unit), time);
        }
        return date.getChrono().ensureChronoLocalDateTime(unit.doAdd(this, amountToAdd));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in years added.
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
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusYears(long years) {
        return with(date.plus(years, ChronoUnit.YEARS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in months added.
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
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusMonths(long months) {
        return with(date.plus(months, ChronoUnit.MONTHS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in weeks added.
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
     * @return a {@code ChronoLocalDateTime} based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusWeeks(long weeks) {
        return with(date.plus(weeks, ChronoUnit.WEEKS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in days added.
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
     * @return a {@code ChronoLocalDateTime} based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusDays(long days) {
        return with(date.plus(days, ChronoUnit.DAYS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in hours added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusHours(long hours) {
        return plusWithOverflow(date, hours, 0, 0, 0, 1);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in minutes added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusMinutes(long minutes) {
        return plusWithOverflow(date, 0, minutes, 0, 0, 1);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusSeconds(long seconds) {
        return plusWithOverflow(date, 0, 0, seconds, 0, 1);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> plusNanos(long nanos) {
        return plusWithOverflow(date, 0, 0, 0, nanos, 1);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in years subtracted.
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
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusYears(long years) {
        return with(date.minus(years, ChronoUnit.YEARS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in months subtracted.
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
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusMonths(long months) {
        return with(date.minus(months, ChronoUnit.MONTHS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in weeks subtracted.
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
     * @return a {@code ChronoLocalDateTime} based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusWeeks(long weeks) {
        return with(date.minus(weeks, ChronoUnit.WEEKS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in days subtracted.
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
     * @return a {@code ChronoLocalDateTime} based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusDays(long days) {
        return with(date.minus(days, ChronoUnit.DAYS), time);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in hours subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusHours(long hours) {
        return plusWithOverflow(date, hours, 0, 0, 0, -1);
   }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in minutes subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusMinutes(long minutes) {
        return plusWithOverflow(date, 0, minutes, 0, 0, -1);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in seconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusSeconds(long seconds) {
        return plusWithOverflow(date, 0, 0, seconds, 0, -1);
    }

    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period in nanoseconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return a {@code ChronoLocalDateTime} based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoDateTimeImpl<C> minusNanos(long nanos) {
        return plusWithOverflow(date, 0, 0, 0, nanos, -1);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ChronoLocalDateTime} with the specified period added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param newDate  the new date to base the calculation on, not null
     * @param hours  the hours to add, may be negative
     * @param minutes the minutes to add, may be negative
     * @param seconds the seconds to add, may be negative
     * @param nanos the nanos to add, may be negative
     * @param sign  the sign to determine add or subtract
     * @return a long nanos-from-midnight value, holding the nano-of-day time and days overflow
     */
    private ChronoDateTimeImpl<C> plusWithOverflow(ChronoLocalDate<C> newDate, long hours, long minutes, long seconds, long nanos, int sign) {
        // 9223372036854775808 long, 2147483648 int
        if ((hours | minutes | seconds | nanos) == 0) {
            return with(newDate, time);
        }
        long totDays = nanos / NANOS_PER_DAY +             //   max/24*60*60*1B
                seconds / SECONDS_PER_DAY +                //   max/24*60*60
                minutes / MINUTES_PER_DAY +                //   max/24*60
                hours / HOURS_PER_DAY;                     //   max/24
        totDays *= sign;                                   // total max*0.4237...
        long totNanos = nanos % NANOS_PER_DAY +                    //   max  86400000000000
                (seconds % SECONDS_PER_DAY) * NANOS_PER_SECOND +   //   max  86400000000000
                (minutes % MINUTES_PER_DAY) * NANOS_PER_MINUTE +   //   max  86400000000000
                (hours % HOURS_PER_DAY) * NANOS_PER_HOUR;          //   max  86400000000000
        long curNoD = time.toNanoOfDay();                       //   max  86400000000000
        totNanos = totNanos * sign + curNoD;                    // total 432000000000000
        totDays += Jdk8Methods.floorDiv(totNanos, NANOS_PER_DAY);
        long newNoD = Jdk8Methods.floorMod(totNanos, NANOS_PER_DAY);
        LocalTime newTime = (newNoD == curNoD ? time : LocalTime.ofNanoOfDay(newNoD));
        return with(newDate.plus(totDays, ChronoUnit.DAYS), newTime);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an offset date-time formed from this date-time and the specified offset.
     * <p>
     * This merges the two objects - {@code this} and the specified offset -
     * to form an instance of {@code OffsetDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the offset to use, not null
     * @return the offset date-time formed from this date-time and the specified offset, not null
     */
    @Override
    public ChronoOffsetDateTime<C> atOffset(ZoneOffset offset) {
        return ChronoOffsetDateTimeImpl.of(this, offset);
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#postGapPreOverlap() post-gap pre-overlap} resolver.
     * This selects the date-time immediately after a gap and the earlier offset in overlaps.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * {@link javax.time.ZonedDateTime#withLaterOffsetAtOverlap()} immediately after this method.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    @Override
    public ChronoZonedDateTime<C> atZone(ZoneId zone) {
        return ChronoZonedDateTimeImpl.of(this, zone, ZoneResolvers.postGapPreOverlap());
    }


    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#postGapPreOverlap() post-gap pre-overlap} resolver.
     * This selects the date-time immediately after a gap and the earlier offset in overlaps.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * {@link javax.time.ZonedDateTime#withLaterOffsetAtOverlap()} immediately after this method.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    @Override
    public ChronoZonedDateTime<C> atZone(ZoneId zone, ZoneResolver resolver) {
        return ChronoZonedDateTimeImpl.of(this, zone, resolver);
    }

    //-----------------------------------------------------------------------
    @Override
    public long periodUntil(DateTime endDateTime, PeriodUnit unit) {
        if (endDateTime instanceof ChronoLocalDateTime == false) {
            throw new DateTimeException("Unable to calculate period between objects of two different types");
        }
        @SuppressWarnings("unchecked")
        ChronoLocalDateTime<C> end = (ChronoLocalDateTime<C>) endDateTime;
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            if (f.isTimeUnit()) {
                long amount = end.getLong(EPOCH_DAY) - date.getLong(EPOCH_DAY);
                switch (f) {
                    case NANOS: amount = Jdk8Methods.safeMultiply(amount, NANOS_PER_DAY); break;
                    case MICROS: amount = Jdk8Methods.safeMultiply(amount, MICROS_PER_DAY); break;
                    case MILLIS: amount = Jdk8Methods.safeMultiply(amount, MILLIS_PER_DAY); break;
                    case SECONDS: amount = Jdk8Methods.safeMultiply(amount, SECONDS_PER_DAY); break;
                    case MINUTES: amount = Jdk8Methods.safeMultiply(amount, MINUTES_PER_DAY); break;
                    case HOURS: amount = Jdk8Methods.safeMultiply(amount, HOURS_PER_DAY); break;
                    case HALF_DAYS: amount = Jdk8Methods.safeMultiply(amount, 2); break;
                }
                return Jdk8Methods.safeAdd(amount, time.periodUntil(end.getTime(), unit));
            }
            ChronoLocalDate<C> endDate = end.getDate();
            if (end.getTime().isBefore(time)) {
                endDate = endDate.minus(1, ChronoUnit.DAYS);
            }
            return date.periodUntil(endDate, unit);
        }
        return unit.between(this, endDateTime).getAmount();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the {@code ChronoLocalDate}.
     *
     * @return the ChronoLocalDate of this date-time, not null
     */
    @Override
    public ChronoLocalDate<C> getDate() {
        return date;
    }

    /**
     * Gets the {@code LocalTime}.
     *
     * @return the LocalTime of this date-time, not null
     */
    @Override
    public LocalTime getTime() {
        return time;
    }

}
