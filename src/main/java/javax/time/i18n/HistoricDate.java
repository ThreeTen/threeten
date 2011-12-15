/*
 * Copyright (c) 2010-2011, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.i18n;

import java.io.Serializable;

import javax.time.CalendricalException;
import javax.time.chronology.Calendrical;
import javax.time.chronology.CalendricalEngine;
import javax.time.chronology.CalendricalRule;
import javax.time.DayOfWeek;
import javax.time.chronology.IllegalCalendarFieldValueException;
import javax.time.chronology.InvalidCalendarFieldException;
import javax.time.LocalDate;
import javax.time.MonthOfYear;

/**
 * A date in the Historic calendar system.
 * <p>
 * HistoricDate is an immutable class that represents a date in the Historic calendar system.
 * The rules of the calendar system are described in {@link HistoricChronology}.
 * The date has a precision of one day and a range within the era from
 * year 1 to year 999,999,999 (inclusive).
 * <p>
 * This class is immutable and thread-safe.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
public final class HistoricDate
        implements Calendrical, Comparable<HistoricDate>, Serializable {

    /**
     * The standard cutover date between the Julian and Gregorian calendar system of 1582-10-15.
     */
    public static final LocalDate STANDARD_CUTOVER = LocalDate.of(1582, 10, 15);
    /**
     * The maximum valid year of era.
     * This is currently set to 999,999,999 but may be changed to increase
     * the valid range in a future version of the specification.
     */
    public static final int MAX_YEAR = 999999999;
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The cutover between the Julian and Gregorian calendar systems.
     */
    private final HistoricChronology chrono;
//    /**
//     * The backing date.
//     */
//    private final LocalDate date;
//    /**
//     * The modified julian day count.
//     */
//    private final long mjDay;
    /**
     * The historic year.
     */
    private final transient int year;
    /**
     * The historic month.
     */
    private final transient MonthOfYear month;
    /**
     * The historic day-of-month.
     */
    private final transient int day;

    //-----------------------------------------------------------------------
    /**
     * Gets the rule for {@code HistoricDate}.
     *
     * @return the rule for the date, not null
     */
    public static CalendricalRule<HistoricDate> rule() {
        return Rule.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDate} from a year, month and day
     * using the standard cutover of 1582-10-15.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     *
     * @param historicYear  the year to represent, from -(MAX_YEAR-1) to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static HistoricDate of(int historicYear, MonthOfYear monthOfYear, int dayOfMonth) {
        return of(STANDARD_CUTOVER, historicYear, monthOfYear, dayOfMonth);
    }

    /**
     * Obtains an instance of {@code LocalDate} from a year, month and day
     * specifying the cutover date to use.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     *
     * @param historicYear  the year to represent, from -(MAX_YEAR-1) to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the local date, not null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static HistoricDate of(LocalDate cutover, int historicYear, MonthOfYear monthOfYear, int dayOfMonth) {
        HistoricChronology.checkNotNull(cutover, "Cutover date must not be null");
        HistoricChronology chrono = HistoricChronology.cutoverAt(cutover);
        chrono.yearRule().checkValidValue(historicYear);
        HistoricChronology.checkNotNull(monthOfYear, "MonthOfYear must not be null");
        chrono.dayOfMonthRule().checkValidValue(dayOfMonth);
        return new HistoricDate(chrono, historicYear, monthOfYear, dayOfMonth);
    }

    /**
     * Obtains an instance of {@code HistoricDate} from a calendrical.
     * <p>
     * This can be used extract the date directly from any implementation
     * of {@code Calendrical}, including those in other calendar systems.
     *
     * @param calendrical  the calendrical to extract from, not null
     * @return the Historic date, not null
     * @throws CalendricalException if the day-of-week cannot be obtained
     */
    public static HistoricDate of(Calendrical calendrical) {
        return rule().getValueChecked(calendrical);
    }

//    /**
//     * Obtains an instance of {@code HistoricDate} from a number of epoch days.
//     *
//     * @param epochDay  the epoch days to use, not null
//     * @return a HistoricDate object, not null
//     * @throws IllegalCalendarFieldValueException if the year range is exceeded
//     */
//    private static HistoricDate historicDateFromEpochDay(int epochDay) {
//        if (epochDay < MIN_EPOCH_DAY || epochDay > MAX_EPOCH_DAY) {
//            throw new IllegalCalendarFieldValueException(
//                    "Date exceeds supported range for HistoricDate", HistoricChronology.yearRule());
//        }
//        int year = ((epochDay * 4) + 1463) / 1461;
//        int startYearEpochDay = (year - 1) * 365 + (year / 4);
//        int doy0 = epochDay - startYearEpochDay;
//        int month = doy0 / 30 + 1;
//        int day = doy0 % 30 + 1;
//        return new HistoricDate(epochDay, year, month, day);
//    }

    //-----------------------------------------------------------------------
    /**
     * Constructs an instance with the specified date.
     *
     * @param chrono  the chronology, not null
     * @param year  the year to represent, valid
     * @param month  the month-of-year to represent, not null
     * @param day  the day-of-month to represent, valid
     */
    HistoricDate(HistoricChronology chrono, int year, MonthOfYear month, int day) {
        this.chrono = chrono;
        this.year = year;
        this.month = month;
        this.day = day;
    }

//    /**
//     * Replaces the date instance from the stream with a valid one.
//     *
//     * @return the resolved date, not null
//     */
//    private Object readResolve() {
//        return historicDateFromEpochDay(epochDay);
//    }

    /**
     * Obtains an instance of {@code HistoricDate} using the previous valid algorithm.
     *
     * @param historicYear  the year to represent
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the historic date, not null
     */
    private HistoricDate previousValid(int historicYear, MonthOfYear monthOfYear, int dayOfMonth) {
        chrono.yearRule().checkValidValue(historicYear);
        HistoricChronology.checkNotNull(monthOfYear, "MonthOfYear must not be null");
        chrono.dayOfMonthRule().checkValidValue(dayOfMonth);
        int lastDay = monthOfYear.getLastDayOfMonth(chrono.isLeapYear(year));
        if (dayOfMonth > lastDay) {
            dayOfMonth = lastDay;
        }
        // TODO: Handle cutover gap
        return new HistoricDate(chrono, year, monthOfYear, dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the chronology that this date uses, which is the historic calendar system.
     *
     * @return the historic chronology, not null
     */
    public HistoricChronology getChronology() {
        return chrono;
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
    @SuppressWarnings("unchecked")
    public <T> T get(CalendricalRule<T> ruleToDerive) {
        if (ruleToDerive == rule()) {
            return (T) this;
        }
        return CalendricalEngine.derive(ruleToDerive, rule(), toLocalDate(), null, null, null, getChronology(), null);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the historic era.
     * <p>
     * The era provides a context for the year-of-era.
     * This calendar system defines two eras, BCE and CE.
     *
     * @return the era, not null
     */
    public HistoricEra getEra() {
        return (year < 1 ? HistoricEra.BCE : HistoricEra.CE);
    }

    /**
     * Gets the historic year-of-era value.
     * <p>
     * The year-of-era is a value that matches the historic definition.
     * Thus, both 1 AD and 1 BCE are represented as year-of-era 1.
     *
     * @return the year, from 1 to MAX_YEAR
     */
    public int getYearOfEra() {
        return (year < 1 ? -(year - 1) : year);
    }

    /**
     * Gets the historic year value, which can be negative.
     * <p>
     * The year is value that is continuous.
     * Thus, 1 AD is represented as year 1, and 1 BCE is represented as year 0.
     *
     * @return the year, from -(MAX_YEAR-1) to MAX_YEAR
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the historic month-of-year value.
     *
     * @return the month-of-year, not null
     */
    public MonthOfYear getMonthOfYear() {
        return month;
    }

    /**
     * Gets the historic day-of-month value.
     *
     * @return the day-of-month, from 1 to 30
     */
    public int getDayOfMonth() {
        return day;
    }

    /**
     * Gets the historic day-of-year value.
     *
     * @return the day-of-year, from 1 to 366
     */
    public int getDayOfYear() {
        return chrono.getDayOfYear(this);
    }

    /**
     * Gets the historic day-of-week.
     *
     * @return the day-of-week, not null
     */
    public DayOfWeek getDayOfWeek() {
        return toLocalDate().getDayOfWeek();
//        return DayOfWeek.of((int) ((mjDay + 4) % 7) + 1);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the date represented is a leap year.
     *
     * @return true if this date is in a leap year
     */
    public boolean isLeapYear() {
        return chrono.isLeapYear(getYear());
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date with the year value altered.
     * <p>
     * The result of setting the year may leave the day-of-month invalid.
     * To avoid this, the day-of-month is changed to the largest valid value.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param historicYear  the year to represent, from MIN_YEAR to MAX_YEAR
     * @return a {@code HistoricDate} based on this date with the specified year, not null
     * @throws IllegalCalendarFieldValueException if the year is out of range
     */
    public HistoricDate withYear(int historicYear) {
        return previousValid(historicYear, getMonthOfYear(), getDayOfMonth());
    }

    /**
     * Returns a copy of this date with the month-of-year value altered.
     * <p>
     * The result of setting the month may leave the day-of-month invalid.
     * To avoid this, the day-of-month is changed to the largest valid value.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to represent, from 1 to 12
     * @return a {@code HistoricDate} based on this date with the specified month, not null
     */
    public HistoricDate withMonthOfYear(MonthOfYear monthOfYear) {
        return previousValid(getYear(), monthOfYear, getDayOfMonth());
    }

    /**
     * Returns a copy of this date with the day-of-month value altered.
     * <p>
     * The specified day-of-month must be valid for the month and year.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return a {@code HistoricDate} based on this date with the specified day, not null
     * @throws IllegalCalendarFieldValueException if the day is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the year and month
     */
    public HistoricDate withDayOfMonth(int dayOfMonth) {
        return of(getYear(), getMonthOfYear(), dayOfMonth);
    }

    /**
     * Returns a copy of this date with the day-of-year value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to represent, from 1 to 366
     * @return a {@code HistoricDate} based on this date with the specified day, not null
     * @throws IllegalCalendarFieldValueException if the day-of-year is out of range
     * @throws InvalidCalendarFieldException if the day-of-year is invalid for the year
     */
    public HistoricDate withDayOfYear(int dayOfYear) {
        chrono.dayOfYearRule().checkValidValue(dayOfYear);
        return chrono.getDateFromDayOfYear(year, dayOfYear);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date with the specified number of years added.
     * <p>
     * The result of changing the year may leave the day-of-month invalid.
     * To avoid this, the day-of-month is changed to the largest valid value.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, positive or negative
     * @return a {@code HistoricDate} based on this date with the specified years added, not null
     * @throws IllegalCalendarFieldValueException if the year range is exceeded
     */
    public HistoricDate plusYears(int years) {
        int newYear = getYear() + years;  // TODO: check overflow
        return previousValid(newYear, month, day);
    }

    /**
     * Returns a copy of this date with the specified number of months added.
     * <p>
     * The result of changing the month may leave the day-of-month invalid.
     * To avoid this, the day-of-month is changed to the largest valid value.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, positive or negative
     * @return a {@code HistoricDate} based on this date with the specified months added, not null
     * @throws IllegalCalendarFieldValueException if the year range is exceeded
     */
    public HistoricDate plusMonths(int months) {
        int month0 = (months % 12) + 12 + getMonthOfYear().ordinal();
        int years = (months / 12) - 1 + (month0 / 12);
        month0 %= 12;
        int newYear = getYear() + years;  // TODO: check overflow
        return previousValid(newYear, MonthOfYear.of(month0 + 1), day);
    }

    /**
     * Returns a copy of this date with the specified number of days added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, positive or negative
     * @return a {@code HistoricDate} based on this date with the specified days added, not null
     * @throws IllegalCalendarFieldValueException if the year range is exceeded
     */
    public HistoricDate plusDays(int days) {
        return of(toLocalDate().plusDays(days));  // TODO: better
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date to an ISO-8601 calendar system {@code LocalDate}.
     *
     * @return the equivalent date in the ISO-8601 calendar system, not null
     */
    public LocalDate toLocalDate() {
        LocalDate possible = LocalDate.of(year, month, day);
        if (possible.isBefore(chrono.getCutover())) {
            long julYear1Days = (year - 1) * 365 + (year / 4) + chrono.getDayOfYear(this) - 1;
            return LocalDate.ofModifiedJulianDay(julYear1Days + 0);  // TODO
        } else {
            return possible;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this date to the specified date.
     * <p>
     * The comparison is based on the year, month, day and cutover date.
     *
     * @param otherDate  the other date instance to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    public int compareTo(HistoricDate otherDate) {
        int cmp = toLocalDate().compareTo(otherDate.toLocalDate());
        if (cmp == 0 ) {
            cmp = chrono.getCutover().compareTo(otherDate.chrono.getCutover());
        }
        return cmp;
    }

    /**
     * Checks is this date is after the specified date.
     * <p>
     * The comparison is based on the year, month, day and cutover date.
     *
     * @param otherDate  the other date instance to compare to, not null
     * @return true if this day is after the specified day
     */
    public boolean isAfter(HistoricDate otherDate) {
        return compareTo(otherDate) > 0;
    }

    /**
     * Checks is this date is before the specified date.
     * <p>
     * The comparison is based on the year, month, day and cutover date.
     *
     * @param otherDate  the other date instance to compare to, not null
     * @return true if this day is before the specified day
     */
    public boolean isBefore(HistoricDate otherDate) {
        return compareTo(otherDate) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks is this date is equal to the specified date.
     * <p>
     * The comparison is based on the year, month, day and cutover date.
     *
     * @param otherDate  the other date instance to compare to, null returns false
     * @return true if this day is equal to the specified day
     */
    @Override
    public boolean equals(Object otherDate) {
        if (this == otherDate) {
            return true;
        }
        if (otherDate instanceof HistoricDate) {
            HistoricDate other = (HistoricDate) otherDate;
            return year == other.year &&
                    month == other.month &&
                    day == other.day &&
                    chrono.getCutover().equals(other.chrono.getCutover());
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
        return (yearValue & 0xFFFFF800) ^ ((yearValue << 11) + (monthValue << 6) + (dayValue)) +
                chrono.getCutover().hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs the date as a {@code String}, such as '1723-13-01 (Historic 2010-10-15)'.
     * <p>
     * The output will be in the format 'yyyy-MM-dd (Historic 2010-10-15)' where
     * 2010-10-15 is the cutover date.
     *
     * @return the formatted date string, not null
     */
    @Override
    public String toString() {
        int yearValue = getYear();
        int monthValue = getMonthOfYear().getValue();
        int dayValue = getDayOfMonth();
        int absYear = Math.abs(yearValue);
        StringBuilder buf = new StringBuilder(12);
        if (absYear < 1000) {
            buf.append(yearValue + 10000).deleteCharAt(0);
        } else {
            buf.append(yearValue);
        }
        return buf.append(monthValue < 10 ? "-0" : "-")
            .append(monthValue)
            .append(dayValue < 10 ? "-0" : "-")
            .append(dayValue)
            .append(" (" + chrono.getName() + ")")
            .toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    static final class Rule extends CalendricalRule<HistoricDate> implements Serializable {
        private static final CalendricalRule<HistoricDate> INSTANCE = new Rule();
        private static final long serialVersionUID = 1L;
        private Rule() {
            super(HistoricDate.class, "HistoricDate");
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        protected HistoricDate deriveFrom(CalendricalEngine engine) {
            LocalDate date = engine.getDate(true);
            if (date == null) {
                return null;
            }
//            long epochDay = ld.toModifiedJulianDay() + MJD_TO_historic;
//            return historicDateFromEpochDay((int) epochDay);
            return null; // TODO
        }
    }

}
