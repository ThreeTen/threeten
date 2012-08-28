/*
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.calendrical;

import static javax.time.calendrical.LocalPeriodUnit.DAYS;
import static javax.time.calendrical.LocalPeriodUnit.ERAS;
import static javax.time.calendrical.LocalPeriodUnit.FOREVER;
import static javax.time.calendrical.LocalPeriodUnit.HALF_DAYS;
import static javax.time.calendrical.LocalPeriodUnit.HOURS;
import static javax.time.calendrical.LocalPeriodUnit.MICROS;
import static javax.time.calendrical.LocalPeriodUnit.MILLIS;
import static javax.time.calendrical.LocalPeriodUnit.MINUTES;
import static javax.time.calendrical.LocalPeriodUnit.MONTHS;
import static javax.time.calendrical.LocalPeriodUnit.NANOS;
import static javax.time.calendrical.LocalPeriodUnit.SECONDS;
import static javax.time.calendrical.LocalPeriodUnit.WEEKS;
import static javax.time.calendrical.LocalPeriodUnit.WEEK_BASED_YEARS;
import static javax.time.calendrical.LocalPeriodUnit.YEARS;

import javax.time.DateTimes;
import javax.time.LocalDate;
import javax.time.Month;
import javax.time.DayOfWeek;
import javax.time.CalendricalException;
import javax.time.chrono.ChronoDate;
import javax.time.chrono.Chronology;
import javax.time.chrono.ISOChronology;

/**
 * A standard set of fields.
 * <p>
 * This set of fields provide field-based access to manipulate a date, time or date-time.
 * The standard set of fields can be extended by implementing {@link DateTimeField}.
 * 
 * <h4>Implementation notes</h4>
 * This is a final, immutable and thread-safe enum.
 */
public enum LocalDateTimeField implements DateTimeField {

    /**
     * The nano-of-second.
     * This counts the nanosecond within the second, from 0 to 999,999,999.
     * This field has the same meaning for all calendar systems.
     */
    NANO_OF_SECOND("NanoOfSecond", NANOS, SECONDS, DateTimeValueRange.of(0, 999999999)),
    /**
     * The nano-of-day.
     * This counts the nanosecond within the day, from 0 to (24 * 60 * 60 * 1,000,000,000) - 1.
     * This field has the same meaning for all calendar systems.
     */
    NANO_OF_DAY("NanoOfDay", NANOS, DAYS, DateTimeValueRange.of(0, 86400L * 1000000000L - 1)),
    /**
     * The micro-of-second.
     * This counts the microsecond within the second, from 0 to 999,999.
     * This field has the same meaning for all calendar systems.
     */
    MICRO_OF_SECOND("MicroOfSecond", MICROS, SECONDS, DateTimeValueRange.of(0, 999999)),
    /**
     * The micro-of-day.
     * This counts the microsecond within the day, from 0 to (24 * 60 * 60 * 1,000,000) - 1.
     * This field has the same meaning for all calendar systems.
     */
    MICRO_OF_DAY("MicroOfDay", MICROS, DAYS, DateTimeValueRange.of(0, 86400L * 1000000L - 1)),
    /**
     * The milli-of-second.
     * This counts the millisecond within the second, from 0 to 999.
     * This field has the same meaning for all calendar systems.
     */
    MILLI_OF_SECOND("MilliOfSecond", MILLIS, SECONDS, DateTimeValueRange.of(0, 999)),
    /**
     * The milli-of-day.
     * This counts the millisecond within the day, from 0 to (24 * 60 * 60 * 1,000) - 1.
     * This field has the same meaning for all calendar systems.
     */
    MILLI_OF_DAY("MilliOfDay", MILLIS, DAYS, DateTimeValueRange.of(0, 86400L * 1000L - 1)),
    /**
     * The second-of-minute.
     * This counts the second within the minute, from 0 to 59.
     * This field has the same meaning for all calendar systems.
     */
    SECOND_OF_MINUTE("SecondOfMinute", SECONDS, MINUTES, DateTimeValueRange.of(0, 59)),
    /**
     * The second-of-day.
     * This counts the second within the day, from 0 to (24 * 60 * 60) - 1.
     * This field has the same meaning for all calendar systems.
     */
    SECOND_OF_DAY("SecondOfDay", SECONDS, DAYS, DateTimeValueRange.of(0, 86400L - 1)),
    /**
     * The minute-of-hour.
     * This counts the minute within the hour, from 0 to 59.
     * This field has the same meaning for all calendar systems.
     */
    MINUTE_OF_HOUR("MinuteOfHour", MINUTES, HOURS, DateTimeValueRange.of(0, 59)),
    /**
     * The minute-of-day.
     * This counts the minute within the day, from 0 to (24 * 60) - 1.
     * This field has the same meaning for all calendar systems.
     */
    MINUTE_OF_DAY("MinuteOfDay", MINUTES, DAYS, DateTimeValueRange.of(0, (24 * 60) - 1)),
    /**
     * The hour-of-am-pm.
     * This counts the hour within the AM/PM, from 0 to 11.
     * This field has the same meaning for all calendar systems.
     */
    HOUR_OF_AMPM("HourOfAmPm", HOURS, HALF_DAYS, DateTimeValueRange.of(0, 11)),
    /**
     * The clock-hour-of-am-pm.
     * This counts the hour within the AM/PM, from 1 to 12.
     * This field has the same meaning for all calendar systems.
     */
    CLOCK_HOUR_OF_AMPM("ClockHourOfAmPm", HOURS, HALF_DAYS, DateTimeValueRange.of(1, 12)),
    /**
     * The hour-of-day.
     * This counts the hour within the day, from 0 to 23.
     * This field has the same meaning for all calendar systems.
     */
    HOUR_OF_DAY("HourOfDay", HOURS, DAYS, DateTimeValueRange.of(0, 23)),
    /**
     * The clock-hour-of-day.
     * This counts the hour within the AM/PM, from 1 to 24.
     * This field has the same meaning for all calendar systems.
     */
    CLOCK_HOUR_OF_DAY("ClockHourOfDay", HOURS, DAYS, DateTimeValueRange.of(1, 24)),
    /**
     * The am-pm-of-day.
     * This counts the AM/PM within the day, from 0 (AM) to 1 (PM).
     * This field has the same meaning for all calendar systems.
     */
    AMPM_OF_DAY("AmPmOfDay", HALF_DAYS, DAYS, DateTimeValueRange.of(0, 1)),
    /**
     * The day-of-week, such as Tuesday.
     * <p>
     * For ISO-8601, the days are numbered from Monday (1) to Sunday (7).
     * This field may have a different meaning in a non-ISO calendar system.
     */
    DAY_OF_WEEK("DayOfWeek", DAYS, WEEKS, DateTimeValueRange.of(1, 7)),
    /**
     * The aligned day-of-week within a month.
     * The value of this field is based on 7 day weeks where the first day of the
     * week is the first day of a given month.
     * Thus, day-of-month 1 to 7 will have aligned-day-of-week values from 1 to 7.
     * From day-of-month 8 to 14 the aligned-day-of-week values will again be from 1 to 7, and so on.
     * This is typically used with {@code ALIGNED_WEEK_OF_MONTH}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    ALIGNED_DAY_OF_WEEK_IN_MONTH("AlignedDayOfWeekInMonth", DAYS, WEEKS, DateTimeValueRange.of(1, 7)),
    /**
     * The aligned day-of-week within a year.
     * The value of this field is based on 7 day weeks where the first day of the
     * week is the first day of a given year.
     * Thus, day-of-year 1 to 7 will have aligned-day-of-week values from 1 to 7.
     * From day-of-year 8 to 14 the aligned-day-of-week values will again be from 1 to 7, and so on.
     * This is used with {@code ALIGNED_WEEK_OF_MONTH}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    ALIGNED_DAY_OF_WEEK_IN_YEAR("AlignedDayOfWeekInYear", DAYS, WEEKS, DateTimeValueRange.of(1, 7)),
    /**
     * The day-of-month.
     * <p>
     * For ISO-8601, the days are numbered from 1 to 31 in most months.
     * April, June, September, November have days from 1 to 30.
     * February has days from 1 to 28, or 29 in a leap year.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    DAY_OF_MONTH("DayOfMonth", DAYS, MONTHS, DateTimeValueRange.of(1, 28, 31)),
    /**
     * The day-of-year.
     * <p>
     * For ISO-8601, the days are numbered from 1 to 365 in standard years and 1 to 366 in leap years.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    DAY_OF_YEAR("DayOfYear", DAYS, YEARS, DateTimeValueRange.of(1, 365, 366)),
    /**
     * The epoch-day, based on the Java epoch of 1970-01-01 (ISO).
     * <p>
     * This field is the sequential count of days where 1970-01-01 (ISO) is zero.
     * <p>
     * All other date fields in this enum can have a different meaning in a non-ISO calendar system.
     * By contrast, this field always has the same meaning, permitting interoperation between calendars.
     */
    EPOCH_DAY("EpochDay", DAYS, FOREVER, DateTimeValueRange.of((long) (DateTimes.MIN_YEAR * 365.25), (long) (DateTimes.MAX_YEAR * 365.25))),
    /**
     * The aligned week within a month.
     * <p>
     * The value of this field is a count of 7 day weeks within a month where the
     * first week starts on the first day of a given month.
     * Thus, day-of-month values 1 to 7 are in aligned-week 1, while day-of-month values
     * 8 to 14 are in week 2, and so on.
     * <p>
     * This field is typically used with {@link ALIGNED_DAY_OF_WEEK_IN_MONTH}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    ALIGNED_WEEK_OF_MONTH("AlignedWeekOfMonth", WEEKS, MONTHS, DateTimeValueRange.of(1, 4, 5)),
    /**
     * The week within a month.
     * <p>
     * The value of this field is a count of 7 day weeks within a month.
     * For ISO-8601, the week starts on Monday and there must be at least 4 days in the first week.
     * Week one is the week starting on a Monday where there are at least 4 days in the month.
     * Thus, week one may start up to three days before the start of the month.
     * If the first week starts after the start of the month then the period before is week zero.
     * <p>
     * For example:<br />
     * - if the 1st day of the month is a Monday, week one starts on the 1st and there is no week zero<br />
     * - if the 2nd day of the month is a Monday, week one starts on the 2nd and the 1st is in week zero<br />
     * - if the 4th day of the month is a Monday, week one starts on the 4th and the 1st to 3rd is in week zero<br />
     * - if the 5th day of the month is a Monday, week two starts on the 5th and the 1st to 4th is in week one<br />
     * <p>
     * This field typically used with {@link DAY_OF_WEEK}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    WEEK_OF_MONTH("WeekOfMonth", WEEKS, MONTHS, DateTimeValueRange.of(0, 1, 4, 5)),
    /**
     * The week within a week-based-year.
     * <p>
     * This field is based on the concept of a week-based-year, rather than the normal year.
     * The week-based-year can start up to 3 days before or 3 days after the regular year.
     * Similarly, the week-based-year can end before or after the end of the regular year.
     * For ISO-8601, the week starts on Monday and it must have at least 4 days in it.
     * Thus, if the 1st day of the regular year is a Tuesday, then the week-based-year starts
     * on December 31st of the previous regular year. Similarly, if the 1st day of the regular
     * year is a Sunday, then the week-based-year starts on January 2nd.
     * <p>
     * Given this definition, the week of the week-based-year counts the week from one
     * to 52 or 53 within the week-based-year.
     * <p>
     * This field typically used with {@link DAY_OF_WEEK} and {@link WEEK_BASED_YEAR}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    WEEK_OF_WEEK_BASED_YEAR("WeekOfWeekBasedYear", WEEKS, WEEK_BASED_YEARS, DateTimeValueRange.of(1, 52, 53)),
    /**
     * The aligned week within a year.
     * The value of this field is a count of 7 day weeks within a month where the
     * first week starts on the first day of a given year.
     * Thus, day-of-year values 1 to 7 are in aligned-week 1, while day-of-year values
     * 8 to 14 are in week 2, and so on.
     * <p>
     * This field typically used with {@link ALIGNED_DAY_OF_WEEK_IN_YEAR}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    ALIGNED_WEEK_OF_YEAR("AlignedWeekOfYear", WEEKS, YEARS, DateTimeValueRange.of(1, 53)),
    /**
     * The week within a year.
     * <p>
     * The value of this field is a count of 7 day weeks within a year.
     * For ISO-8601, the week starts on Monday and there must be at least 4 days in the first week.
     * Week one is the week starting on a Monday where there are at least 4 days in the year.
     * Thus, week one may start up to three days before the start of the year.
     * If the first week starts after the start of the year then the period before is week zero.
     * <p>
     * For example:<br />
     * - if the 1st day of the year is a Monday, week one starts on the 1st and there is no week zero<br />
     * - if the 2nd day of the year is a Monday, week one starts on the 2nd and the 1st is in week zero<br />
     * - if the 4th day of the year is a Monday, week one starts on the 4th and the 1st to 3rd is in week zero<br />
     * - if the 5th day of the year is a Monday, week two starts on the 5th and the 1st to 4th is in week one<br />
     * <p>
     * This field typically used with {@link DAY_OF_WEEK}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    WEEK_OF_YEAR("WeekOfYear", WEEKS, YEARS, DateTimeValueRange.of(0, 1, 52, 53)),
    /**
     * The month-of-year, such as March.
     * <p>
     * For ISO-8601, the months are numbered from 1 to 12.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    MONTH_OF_YEAR("MonthOfYear", MONTHS, YEARS, DateTimeValueRange.of(1, 12)),
    /**
     * The epoch month based on the Java epoch of 1970-01-01.
     * <p>
     * For ISO-8601, the value is a sequential count of months where January 1970 is zero.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    EPOCH_MONTH("EpochMonth", MONTHS, FOREVER, DateTimeValueRange.of((DateTimes.MIN_YEAR - 1970L) * 12, (DateTimes.MAX_YEAR - 1970L) * 12L - 1L)),
    /**
     * The week-based-year.
     * <p>
     * This field is based on the concept of a week-based-year, rather than the normal year.
     * The week-based-year can start up to 3 days before or 3 days after the regular year.
     * Similarly, the week-based-year can end before or after the end of the regular year.
     * For ISO-8601, the week starts on Monday and it must have at least 4 days in it.
     * Thus, if the 1st day of the regular year is a Tuesday, then the week-based-year starts
     * on December 31st of the previous regular year. Similarly, if the 1st day of the regular
     * year is a Sunday, then the week-based-year starts on January 2nd.
     * <p>
     * This field typically used with {@link DAY_OF_WEEK} and {@link WEEK_OF_WEEK_BASED_YEAR}.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    WEEK_BASED_YEAR("WeekBasedYear", WEEK_BASED_YEARS, FOREVER, DateTimeValueRange.of(DateTimes.MIN_YEAR, DateTimes.MAX_YEAR)),
    /**
     * The year within the era.
     * <p>
     * For ISO-8601, there are two eras, see {@code #ERA}.
     * The year-of-era is always positive.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    YEAR_OF_ERA("YearOfEra", YEARS, FOREVER, DateTimeValueRange.of(1, DateTimes.MAX_YEAR)),
    /**
     * The year, such as 2012.
     * <p>
     * For ISO-8601, the standard ISO year.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    YEAR("Year", YEARS, FOREVER, DateTimeValueRange.of(DateTimes.MIN_YEAR, DateTimes.MAX_YEAR)),
    /**
     * The era.
     * <p>
     * For ISO-8601, there are two artificial eras.
     * The current one is from year one onwards.
     * The previous era is from year zero backwards.
     * This field may have a different meaning in a non-ISO calendar system.
     */
    ERA("Era", ERAS, FOREVER, DateTimeValueRange.of(1, 9999));

    private final String name;
    private final PeriodUnit baseUnit;
    private final PeriodUnit rangeUnit;
    private final DateTimeValueRange range;

    private LocalDateTimeField(String name, PeriodUnit baseUnit, PeriodUnit rangeUnit, DateTimeValueRange range) {
        this.name = name;
        this.baseUnit = baseUnit;
        this.rangeUnit = rangeUnit;
        this.range = range;
    }

    //-----------------------------------------------------------------------
    @Override
    public String getName() {
        return name;
    }

    @Override
    public PeriodUnit getBaseUnit() {
        return baseUnit;
    }

    @Override
    public PeriodUnit getRangeUnit() {
        return rangeUnit;
    }

    @Override
    public DateTimeValueRange range() {
        return range;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks that the specified value is valid for this field.
     * <p>
     * This validates that the value is within the outer range of valid values
     * returned by {@link #range()}.
     * 
     * @param value  the value to check
     * @return the value that was passed in
     */
    public long checkValidValue(long value) {  // JAVA8 default method on interface
        return range().checkValidValue(value, this);
    }

    /**
     * Checks that the specified value is valid and fits in an {@code int}.
     * <p>
     * This validates that the value is within the outer range of valid values
     * returned by {@link #range()}.
     * It also checks that all valid values are within the bounds of an {@code int}.
     * 
     * @param value  the value to check
     * @return the value that was passed in
     */
    public int checkValidIntValue(long value) {  // JAVA8 default method on interface
        return range().checkValidIntValue(value, this);
    }

    //-------------------------------------------------------------------------
    /**
     * Checks if this field is a date field.
     * 
     * @return true if a date field, false if a time field
     */
    public boolean isDateField() {
        return this.compareTo(DAY_OF_WEEK) >= 0;
    }

    @Override
    public int compare(DateTime calendrical1, DateTime calendrical2) {
        return DateTimes.safeCompare(calendrical1.get(this), calendrical2.get(this));
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTimeValueRange range(DateTime calendrical) {
        // TODO: should this be based on DateTime fields interface
        LocalDate date = calendrical.extract(LocalDate.class);
        if (date != null) {
            switch (this) {
                case DAY_OF_MONTH: return DateTimeValueRange.of(1, date.lengthOfMonth());
                case DAY_OF_YEAR: return DateTimeValueRange.of(1, date.lengthOfYear());
                case ALIGNED_WEEK_OF_MONTH: return DateTimeValueRange.of(1,
                            date.getMonth() == Month.FEBRUARY && date.isLeapYear() == false ? 4 : 5);
                case WEEK_OF_MONTH: throw new UnsupportedOperationException("TODO");
                case WEEK_OF_WEEK_BASED_YEAR: throw new UnsupportedOperationException("TODO");
                case WEEK_OF_YEAR: throw new UnsupportedOperationException("TODO");
            }
        }
        return range();
    }

    //-----------------------------------------------------------------------
    @Override
    public long doGet(DateTime calendrical) {
        return calendrical.get(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends DateTime> R doSet(R calendrical, long newValue) {
        return (R) calendrical.with(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * Synthesize year, month, day from this field and hour, minute, second, nanos
     * if the builder contains enough information.
     *
     * @param builder the builder
     * @param value the value of this field
     * @return {@code true} if the builder was changed; {@code false} otherwise
     *
     * @throws CalendricalException if there are not enough or the right
     *        fields to convert to the basic fields, year, month, and day.
     */
    @Override
    public boolean resolve(DateTimeBuilder builder, long value) {
        Chronology chrono = builder.extract(Chronology.class);
        if (chrono == null) {
            chrono = ISOChronology.INSTANCE;
        }
        switch (this) {
            case YEAR:
                return false;
            case MONTH_OF_YEAR:
                return false;
            case ALIGNED_WEEK_OF_YEAR:
                // Handled in ALIGNED_DAY_OF_WEEK_IN_YEAR
                return false;
            case ALIGNED_WEEK_OF_MONTH:
                // Handled in ALIGNED_DAY_OF_WEEK_IN_MONTH
                return false;
            case EPOCH_DAY: {
                // Expand EPOCH_DAY to YEAR, MONTH_OF_YEAR, DAY_OF_MONTH
                long ed = DateTimes.safeToInt(value);
                ChronoDate d = chrono.dateFromEpochDay(ed);
                builder.addFieldValue(DAY_OF_MONTH, d.getDayOfMonth());
                builder.addFieldValue(MONTH_OF_YEAR, d.getMonth());
                builder.addFieldValue(YEAR, d.getProlepticYear());
                break;
            }
            case DAY_OF_YEAR: {
                // Expand DAY_OF_YEAR to MONTH_OF_YEAR, DAY_OF_MONTH
                long y = builder.get(YEAR);
                long doy = value;
                ChronoDate d = chrono.date((int) y, (int) doy);
                builder.addFieldValue(DAY_OF_MONTH, d.getDayOfMonth());
                builder.addFieldValue(MONTH_OF_YEAR, d.getMonth());
                break;
            }
            case ALIGNED_DAY_OF_WEEK_IN_MONTH: {
                // Needs YEAR and ALIGNED_WEEK_OF_MONTH
                int y = DateTimes.safeToInt(builder.get(YEAR));
                int moy = DateTimes.safeToInt(builder.get(MONTH_OF_YEAR));
                int aw = DateTimes.safeToInt(builder.get(ALIGNED_WEEK_OF_MONTH));
                int ad = DateTimes.safeToInt(value);
                ChronoDate d = chrono.date(y, moy, 1).plusDays((aw - 1) * 7 + (ad - 1));
                builder.addFieldValue(YEAR, d.getProlepticYear());
                builder.addFieldValue(MONTH_OF_YEAR, d.getMonth());
                builder.addFieldValue(DAY_OF_MONTH, d.getDayOfMonth());
                break;
            }
            case ALIGNED_DAY_OF_WEEK_IN_YEAR: {
                // Needs YEAR and ALIGNED_WEEK_OF_YEAR
                int y = DateTimes.safeToInt(builder.get(YEAR));
                int aw = DateTimes.safeToInt(builder.get(ALIGNED_WEEK_OF_YEAR));
                int ad = DateTimes.safeToInt(value);
                int doy = (aw - 1) * 7 + (ad - 1);
                ChronoDate d = chrono.date(y, doy);
                builder.addFieldValue(MONTH_OF_YEAR, d.getMonth());
                builder.addFieldValue(DAY_OF_MONTH, d.getDayOfMonth());
                break;
            }
            case DAY_OF_WEEK: {
                // Needs either ALIGNED_WEEK_OF_YEAR or ALIGNED_WEEK_OF_MONTH + MONTH_OF_YEAR
                ChronoDate d = null;
                if (builder.containsFieldValue(ALIGNED_WEEK_OF_YEAR)) {
                    int y = DateTimes.safeToInt(builder.get(YEAR));
                    int aw = DateTimes.safeToInt(builder.get(ALIGNED_WEEK_OF_YEAR));
                    d = chrono.date(y, (aw - 1) * 7);
                } else if (builder.containsFieldValue(ALIGNED_WEEK_OF_MONTH)) {
                    int y = DateTimes.safeToInt(builder.get(YEAR));
                    int m = DateTimes.safeToInt(builder.get(MONTH_OF_YEAR));
                    int aw = DateTimes.safeToInt(builder.get(ALIGNED_WEEK_OF_MONTH));
                    d = chrono.date(y, m, (aw - 1) * 7);
                } else {
                    throw new CalendricalException("Not enough fields to resolve from DAY_OF_WEEK");
                }
                int dow = DateTimes.safeToInt(value);
                d = d.with(DateTimeAdjusters.nextOrCurrent(DayOfWeek.of(dow)));
                builder.addFieldValue(MONTH_OF_YEAR, d.getMonth());
                builder.addFieldValue(DAY_OF_MONTH, d.getDayOfMonth());
                break;
            }
            case HOUR_OF_DAY: {
                break;
            }
            case MINUTE_OF_HOUR: {
                break;
            }
            case SECOND_OF_MINUTE: {
                break;
            }
            case NANO_OF_SECOND: {
                break;
            }
            case AMPM_OF_DAY: {
                // Handled by HOUR_OF_AMPM or CLOCK_HOUR_OF_AMPM
                break;
            }
            case HOUR_OF_AMPM: {
                if (builder.containsFieldValue(AMPM_OF_DAY)) {
                    builder.addFieldValue(HOUR_OF_DAY, value + 12 * builder.getFieldValue(AMPM_OF_DAY));
                    return true;
                }
                return false;
            }
            case CLOCK_HOUR_OF_DAY: {
                builder.addFieldValue(HOUR_OF_DAY, value == 24 ? 0 : value);
                return true;
            }
            case CLOCK_HOUR_OF_AMPM: {
                if (builder.containsFieldValue(AMPM_OF_DAY)) {
                    builder.addFieldValue(HOUR_OF_DAY, (value == 12 ? 0 : value) + 12 * builder.getFieldValue(AMPM_OF_DAY));
                    return true;
                }
                return false;
            }
            case NANO_OF_DAY: {
                // TBD: is value >= 0?
                long seconds = value / DateTimes.NANOS_PER_SECOND;
                long minutes = seconds / DateTimes.SECONDS_PER_MINUTE;
                long hours = minutes / DateTimes.MINUTES_PER_HOUR;
                builder.addFieldValue(HOUR_OF_DAY, hours % DateTimes.HOURS_PER_DAY);
                builder.addFieldValue(MINUTE_OF_HOUR, minutes % DateTimes.MINUTES_PER_HOUR);
                builder.addFieldValue(SECOND_OF_MINUTE, seconds % DateTimes.SECONDS_PER_MINUTE);
                builder.addFieldValue(NANO_OF_SECOND, value % DateTimes.NANOS_PER_SECOND);
                return true;
            }
            case MICRO_OF_DAY: {
                long seconds = value / 1000000L;
                long minutes = seconds / DateTimes.SECONDS_PER_MINUTE;
                long hours = minutes / DateTimes.MINUTES_PER_HOUR;
                builder.addFieldValue(HOUR_OF_DAY, hours % DateTimes.HOURS_PER_DAY);
                builder.addFieldValue(MINUTE_OF_HOUR, minutes % DateTimes.MINUTES_PER_HOUR);
                builder.addFieldValue(SECOND_OF_MINUTE, seconds % DateTimes.SECONDS_PER_MINUTE);
                builder.addFieldValue(NANO_OF_SECOND, value % 1000000L * 1000L);
                return true;
            }
            case MILLI_OF_DAY: {
                long seconds = value / 1000L;
                long minutes = seconds / DateTimes.SECONDS_PER_MINUTE;
                long hours = minutes / DateTimes.MINUTES_PER_HOUR;
                builder.addFieldValue(HOUR_OF_DAY, hours % DateTimes.HOURS_PER_DAY);
                builder.addFieldValue(MINUTE_OF_HOUR, minutes % DateTimes.MINUTES_PER_HOUR);
                builder.addFieldValue(SECOND_OF_MINUTE, seconds % DateTimes.SECONDS_PER_MINUTE);
                builder.addFieldValue(NANO_OF_SECOND, value % 1000 * 1000000L);
                return true;
            }
            case SECOND_OF_DAY: {
                builder.addFieldValue(HOUR_OF_DAY, value / 3600);
                builder.addFieldValue(MINUTE_OF_HOUR, (value / 60) % 60);
                builder.addFieldValue(SECOND_OF_MINUTE, value % 60);
                return true;
            }
            case MINUTE_OF_DAY: {
                builder.addFieldValue(HOUR_OF_DAY, value / 60);
                builder.addFieldValue(MINUTE_OF_HOUR, value % 60);
                return true;
            }
            case MILLI_OF_SECOND: {
                // TBD: Odd combination; addFieldValue should fail since the value is different
                if (builder.containsFieldValue(MICRO_OF_SECOND)) {
                    long cos = builder.removeFieldValue(MICRO_OF_SECOND);
                    builder.addFieldValue(MICRO_OF_SECOND, value * 1000 + (cos % 1000));
                    return true;
                }
                return false;
            }

            default:
                break;
        }
        return false;  // resolve implemented in builder
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getName();
    }

}
