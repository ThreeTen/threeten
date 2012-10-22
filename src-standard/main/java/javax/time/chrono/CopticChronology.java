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

import java.io.Serializable;

import javax.time.DateTimeException;
import javax.time.DateTimes;
import javax.time.LocalDate;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.LocalDateTimeField;

/**
 * The Coptic calendar system.
 * <p>
 * This chronology defines the rules of the Coptic calendar system.
 * This calendar system is primarily used in Christian Egypt.
 * Dates are aligned such that {@code 0001AM-01-01 (Coptic)} is {@code 0284-08-29 (ISO)}.
 * <p>
 * The fields are defined as follows:
 * <ul>
 * <li>era - There are two eras, the current 'Era of the Martyrs' (AM) and the previous era (BEFORE_AM).
 * <li>year-of-era - The year-of-era for the current era increases uniformly from the epoch at year one.
 *  For the previous era the year increases from one as time goes backwards.
 * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
 *  current era. For the previous era, years have zero, then negative values.
 * <li>month-of-year - There are 13 months in a Coptic year, numbered from 1 to 13.
 * <li>day-of-month - There are 30 days in each of the first 12 Coptic months, numbered 1 to 30.
 *  The 13th month has 5 days, or 6 in a leap year, numbered 1 to 5 or 1 to 6.
 * <li>day-of-year - There are 365 days in a standard Coptic year and 366 in a leap year.
 *  The days are numbered from 1 to 365 or 1 to 366.
 * <li>leap-year - Leap years occur every 4 years.
 * </ul>
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class CopticChronology extends Chronology implements Serializable {

    /**
     * Singleton instance.
     */
    public static final CopticChronology INSTANCE = new CopticChronology();

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Range of months.
     */
    static final DateTimeValueRange MOY_RANGE = DateTimeValueRange.of(1, 13);
    /**
     * Range of days.
     */
    static final DateTimeValueRange DOM_RANGE = DateTimeValueRange.of(1, 5, 30);
    /**
     * Range of days.
     */
    static final DateTimeValueRange DOM_RANGE_NONLEAP = DateTimeValueRange.of(1, 5);
    /**
     * Range of days.
     */
    static final DateTimeValueRange DOM_RANGE_LEAP = DateTimeValueRange.of(1, 6);

    /**
     * Restricted constructor.
     */
    private CopticChronology() {
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
    @Override
    public String getName() {
        return "Coptic";
    }

    @Override
    public String getCalendarType() {
        return "coptic";
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoDate date(int prolepticYear, int month, int dayOfMonth) {
        return new CopticDate(prolepticYear, month, dayOfMonth);
    }

    @Override
    public ChronoDate dateFromYearDay(int prolepticYear, int dayOfYear) {
        return new CopticDate(prolepticYear, (dayOfYear - 1) / 30 + 1, (dayOfYear - 1) % 30 + 1);
    }

    @Override
    public ChronoDate date(DateTimeAccessor calendrical) {
        if (calendrical instanceof LocalDate) {
            return dateFromEpochDay(((LocalDate) calendrical).toEpochDay());
        }
        if (calendrical instanceof CopticDate) {
            return (CopticDate) calendrical;
        }
        return super.date(calendrical);
    }

    @Override
    public ChronoDate dateFromEpochDay(long epochDay) {
        return CopticDate.ofEpochDay(epochDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified year is a leap year.
     * <p>
     * A Coptic proleptic-year is leap if the remainder after division by four equals three.
     * This method does not validate the year passed in, and only has a
     * well-defined result for years in the supported range.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return DateTimes.floorMod(prolepticYear, 4) == 3;
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof CopticEra == false) {
            throw new DateTimeException("Era must be CopticEra");
        }
        return (era == CopticEra.AM ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public CopticEra createEra(int eraValue) {
        return CopticEra.of(eraValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTimeValueRange range(LocalDateTimeField field) {
        switch (field) {
            case DAY_OF_MONTH: return DateTimeValueRange.of(1, 5, 30);
            case ALIGNED_WEEK_OF_MONTH: return DateTimeValueRange.of(1, 1, 5);
            case MONTH_OF_YEAR: return DateTimeValueRange.of(1, 13);
            case EPOCH_MONTH: return DateTimeValueRange.of(-1000, 1000);  // TODO
            case YEAR_OF_ERA: return DateTimeValueRange.of(1, 999, 1000);  // TODO
            case YEAR: return DateTimeValueRange.of(-1000, 1000);  // TODO
        }
        return field.range();
    }

}
