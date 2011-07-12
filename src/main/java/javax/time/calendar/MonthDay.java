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

import static javax.time.calendar.ISODateTimeRule.DAY_OF_MONTH;
import static javax.time.calendar.ISODateTimeRule.MONTH_OF_YEAR;
import static javax.time.calendar.MonthOfYear.FEBRUARY;

import java.io.Serializable;

import javax.time.CalendricalException;
import javax.time.MathUtils;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

/**
 * A month-day in the ISO-8601 calendar system, such as {@code --12-03}.
 * <p>
 * {@code MonthDay} is an immutable calendrical that represents the combination
 * of a year and month. Any field that can be derived from a month and day, such as
 * quarter-of-year, can be obtained.
 * <p>
 * This class does not store or represent a year, time or time-zone.
 * Thus, for example, the value "3rd December" can be stored in a {@code MonthDay}.
 * <p>
 * Since a {@code MonthDay} does not possess a year, the leap day of
 * 29th of February is considered valid.
 * <p>
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. It is equivalent to the proleptic Gregorian calendar
 * system, in which todays's rules for leap years are applied for all time.
 * For most applications written today, the ISO-8601 rules are entirely suitable.
 * Any application that uses historical dates should consider using {@code HistoricDate}.
 * <p>
 * MonthDay is immutable and thread-safe.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
public final class MonthDay
        implements Calendrical, CalendricalMatcher, DateAdjuster, Comparable<MonthDay>, Serializable {

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = -254395108L;
    /**
     * Parser.
     */
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
        .appendLiteral("--")
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(DAY_OF_MONTH, 2)
        .toFormatter();

    /**
     * The month-of-year, not null.
     */
    private final MonthOfYear month;
    /**
     * The day-of-month.
     */
    private final int day;

    //-----------------------------------------------------------------------
    /**
     * Gets the rule for {@code MonthDay}.
     *
     * @return the rule for the month-day, not null
     */
    public static CalendricalRule<MonthDay> rule() {
        return ExtendedCalendricalRule.MONTH_DAY;
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains the current month-day from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current month-day.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @return the current month-day using the system clock, not null
     */
    public static MonthDay now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current month-day from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current month-day.
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current month-day, not null
     */
    public static MonthDay now(Clock clock) {
        final LocalDate now = LocalDate.now(clock);  // called once
        return MonthDay.of(now.getMonthOfYear(), now.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code MonthDay}.
     * <p>
     * The day-of-month must be valid for the month within a leap year.
     * Hence, for February, day 29 is valid.
     * <p>
     * For example, passing in April and day 31 will throw an exception, as
     * there can never be a 31st April in any year. Alternately, passing in
     * 29th February is valid, as that month-day can be valid.
     *
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the month-day, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month
     */
    public static MonthDay of(MonthOfYear monthOfYear, int dayOfMonth) {
        ISOChronology.checkNotNull(monthOfYear, "MonthOfYear must not be null");
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        if (dayOfMonth > monthOfYear.maxLengthInDays()) {
            throw new InvalidCalendarFieldException("Illegal value for DayOfMonth field, value " + dayOfMonth +
                    " is not valid for month " + monthOfYear.name(), DAY_OF_MONTH);
        }
        return new MonthDay(monthOfYear, dayOfMonth);
    }

    /**
     * Obtains an instance of {@code MonthDay}.
     * <p>
     * The day-of-month must be valid for the month within a leap year.
     * Hence, for month 2 (February), day 29 is valid.
     * <p>
     * For example, passing in month 4 (April) and day 31 will throw an exception, as
     * there can never be a 31st April in any year. Alternately, passing in
     * 29th February is valid, as that month-day can be valid.
     *
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the month-day, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month
     */
    public static MonthDay of(int monthOfYear, int dayOfMonth) {
        return of(MonthOfYear.of(monthOfYear), dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code MonthDay} from a set of calendricals.
     * <p>
     * A calendrical represents some form of date and time information.
     * This method combines the input calendricals into a month-day.
     *
     * @param calendricals  the calendricals to create a month-day from, no nulls, not null
     * @return the month-day, not null
     * @throws CalendricalException if unable to merge to a month-day
     */
    public static MonthDay from(Calendrical... calendricals) {
        return CalendricalNormalizer.merge(calendricals).deriveChecked(rule());
    }

    /**
     * Obtains an instance of {@code MonthDay} from the normalized form.
     * <p>
     * This internal method is used by the associated rule.
     *
     * @param normalized  the normalized calendrical, not null
     * @return the MonthDay singleton, null if unable to obtain
     */
    static MonthDay deriveFrom(CalendricalNormalizer merger) {
        DateTimeField moy = merger.getFieldDerived(MONTH_OF_YEAR, true);
        DateTimeField dom = merger.getFieldDerived(DAY_OF_MONTH, true);
        if (moy == null || dom == null) {
            return null;
        }
        return of(moy.getValidIntValue(), dom.getValidIntValue());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code MonthDay} from a text string such as {@code --12-03}.
     * <p>
     * The following formats are accepted in ASCII:
     * <ul>
     * <li>--{monthOfYear}-{dayOfMonth}
     * </ul>
     * The month-of-year has 2 digits and has values from 1 to 12.
     * <p>
     * The day-of-month has 2 digits with values from 1 to 31 appropriate to the month.
     *
     * @param text  the text to parse such as "--12-03", not null
     * @return the parsed month-day, not null
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static MonthDay parse(CharSequence text) {
        return PARSER.parse(text, rule());
    }

    /**
     * Obtains an instance of {@code MonthDay} from a text string using a specific formatter.
     * <p>
     * The text is parsed using the formatter, returning a month-day.
     *
     * @param text  the text to parse, not null
     * @param formatter  the formatter to use, not null
     * @return the parsed month-day, not null
     * @throws UnsupportedOperationException if the formatter cannot parse
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static MonthDay parse(CharSequence text, DateTimeFormatter formatter) {
        ISOChronology.checkNotNull(formatter, "DateTimeFormatter must not be null");
        return formatter.parse(text, rule());
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor, previously validated.
     *
     * @param monthOfYear  the month-of-year to represent, validated not null
     * @param dayOfMonth  the day-of-month to represent, validated from 1 to 29-31
     */
    private MonthDay(MonthOfYear monthOfYear, int dayOfMonth) {
        this.month = monthOfYear;
        this.day = dayOfMonth;
    }

    /**
     * Returns a copy of this month-day with the new month and day, checking
     * to see if a new object is in fact required.
     *
     * @param newMonth  the month-of-year to represent, validated not null
     * @param newDay  the day-of-month to represent, validated from 1 to 31
     * @return the month-day, not null
     */
    private MonthDay with(MonthOfYear newMonth, int newDay) {
        if (month == newMonth && day == newDay) {
            return this;
        }
        return new MonthDay(newMonth, newDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the specified calendrical rule.
     * <p>
     * This method queries the value of the specified calendrical rule.
     * If the value cannot be returned for the rule from this month-day then
     * {@code null} will be returned.
     *
     * @param ruleToDerive  the rule to derive, not null
     * @return the value for the rule, null if the value cannot be returned
     */
    @SuppressWarnings("unchecked")
    public <T> T get(CalendricalRule<T> ruleToDerive) {
        if (ruleToDerive == rule()) {
            return (T) this;
        }
        return CalendricalNormalizer.derive(ruleToDerive, rule(), null, null, null, null, ISOChronology.INSTANCE, toFields());
    }

    //-----------------------------------------------------------------------
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
        return month;
    }

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    public int getDayOfMonth() {
        return day;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code MonthDay} with the month-of-year altered.
     * <p>
     * If the day-of-month is invalid for the specified month, the day will
     * be adjusted to the last valid day-of-month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned month-day, not null
     * @return a {@code MonthDay} based on this month-day with the requested month, not null
     */
    public MonthDay with(MonthOfYear monthOfYear) {
        ISOChronology.checkNotNull(monthOfYear, "MonthOfYear must not be null");
        int maxDays = monthOfYear.maxLengthInDays();
        if (day > maxDays) {
            return with(monthOfYear, maxDays);
        }
        return with(monthOfYear, day);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code MonthDay} with the month-of-year altered.
     * <p>
     * If the day-of-month is invalid for the specified month, the day will
     * be adjusted to the last valid day-of-month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned month-day, from 1 (January) to 12 (December)
     * @return a {@code MonthDay} based on this month-day with the requested month, not null
     * @throws IllegalCalendarFieldValueException if the month-of-year value is invalid
     */
    public MonthDay withMonthOfYear(int monthOfYear) {
        return with(MonthOfYear.of(monthOfYear));
    }

    /**
     * Returns a copy of this {@code MonthDay} with the day-of-month altered.
     * <p>
     * If the day-of-month is invalid for the current month, an exception
     * will be thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the return month-day, from 1 to 31
     * @return a {@code MonthDay} based on this month-day with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-month value is invalid
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month
     */
    public MonthDay withDayOfMonth(int dayOfMonth) {
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        int maxDays = month.maxLengthInDays();
        if (dayOfMonth > maxDays) {
            throw new InvalidCalendarFieldException("Day of month cannot be changed to " +
                    dayOfMonth + " for the month " + month, DAY_OF_MONTH);
        }
        return with(month, dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Rolls the month-of-year, adding the specified number of months to a copy
     * of this {@code MonthDay}.
     * <p>
     * This method will add the specified number of months to the month-day,
     * rolling from December back to January if necessary.
     * <p>
     * If the day-of-month is invalid for the specified month in the result,
     * the day will be adjusted to the last valid day-of-month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to roll by, positive or negative
     * @return a {@code MonthDay} based on this month-day with the month rolled, not null
     */
    public MonthDay rollMonthOfYear(int months) {
        return with(month.roll(months));
    }

    /**
     * Rolls the day-of-month, adding the specified number of days to a copy
     * of this {@code MonthDay}.
     * <p>
     * This method will add the specified number of days to the month-day,
     * rolling from last day-of-month to the first if necessary.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to roll by, positive or negative
     * @return a {@code MonthDay} based on this month-day with the day rolled, not null
     */
    public MonthDay rollDayOfMonth(int days) {
        if (days == 0) {
            return this;
        }
        int monthLength = month.maxLengthInDays();
        int newDOM0 = (days % monthLength) + (day - 1);
        newDOM0 = (newDOM0 + monthLength) % monthLength;
        return withDayOfMonth(++newDOM0);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the month-day extracted from the calendrical matches this.
     * <p>
     * This method implements the {@code CalendricalMatcher} interface.
     * It is intended that applications use {@link LocalDate#matches} rather than this method.
     *
     * @param calendrical  the calendrical to match, not null
     * @return true if the calendrical matches, false otherwise
     */
    public boolean matchesCalendrical(Calendrical calendrical) {
        return this.equals(calendrical.get(rule()));
    }

    /**
     * Adjusts a date to have the value of this month-day, returning a new date.
     * <p>
     * This method implements the {@link DateAdjuster} interface.
     * It is intended that, instead of calling this method directly, it is used from
     * an instance of {@code LocalDate}:
     * <pre>
     *   date = date.with(monthDay);
     * </pre>
     * <p>
     * This implementation handles the case where this represents February 29 and
     * the year is not a leap year by throwing an exception.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param date  the date to be adjusted, not null
     * @return the adjusted date, not null
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the year
     */
    public LocalDate adjustDate(LocalDate date) {
        return adjustDate(date, DateResolvers.strict());
    }

    /**
     * Adjusts a date to have the value of this month-day, using a resolver to
     * handle the case when the day-of-month becomes invalid.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param date  the date to be adjusted, not null
     * @param resolver  the date resolver to use if the day-of-month is invalid, not null
     * @return the adjusted date, not null
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the year
     */
    public LocalDate adjustDate(LocalDate date, DateResolver resolver) {
        ISOChronology.checkNotNull(date, "LocalDate must not be null");
        ISOChronology.checkNotNull(resolver, "DateResolver must not be null");
        if (date.getMonthOfYear() == month && date.getDayOfMonth() == day) {
            return date;
        }
        LocalDate resolved = resolver.resolveDate(date.getYear(), month, day);
        ISOChronology.checkNotNull(resolved, "The implementation of DateResolver must not return null");
        return resolved;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the year is valid for this month-day.
     * <p>
     * This method checks whether this month and day and the input year form
     * a valid date.
     *
     * @param year  the year to validate, an out of range value returns false
     * @return true if the year is valid for this month-day
     * @see Year#isValidMonthDay(MonthDay)
     */
    public boolean isValidYear(int year) {
        return (day == 29 && month == FEBRUARY && ISOChronology.isLeapYear(year) == false) == false;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a date formed from this month-day at the specified year.
     * <p>
     * This method merges {@code this} and the specified year to form an
     * instance of {@code LocalDate}.
     * <pre>
     * LocalDate date = monthDay.atYear(year);
     * </pre>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to use, from MIN_YEAR to MAX_YEAR
     * @return the local date formed from this month-day and the specified year, not null
     * @see Year#atMonthDay(MonthDay)
     */
    public LocalDate atYear(int year) {
        return LocalDate.of(year, month, day);
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this month-day to an equivalent fields object.
     * <p>
     * The fields will contain {@link ISODateTimeRule#MONTH_OF_YEAR} and
     * {@link ISODateTimeRule#DAY_OF_MONTH}.
     *
     * @return the equivalent fields, not null
     */
    public DateTimeFields toFields() {
        return DateTimeFields.of(MONTH_OF_YEAR, month.getValue(), DAY_OF_MONTH, day);
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this month-day to another month-day.
     *
     * @param other  the other month-day to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if {@code other} is null
     */
    public int compareTo(MonthDay other) {
        int cmp = month.compareTo(other.month);
        if (cmp == 0) {
            cmp = MathUtils.safeCompare(day, other.day);
        }
        return cmp;
    }

    /**
     * Is this month-day after the specified month-day.
     *
     * @param other  the other month-day to compare to, not null
     * @return true if this is after the specified month-day
     * @throws NullPointerException if {@code other} is null
     */
    public boolean isAfter(MonthDay other) {
        return compareTo(other) > 0;
    }

    /**
     * Is this month-day before the specified month-day.
     *
     * @param other  the other month-day to compare to, not null
     * @return true if this point is before the specified month-day
     * @throws NullPointerException if {@code other} is null
     */
    public boolean isBefore(MonthDay other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this month-day is equal to another month-day.
     * <p>
     * The comparison is based on the time-line position of the month-day within a year.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other month-day
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MonthDay) {
            MonthDay other = (MonthDay) obj;
            return month == other.month && day == other.day;
        }
        return false;
    }

    /**
     * A hash code for this month-day.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return (month.getValue() << 6) + day;
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this month-day as a {@code String}, such as {@code --12-03}.
     * <p>
     * The output will be in the format {@code --MM-dd}:
     *
     * @return a string representation of this month-day, not null
     */
    @Override
    public String toString() {
        int monthValue = month.getValue();
        int dayValue = day;
        return new StringBuilder(10).append("--")
            .append(monthValue < 10 ? "0" : "").append(monthValue)
            .append(dayValue < 10 ? "-0" : "-").append(dayValue)
            .toString();
    }

    /**
     * Outputs this month-day as a {@code String} using the formatter.
     *
     * @param formatter  the formatter to use, not null
     * @return the formatted month-day string, not null
     * @throws UnsupportedOperationException if the formatter cannot print
     * @throws CalendricalException if an error occurs during printing
     */
    public String toString(DateTimeFormatter formatter) {
        ISOChronology.checkNotNull(formatter, "DateTimeFormatter must not be null");
        return formatter.print(this);
    }

}
