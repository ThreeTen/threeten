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
package javax.time.chrono;

import static javax.time.calendrical.LocalDateTimeField.WEEK_BASED_YEAR;
import static javax.time.calendrical.LocalDateTimeField.YEAR;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.time.DateTimeException;
import javax.time.DateTimes;
import javax.time.LocalDate;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.LocalDateTimeField;

/**
 * The Minguo calendar system.
 * <p>
 * This chronology defines the rules of the Minguo calendar system.
 * This calendar system is primarily used in the Republic of China, often known as Taiwan.
 * Dates are aligned such that {@code 0001-01-01 (Minguo)} is {@code 1911-01-01 (ISO)}.
 * <p>
 * The fields are defined as follows:
 * <ul>
 * <li>era - There are two eras, the current 'Republic' (ERA_ROC) and the previous era (ERA_BEFORE_ROC).
 * <li>year-of-era - The year-of-era for the current era increases uniformly from the epoch at year one.
 *  For the previous era the year increases from one as time goes backwards.
 *  The value for the current era is equal to the ISO proleptic-year minus 1911.
 * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
 *  current era. For the previous era, years have zero, then negative values.
 *  The value is equal to the ISO proleptic-year minus 1911.
 * <li>month-of-year - The Minguo month-of-year exactly matches ISO.
 * <li>day-of-month - The Minguo day-of-month exactly matches ISO.
 * <li>day-of-year - The Minguo day-of-year exactly matches ISO.
 * <li>leap-year - The Minguo leap-year pattern exactly matches ISO, such that the two calendars
 *  are never out of step.
 * </ul>
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class MinguoChronology extends Chronology<MinguoChronology> implements Serializable {

    /**
     * Singleton instance for the Minguo Chronology.
     */
    public static final MinguoChronology INSTANCE = new MinguoChronology();

    /**
     * The singleton instance for the era ROC.
     */
    public static final Era<MinguoChronology> ERA_ROC = MinguoEra.ERA_ROC;
    
    /**
     * The singleton instance for the era BEFORE_ROC.
     */
    public static final Era<MinguoChronology> ERA_BEFORE_ROC = MinguoEra.ERA_BEFORE_ROC;

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The difference in years between ISO and Minguo.
     */
    static final int YEARS_DIFFERENCE = 1911;

    /**
     * Restricted constructor.
     */
    private MinguoChronology() {
    }

    /**
     * Resolve singleton.
     * 
     * @return the singleton instance, not null
     */
    private Object readResolve() {
        return INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the ID of the chronology - 'Minguo'.
     * <p>
     * The ID uniquely identifies the {@code Chronology}.
     * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
     * 
     * @return the chronology ID - 'Minguo'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "Minguo";
    }

    /**
     * Gets the calendar type of the underlying calendar system - 'roc'.
     * <p>
     * The calendar type is an identifier defined by the
     * <em>Unicode Locale Data Markup Language (LDML)</em> specification.
     * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
     * It can also be used as part of a locale, accessible via
     * {@link Locale#getUnicodeLocaleType(String)} with the key 'ca'.
     * 
     * @return the calendar system type - 'roc'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "roc";
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoLocalDate<MinguoChronology> date(int prolepticYear, int month, int dayOfMonth) {
        return new MinguoDate(LocalDate.of(prolepticYear + YEARS_DIFFERENCE, month, dayOfMonth));
    }

    @Override
    public ChronoLocalDate<MinguoChronology> dateFromYearDay(int prolepticYear, int dayOfYear) {
        return new MinguoDate(LocalDate.ofYearDay(prolepticYear + YEARS_DIFFERENCE, dayOfYear));
    }

    @Override
    public ChronoLocalDate<MinguoChronology> date(DateTimeAccessor dateTime) {
        if (dateTime instanceof MinguoDate) {
            return (MinguoDate) dateTime;
        }
        return new MinguoDate(LocalDate.from(dateTime));
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified year is a leap year.
     * <p>
     * Minguo leap years occur exactly in line with ISO leap years.
     * This method does not validate the year passed in, and only has a
     * well-defined result for years in the supported range.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return DateTimes.isLeapYear(prolepticYear + YEARS_DIFFERENCE);
    }

    @Override
    public int prolepticYear(Era<MinguoChronology> era, int yearOfEra) {
        if (era instanceof MinguoEra == false) {
            throw new DateTimeException("Era must be MinguoEra");
        }
        return (era == MinguoEra.ERA_ROC ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public Era<MinguoChronology> eraOf(int eraValue) {
        return MinguoEra.of(eraValue);
    }

    @Override
    public List<Era<MinguoChronology>> eras() {
        return Arrays.<Era<MinguoChronology>>asList(MinguoEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTimeValueRange range(LocalDateTimeField field) {
        switch (field) {
            case WEEK_BASED_YEAR: {
                DateTimeValueRange range = WEEK_BASED_YEAR.range();
                return DateTimeValueRange.of(range.getMinimum() - YEARS_DIFFERENCE, range.getMaximum() - YEARS_DIFFERENCE);
            }
            case YEAR_OF_ERA: {
                DateTimeValueRange range = YEAR.range();
                return DateTimeValueRange.of(1, range.getMaximum() - YEARS_DIFFERENCE, -range.getMinimum() + 1 + YEARS_DIFFERENCE);
            }
            case YEAR: {
                DateTimeValueRange range = YEAR.range();
                return DateTimeValueRange.of(range.getMinimum() - YEARS_DIFFERENCE, range.getMaximum() - YEARS_DIFFERENCE);
            }
        }
        return field.range();
    }

}
