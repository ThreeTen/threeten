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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.time.DateTimeException;
import javax.time.LocalDate;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.jdk8.Jdk8Methods;

/**
 * The Thai Buddhist calendar system.
 * <p>
 * This chronology defines the rules of the Thai Buddhist calendar system.
 * This calendar system is primarily used in Thailand.
 * Dates are aligned such that {@code 2484-01-01 (Buddhist)} is {@code 1941-01-01 (ISO)}.
 * <p>
 * The fields are defined as follows:
 * <ul>
 * <li>era - There are two eras, the current 'Buddhist' (ERA_BE) and the previous era (ERA_BEFORE_BE).
 * <li>year-of-era - The year-of-era for the current era increases uniformly from the epoch at year one.
 *  For the previous era the year increases from one as time goes backwards.
 *  The value for the current era is equal to the ISO proleptic-year plus 543.
 * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
 *  current era. For the previous era, years have zero, then negative values.
 *  The value is equal to the ISO proleptic-year plus 543.
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
public final class BuddhistChronology extends Chronology<BuddhistChronology> implements Serializable {

    /**
     * Singleton instance of the Buddhist Chronology.
     */
    public static final BuddhistChronology INSTANCE = new BuddhistChronology();
    /**
     * The singleton instance for the era before the current one - Before Buddhist -
     * which has the value 0.
     */
    public static final Era<BuddhistChronology> ERA_BEFORE_BE = BuddhistEra.ERA_BEFORE_BE;
    /**
     * The singleton instance for the current era - Buddhist - which has the value 1.
     */
    public static final Era<BuddhistChronology> ERA_BE = BuddhistEra.ERA_BE;

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Containing the offset to add to the ISO year.
     */
    static final int YEARS_DIFFERENCE = 543;
    /**
     * Narrow names for eras.
     */
    private static final HashMap<String, String[]> ERA_NARROW_NAMES = new HashMap<>();
    /**
     * Short names for eras.
     */
    private static final HashMap<String, String[]> ERA_SHORT_NAMES = new HashMap<>();
    /**
     * Full names for eras.
     */
    private static final HashMap<String, String[]> ERA_FULL_NAMES = new HashMap<>();
    /**
     * Fallback language for the era names.
     */
    private static final String FALLBACK_LANGUAGE = "en";
    /**
     * Language that has the era names.
     */
    private static final String TARGET_LANGUAGE = "th";
    /**
     * Name data.
     */
    static {
        ERA_NARROW_NAMES.put(FALLBACK_LANGUAGE, new String[]{"BB", "BE"});
        ERA_NARROW_NAMES.put(TARGET_LANGUAGE, new String[]{"BB", "BE"});
        ERA_SHORT_NAMES.put(FALLBACK_LANGUAGE, new String[]{"B.B.", "B.E."});
        ERA_SHORT_NAMES.put(TARGET_LANGUAGE,
                new String[]{"\u0e1e.\u0e28.",
                "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"});
        ERA_FULL_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Before Buddhist", "Budhhist Era"});
        ERA_FULL_NAMES.put(TARGET_LANGUAGE,
                new String[]{"\u0e1e\u0e38\u0e17\u0e18\u0e28\u0e31\u0e01\u0e23\u0e32\u0e0a",
                "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"});
    }

    /**
     * Restricted constructor.
     */
    private BuddhistChronology() {
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
     * Gets the ID of the chronology - 'Buddhist'.
     * <p>
     * The ID uniquely identifies the {@code Chronology}.
     * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
     * 
     * @return the chronology ID - 'Buddhist'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "Buddhist";
    }

    /**
     * Gets the calendar type of the underlying calendar system - 'buddhist'.
     * <p>
     * The calendar type is an identifier defined by the
     * <em>Unicode Locale Data Markup Language (LDML)</em> specification.
     * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
     * It can also be used as part of a locale, accessible via
     * {@link Locale#getUnicodeLocaleType(String)} with the key 'ca'.
     * 
     * @return the calendar system type - 'buddhist'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "buddhist";
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoLocalDate<BuddhistChronology> date(int prolepticYear, int month, int dayOfMonth) {
        return BuddhistDate.of(prolepticYear, month, dayOfMonth);
    }

    @Override
    public ChronoLocalDate<BuddhistChronology> dateFromYearDay(int prolepticYear, int dayOfYear) {
        return BuddhistDate.ofYearDay(prolepticYear, dayOfYear);
    }

    @Override
    public ChronoLocalDate<BuddhistChronology> date(DateTimeAccessor dateTime) {
        if (dateTime instanceof MinguoDate) {
            return (BuddhistDate) dateTime;
        }
        return new BuddhistDate(LocalDate.from(dateTime));
    }
    
    //-----------------------------------------------------------------------
    /**
     * Checks if the specified year is a leap year.
     * <p>
     * Thai Buddhist leap years occur exactly in line with ISO leap years.
     * This method does not validate the year passed in, and only has a
     * well-defined result for years in the supported range.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return ISOChronology.INSTANCE.isLeapYear(prolepticYear - YEARS_DIFFERENCE);
    }

    @Override
    public int prolepticYear(Era<BuddhistChronology> era, int yearOfEra) {
        if (era instanceof BuddhistEra == false) {
            throw new DateTimeException("Era must be BuddhistEra");
        }
        return (era == BuddhistEra.ERA_BE ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public Era<BuddhistChronology> eraOf(int eraValue) {
        return BuddhistEra.of(eraValue);
    }

    @Override
    public List<Era<BuddhistChronology>> eras() {
        return Arrays.<Era<BuddhistChronology>>asList(BuddhistEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTimeValueRange range(LocalDateTimeField field) {
        switch (field) {
            case WEEK_BASED_YEAR: {
                DateTimeValueRange range = WEEK_BASED_YEAR.range();
                return DateTimeValueRange.of(range.getMinimum() + YEARS_DIFFERENCE, range.getMaximum() + YEARS_DIFFERENCE);
            }
            case YEAR_OF_ERA: {
                DateTimeValueRange range = YEAR.range();
                return DateTimeValueRange.of(1, -(range.getMinimum() + YEARS_DIFFERENCE) + 1, range.getMaximum() + YEARS_DIFFERENCE);
            }
            case YEAR: {
                DateTimeValueRange range = YEAR.range();
                return DateTimeValueRange.of(range.getMinimum() + YEARS_DIFFERENCE, range.getMaximum() + YEARS_DIFFERENCE);
            }
        }
        return field.range();
    }

}
