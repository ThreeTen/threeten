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
 * The ISO calendar system.
 * <p>
 * This chronology defines the rules of the ISO calendar system.
 * This calendar system is based on the ISO-8601 standard, which is the
 * <i>de facto</i> world calendar.
 * <p>
 * The fields are defined as follows:
 * <ul>
 * <li>era - There are two eras, 'Current Era' (CE) and 'Before Current Era' (BCE).
 * <li>year-of-era - The year-of-era is the same as the proleptic-year for the current CE era.
 *  For the BCE era before the ISO epoch the year increases from 1 upwards as time goes backwards.
 * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
 *  current era. For the previous era, years have zero, then negative values.
 * <li>month-of-year - There are 12 months in an ISO year, numbered from 1 to 12.
 * <li>day-of-month - There are between 28 and 31 days in each of the ISO month, numbered from 1 to 31.
 *  Months 4, 6, 9 and 11 have 30 days, Months 1, 3, 5, 7, 8, 10 and 12 have 31 days.
 *  Month 2 has 28 days, or 29 in a leap year.
 * <li>day-of-year - There are 365 days in a standard ISO year and 366 in a leap year.
 *  The days are numbered from 1 to 365 or 1 to 366.
 * <li>leap-year - Leap years occur every 4 years, except where the year is divisble by 100 and not divisble by 400.
 * </ul>
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class ISOChronology extends Chronology implements Serializable {

    /**
     * Singleton instance.
     */
    public static final ISOChronology INSTANCE = new ISOChronology();

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Restricted constructor.
     */
    private ISOChronology() {
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
        return "ISO";
    }

    @Override
    public String getCalendarType() {
        return "iso8601";
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoDate date(int prolepticYear, int month, int dayOfMonth) {
        return new ISODate(LocalDate.of(prolepticYear, month, dayOfMonth));
    }

    @Override
    public ChronoDate dateFromYearDay(int prolepticYear, int dayOfYear) {
        return new ISODate(LocalDate.ofYearDay(prolepticYear, dayOfYear));
    }

    @Override
    public ChronoDate date(DateTimeAccessor calendrical) {
        if (calendrical instanceof LocalDate) {
            return new ISODate((LocalDate) calendrical);
        }
        if (calendrical instanceof ISODate) {
            return (ISODate) calendrical;
        }
        return super.date(calendrical);
    }

    @Override
    public ChronoDate dateFromEpochDay(long epochDay) {
        return new ISODate(LocalDate.ofEpochDay(epochDay));
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified year is a leap year.
     * <p>
     * ISO leap years occur exactly in line with ISO leap years.
     * This method does not validate the year passed in, and only has a
     * well-defined result for years in the supported range.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return DateTimes.isLeapYear(prolepticYear);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof ISOEra == false) {
            throw new DateTimeException("Era must be ISOEra");
        }
        return (era == ISOEra.ISO_CE ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public ISOEra createEra(int eraValue) {
        return ISOEra.of(eraValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTimeValueRange range(LocalDateTimeField field) {
        return field.range();
    }

}
