/*
 * Copyright (c) 2008-2010, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time;

import java.io.Serializable;

import javax.time.calendrical.LocalDateTimeField;
import javax.time.calendrical.LocalPeriodUnit;
import javax.time.format.CalendricalParseException;

/**
 * An immutable ISOPeriod consisting of the ISO-8601 year, month, day, hour,
 * minute, second and nanosecond units, such as '3 Months, 4 Days and 7 Hours'.
 * <p>
 * A ISOPeriod is a human-scale description of an amount of time.
 * This class represents the 7 standard definitions from {@link LocalDateTimeField}.
 * The ISOPeriod units used are 'Years', 'Months', 'Days', 'Hours', 'Minutes',
 * 'Seconds' and 'Nanoseconds'.
 * <p>
 * The {@code LocalPeriodUnit} defines a relationship between some of the units:
 * <ul>
 * <li>12 months in a year</li>
 * <li>24 hours in a day (ignoring time-zones)</li>
 * <li>60 minutes in an hour</li>
 * <li>60 seconds in a minute</li>
 * <li>1,000,000,000 nanoseconds in a second</li>
 * </ul>
 * The 24 hours in a day connection is not always true, due to time-zone changes.
 * As such, methods on this class make it clear when the that connection is being used.
 * <p>
 * ISOPeriod is immutable and thread-safe.
 *
 * @author Stephen Colebourne
 * @author Richard Warburton
 */
public final class ISOPeriod implements Serializable {

    /**
     * A constant for a ISOPeriod of zero.
     */
    public static final ISOPeriod ZERO = new ISOPeriod(0, 0, 0, 0, 0, 0, 0);
    /**
     * The serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The number of years.
     */
    private final int years;
    /**
     * The number of months.
     */
    private final int months;
    /**
     * The number of days.
     */
    private final int days;
    /**
     * The number of hours.
     */
    private final int hours;
    /**
     * The number of minutes.
     */
    private final int minutes;
    /**
     * The number of seconds.
     */
    private final int seconds;
    /**
     * The number of nanoseconds.
     */
    private final long nanos;
    /**
     * The cached toString value.
     */
    private transient volatile String string;

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from date-based and time-based fields.
     * <p>
     * This creates an instance based on years, months, days, hours, minutes and seconds.
     *
     * @param years  the amount of years, may be negative
     * @param months  the amount of months, may be negative
     * @param days  the amount of days, may be negative
     * @param hours  the amount of hours, may be negative
     * @param minutes  the amount of minutes, may be negative
     * @param seconds  the amount of seconds, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod of(int years, int months, int days, int hours, int minutes, int seconds) {
        return of(years, months, days, hours, minutes, seconds, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from date-based and time-based fields.
     * <p>
     * This creates an instance based on years, months, days, hours, minutes, seconds and nanoseconds.
     * The resulting ISOPeriod will have normalized seconds and nanoseconds.
     *
     * @param years  the amount of years, may be negative
     * @param months  the amount of months, may be negative
     * @param days  the amount of days, may be negative
     * @param hours  the amount of hours, may be negative
     * @param minutes  the amount of minutes, may be negative
     * @param seconds  the amount of seconds, may be negative
     * @param nanos  the amount of nanos, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod of(int years, int months, int days, int hours, int minutes, int seconds, long nanos) {
        if ((years | months | days | hours | minutes | seconds | nanos) == 0) {
            return ZERO;
        }
        return new ISOPeriod(years, months, days, hours, minutes, seconds, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from date-based fields.
     * <p>
     * This creates an instance based on years, months and days.
     *
     * @param years  the amount of years, may be negative
     * @param months  the amount of months, may be negative
     * @param days  the amount of days, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofDateFields(int years, int months, int days) {
        return of(years, months, days, 0, 0, 0, 0);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from time-based fields.
     * <p>
     * This creates an instance based on hours, minutes and seconds.
     *
     * @param hours  the amount of hours, may be negative
     * @param minutes  the amount of minutes, may be negative
     * @param seconds  the amount of seconds, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofTimeFields(int hours, int minutes, int seconds) {
        return of(0, 0, 0, hours, minutes, seconds, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from time-based fields.
     * <p>
     * This creates an instance based on hours, minutes, seconds and nanoseconds.
     *
     * @param hours  the amount of hours, may be negative
     * @param minutes  the amount of minutes, may be negative
     * @param seconds  the amount of seconds, may be negative
     * @param nanos  the amount of nanos, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofTimeFields(int hours, int minutes, int seconds, long nanos) {
        return of(0, 0, 0, hours, minutes, seconds, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from an amount and unit.
     * <p>
     * The parameters represent the two parts of a phrase like '6 Days'.
     * <p>
     * A {@code ISOPeriod} supports 7 units, ISO years, months, days, hours,
     * minutes, seconds and nanoseconds. The unit must be one of these, or be
     * able to be converted to one of these.
     *
     * @param amount  the amount of the ISOPeriod, measured in terms of the unit, positive or negative
     * @param unit  the unit that the ISOPeriod is measured in, not null
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod of(int amount, LocalPeriodUnit unit) {
    	switch (unit) {
		case CENTURIES: return ofYears(DateTimes.safeMultiply(amount, 100));
		case DAYS: return ofDays(amount);
		case DECADES: return ofYears(DateTimes.safeMultiply(amount, 10));
		case HALF_DAYS: return ofHours(DateTimes.safeMultiply(amount, 12));
		case HALF_YEARS: return ofMonths(DateTimes.safeMultiply(amount, 6));
		case HOURS: return ofHours(amount);
		case MICROS: return ofNanos(DateTimes.safeMultiply(amount, 10));
		case MILLENNIA: return ofYears(DateTimes.safeMultiply(amount, 1000));
		case MILLIS: return ofNanos(DateTimes.safeMultiply(amount, 100));
		case MINUTES: return ofMinutes(amount);
		case MONTHS: return ofMonths(amount);
		case NANOS: return ofNanos(amount);
		case QUARTER_YEARS: return ofMonths(DateTimes.safeMultiply(amount, 3));
		case SECONDS: return ofSeconds(amount);
		case WEEKS: return ofDays(DateTimes.safeMultiply(amount, 7));
		case YEARS: return ofYears(amount);
		default: throw new CalendricalException("Unable to create a period of: " + unit);
		}
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from a number of years.
     *
     * @param years  the amount of years, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofYears(int years) {
        if (years == 0) {
            return ZERO;
        }
        return new ISOPeriod(years, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from a number of months.
     *
     * @param months  the amount of months, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofMonths(int months) {
        if (months == 0) {
            return ZERO;
        }
        return new ISOPeriod(0, months, 0, 0, 0, 0, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from a number of days.
     *
     * @param days  the amount of days, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofDays(int days) {
        if (days == 0) {
            return ZERO;
        }
        return new ISOPeriod(0, 0, days, 0, 0, 0, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from a number of hours.
     *
     * @param hours  the amount of hours, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofHours(int hours) {
        if (hours == 0) {
            return ZERO;
        }
        return new ISOPeriod(0, 0, 0, hours, 0, 0, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from a number of minutes.
     *
     * @param minutes  the amount of minutes, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofMinutes(int minutes) {
        if (minutes == 0) {
            return ZERO;
        }
        return new ISOPeriod(0, 0, 0, 0, minutes, 0, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from a number of seconds.
     *
     * @param seconds  the amount of seconds, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofSeconds(int seconds) {
        if (seconds == 0) {
            return ZERO;
        }
        return new ISOPeriod(0, 0, 0, 0, 0, seconds, 0);
    }

    /**
     * Obtains a {@code ISOPeriod} from a number of nanoseconds.
     *
     * @param nanos  the amount of nanos, may be negative
     * @return the ISOPeriod, never null
     */
    public static ISOPeriod ofNanos(long nanos) {
        if (nanos == 0) {
            return ZERO;
        }
        return new ISOPeriod(0, 0, 0, 0, 0, 0, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from a {@code Duration}.
     * <p>
     * The created ISOPeriod will have normalized values for the hours, minutes,
     * seconds and nanoseconds fields. The years, months and days fields will be zero.
     * <p>
     * To populate the days field, call {@link #normalizedWith24HourDays()} on the created ISOPeriod.
     *
     * @param duration  the duration to create from, not null
     * @return the {@code ISOPeriodFields} instance, never null
     * @throws ArithmeticException if the result exceeds the supported ISOPeriod range
     */
    public static ISOPeriod of(Duration duration) {
        DateTimes.checkNotNull(duration, "Duration must not be null");
        if (duration.isZero()) {
            return ZERO;
        }
        int hours = DateTimes.safeToInt(duration.getSeconds() / 3600);
        int amount = (int) (duration.getSeconds() % 3600L);
        return new ISOPeriod(0, 0, 0, hours, amount / 60, amount % 60, duration.getNano());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} consisting of the number of days, months
     * and years between two dates.
     * <p>
     * The start date is included, but the end date is not. Only whole years count.
     * For example, from {@code 2010-01-15} to {@code 2011-03-18} is one year, two months and three days.
     * <p>
     * The result of this method can be a negative ISOPeriod if the end is before the start.
     * The negative sign will be the same in each of year, month and day.
     * <p>
     * Adding the result of this method to the start date will always yield the end date.
     *
     * @param startDateProvider  the start date, inclusive, not null
     * @param endDateProvider  the end date, exclusive, not null
     * @return the ISOPeriod in days, never null
     * @throws ArithmeticException if the ISOPeriod exceeds the supported range
     */
    public static ISOPeriod between(LocalDate startDate, LocalDate endDate) {
        long startMonth = startDate.getYear() * 12L + startDate.getMonthValue();  // safe
        long endMonth = endDate.getYear() * 12L + endDate.getMonthValue();  // safe
        long totalMonths = endMonth - startMonth;  // safe
        int days = endDate.getDayOfMonth() - startDate.getDayOfMonth();
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            LocalDate calcDate = startDate.plusMonths(totalMonths);
            days = (int) (endDate.toEpochDay() - calcDate.toEpochDay());  // safe
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= endDate.getMonth().length(endDate.isLeapYear());
        }
        long years = totalMonths / 12;  // safe
        int months = (int) (totalMonths % 12);  // safe
        return ofDateFields(DateTimes.safeToInt(years), months, days);
    }

    /**
     * Obtains a {@code ISOPeriod} consisting of the number of years between two dates.
     * <p>
     * The start date is included, but the end date is not. Only whole years count.
     * For example, from {@code 2010-01-15} to {@code 2012-01-15} is two years,
     * whereas from {@code 2010-01-15} to {@code 2012-01-14} is only one year.
     * <p>
     * The result of this method can be a negative ISOPeriod if the end is before the start.
     *
     * @param startDateProvider  the start date, inclusive, not null
     * @param endDateProvider  the end date, exclusive, not null
     * @return the ISOPeriod in days, never null
     * @throws ArithmeticException if the ISOPeriod exceeds the supported range
     */
    public static ISOPeriod yearsBetween(LocalDate startDate, LocalDate endDate) {
        long startMonth = startDate.getYear() * 12L + startDate.getMonthValue();  // safe
        long endMonth = endDate.getYear() * 12L + endDate.getMonthValue();  // safe
        long years = (endMonth - startMonth) / 12;  // safe
        if (endDate.getMonth() == startDate.getMonth()) {
            if (years > 0 && endDate.getDayOfMonth() < startDate.getDayOfMonth()) {
                years--;  // safe
            } else if (years < 0 && endDate.getDayOfMonth() > startDate.getDayOfMonth()) {
                years++;  // safe
            }
        }
        return ofYears(DateTimes.safeToInt(years));
    }

    /**
     * Obtains a {@code ISOPeriod} consisting of the number of months between two dates.
     * <p>
     * The start date is included, but the end date is not. Only whole months count.
     * For example, from {@code 2010-01-15} to {@code 2010-03-15} is two months,
     * whereas from {@code 2010-01-15} to {@code 2010-03-14} is only one month.
     * <p>
     * The result of this method can be a negative ISOPeriod if the end is before the start.
     *
     * @param startDateProvider  the start date, inclusive, not null
     * @param endDateProvider  the end date, exclusive, not null
     * @return the ISOPeriod in days, never null
     * @throws ArithmeticException if the ISOPeriod exceeds the supported range
     */
    public static ISOPeriod monthsBetween(LocalDate startDate, LocalDate endDate) {
        long startMonth = startDate.getYear() * 12L + startDate.getMonthValue();  // safe
        long endMonth = endDate.getYear() * 12L + endDate.getMonthValue();  // safe
        long months = endMonth - startMonth;  // safe
        if (months > 0 && endDate.getDayOfMonth() < startDate.getDayOfMonth()) {
            months--;  // safe
        } else if (months < 0 && endDate.getDayOfMonth() > startDate.getDayOfMonth()) {
            months++;  // safe
        }
        return ofMonths(DateTimes.safeToInt(months));
    }

    /**
     * Obtains a {@code ISOPeriod} consisting of the number of days between two dates.
     * <p>
     * The start date is included, but the end date is not. For example, from
     * {@code 2010-01-15} to {@code 2010-01-18} is three days.
     * <p>
     * The result of this method can be a negative ISOPeriod if the end is before the start.
     *
     * @param startDateProvider  the start date, inclusive, not null
     * @param endDateProvider  the end date, exclusive, not null
     * @return the ISOPeriod in days, never null
     * @throws ArithmeticException if the ISOPeriod exceeds the supported range
     */
    public static ISOPeriod daysBetween(LocalDate startDate, LocalDate endDate) {
        long days = DateTimes.safeSubtract(endDate.toEpochDay(), startDate.toEpochDay());
        return ofDays(DateTimes.safeToInt(days));
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a {@code ISOPeriod} from a text string such as {@code PnYnMnDTnHnMn.nS}.
     * <p>
     * This will parse the string produced by {@code toString()} which is
     * a subset of the ISO8601 ISOPeriod format {@code PnYnMnDTnHnMn.nS}.
     * <p>
     * The string consists of a series of numbers with a suffix identifying their meaning.
     * The values, and suffixes, must be in the sequence year, month, day, hour, minute, second.
     * Any of the number/suffix pairs may be omitted providing at least one is present.
     * If the ISOPeriod is zero, the value is normally represented as {@code PT0S}.
     * The numbers must consist of ASCII digits.
     * Any of the numbers may be negative. Negative zero is not accepted.
     * The number of nanoseconds is expressed as an optional fraction of the seconds.
     * There must be at least one digit before any decimal point.
     * There must be between 1 and 9 inclusive digits after any decimal point.
     * The letters will all be accepted in upper or lower case.
     * The decimal point may be either a dot or a comma.
     *
     * @param text  the text to parse, not null
     * @return the parsed ISOPeriod, never null
     * @throws CalendricalParseException if the text cannot be parsed to a ISOPeriod
     */
    public static ISOPeriod parse(final String text) {
        DateTimes.checkNotNull(text, "Text to parse must not be null");
        throw new UnsupportedOperationException("TODO"); // TODO
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param years  the amount
     * @param months  the amount
     * @param days  the amount
     * @param hours  the amount
     * @param minutes  the amount
     * @param seconds  the amount
     * @param nanos  the amount
     */
    private ISOPeriod(int years, int months, int days, int hours, int minutes, int seconds, long nanos) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.nanos = nanos;
    }

    /**
     * Resolves singletons.
     *
     * @return the resolved instance
     */
    private Object readResolve() {
        if ((years | months | days | hours | minutes | seconds | nanos) == 0) {
            return ZERO;
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this ISOPeriod is zero-length.
     *
     * @return true if this ISOPeriod is zero-length
     */
    public boolean isZero() {
        return (this == ZERO);
    }

    /**
     * Checks if this ISOPeriod is fully positive, excluding zero.
     * <p>
     * This checks whether all the amounts in the ISOPeriod are positive,
     * defined as greater than zero.
     *
     * @return true if this ISOPeriod is fully positive excluding zero
     */
    public boolean isPositive() {
        return ((years | months | days | hours | minutes | seconds | nanos) > 0);
    }

    /**
     * Checks if this ISOPeriod is fully positive, including zero.
     * <p>
     * This checks whether all the amounts in the ISOPeriod are positive,
     * defined as greater than or equal to zero.
     *
     * @return true if this ISOPeriod is fully positive including zero
     */
    public boolean isPositiveOrZero() {
        return ((years | months | days | hours | minutes | seconds | nanos) >= 0);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the amount of years of this ISOPeriod, if any.
     *
     * @return the amount of years of this ISOPeriod
     */
    public int getYears() {
        return years;
    }

    /**
     * Gets the amount of months of this ISOPeriod, if any.
     *
     * @return the amount of months of this ISOPeriod
     */
    public int getMonths() {
        return months;
    }

    /**
     * Gets the amount of days of this ISOPeriod, if any.
     *
     * @return the amount of days of this ISOPeriod
     */
    public int getDays() {
        return days;
    }

    /**
     * Gets the amount of hours of this ISOPeriod, if any.
     *
     * @return the amount of hours of this ISOPeriod
     */
    public int getHours() {
        return hours;
    }

    /**
     * Gets the amount of minutes of this ISOPeriod, if any.
     *
     * @return the amount of minutes of this ISOPeriod
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Gets the amount of seconds of this ISOPeriod, if any.
     *
     * @return the amount of seconds of this ISOPeriod
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Gets the amount of nanoseconds of this ISOPeriod, if any.
     *
     * @return the amount of nanoseconds of this ISOPeriod
     */
    public long getNanos() {
        return nanos;
    }

    /**
     * Gets the amount of nanoseconds of this ISOPeriod safely converted
     * to an {@code int}.
     *
     * @return the amount of nanoseconds of this ISOPeriod
     * @throws ArithmeticException if the number of nanoseconds exceeds the capacity of an {@code int}
     */
    public int getNanosInt() {
        return DateTimes.safeToInt(nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with the specified amount of years.
     * <p>
     * This method will only affect the the years field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested years, never null
     */
    public ISOPeriod withYears(int years) {
        if (years == this.years) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with the specified amount of months.
     * <p>
     * This method will only affect the the months field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested months, never null
     */
    public ISOPeriod withMonths(int months) {
        if (months == this.months) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with the specified amount of days.
     * <p>
     * This method will only affect the the days field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested days, never null
     */
    public ISOPeriod withDays(int days) {
        if (days == this.days) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with the specified amount of hours.
     * <p>
     * This method will only affect the the hours field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested hours, never null
     */
    public ISOPeriod withHours(int hours) {
        if (hours == this.hours) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with the specified amount of minutes.
     * <p>
     * This method will only affect the the minutes field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested minutes, never null
     */
    public ISOPeriod withMinutes(int minutes) {
        if (minutes == this.minutes) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with the specified amount of seconds.
     * <p>
     * This method will only affect the the seconds field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested seconds, never null
     */
    public ISOPeriod withSeconds(int seconds) {
        if (seconds == this.seconds) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with the specified amount of nanoseconds.
     * <p>
     * This method will only affect the the nanoseconds field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanoseconds to represent
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested nanoseconds, never null
     */
    public ISOPeriod withNanos(long nanos) {
        if (nanos == this.nanos) {
            return this;
        }
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with only the date-based fields retained.
     * <p>
     * The returned ISOPeriod will have the same values for the date-based fields
     * (years, months and days) and zero values for the time-based fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code ISOPeriod} based on this ISOPeriod with zero values for time-based fields, never null
     */
    public ISOPeriod withDateFieldsOnly() {
        if ((hours | minutes | seconds | nanos) == 0) {
            return this;
        }
        return ofDateFields(years, months, days);
    }

    /**
     * Returns a copy of this ISOPeriod with only the time-based fields retained.
     * <p>
     * The returned ISOPeriod will have the same values for the time-based fields
     * (hours, minutes, seconds and nanoseconds) and zero values for the date-based fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code ISOPeriod} based on this ISOPeriod with zero values for date-based fields, never null
     */
    public ISOPeriod withTimeFieldsOnly() {
        if ((years | months | days) == 0) {
            return this;
        }
        return of(0, 0, 0, hours, minutes, seconds, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with the specified ISOPeriod added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param ISOPeriodProvider  the ISOPeriod to add, not null
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested ISOPeriod added, never null
     * @throws ArithmeticException if the capacity of any field is exceeded
     */
    public ISOPeriod plus(ISOPeriod other) {
        return of(
                DateTimes.safeAdd(years, other.years),
                DateTimes.safeAdd(months, other.months),
                DateTimes.safeAdd(days, other.days),
                DateTimes.safeAdd(hours, other.hours),
                DateTimes.safeAdd(minutes, other.minutes),
                DateTimes.safeAdd(seconds, other.seconds),
                DateTimes.safeAdd(nanos, other.nanos));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with the specified number of years added.
     * <p>
     * This method will only affect the the years field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested years added, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod plusYears(int years) {
        return withYears(DateTimes.safeAdd(this.years, years));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of months added.
     * <p>
     * This method will only affect the the months field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested months added, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod plusMonths(int months) {
        return withMonths(DateTimes.safeAdd(this.months, months));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of days added.
     * <p>
     * This method will only affect the the days field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested days added, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod plusDays(int days) {
        return withDays(DateTimes.safeAdd(this.days, days));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of hours added.
     * <p>
     * This method will only affect the the hours field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested hours added, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod plusHours(int hours) {
        return withHours(DateTimes.safeAdd(this.hours, hours));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of minutes added.
     * <p>
     * This method will only affect the the minutes field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested minutes added, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod plusMinutes(int minutes) {
        return withMinutes(DateTimes.safeAdd(this.minutes, minutes));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of seconds added.
     * <p>
     * This method will only affect the the seconds field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested seconds added, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod plusSeconds(int seconds) {
        return withSeconds(DateTimes.safeAdd(this.seconds, seconds));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of nanoseconds added.
     * <p>
     * This method will only affect the the nanoseconds field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanoseconds to add, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested nanoseconds added, never null
     * @throws ArithmeticException if the capacity of a {@code long} is exceeded
     */
    public ISOPeriod plusNanos(long nanos) {
        return withNanos(DateTimes.safeAdd(this.nanos, nanos));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with the specified ISOPeriod subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param ISOPeriodProvider  the ISOPeriod to subtract, not null
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested ISOPeriod subtracted, never null
     * @throws ArithmeticException if the capacity of any field is exceeded
     */
    public ISOPeriod minus(ISOPeriod other) {
        return of(
                DateTimes.safeSubtract(years, other.years),
                DateTimes.safeSubtract(months, other.months),
                DateTimes.safeSubtract(days, other.days),
                DateTimes.safeSubtract(hours, other.hours),
                DateTimes.safeSubtract(minutes, other.minutes),
                DateTimes.safeSubtract(seconds, other.seconds),
                DateTimes.safeSubtract(nanos, other.nanos));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with the specified number of years subtracted.
     * <p>
     * This method will only affect the the years field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested years subtracted, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod minusYears(int years) {
        return withYears(DateTimes.safeSubtract(this.years, years));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of months subtracted.
     * <p>
     * This method will only affect the the months field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested months subtracted, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod minusMonths(int months) {
        return withMonths(DateTimes.safeSubtract(this.months, months));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of days subtracted.
     * <p>
     * This method will only affect the the days field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested days subtracted, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod minusDays(int days) {
        return withDays(DateTimes.safeSubtract(this.days, days));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of hours subtracted.
     * <p>
     * This method will only affect the the hours field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested hours subtracted, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod minusHours(int hours) {
        return withHours(DateTimes.safeSubtract(this.hours, hours));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of minutes subtracted.
     * <p>
     * This method will only affect the the minutes field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested minutes subtracted, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod minusMinutes(int minutes) {
        return withMinutes(DateTimes.safeSubtract(this.minutes, minutes));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of seconds subtracted.
     * <p>
     * This method will only affect the the seconds field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested seconds subtracted, never null
     * @throws ArithmeticException if the capacity of an {@code int} is exceeded
     */
    public ISOPeriod minusSeconds(int seconds) {
        return withSeconds(DateTimes.safeSubtract(this.seconds, seconds));
    }

    /**
     * Returns a copy of this ISOPeriod with the specified number of nanoseconds subtracted.
     * <p>
     * This method will only affect the the nanoseconds field.
     * All other fields are left untouched.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanoseconds to subtract, positive or negative
     * @return a {@code ISOPeriod} based on this ISOPeriod with the requested nanoseconds subtracted, never null
     * @throws ArithmeticException if the capacity of a {@code long} is exceeded
     */
    public ISOPeriod minusNanos(long nanos) {
        return withNanos(DateTimes.safeSubtract(this.nanos, nanos));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with each element in this ISOPeriod multiplied
     * by the specified scalar.
     *
     * @param scalar  the scalar to multiply by, not null
     * @return a {@code ISOPeriod} based on this ISOPeriod with the amounts multiplied by the scalar, never null
     * @throws ArithmeticException if the capacity of any field is exceeded
     */
    public ISOPeriod multipliedBy(int scalar) {
        if (this == ZERO || scalar == 1) {
            return this;
        }
        return of(
                DateTimes.safeMultiply(years, scalar),
                DateTimes.safeMultiply(months, scalar),
                DateTimes.safeMultiply(days, scalar),
                DateTimes.safeMultiply(hours, scalar),
                DateTimes.safeMultiply(minutes, scalar),
                DateTimes.safeMultiply(seconds, scalar),
                DateTimes.safeMultiply(nanos, scalar));
    }

    /**
     * Returns a new instance with each element in this ISOPeriod divided
     * by the specified value.
     * <p>
     * The implementation simply divides each separate field by the divisor
     * using integer division.
     *
     * @param divisor  the value to divide by, not null
     * @return a {@code ISOPeriod} based on this ISOPeriod with the amounts divided by the divisor, never null
     * @throws ArithmeticException if dividing by zero
     */
    public ISOPeriod dividedBy(int divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        if (this == ZERO || divisor == 1) {
            return this;
        }
        return of(
                years / divisor, months / divisor, days / divisor,
                hours / divisor, minutes / divisor, seconds / divisor, nanos / divisor);
    }

    /**
     * Returns a new instance with each amount in this ISOPeriod negated.
     *
     * @return a {@code ISOPeriod} based on this ISOPeriod with the amounts negated, never null
     * @throws ArithmeticException if any field has the minimum value
     */
    public ISOPeriod negated() {
        return multipliedBy(-1);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this ISOPeriod with all amounts normalized to the
     * standard ranges for date-time fields.
     * <p>
     * Two normalizations occur, one for years and months, and one for
     * hours, minutes, seconds and nanoseconds.
     * Days are not normalized, as a day may vary in length at daylight savings cutover.
     * For example, a ISOPeriod of {@code P1Y15M1DT28H61M} will be normalized to {@code P2Y3M1DT29H1M}.
     * <p>
     * Note that this method normalizes using assumptions:
     * <ul>
     * <li>12 months in a year</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code ISOPeriod} based on this ISOPeriod with the amounts normalized, never null
     * @throws ArithmeticException if the capacity of any field is exceeded
     */
    public ISOPeriod normalized() {
        if (this == ZERO) {
            return ZERO;
        }
        int years = this.years;
        int months = this.months;
        if (months >= 12) {
            years = DateTimes.safeAdd(years, months / 12);
            months = months % 12;
        }
        long total = (hours * 60L * 60L) + (minutes * 60L) + seconds;  // will not overflow
        total = DateTimes.safeMultiply(total, 1000000000);
        total = DateTimes.safeAdd(total, nanos);
        long nanos = total % 1000000000L;
        total /= 1000000000L;
        int seconds = (int) (total % 60);
        total /= 60;
        int minutes = (int) (total % 60);
        total /= 60;
        int hours = DateTimes.safeToInt(total);
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    /**
     * Returns a copy of this ISOPeriod with all amounts normalized to the
     * standard ranges for date-time fields including the assumption that
     * days are 24 hours long.
     * <p>
     * Two normalizations occur, one for years and months, and one for
     * days, hours, minutes, seconds and nanoseconds.
     * For example, a ISOPeriod of {@code P1Y15M1DT28H} will be normalized to {@code P2Y3M2DT4H}.
     * <p>
     * Note that this method normalizes using assumptions:
     * <ul>
     * <li>12 months in a year</li>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code ISOPeriod} based on this ISOPeriod with the amounts normalized, never null
     * @throws ArithmeticException if the capacity of any field is exceeded
     */
    public ISOPeriod normalizedWith24HourDays() {
        if (this == ZERO) {
            return ZERO;
        }
        int years = this.years;
        int months = this.months;
        if (months >= 12) {
            years = DateTimes.safeAdd(years, months / 12);
            months = months % 12;
        }
        long total = (days * 24L * 60L * 60L) +
                        (hours * 60L * 60L) +
                        (minutes * 60L) + seconds;  // will not overflow
        total = DateTimes.safeAdd(total, DateTimes.floorDiv(this.nanos, 1000000000));
        int nanos = DateTimes.floorMod(this.nanos, 1000000000);
        int seconds = (int) (total % 60);
        total /= 60;
        int minutes = (int) (total % 60);
        total /= 60;
        int hours = (int) (total % 24);
        total /= 24;
        int days = DateTimes.safeToInt(total);
        return of(years, months, days, hours, minutes, seconds, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total number of years represented by this ISOPeriod using standard
     * assumptions for the meaning of month.
     * <p>
     * This method ignores days, hours, minutes, seconds and nanos.
     * It calculates using the assumption:
     * <ul>
     * <li>12 months in a year</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of years
     * @throws ArithmeticException if the capacity of a {@code long} is exceeded
     */
    public long totalYears() {
        return DateTimes.safeAdd((long) years, (long) (months / 12));
    }

    /**
     * Gets the total number of months represented by this ISOPeriod using standard
     * assumptions for the meaning of month.
     * <p>
     * This method ignores days, hours, minutes, seconds and nanos.
     * It calculates using the assumption:
     * <ul>
     * <li>12 months in a year</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of years
     * @throws ArithmeticException if the capacity of a {@code long} is exceeded
     */
    public long totalMonths() {
        return DateTimes.safeAdd(DateTimes.safeMultiply((long) years, 12), months);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total number of days represented by this ISOPeriod using standard
     * assumptions for the meaning of day, hour, minute and second.
     * <p>
     * This method ignores years and months.
     * It calculates using assumptions:
     * <ul>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of days
     */
    public long totalDaysWith24HourDays() {
        if (this == ZERO) {
            return 0;
        }
        return days + (hours + (minutes + (seconds + (nanos / 1000000000L)) / 60L) / 60L) / 24L;  // will not overflow
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total number of hours represented by this ISOPeriod using standard
     * assumptions for the meaning of hour, minute and second.
     * <p>
     * This method ignores years, months and days.
     * It calculates using assumptions:
     * <ul>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of hours
     */
    public long totalHours() {
        if (this == ZERO) {
            return 0;
        }
        return hours + (minutes + (seconds + (nanos / 1000000000L)) / 60L) / 60L;  // will not overflow
    }

    /**
     * Gets the total number of hours represented by this ISOPeriod using standard
     * assumptions for the meaning of day, hour, minute and second.
     * <p>
     * This method ignores years and months.
     * It calculates using assumptions:
     * <ul>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of hours
     */
    public long totalHoursWith24HourDays() {
        if (this == ZERO) {
            return 0;
        }
        return days * 24L + hours + (minutes + (seconds + (nanos / 1000000000L)) / 60L) / 60L;  // will not overflow
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total number of minutes represented by this ISOPeriod using standard
     * assumptions for the meaning of hour, minute and second.
     * <p>
     * This method ignores years, months and days.
     * It calculates using assumptions:
     * <ul>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of minutes
     */
    public long totalMinutes() {
        if (this == ZERO) {
            return 0;
        }
        return hours * 60L + minutes + (seconds + (nanos / 1000000000L)) / 60L;  // will not overflow
    }

    /**
     * Gets the total number of minutes represented by this ISOPeriod using standard
     * assumptions for the meaning of day, hour, minute and second.
     * <p>
     * This method ignores years and months.
     * It calculates using assumptions:
     * <ul>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of minutes
     */
    public long totalMinutesWith24HourDays() {
        if (this == ZERO) {
            return 0;
        }
        return (days * 24L + hours) * 60L + minutes + (seconds + (nanos / 1000000000L)) / 60L;  // will not overflow
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total number of seconds represented by this ISOPeriod using standard
     * assumptions for the meaning of hour, minute and second.
     * <p>
     * This method ignores years, months and days.
     * It calculates using assumptions:
     * <ul>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of seconds
     */
    public long totalSeconds() {
        if (this == ZERO) {
            return 0;
        }
        return (hours * 60L + minutes) * 60L + seconds + nanos / 1000000000L;  // will not overflow
    }

    /**
     * Gets the total number of seconds represented by this ISOPeriod using standard
     * assumptions for the meaning of day, hour, minute and second.
     * <p>
     * This method ignores years and months.
     * It calculates using assumptions:
     * <ul>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of seconds
     */
    public long totalSecondsWith24HourDays() {
        if (this == ZERO) {
            return 0;
        }
        return ((days * 24L + hours) * 60L + minutes) * 60L + seconds + nanos / 1000000000L;  // will not overflow
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total number of nanoseconds represented by this ISOPeriod using standard
     * assumptions for the meaning of hour, minute and second.
     * <p>
     * This method ignores years, months and days.
     * It calculates using assumptions:
     * <ul>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of nanoseconds
     * @throws ArithmeticException if the capacity of a {@code long} is exceeded
     */
    public long totalNanos() {
        if (this == ZERO) {
            return 0;
        }
        long secs = ((hours * 60L + minutes) * 60L + seconds);  // will not overflow
        long otherNanos = DateTimes.safeMultiply(secs, 1000000000L);
        return DateTimes.safeAdd(otherNanos, nanos);
    }

    /**
     * Gets the total number of nanoseconds represented by this ISOPeriod using standard
     * assumptions for the meaning of day, hour, minute and second.
     * <p>
     * This method ignores years and months.
     * It calculates using assumptions:
     * <ul>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return the total number of nanoseconds
     * @throws ArithmeticException if the capacity of a {@code long} is exceeded
     */
    public long totalNanosWith24HourDays() {
        if (this == ZERO) {
            return 0;
        }
        long secs = (((days * 24L + hours) * 60L + minutes) * 60L + seconds);  // will not overflow
        long otherNanos = DateTimes.safeMultiply(secs, 1000000000L);
        return DateTimes.safeAdd(otherNanos, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Estimates the duration of this ISOPeriod.
     * <p>
     * Each {@link ISOPeriodUnit} contains an estimated duration for that unit.
     * The per-unit estimate allows an estimate to be calculated for the whole ISOPeriod
     * including years, months and days. The estimate will equal the {@link #toDuration accurate}
     * calculation if the years, months and days fields are zero.
     *
     * @return the estimated duration of this ISOPeriod, never null
     * @throws ArithmeticException if the calculation overflows
     */
    public Duration toEstimatedDuration() {
        return null; // TODO
    }

    /**
     * Calculates the accurate duration of this ISOPeriod.
     * <p>
     * The calculation uses the hours, minutes, seconds and nanoseconds fields.
     * If years, months or days are present an exception is thrown.
     * <p>
     * The duration is calculated using assumptions:
     * <ul>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return a {@code Duration} equivalent to this ISOPeriod, never null
     * @throws CalendricalException if the ISOPeriod cannot be converted as it contains years/months/days
     */
    public Duration toDuration() {
        if ((years | months | days) > 0) {
            throw new CalendricalException("Unable to convert ISOPeriod to duration as years/months/days are present: " + this);
        }
        long secs = (hours * 60L + minutes) * 60L + seconds;  // will not overflow
        return Duration.ofSeconds(secs, nanos);
    }

    /**
     * Calculates the accurate duration of this ISOPeriod.
     * <p>
     * The calculation uses the days, hours, minutes, seconds and nanoseconds fields.
     * If years or months are present an exception is thrown.
     * <p>
     * The duration is calculated using assumptions:
     * <ul>
     * <li>24 hours in a day</li>
     * <li>60 minutes in an hour</li>
     * <li>60 seconds in a minute</li>
     * <li>1,000,000,000 nanoseconds in a second</li>
     * </ul>
     * This method is only appropriate to call if these assumptions are met.
     *
     * @return a {@code Duration} equivalent to this ISOPeriod, never null
     * @throws CalendricalException if the ISOPeriod cannot be converted as it contains years/months/days
     */
    public Duration toDurationWith24HourDays() {
        if ((years | months) > 0) {
            throw new CalendricalException("Unable to convert ISOPeriod to duration as years/months are present: " + this);
        }
        long secs = ((days * 24L + hours) * 60L + minutes) * 60L + seconds;  // will not overflow
        return Duration.ofSeconds(secs, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Is this ISOPeriod equal to the specified ISOPeriod.
     *
     * @param obj  the other ISOPeriod to compare to, null returns false
     * @return true if this instance is equal to the specified ISOPeriod
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ISOPeriod) {
            ISOPeriod other = (ISOPeriod) obj;
            return years == other.years && months == other.months && days == other.days &
                    hours == other.hours && minutes == other.minutes &&
                    seconds == other.seconds && nanos == other.nanos;
        }
        return false;
    }

    /**
     * Returns the hash code for this ISOPeriod.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        // SPEC: Require unique hash code for all ISOPeriods where fields within these inclusive bounds:
        // years 0-31, months 0-11, days 0-31, hours 0-23, minutes 0-59, seconds 0-59
        // IMPL: Ordered such that overflow from one field doesn't immediately affect the next field
        // years 5 bits, months 4 bits, days 6 bits, hours 5 bits, minutes 6 bits, seconds 6 bits
        return ((years << 27) | (years >>> 5)) ^
                ((hours << 22) | (hours >>> 10)) ^
                ((months << 18) | (months >>> 14)) ^
                ((minutes << 12) | (minutes >>> 20)) ^
                ((days << 6) | (days >>> 26)) ^ seconds ^ (((int) nanos) + 37);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string representation of the amount of time.
     *
     * @return the amount of time in ISO8601 string format
     */
    @Override
    public String toString() {
        // TODO: toString doesn't match state nanos/secs
        String str = string;
        if (str == null) {
            if (this == ZERO) {
                str = "PT0S";
            } else {
                StringBuilder buf = new StringBuilder();
                buf.append('P');
                if (years != 0) {
                    buf.append(years).append('Y');
                }
                if (months != 0) {
                    buf.append(months).append('M');
                }
                if (days != 0) {
                    buf.append(days).append('D');
                }
                if ((hours | minutes | seconds) != 0 || nanos != 0) {
                    buf.append('T');
                    if (hours != 0) {
                        buf.append(hours).append('H');
                    }
                    if (minutes != 0) {
                        buf.append(minutes).append('M');
                    }
                    if (seconds != 0 || nanos != 0) {
                        if (nanos == 0) {
                            buf.append(seconds).append('S');
                        } else {
                            long s = seconds + (nanos / 1000000000);
                            long n = nanos % 1000000000;
                            if (s < 0 && n > 0) {
                                n -= 1000000000;
                                s++;
                            } else if (s > 0 && n < 0) {
                                n += 1000000000;
                                s--;
                            }
                            if (n < 0) {
                                n = -n;
                                if (s == 0) {
                                    buf.append('-');
                                }
                            }
                            buf.append(s);
                            int dotPos = buf.length();
                            n += 1000000000;
                            while (n % 10 == 0) {
                                n /= 10;
                            }
                            buf.append(n);
                            buf.setCharAt(dotPos, '.');
                            buf.append('S');
                        }
                    }
                }
                str = buf.toString();
            }
            string = str;
        }
        return str;
    }

}
