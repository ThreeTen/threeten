/*
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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

import static javax.time.calendrical.QuarterYearField.QUARTER_OF_YEAR;

import java.util.Locale;

import javax.time.calendrical.AdjustableDateTime;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTimeAdjuster;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.calendrical.LocalPeriodUnit;
import javax.time.calendrical.PeriodUnit;
import javax.time.chrono.Chronology;
import javax.time.chrono.ISOChronology;
import javax.time.format.DateTimeFormatterBuilder;
import javax.time.format.TextStyle;

/**
 * A quarter-of-year, such as 'Q2'.
 * <p>
 * {@code QuarterOfYear} is an enum representing the 4 quarters of the year -
 * Q1, Q2, Q3 and Q4. These are defined as January to March, April to June,
 * July to September and October to December.
 * <p>
 * The calendrical framework requires date-time fields to have an {@code int} value.
 * The {@code int} value follows the quarter, from 1 (Q1) to 4 (Q4).
 * It is recommended that applications use the enum rather than the {@code int} value
 * to ensure code clarity.
 * <p>
 * <b>Do not use {@code ordinal()} to obtain the numeric representation of {@code QuarterOfYear}.
 * Use {@code getValue()} instead.</b>
 * <p>
 * This enum represents a common concept that is found in many calendar systems.
 * As such, this enum may be used by any calendar system that has the quarter-of-year
 * concept defined exactly equivalent to the ISO calendar system.
 * 
 * <h4>Implementation notes</h4>
 * This is an immutable and thread-safe enum.
 */
public enum QuarterOfYear implements AdjustableDateTime, DateTimeAdjuster {

    /**
     * The singleton instance for the first quarter-of-year, from January to March.
     * This has the numeric value of {@code 1}.
     */
    Q1,
    /**
     * The singleton instance for the second quarter-of-year, from April to June.
     * This has the numeric value of {@code 2}.
     */
    Q2,
    /**
     * The singleton instance for the third quarter-of-year, from July to September.
     * This has the numeric value of {@code 3}.
     */
    Q3,
    /**
     * The singleton instance for the fourth quarter-of-year, from October to December.
     * This has the numeric value of {@code 4}.
     */
    Q4;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code QuarterOfYear} from an {@code int} value.
     * <p>
     * {@code QuarterOfYear} is an enum representing the 4 quarters of the year.
     * This factory allows the enum to be obtained from the {@code int} value.
     * The {@code int} value follows the quarter, from 1 (Q1) to 4 (Q4).
     *
     * @param quarterOfYear  the quarter-of-year to represent, from 1 (Q1) to 4 (Q4)
     * @return the QuarterOfYear singleton, not null
     * @throws DateTimeException if the quarter-of-year is invalid
     */
    public static QuarterOfYear of(int quarterOfYear) {
        switch (quarterOfYear) {
            case 1: return Q1;
            case 2: return Q2;
            case 3: return Q3;
            case 4: return Q4;
            default: throw new DateTimeException("Invalid value for QuarterOfYear: " + quarterOfYear);
        }
    }

    /**
     * Obtains an instance of {@code QuarterOfYear} from a month-of-year.
     * <p>
     * {@code QuarterOfYear} is an enum representing the 4 quarters of the year.
     * This factory allows the enum to be obtained from the {@code Month} value.
     * <p>
     * January to March are Q1, April to June are Q2, July to September are Q3
     * and October to December are Q4.
     *
     * @param month  the month-of-year to convert from, from 1 to 12
     * @return the QuarterOfYear singleton, not null
     */
    public static QuarterOfYear ofMonth(Month month) {
        DateTimes.checkNotNull(month, "Month must not be null");
        return of(month.ordinal() / 3 + 1);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code QuarterOfYear} from a calendrical.
     * <p>
     * A calendrical represents some form of date and time information.
     * This factory converts the arbitrary calendrical to an instance of {@code QuarterOfYear}.
     * 
     * @param calendrical  the calendrical to convert, not null
     * @return the quarter-of-year, not null
     * @throws DateTimeException if unable to convert to a {@code QuarterOfYear}
     */
    public static QuarterOfYear from(DateTime calendrical) {
        if (calendrical instanceof QuarterOfYear) {
            return (QuarterOfYear) calendrical;
        }
        return of((int) calendrical.get(QUARTER_OF_YEAR));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the quarter-of-year {@code int} value.
     * <p>
     * The values are numbered following the ISO-8601 standard,
     * from 1 (Q1) to 4 (Q4).
     *
     * @return the quarter-of-year, from 1 (Q1) to 4 (Q4)
     */
    public int getValue() {
        return ordinal() + 1;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the textual representation, such as 'Q1' or '4th quarter'.
     * <p>
     * This returns the textual name used to identify the quarter-of-year.
     * The parameters control the length of the returned text and the locale.
     * <p>
     * If no textual mapping is found then the {@link #getValue() numeric value} is returned.
     *
     * @param style  the length of the text required, not null
     * @param locale  the locale to use, not null
     * @return the text value of the quarter-of-year, not null
     */
    public String getText(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(QUARTER_OF_YEAR, style).toFormatter(locale).print(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTimeValueRange range(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            if (this != Q1) {
                switch ((LocalDateTimeField) field) {
                    case DAY_OF_MONTH: return DateTimeValueRange.of(1, 30, 31);
                    case ALIGNED_WEEK_OF_MONTH: return DateTimeValueRange.of(1, 5);
                }
            }
            return field.range();
        }
        return field.doRange(this);
    }

    @Override
    public long get(DateTimeField field) {
        if (field == QUARTER_OF_YEAR) {
            return getValue();
        }
        if (field instanceof LocalDateTimeField) {
            throw new DateTimeException("Unsupported field: " + field.getName());
        }
        return field.doGet(this);
    }

    @Override
    public QuarterOfYear with(DateTimeField field, long newValue) {
        if (field == QUARTER_OF_YEAR) {
            int val = QUARTER_OF_YEAR.range().checkValidIntValue(newValue, QUARTER_OF_YEAR);
            return QuarterOfYear.of(val);
        }
        if (field instanceof LocalDateTimeField) {
            throw new DateTimeException("Unsupported field: " + field.getName());
        }
        return field.doSet(this, newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public QuarterOfYear plus(long periodAmount, PeriodUnit unit) {
        if (unit instanceof LocalPeriodUnit) {
            switch ((LocalPeriodUnit) unit) {
                case QUARTER_YEARS: return plus(periodAmount);
                case HALF_YEARS: return plus((periodAmount % 2) * 2);
                case YEARS: return this;
            }
            throw new DateTimeException("Unsupported unit: " + unit.getName());
        }
        return unit.doAdd(this, periodAmount);
    }

    /**
     * Returns the quarter that is the specified number of quarters after this one.
     * <p>
     * The calculation rolls around the end of the year from Q4 to Q1.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param quarters  the quarters to add, positive or negative
     * @return the resulting quarter, not null
     */
    public QuarterOfYear plus(long quarters) {
        int amount = (int) quarters % 4;
        return values()[(ordinal() + (amount + 4)) % 4];
    }

    //-----------------------------------------------------------------------
    @Override
    public QuarterOfYear minus(long periodAmount, PeriodUnit unit) {
        if (unit instanceof LocalPeriodUnit) {
            return plus(-(periodAmount % 4), unit);
        }
        return unit.doAdd(this, DateTimes.safeNegate(periodAmount));
    }

    /**
     * Returns the quarter that is the specified number of quarters before this one.
     * <p>
     * The calculation rolls around the start of the year from Q1 to Q4.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param quarters  the quarters to subtract, positive or negative
     * @return the resulting quarter, not null
     */
    public QuarterOfYear minus(long quarters) {
        return plus(-(quarters % 4));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the first of the three months that this quarter refers to.
     * <p>
     * Q1 will return January.<br />
     * Q2 will return April.<br />
     * Q3 will return July.<br />
     * Q4 will return October.
     * <p>
     * To obtain the other two months of the quarter, simply use {@link Month#plus(long)}
     * on the returned month.
     *
     * @return the first month in the quarter, not null
     */
    public Month firstMonth() {
        switch (this) {
            case Q1: return Month.JANUARY;
            case Q2: return Month.APRIL;
            case Q3: return Month.JULY;
            case Q4: return Month.OCTOBER;
            default: throw new IllegalStateException("Unreachable");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Extracts date-time information in a generic way.
     * <p>
     * This method exists to fulfill the {@link DateTime} interface.
     * This implementation always returns null.
     * 
     * @param <R> the type to extract
     * @param type  the type to extract, null returns null
     * @return the extracted object, null if unable to extract
     */
    @Override
    public <R> R extract(Class<R> type) {
        return null;
    }

    /**
     * Implementation of the strategy to make an adjustment to the specified date-time object.
     * <p>
     * This method is not intended to be called by application code directly.
     * Applications should use the {@code with(DateTimeAdjuster)} method on the
     * date-time object to make the adjustment passing this as the argument.
     * <p>
     * This instance is immutable and unaffected by this method call.
     * 
     * <h4>Implementation notes</h4>
     * Adjusts the specified date-time to have the value of this quarter.
     * The date-time object must use the ISO calendar system.
     * The adjustment is equivalent to using {@link AdjustableDateTime#with(DateTimeField, long)}
     * passing {@code QUARTER_OF_YEAR} as the field.
     *
     * @param dateTime  the target object to be adjusted, not null
     * @return the adjusted object, not null
     */
    @Override
    public AdjustableDateTime doAdjustment(AdjustableDateTime dateTime) {
        if (Chronology.from(dateTime).equals(ISOChronology.INSTANCE) == false) {
            throw new DateTimeException("Adjustment only supported on ISO date-time");
        }
        return dateTime.with(QUARTER_OF_YEAR, getValue());
    }

}
