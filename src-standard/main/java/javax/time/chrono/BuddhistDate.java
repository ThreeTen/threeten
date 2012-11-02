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

import static javax.time.calendrical.LocalDateTimeField.YEAR;
import static javax.time.chrono.BuddhistChronology.YEARS_DIFFERENCE;

import java.io.Serializable;
import java.util.Objects;

import javax.time.DateTimeException;
import javax.time.LocalDate;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.jdk8.Jdk8Methods;

/**
 * A date in the Thai Buddhist calendar system.
 * <p>
 * This implements {@code ChronoLocalDate} for the {@link BuddhistChronology Thai Buddhist calendar}.
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
final class BuddhistDate extends ChronoDateImpl<BuddhistChronology>
        implements Comparable<ChronoLocalDate<BuddhistChronology>>, Serializable {
    // this class is package-scoped so that future conversion to public
    // would not change serialization

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The underlying date.
     */
    private final LocalDate isoDate;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code BuddhistDate} from the Thai Buddhist proleptic year,
     * month-of-year and day-of-month. This uses the Thai Buddhist era.
     *
     * @param prolepticYear  the year to represent in the Thai Buddhist era, from 1 to MAX_YEAR
     * @param month  the month-of-year to represent, 1 to 12
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the Thai Buddhist date, never null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static BuddhistDate of(int prolepticYear, int month, int dayOfMonth) {
        return new BuddhistDate(LocalDate.of(prolepticYear - YEARS_DIFFERENCE, month, dayOfMonth));
    }

    /**
     * Obtains an instance of {@code BuddhistDate} from the Thai Buddhist proleptic year,
     * month-of-year and day-of-month. This uses the Thai Buddhist era.
     *
     * @param prolepticYear  the year to represent in the Thai Buddhist era, from 1 to MAX_YEAR
     * @param dayOfYear  the day-of-year to represent, from 1 to 266
     * @return the Thai Buddhist date, never null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static BuddhistDate ofYearDay(int prolepticYear, int dayOfYear) {
        return new BuddhistDate(LocalDate.ofYearDay(prolepticYear - YEARS_DIFFERENCE, dayOfYear));
    }

    /**
     * Obtains an instance of {@code BuddhistDate} from a date-time object.

     * <p>
     * A {@code DateTimeAccessor} represents some form of date and time information.
     * This factory converts the arbitrary date-time object to an instance of {@code BuddhistDate}.
     *
     * @param dateTime  the date-time object to convert, not null
     * @return the BuddhistDate, not null
     * @throws DateTimeException if unable to convert to a {@code LocalDate}
     */
    public static BuddhistDate from(DateTimeAccessor dateTime) {
        if (dateTime instanceof BuddhistDate) {
            return (BuddhistDate) dateTime;
        }
        return new BuddhistDate(LocalDate.from(dateTime));
    }

    /**
     * Obtains an instance of {@code BuddhistDate} from the epoch day count.
     * <p>
     * The Epoch Day count is a simple incrementing count of days
     * where day 0 is 1970-01-01. Negative numbers represent earlier days.
     *
     * @param epochDay  the Epoch Day to convert, based on the epoch 1970-01-01
     * @return the BuddhistDate, not null
     * @throws DateTimeException if the epoch days exceeds the supported date range
     */
    public static BuddhistDate ofEpochDay(long epochDay) {
        return new BuddhistDate(LocalDate.ofEpochDay(epochDay));
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param date  the time-line date, not null
     */
    BuddhistDate(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate");
        this.isoDate = date;
    }

    //-----------------------------------------------------------------------
    @Override
    public BuddhistChronology getChronology() {
        return BuddhistChronology.INSTANCE;
    }

    //-----------------------------------------------------------------------
    @Override
    public int lengthOfMonth() {
        return isoDate.lengthOfMonth();
    }

    @Override
    public DateTimeValueRange range(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            if (isSupported(field)) {
                LocalDateTimeField f = (LocalDateTimeField) field;
                switch (f) {
                    case DAY_OF_MONTH:
                    case DAY_OF_YEAR:
                    case ALIGNED_WEEK_OF_MONTH:
                        return isoDate.range(field);
                    case YEAR_OF_ERA: {
                        DateTimeValueRange range = YEAR.range();
                        long max = (getProlepticYear() <= 0 ? -(range.getMinimum() + YEARS_DIFFERENCE) + 1 : range.getMaximum() + YEARS_DIFFERENCE);
                        return DateTimeValueRange.of(1, max);
                    }
                }
                return getChronology().range(f);
            }
            throw new DateTimeException("Unsupported field: " + field.getName());
        }
        return field.doRange(this);
    }

    @Override
    public long getLong(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            switch ((LocalDateTimeField) field) {
                case YEAR_OF_ERA: return getYear();
                case YEAR: return getProlepticYear();
                case ERA: return getEra().getValue();
            }
            return isoDate.getLong(field);
        }
        return field.doGet(this);
    }

    /**
     * Gets the Thai Buddhist era field.
     *
     * @return the era, never null
     */
    @Override
    public BuddhistEra getEra() {
        return getProlepticYear() < 1 ? BuddhistEra.ERA_BEFORE_BE : BuddhistEra.ERA_BE;
    }

    private int getProlepticYear() {
        return isoDate.getYear() + YEARS_DIFFERENCE;
    }

    /**
     * Gets the Thai Buddhist year-of-era field.
     *
     * @return the year-of-era
     */
    @Override
    public int getYear() {
        int year = getProlepticYear();
        return year < 1 ? 1 - year : year;
    }

    //-----------------------------------------------------------------------
    @Override
    public BuddhistDate with(DateTimeField field, long newValue) {
        if (field instanceof LocalDateTimeField) {
            LocalDateTimeField f = (LocalDateTimeField) field;
            if (getLong(f) == newValue) {
                return this;
            }
            switch (f) {
                case YEAR_OF_ERA:
                case YEAR:
                case ERA: {
//                    f.checkValidValue(newValue);  // TODO ranges
                    int nvalue = (int) newValue;
                    switch (f) {
                        case YEAR_OF_ERA:
                            return with(isoDate.withYear((getProlepticYear() >= 1 ? nvalue : 1 - nvalue)  - YEARS_DIFFERENCE));
                        case YEAR:
                            return with(isoDate.withYear(nvalue - YEARS_DIFFERENCE));
                        case ERA:
                            return with(isoDate.withYear((1 - getProlepticYear()) - YEARS_DIFFERENCE));
                    }
                }
            }
            return with(isoDate.with(field, newValue));
        }
        return field.doSet(this, newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isLeapYear() {
        return getChronology().isLeapYear(get(LocalDateTimeField.YEAR));
    }

    @Override
    public int lengthOfYear() {
        return (isLeapYear() ? 366 : 365);
    }

    @Override
    public BuddhistDate plusYears(long years) {
        return with(isoDate.plusYears(years));
    }

    @Override
    public BuddhistDate plusMonths(long months) {
        return with(isoDate.plusMonths(months));
    }

    @Override
    public BuddhistDate plusWeeks(long weeksToAdd) {
        return plusDays(Jdk8Methods.safeMultiply(weeksToAdd, 7));
    }

    @Override
    public BuddhistDate plusDays(long days) {
        return with(isoDate.plusDays(days));
    }

    private BuddhistDate with(LocalDate newDate) {
        return (newDate.equals(isoDate) ? this : new BuddhistDate(newDate));
    }

}
