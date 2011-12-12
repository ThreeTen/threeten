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
import static javax.time.calendar.ISODateTimeRule.YEAR;

import java.io.Serializable;

import javax.time.CalendricalException;
import javax.time.Instant;
import javax.time.MathUtils;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

/**
 * A date without a time-zone in the ISO-8601 calendar system,
 * such as {@code 2007-12-03}.
 * <p>
 * {@code LocalDate} is an immutable calendrical that represents a date, often viewed
 * as year-month-day. This object can also access other date fields such as
 * day-of-year, day-of-week and week-of-year.
 * <p>
 * This class does not store or represent a time or time-zone.
 * For example, the value "2nd October 2007" can be stored in a {@code LocalDate}.
 * <p>
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. It is equivalent to the proleptic Gregorian calendar
 * system, in which todays's rules for leap years are applied for all time.
 * For most applications written today, the ISO-8601 rules are entirely suitable.
 * <p>
 * However, any application that makes use of historical dates and requires them
 * to be accurate will find the ISO-8601 rules unsuitable. In this case, the
 * application code should use {@code HistoricDate} and define an explicit
 * cutover date between the Julian and Gregorian calendar systems.
 * <p>
 * LocalDate is immutable and thread-safe.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
public final class LocalDate
        implements Calendrical, CalendricalMatcher, DateAdjuster, Comparable<LocalDate>, Serializable {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constant for the minimum date on the proleptic ISO calendar system, -999999999-01-01.
     * This could be used by an application as a "far past" date.
     */
    public static final LocalDate MIN_DATE = LocalDate.of(Year.MIN_YEAR, 1, 1);
    /**
     * Constant for the maximum date on the proleptic ISO calendar system, +999999999-12-31.
     * This could be used by an application as a "far future" date.
     */
    public static final LocalDate MAX_DATE = LocalDate.of(Year.MAX_YEAR, 12, 31);

    /**
     * The year.
     */
    private final int year;
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
     * Gets the rule for {@code LocalDate}.
     *
     * @return the rule for the date, not null
     */
    public static CalendricalRule<LocalDate> rule() {
        return ISOCalendricalRule.LOCAL_DATE;
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains the current date from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current date.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @return the current date using the system clock, not null
     */
    public static LocalDate now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current date from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current date - today.
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current date, not null
     */
    public static LocalDate now(Clock clock) {
        ISOChronology.checkNotNull(clock, "Clock must not be null");
        // inline OffsetDate factory to avoid creating object and InstantProvider checks
        final Instant now = clock.instant();  // called once
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        long epochSec = now.getEpochSecond() + offset.getAmountSeconds();  // overflow caught later
        long yearZeroDay = MathUtils.floorDiv(epochSec, ISOChronology.SECONDS_PER_DAY) + ISOChronology.DAYS_0000_TO_1970;
        return LocalDate.ofYearZeroDay(yearZeroDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDate} from a year, month and day.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static LocalDate of(int year, MonthOfYear monthOfYear, int dayOfMonth) {
        YEAR.checkValidValue(year);
        ISOChronology.checkNotNull(monthOfYear, "MonthOfYear must not be null");
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return create(year, monthOfYear, dayOfMonth);
    }

    /**
     * Obtains an instance of {@code LocalDate} from a year, month and day.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static LocalDate of(int year, int monthOfYear, int dayOfMonth) {
        YEAR.checkValidValue(year);
        MONTH_OF_YEAR.checkValidValue(monthOfYear);
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return create(year, MonthOfYear.of(monthOfYear), dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDate} from the epoch day count.
     * <p>
     * The Epoch Day count is a simple incrementing count of days
     * where day 0 is 1970-01-01. Negative numbers represent earlier days.
     *
     * @param epochDay  the Epoch Day to convert, based on the epoch 1970-01-01
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the epoch days exceeds the supported date range
     */
    public static LocalDate ofEpochDay(long epochDay) {
        return ofYearZeroDay(epochDay + ISOChronology.DAYS_0000_TO_1970);
    }

    /**
     * Obtains an instance of {@code LocalDate} from the Modified Julian Day (MJD).
     * <p>
     * The Modified Julian Day count is a simple incrementing count of days
     * where day 0 is 1858-11-17. Negative numbers represent earlier days.
     *
     * @param mjDay  the Modified Julian Day to convert, based on the epoch 1858-11-17
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the modified julian days value is outside the supported range
     */
    public static LocalDate ofModifiedJulianDay(long mjDay) {
        return ofYearZeroDay(mjDay + ISOChronology.DAYS_0000_TO_MJD_EPOCH);
    }

    /**
     * Converts a year zero day count to a date.
     * <p>
     * The year zero day count is a simple incrementing count of days
     * where day 0 is 0000-01-01. Negative numbers represent earlier days.
     *
     * @param zeroDay  the Year zero Day to convert, based on the epoch 0000-01-01
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the epoch days exceeds the supported date range
     */
    static LocalDate ofYearZeroDay(long zeroDay) {
        // find the march-based year
        zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
        long adjust = 0;
        if (zeroDay < 0) {
            // adjust negative years to positive for calculation
            long adjustCycles = (zeroDay + 1) / ISOChronology.DAYS_PER_CYCLE - 1;
            adjust = adjustCycles * 400;
            zeroDay += -adjustCycles * ISOChronology.DAYS_PER_CYCLE;
        }
        long yearEst = (400 * zeroDay + 591) / ISOChronology.DAYS_PER_CYCLE;
        long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        if (doyEst < 0) {
            // fix estimate
            yearEst--;
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        }
        yearEst += adjust;  // reset any negative year
        int marchDoy0 = (int) doyEst;
        
        // convert march-based values back to january-based
        int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        int month = (marchMonth0 + 2) % 12 + 1;
        int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;
        
        // check year now we are certain it is correct
        int year = YEAR.checkValidIntValue(yearEst);
        return new LocalDate(year, MonthOfYear.of(month), dom);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDate} from a set of calendricals.
     * <p>
     * A calendrical represents some form of date and time information.
     * This method combines the input calendricals into a date.
     *
     * @param calendricals  the calendricals to create a date from, no nulls, not null
     * @return the local date, not null
     * @throws CalendricalException if unable to merge to a local date
     */
    public static LocalDate from(Calendrical... calendricals) {
        return CalendricalEngine.merge(calendricals).deriveChecked(rule());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDate} from a text string such as {@code 2007-12-03}.
     * <p>
     * The string must represent a valid date and is parsed using
     * {@link DateTimeFormatters#isoLocalDate()}.
     * Year, month and day-of-month are required.
     * Years outside the range 0000 to 9999 must be prefixed by the plus or minus symbol.
     *
     * @param text  the text to parse such as "2007-12-03", not null
     * @return the parsed local date, not null
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static LocalDate parse(CharSequence text) {
        return DateTimeFormatters.isoLocalDate().parse(text, rule());
    }

    /**
     * Obtains an instance of {@code LocalDate} from a text string using a specific formatter.
     * <p>
     * The text is parsed using the formatter, returning a date.
     *
     * @param text  the text to parse, not null
     * @param formatter  the formatter to use, not null
     * @return the parsed local date, not null
     * @throws UnsupportedOperationException if the formatter cannot parse
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
        ISOChronology.checkNotNull(formatter, "DateTimeFormatter must not be null");
        return formatter.parse(text, rule());
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a local date from the year, month and day fields.
     *
     * @param year  the year to represent, validated from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, validated not null
     * @param dayOfMonth  the day-of-month to represent, validated from 1 to 31
     * @return the local date, not null
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    private static LocalDate create(int year, MonthOfYear monthOfYear, int dayOfMonth) {
        if (dayOfMonth > 28 && dayOfMonth > monthOfYear.lengthInDays(ISOChronology.isLeapYear(year))) {
            if (dayOfMonth == 29) {
                throw new InvalidCalendarFieldException("Illegal value for DayOfMonth field, value 29 is not valid as " +
                        year + " is not a leap year", DAY_OF_MONTH);
            } else {
                throw new InvalidCalendarFieldException("Illegal value for DayOfMonth field, value " + dayOfMonth +
                        " is not valid for month " + monthOfYear.name(), DAY_OF_MONTH);
            }
        }
        return new LocalDate(year, monthOfYear, dayOfMonth);
    }

    /**
     * Constructor, previously validated.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, valid for year-month, from 1 to 31
     */
    private LocalDate(int year, MonthOfYear monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the specified calendrical rule.
     * <p>
     * This method queries the value of the specified calendrical rule.
     * If the value cannot be returned for the rule from this date then
     * {@code null} will be returned.
     *
     * @param ruleToDerive  the rule to derive, not null
     * @return the value for the rule, null if the value cannot be returned
     */
    public <T> T get(CalendricalRule<T> ruleToDerive) {
        return CalendricalEngine.derive(ruleToDerive, rule(), this, null, null, null, ISOChronology.INSTANCE, null);
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
        return year;
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

    /**
     * Gets the day-of-year field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    public int getDayOfYear() {
        return ISOChronology.getDayOfYearFromDate(this);
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
        return ISOChronology.getDayOfWeekFromDate(this);
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
        return ISOChronology.isLeapYear(year);
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the date, handling incorrectly implemented resolvers.
     *
     * @param dateResolver  the resolver, not null
     * @param year  the year, from MIN_YEAR to MAX_YEAR
     * @param month  the month, not null
     * @param day  the day-of-month, from 1 to 31
     * @return the resolved date, not null
     * @throws NullPointerException if the resolver returned null
     */
    private LocalDate resolveDate(DateResolver dateResolver, int year, MonthOfYear month, int day) {
        YEAR.checkValidValue(year);
        DAY_OF_MONTH.checkValidValue(day);
        LocalDate date = dateResolver.resolveDate(year, month, day);
        ISOChronology.checkNotNull(date, "DateResolver implementation must not return null");
        return date;
    }

    /**
     * Returns a copy of this {@code LocalDate} with the date altered using the adjuster.
     * <p>
     * This adjusts the date according to the rules of the specified adjuster.
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date to the last day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster  the adjuster to use, not null
     * @return a {@code LocalDate} based on this date adjusted as necessary, not null
     */
    public LocalDate with(DateAdjuster adjuster) {
        ISOChronology.checkNotNull(adjuster, "DateAdjuster must not be null");
        LocalDate date = adjuster.adjustDate(this);
        ISOChronology.checkNotNull(date, "DateAdjuster implementation must not return null");
        return date;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDate} with the year altered.
     * If the resulting date is invalid, it will be resolved using {@link DateResolvers#previousValid()}.
     * <p>
     * This method does the same as {@code withYear(year, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @return a {@code LocalDate} based on this date with the requested year, not null
     * @throws IllegalCalendarFieldValueException if the year value is invalid
     */
    public LocalDate withYear(int year) {
        return withYear(year, DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the year altered.
     * If the resulting date is invalid, it will be resolved using {@code dateResolver}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return a {@code LocalDate} based on this date with the requested year, not null
     * @throws IllegalCalendarFieldValueException if the year value is invalid
     */
    public LocalDate withYear(int year, DateResolver dateResolver) {
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (this.year == year) {
            return this;
        }
        return resolveDate(dateResolver, year, month, day);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the month-of-year altered.
     * If the resulting date is invalid, it will be resolved using {@link DateResolvers#previousValid()}.
     * <p>
     * This method does the same as {@code withMonthOfYear(monthOfYear, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @return a {@code LocalDate} based on this date with the requested month, not null
     * @throws IllegalCalendarFieldValueException if the month-of-year value is invalid
     */
    public LocalDate withMonthOfYear(int monthOfYear) {
        return with(MonthOfYear.of(monthOfYear), DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the month-of-year altered.
     * If the resulting date is invalid, it will be resolved using {@code dateResolver}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return a {@code LocalDate} based on this date with the requested month, not null
     * @throws IllegalCalendarFieldValueException if the month-of-year value is invalid
     */
    public LocalDate withMonthOfYear(int monthOfYear, DateResolver dateResolver) {
        return with(MonthOfYear.of(monthOfYear), dateResolver);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the month-of-year altered.
     * If the resulting date is invalid, it will be resolved using {@link DateResolvers#previousValid()}.
     * <p>
     * This method does the same as {@code with(monthOfYear, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, not null
     * @return a {@code LocalDate} based on this date with the requested month, not null
     */
    public LocalDate with(MonthOfYear monthOfYear) {
        return with(monthOfYear, DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the month-of-year altered.
     * If the resulting date is invalid, it will be resolved using {@code dateResolver}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, not null
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return a {@code LocalDate} based on this date with the requested month, not null
     */
    public LocalDate with(MonthOfYear monthOfYear, DateResolver dateResolver) {
        ISOChronology.checkNotNull(monthOfYear, "MonthOfYear must not be null");
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (this.month == monthOfYear) {
            return this;
        }
        return resolveDate(dateResolver, year, monthOfYear, day);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the day-of-month altered.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 28-31
     * @return a {@code LocalDate} based on this date with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-month value is invalid
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public LocalDate withDayOfMonth(int dayOfMonth) {
        if (this.day == dayOfMonth) {
            return this;
        }
        return of(year, month, dayOfMonth);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the day-of-month altered.
     * If the resulting date is invalid, it will be resolved using {@code dateResolver}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 31
     * @param dateResolver the DateResolver to be used if the resulting date would be invalid
     * @return a {@code LocalDate} based on this date with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-month value is invalid
     */
    public LocalDate withDayOfMonth(int dayOfMonth, DateResolver dateResolver) {
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (this.day == dayOfMonth) {
            return this;
        }
        return resolveDate(dateResolver, year, month, dayOfMonth);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the day-of-year altered.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 365-366
     * @return a {@code LocalDate} based on this date with the requested day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-year value is invalid
     * @throws InvalidCalendarFieldException if the day-of-year is invalid for the year
     */
    public LocalDate withDayOfYear(int dayOfYear) {
        if (this.getDayOfYear() == dayOfYear) {
            return this;
        }
        return ISOChronology.getDateFromDayOfYear(year, dayOfYear);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDate} with the specified date period added.
     * <p>
     * This adds the specified period to this date, returning a new date.
     * Before addition, the period is converted to a date-based {@code Period} using
     * {@link Period#ofDateFields(PeriodProvider)}.
     * That factory ignores any time-based ISO fields, thus adding a time-based
     * period to this date will have no effect. If you want to take time fields into
     * account, call {@link Period#normalizedWith24HourDays()} on the input period.
     * <p>
     * The detailed rules for the addition have some complexity due to variable length months.
     * The goal is to match the code for {@code plusYears().plusMonths().plusDays()} in most cases.
     * The principle case of difference is best expressed by example:
     * {@code 2010-01-31} plus {@code P1M-1D} yields {@code 2010-02-28} whereas
     * {@code plusMonths(1).plusDays(-1)} gives {@code 2010-02-27}.
     * <p>
     * The rules are expressed in five steps:
     * <ol>
     * <li>Add the input years and months to calculate the resulting year-month</li>
     * <li>Form an imaginary date from the year-month and the original day-of-month,
     *  a date that may be invalid, such as February 30th</li>
     * <li>Add the input days to the imaginary date treating the first move to a later date
     *  from an invalid date as a move to the 1st of the next month</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, this table shows what happens when for various inputs and periods:
     * <pre>
     *   2010-01-30 plus P1M2D  = 2010-03-02
     *   2010-01-30 plus P1M1D  = 2010-03-01
     *   2010-01-30 plus P1M    = 2010-02-28
     *   2010-01-30 plus P1M-1D = 2010-02-28
     *   2010-01-30 plus P1M-2D = 2010-02-28
     *   2010-01-30 plus P1M-3D = 2010-02-27
     * </pre>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param periodProvider  the period to add, not null
     * @return a {@code LocalDate} based on this date with the period added, not null
     * @throws CalendricalException if the specified period cannot be converted to a {@code Period}
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate plus(PeriodProvider periodProvider) {
        Period period = Period.ofDateFields(periodProvider);
        long periodMonths = period.totalMonths();
        long periodDays = period.getDays();
        if (periodMonths == 0) {
            return plusDays(periodDays);  // optimization that also returns this for zero
        }
        long monthCount = ((long) year) * 12 + (month.getValue() - 1);
        long calcMonths = monthCount + periodMonths;  // safe overflow
        int newYear = YEAR.checkValidIntValue(MathUtils.floorDiv(calcMonths, 12));
        MonthOfYear newMonth = MonthOfYear.of(MathUtils.floorMod(calcMonths, 12) + 1);
        int newMonthLen = newMonth.lengthInDays(ISOChronology.isLeapYear(newYear));
        int newDay = Math.min(day, newMonthLen);
        if (periodDays < 0 && day > newMonthLen) {
            periodDays = Math.min(periodDays + (day - newMonthLen), 0);  // adjust for invalid days
        }
        return LocalDate.of(newYear, newMonth, newDay).plusDays(periodDays);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDate} with the specified period in years added.
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
     * @return a {@code LocalDate} based on this date with the years added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #plusYears(long, javax.time.calendar.DateResolver)
     */
    public LocalDate plusYears(long years) {
        return plusYears(years, DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in years added.
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
     * @return a {@code LocalDate} based on this date with the years added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate plusYears(long years, DateResolver dateResolver) {
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (years == 0) {
            return this;
        }
        int newYear = YEAR.checkValidIntValue(year + years);  // safe overflow
        return resolveDate(dateResolver, newYear, month, day);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in months added.
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
     * @return a {@code LocalDate} based on this date with the months added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #plusMonths(long, javax.time.calendar.DateResolver)
     */
    public LocalDate plusMonths(long months) {
        return plusMonths(months, DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in months added.
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
     * @return a {@code LocalDate} based on this date with the months added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate plusMonths(long months, DateResolver dateResolver) {
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (months == 0) {
            return this;
        }
        long monthCount = year * 12L + (month.getValue() - 1);
        long calcMonths = monthCount + months;  // safe overflow
        int newYear = YEAR.checkValidIntValue(MathUtils.floorDiv(calcMonths, 12));
        MonthOfYear newMonth = MonthOfYear.of(MathUtils.floorMod(calcMonths, 12) + 1);
        return resolveDate(dateResolver, newYear, newMonth, day);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in weeks added.
     * <p>
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one week would result in 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return a {@code LocalDate} based on this date with the weeks added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate plusWeeks(long weeks) {
        return plusDays(MathUtils.safeMultiply(weeks, 7));
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified number of days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one day would result in 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return a {@code LocalDate} based on this date with the days added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate plusDays(long days) {
        if (days == 0) {
            return this;
        }
        long mjDay = MathUtils.safeAdd(toModifiedJulianDay(), days);
        return LocalDate.ofModifiedJulianDay(mjDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDate} with the specified date period subtracted.
     * <p>
     * This subtracts the specified period from this date, returning a new date.
     * Before subtraction, the period is converted to a date-based {@code Period} using
     * {@link Period#ofDateFields(PeriodProvider)}.
     * That factory ignores any time-based ISO fields, thus subtracting a time-based
     * period from this date will have no effect. If you want to take time fields into
     * account, call {@link Period#normalizedWith24HourDays()} on the input period.
     * <p>
     * The detailed rules for the subtraction have some complexity due to variable length months.
     * The goal is to match the code for {@code minusYears().minusMonths().minusDays()} in most cases.
     * The principle case of difference is best expressed by example:
     * {@code 2010-03-31} minus {@code P1M1D} yields {@code 2010-02-28} whereas
     * {@code minusMonths(1).minusDays(1)} gives {@code 2010-02-27}.
     * <p>
     * The rules are expressed in five steps:
     * <ol>
     * <li>Subtract the input years and months to calculate the resulting year-month</li>
     * <li>Form an imaginary date from the year-month and the original day-of-month,
     *  a date that may be invalid, such as February 30th</li>
     * <li>Subtract the input days from the imaginary date treating the first move to a later date
     *  from an invalid date as a move to the 1st of the next month</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, this table shows what happens when for various inputs and periods:
     * <pre>
     *   2010-03-30 minus P1M3D  = 2010-02-27
     *   2010-03-30 minus P1M2D  = 2010-02-28
     *   2010-03-30 minus P1M1D  = 2010-02-28
     *   2010-03-30 minus P1M    = 2010-02-28
     *   2010-03-30 minus P1M-1D = 2010-03-01
     *   2010-03-30 minus P1M-2D = 2010-03-02
     * </pre>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param periodProvider  the period to subtract, not null
     * @return a {@code LocalDate} based on this date with the period subtracted, not null
     * @throws CalendricalException if the specified period cannot be converted to a {@code Period}
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate minus(PeriodProvider periodProvider) {
        Period period = Period.ofDateFields(periodProvider);
        long periodMonths = period.totalMonths();
        long periodDays = period.getDays();
        if (periodMonths == 0) {
            return minusDays(periodDays);  // optimization that also returns this for zero
        }
        long monthCount = ((long) year) * 12 + (month.getValue() - 1);
        long calcMonths = monthCount - periodMonths;  // safe overflow
        int newYear = YEAR.checkValidIntValue(MathUtils.floorDiv(calcMonths, 12));
        MonthOfYear newMonth = MonthOfYear.of(MathUtils.floorMod(calcMonths, 12) + 1);
        int newMonthLen = newMonth.lengthInDays(ISOChronology.isLeapYear(newYear));
        int newDay = Math.min(day, newMonthLen);
        if (periodDays > 0 && day > newMonthLen) {
            periodDays = Math.max(periodDays - (day - newMonthLen), 0);  // adjust for invalid days
        }
        return LocalDate.of(newYear, newMonth, newDay).minusDays(periodDays);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDate} with the specified period in years subtracted.
     * <p>
     * This method subtracts the specified amount from the years field in three steps:
     * <ol>
     * <li>Subtract the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2007-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2007-02-28, is selected instead.
     * <p>
     * This method does the same as {@code minusYears(years, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return a {@code LocalDate} based on this date with the years subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #minusYears(long, javax.time.calendar.DateResolver)
     */
    public LocalDate minusYears(long years) {
        return minusYears(years, DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in years subtracted.
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
     * @return a {@code LocalDate} based on this date with the years subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate minusYears(long years, DateResolver dateResolver) {
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (years == 0) {
            return this;
        }
        int newYear = YEAR.checkValidIntValue(year - years);  // safe overflow
        return resolveDate(dateResolver, newYear, month, day);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in months subtracted.
     * <p>
     * This method subtracts the specified amount from the months field in three steps:
     * <ol>
     * <li>Subtract the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-02-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-02-28, is selected instead.
     * <p>
     * This method does the same as {@code minusMonths(months, DateResolvers.previousValid())}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return a {@code LocalDate} based on this date with the months subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     * @see #minusMonths(long, javax.time.calendar.DateResolver)
     */
    public LocalDate minusMonths(long months) {
        return minusMonths(months, DateResolvers.previousValid());
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in months subtracted.
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
     * @return a {@code LocalDate} based on this date with the months subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate minusMonths(long months, DateResolver dateResolver) {
        ISOChronology.checkNotNull(dateResolver, "DateResolver must not be null");
        if (months == 0) {
            return this;
        }
        long monthCount = year * 12L + (month.getValue() - 1);
        long calcMonths = monthCount - months;  // safe overflow
        int newYear = YEAR.checkValidIntValue(MathUtils.floorDiv(calcMonths, 12));
        MonthOfYear newMonth = MonthOfYear.of(MathUtils.floorMod(calcMonths, 12) + 1);
        return resolveDate(dateResolver, newYear, newMonth, day);
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified period in weeks subtracted.
     * <p>
     * This method subtracts the specified amount in weeks from the days field decrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2009-01-07 minus one week would result in 2008-12-31.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return a {@code LocalDate} based on this date with the weeks subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate minusWeeks(long weeks) {
        return minusDays(MathUtils.safeMultiply(weeks, 7));
    }

    /**
     * Returns a copy of this {@code LocalDate} with the specified number of days subtracted.
     * <p>
     * This method subtracts the specified amount from the days field decrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2009-01-01 minus one day would result in 2008-12-31.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return a {@code LocalDate} based on this date with the days subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDate minusDays(long days) {
        if (days == 0) {
            return this;
        }
        long mjDay = MathUtils.safeSubtract(toModifiedJulianDay(), days);
        return LocalDate.ofModifiedJulianDay(mjDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether this {@code LocalDate} matches the specified matcher.
     * <p>
     * Matchers can be used to query the date.
     * A simple matcher might simply compare one of the fields, such as the year field.
     * A more complex matcher might check if the date is the last day of the month.
     *
     * @param matcher  the matcher to use, not null
     * @return true if this date matches the matcher, false otherwise
     */
    public boolean matches(CalendricalMatcher matcher) {
        return matcher.matchesCalendrical(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the date extracted from the calendrical matches this date.
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
     * Adjusts a date to have the value of this date.
     * <p>
     * This method implements the {@code DateAdjuster} interface.
     * It is intended that applications use {@link #with(DateAdjuster)} rather than this method.
     *
     * @param date  the date to be adjusted, not null
     * @return the adjusted date, not null
     */
    public LocalDate adjustDate(LocalDate date) {
        ISOChronology.checkNotNull(date, "LocalDate must not be null");
        return this.equals(date) ? date : this;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a local date-time formed from this date at the specified offset time.
     * <p>
     * This merges the two objects - {@code this} and the specified time -
     * to form an instance of {@code OffsetDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offsetTime  the offset time to use, not null
     * @return the offset date-time formed from this date and the specified time, not null
     */
    public OffsetDateTime atTime(OffsetTime offsetTime) {
        return OffsetDateTime.of(this, offsetTime);
    }

    /**
     * Returns a local date-time formed from this date at the specified time.
     * <p>
     * This merges the two objects - {@code this} and the specified time -
     * to form an instance of {@code LocalDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param localTime  the local time to use, not null
     * @return the local date-time formed from this date and the specified time, not null
     */
    public LocalDateTime atTime(LocalTime localTime) {
        return LocalDateTime.of(this, localTime);
    }

    /**
     * Returns a local date-time formed from this date at the specified time.
     * <p>
     * This merges the three values - {@code this} and the specified time -
     * to form an instance of {@code LocalDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to use, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to use, from 0 to 59
     * @return the local date-time formed from this date and the specified time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     */
    public LocalDateTime atTime(int hourOfDay, int minuteOfHour) {
        return atTime(LocalTime.of(hourOfDay, minuteOfHour));
    }

    /**
     * Returns a local date-time formed from this date at the specified time.
     * <p>
     * This merges the four values - {@code this} and the specified time -
     * to form an instance of {@code LocalDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to use, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to use, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return the local date-time formed from this date and the specified time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     */
    public LocalDateTime atTime(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        return atTime(LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute));
    }

    /**
     * Returns a local date-time formed from this date at the specified time.
     * <p>
     * This merges the five values - {@code this} and the specified time -
     * to form an instance of {@code LocalDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to use, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to use, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return the local date-time formed from this date and the specified time, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     */
    public LocalDateTime atTime(int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond) {
        return atTime(LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond));
    }

    /**
     * Returns a local date-time formed from this date at the time of midnight.
     * <p>
     * This merges the two objects - {@code this} and {@link LocalTime#MIDNIGHT} -
     * to form an instance of {@code LocalDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return the local date-time formed from this date and the time of midnight, not null
     */
    public LocalDateTime atMidnight() {
        return LocalDateTime.of(this, LocalTime.MIDNIGHT);
    }

    /**
     * Returns an offset date formed from this time and the specified offset.
     * <p>
     * This merges the two objects - {@code this} and the specified offset -
     * to form an instance of {@code OffsetDate}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the offset to use, not null
     * @return the offset date formed from this date and the specified offset, not null
     */
    public OffsetDate atOffset(ZoneOffset offset) {
        return OffsetDate.of(this, offset);
    }

    /**
     * Returns a zoned date-time from this date at the earliest valid time according
     * to the rules in the time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#postGapPreOverlap() post-gap pre-overlap} resolver.
     * This selects the date-time immediately after a gap and the earlier offset in overlaps.
     * This combination chooses the earliest valid local time on the date, typically midnight.
     * <p>
     * To convert to a specific time in a given time-zone call {@link #atTime(LocalTime)}
     * followed by {@link LocalDateTime#atZone(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date and the earliest valid time for the zone, not null
     */
    public ZonedDateTime atStartOfDayInZone(ZoneId zone) {
        return ZonedDateTime.of(this, LocalTime.MIDNIGHT, zone, ZoneResolvers.postGapPreOverlap());
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date to a {@code LocalDate}, trivially
     * returning {@code this}.
     *
     * @return {@code this}, not null
     */
    public LocalDate toLocalDate() {
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this {@code LocalDate} to Epoch Days.
     * <p>
     * The Epoch Day count is a simple incrementing count of days
     * where day 0 is 1970-01-01.
     *
     * @return the Epoch Day equivalent to this date
     */
    public long toEpochDay() {
        return toYearZeroDay() - ISOChronology.DAYS_0000_TO_1970;
    }

    /**
     * Converts this {@code LocalDate} to Modified Julian Days (MJD).
     * <p>
     * The Modified Julian Day count is a simple incrementing count of days
     * where day 0 is 1858-11-17.
     *
     * @return the Modified Julian Day equivalent to this date
     */
    public long toModifiedJulianDay() {
        return toYearZeroDay() - ISOChronology.DAYS_0000_TO_MJD_EPOCH;
    }

    /**
     * Converts this {@code LocalDate} to year zero days.
     * <p>
     * The year zero day count is a simple incrementing count of days
     * where day 0 is 0000-01-01.
     *
     * @return the year zero days count equal to this date
     */
    long toYearZeroDay() {
        long y = year;
        long m = month.getValue();
        long total = 0;
        total += 365 * y;
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
        } else {
            total -= y / -4 - y / -100 + y / -400;
        }
        total += ((367 * m - 362) / 12);
        total += day - 1;
        if (m > 2) {
            total--;
            if (ISOChronology.isLeapYear(year) == false) {
                total--;
            }
        }
        return total;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this {@code LocalDate} to another date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    public int compareTo(LocalDate other) {
        int cmp = MathUtils.safeCompare(year, other.year);
        if (cmp == 0) {
            cmp = month.compareTo(other.month);
            if (cmp == 0) {
                cmp = MathUtils.safeCompare(day, other.day);
            }
        }
        return cmp;
    }

    /**
     * Checks if this {@code LocalDate} is after the specified date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is after the specified date
     */
    public boolean isAfter(LocalDate other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this {@code LocalDate} is before the specified date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is before the specified date
     */
    public boolean isBefore(LocalDate other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this date is equal to another date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other date
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalDate) {
            LocalDate other = (LocalDate) obj;
            return (year == other.year && month == other.month && day == other.day);
        }
        return false;
    }

    /**
     * A hash code for this date.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        int yearValue = year;
        int monthValue = month.getValue();
        int dayValue = day;
        return (yearValue & 0xFFFFF800) ^ ((yearValue << 11) + (monthValue << 6) + (dayValue));
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this date as a {@code String}, such as {@code 2007-12-03}.
     * <p>
     * The output will be in the ISO-8601 format {@code yyyy-MM-dd}.
     *
     * @return a string representation of this date, not null
     */
    @Override
    public String toString() {
        int yearValue = year;
        int monthValue = month.getValue();
        int dayValue = day;
        int absYear = Math.abs(yearValue);
        StringBuilder buf = new StringBuilder(10);
        if (absYear < 1000) {
            if (yearValue < 0) {
                buf.append(yearValue - 10000).deleteCharAt(1);
            } else {
                buf.append(yearValue + 10000).deleteCharAt(0);
            }
        } else {
            if (yearValue > 9999) {
                buf.append('+');
            }
            buf.append(yearValue);
        }
        return buf.append(monthValue < 10 ? "-0" : "-")
            .append(monthValue)
            .append(dayValue < 10 ? "-0" : "-")
            .append(dayValue)
            .toString();
    }

    /**
     * Outputs this date as a {@code String} using the formatter.
     *
     * @param formatter  the formatter to use, not null
     * @return the formatted date string, not null
     * @throws UnsupportedOperationException if the formatter cannot print
     * @throws CalendricalException if an error occurs during printing
     */
    public String toString(DateTimeFormatter formatter) {
        ISOChronology.checkNotNull(formatter, "DateTimeFormatter must not be null");
        return formatter.print(this);
    }

}
