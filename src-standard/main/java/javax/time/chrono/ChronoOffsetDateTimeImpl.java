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
package javax.time.chrono;

import static javax.time.DateTimeConstants.SECONDS_PER_DAY;
import static javax.time.calendrical.ChronoField.EPOCH_DAY;
import static javax.time.calendrical.ChronoField.NANO_OF_DAY;
import static javax.time.calendrical.ChronoField.OFFSET_SECONDS;

import java.io.Serializable;
import java.util.Objects;

import javax.time.DateTimeException;
import javax.time.DayOfWeek;
import javax.time.Instant;
import javax.time.LocalDate;
import javax.time.LocalDateTime;
import javax.time.LocalTime;
import javax.time.OffsetDateTime;
import javax.time.ZoneId;
import javax.time.ZoneOffset;
import javax.time.calendrical.ChronoField;
import javax.time.calendrical.ChronoUnit;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeAdjusters;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.PeriodUnit;
import javax.time.jdk8.DefaultInterfaceChronoOffsetDateTime;
import javax.time.jdk8.Jdk8Methods;
import javax.time.zone.ZoneResolver;
import javax.time.zone.ZoneResolvers;
import javax.time.zone.ZoneRules;

/**
 * A date-time with a zone offset from UTC/Greenwich for the calendar neutral API.
 * <p>
 * {@code ChronoOffsetDateTime} is an immutable representation of a date-time with an offset.
 * This class stores all date and time fields, to a precision of nanoseconds,
 * as well as the offset from UTC/Greenwich. For example, the value
 * "2nd October 2007 at 13:45.30.123456789 +02:00" can be stored in an {@code OffsetDateTime}.
 * <p>
 * {@code ChronoOffsetDateTime} and {@link Instant} both store an instant on the time-line
 * to nanosecond precision. The main difference is that this class also stores the
 * offset from UTC/Greenwich. {@code Instant} should be used when you only need to compare the
 * object to other instants. {@code ChronoOffsetDateTime} should be used when you want to actively
 * query and manipulate the date and time fields, although you should also consider using
 * {@link ChronoZonedDateTime}.
 *
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 *
 * @param <C> the chronology of this date
 */
class ChronoOffsetDateTimeImpl<C extends Chrono<C>>
        extends DefaultInterfaceChronoOffsetDateTime<C>
        implements  ChronoOffsetDateTime<C>, DateTime, WithAdjuster, Serializable {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = -2187570992341262959L;

    /**
     * The local date-time.
     */
    private final ChronoDateTimeImpl<C> dateTime;
    /**
     * The zone offset.
     */
    private final ZoneOffset offset;

    /**
     * Obtains an instance of {@code ChronoOffsetDateTime} from a date-time and offset.
     *
     * @param dateTime  the local date-time, not null
     * @param offset  the zone offset, not null
     * @return the offset date-time, not null
     */
    static <R extends Chrono<R>> ChronoOffsetDateTime<R> of(ChronoDateTimeImpl<R> dateTime, ZoneOffset offset) {
        return new ChronoOffsetDateTimeImpl<>(dateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param dateTime  the date-time, not null
     * @param offset  the zone offset, not null
     */
    protected ChronoOffsetDateTimeImpl(ChronoDateTimeImpl<C> dateTime, ZoneOffset offset) {
        Objects.requireNonNull(dateTime, "dateTime");
        Objects.requireNonNull(offset, "offset");
        this.dateTime = dateTime;
        this.offset = offset;
    }

    /**
     * Returns a new date-time based on this one, returning {@code this} where possible.
     * <p>
     * This method must be overridden so the subclass can create its own type
     * of ChronoLocalDateTime.
     *
     * @param dateTime  the date-time to create with, not null
     * @param offset  the zone offset to create with, not null
     */
    @SuppressWarnings("unchecked")
    private  ChronoOffsetDateTimeImpl<C> with(ChronoDateTimeImpl<C> dateTime, ZoneOffset offset) {
        if (this.dateTime == dateTime && this.offset.equals(offset)) {
            return this;
        }
        ChronoDateTimeImpl<C> impl = getDate().getChrono().ensureChronoLocalDateTime(dateTime);
        return new ChronoOffsetDateTimeImpl<>(impl, offset);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(DateTimeField field) {
        return field instanceof ChronoField || (field != null && field.doIsSupported(this));  // TODO: not all ChronoField are supported
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the zone offset.
     *
     * @return the zone offset, not null
     */
    @Override
    public ZoneOffset getOffset() {
        return offset;
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified offset ensuring
     * that the result has the same local date-time.
     * <p>
     * This method returns an object with the same {@code ChronoLocalDateTime} and the specified {@code ZoneOffset}.
     * No calculation is needed or performed.
     * For example, if this time represents {@code 2007-12-03T10:30+02:00} and the offset specified is
     * {@code +03:00}, then this method will return {@code 2007-12-03T10:30+03:00}.
     * <p>
     * To take into account the difference between the offsets, and adjust the time fields,
     * use {@link #withOffsetSameInstant}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the zone offset to change to, not null
     * @return an {@code OffsetDateTime} based on this date-time with the requested offset, not null
     */
    @Override
    public ChronoOffsetDateTimeImpl<C> withOffsetSameLocal(ZoneOffset offset) {
        return with(dateTime, offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified offset ensuring
     * that the result is at the same instant.
     * <p>
     * This method returns an object with the specified {@code ZoneOffset} and a {@code ChronoLocalDateTime}
     * adjusted by the difference between the two offsets.
     * This will result in the old and new objects representing the same instant.
     * This is useful for finding the local time in a different offset.
     * For example, if this time represents {@code 2007-12-03T10:30+02:00} and the offset specified is
     * {@code +03:00}, then this method will return {@code 2007-12-03T11:30+03:00}.
     * <p>
     * To change the offset without adjusting the local time use {@link #withOffsetSameLocal}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the zone offset to change to, not null
     * @return an {@code OffsetDateTime} based on this date-time with the requested offset, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @Override
    public ChronoOffsetDateTimeImpl<C> withOffsetSameInstant(ZoneOffset offset) {
        if (offset.equals(this.offset)) {
            return this;
        }
        int difference = offset.getTotalSeconds() - this.offset.getTotalSeconds();
        ChronoDateTimeImpl<C> adjusted = dateTime.plusSeconds(difference);
        return new ChronoOffsetDateTimeImpl<C>(adjusted, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the year field.
     * <p>
     * This method returns the primitive {@code int} value for the year.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    int getYear() {
        return dateTime.getYear();
    }

    /**
     * Gets the month-of-year field from 1 to 12 or 13 depending on the chronology.
     *
     * @return the month-of-year, from 1 to 12 or 13
     */
    int getMonthValue() {
        return dateTime.getMonthValue();
    }

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    /**
     * Gets the day-of-year field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    int getDayOfYear() {
        return dateTime.getDayOfYear();
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
    DayOfWeek getDayOfWeek() {
        return dateTime.getDayOfWeek();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    int getHour() {
        return dateTime.getHour();
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    int getMinute() {
        return dateTime.getMinute();
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    int getSecond() {
        return dateTime.getSecond();
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    int getNano() {
        return dateTime.getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an adjusted date-time based on this date-time.
     * <p>
     * This adjusts the date-time according to the rules of the specified adjuster.
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date-time to the last day of the month.
     * A selection of common adjustments is provided in {@link DateTimeAdjusters}.
     * These include finding the "last day of the month" and "next Wednesday".
     * The adjuster is responsible for handling special cases, such as the varying
     * lengths of month and leap years.
     * <p>
     * In addition, all principal classes implement the {@link javax.time.calendrical.DateTime.WithAdjuster} interface,
     * including this one. For example, {@link ChronoLocalDate} implements the adjuster interface.
     * As such, this code will compile and run:
     * <pre>
     *  dateTime.with(date);
     * </pre>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster the adjuster to use, not null
     * @return an {@code OffsetDateTime} based on this date-time with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     */
    @SuppressWarnings("unchecked")
    @Override
    public ChronoOffsetDateTime<C> with(WithAdjuster adjuster) {
        if (adjuster instanceof ChronoLocalDate || adjuster instanceof LocalTime || adjuster instanceof ChronoLocalDateTime) {
            return with(dateTime.with(adjuster), offset);
        } else if (adjuster instanceof ZoneOffset) {
            return with(dateTime, (ZoneOffset) adjuster);
        } else if (adjuster instanceof ChronoOffsetDateTime) {
            return (ChronoOffsetDateTime<C>) adjuster;
        }
        return getDate().getChrono().ensureChronoOffsetDateTime((ChronoOffsetDateTime<C>) adjuster.doWithAdjustment(this));
    }

    /**
     * Returns a copy of this date-time with the specified field altered.
     * <p>
     * This method returns a new date-time based on this date-time with a new value for the specified field.
     * This can be used to change any field, for example to set the year, month of day-of-month.
     * The offset is not part of the calculation and will be unchanged in the result.
     * <p>
     * In some cases, changing the specified field can cause the resulting date-time to become invalid,
     * such as changing the month from January to February would make the day-of-month 31 invalid.
     * In cases like this, the field is responsible for resolving the date. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param field  the field to set in the returned date-time, not null
     * @param newValue  the new value of the field in the returned date-time, not null
     * @return an {@code OffsetDateTime} based on this date-time with the specified field set, not null
     * @throws DateTimeException if the value is invalid
     */
    @Override
    public ChronoOffsetDateTimeImpl<C> with(DateTimeField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            switch (f) {
                case INSTANT_SECONDS:
                    long epochDays = Jdk8Methods.floorDiv(newValue, SECONDS_PER_DAY);
                    ChronoOffsetDateTimeImpl<C> odt = with(ChronoField.EPOCH_DAY, epochDays);
                    int secsOfDay = Jdk8Methods.floorMod(newValue, SECONDS_PER_DAY);
                    odt  = odt.with(ChronoField.SECOND_OF_DAY, secsOfDay);
                    return odt;

                case OFFSET_SECONDS: {
                    return with(dateTime, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
                }
            }
            return with(dateTime.with(field, newValue), offset);
        }
        return getDate().getChrono().ensureChronoOffsetDateTime(field.doSet(this, newValue));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the year altered.
     * The offset does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @return an {@code OffsetDateTime} based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    ChronoOffsetDateTimeImpl<C> withYear(int year) {
        return with(dateTime.withYear(year), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the month-of-year altered.
     * The offset does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @return an {@code OffsetDateTime} based on this date-time with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    ChronoOffsetDateTime<C> withMonth(int month) {
        return with(dateTime.withMonth(month), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the day-of-month altered.
     * If the resulting {@code OffsetDateTime} is invalid, an exception is thrown.
     * The offset does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 28-31
     * @return an {@code OffsetDateTime} based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     */
    ChronoOffsetDateTime<C> withDayOfMonth(int dayOfMonth) {
        return with(dateTime.withDayOfMonth(dayOfMonth), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the day-of-year altered.
     * If the resulting {@code OffsetDateTime} is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 365-366
     * @return an {@code OffsetDateTime} based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid
     * @throws DateTimeException if the day-of-year is invalid for the year
     */
    ChronoOffsetDateTime<C> withDayOfYear(int dayOfYear) {
        return with(dateTime.withDayOfYear(dayOfYear), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the date values altered.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_VALUE + 1 to MAX_VALUE
     * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return an {@code OffsetDateTime} based on this date-time with the requested date, not null
     * @throws DateTimeException if any field value is invalid
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     */
    ChronoOffsetDateTime<C> withDate(int year, int month, int dayOfMonth) {
        return with(dateTime.withDate(year, month, dayOfMonth), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the hour-of-day value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @return an {@code OffsetDateTime} based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    ChronoOffsetDateTime<C> withHour(int hour) {
        ChronoDateTimeImpl<C> newDT = dateTime.withHour(hour);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the minute-of-hour value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    ChronoOffsetDateTime<C> withMinute(int minute) {
        ChronoDateTimeImpl<C> newDT = dateTime.withMinute(minute);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the second-of-minute value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    ChronoOffsetDateTime<C> withSecond(int second) {
        return with(dateTime.withSecond(second), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the nano-of-second value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return an {@code OffsetDateTime} based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nanos value is invalid
     */
    ChronoOffsetDateTime<C> withNano(int nanoOfSecond) {
        return with(dateTime.withNano(nanoOfSecond), offset);
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the second and nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    ChronoOffsetDateTimeImpl<C> withTime(int hour, int minute) {
        ChronoDateTimeImpl<C> newDT = dateTime.withTime(hour, minute);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return an {@code OffsetDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    ChronoOffsetDateTime<C> withTime(int hour, int minute, int second) {
        ChronoDateTimeImpl<C> newDT = dateTime.withTime(hour, minute, second);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this OffsetDateTime with the time values altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return an {@code OffsetDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    ChronoOffsetDateTimeImpl<C> withTime(int hour, int minute, int second, int nanoOfSecond) {
        ChronoDateTimeImpl<C> newDT = dateTime.withTime(hour, minute, second, nanoOfSecond);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoOffsetDateTime<C> plus(long amountToAdd, PeriodUnit unit) {
        if (unit instanceof ChronoUnit) {
            return with(dateTime.plus(amountToAdd, unit), offset);
        }
        return getDate().getChrono().ensureChronoOffsetDateTime(unit.doAdd(this, amountToAdd));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in years added.
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
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusYears(long years) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusYears(years);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in months added.
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
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusMonths(long months) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusMonths(months);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified period in weeks added.
     * <p>
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one week would result in the 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusWeeks(long weeks) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusWeeks(weeks);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified period in days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one day would result in the 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusDays(long days) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusDays(days);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in hours added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusHours(long hours) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusHours(hours);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in minutes added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusMinutes(long minutes) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusMinutes(minutes);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTimeImpl<C> plusSeconds(long seconds) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusSeconds(seconds);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    ChronoOffsetDateTimeImpl<C> plusNanos(long nanos) {
        ChronoDateTimeImpl<C> newDT = dateTime.plusNanos(nanos);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in years subtracted.
     * <p>
     * This method subtracts the specified amount from the years field in three steps:
     * <ol>
     * <li>Subtract the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusYears(long years) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusYears(years);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in months subtracted.
     * <p>
     * This method subtracts the specified amount from the months field in three steps:
     * <ol>
     * <li>Subtract the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusMonths(long months) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusMonths(months);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in weeks subtracted.
     * <p>
     * This method subtracts the specified amount in weeks from the days field decrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one week would result in the 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusWeeks(long weeks) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusWeeks(weeks);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in days subtracted.
     * <p>
     * This method subtracts the specified amount from the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one day would result in the 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusDays(long days) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusDays(days);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in hours subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusHours(long hours) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusHours(hours);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in minutes subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusMinutes(long minutes) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusMinutes(minutes);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in seconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusSeconds(long seconds) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusSeconds(seconds);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    /**
     * Returns a copy of this {@code OffsetDateTime} with the specified period in nanoseconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return an {@code OffsetDateTime} based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    ChronoOffsetDateTime<C> minusNanos(long nanos) {
        ChronoDateTimeImpl<C> newDT = dateTime.minusNanos(nanos);
        return (newDT == dateTime ? this : with(newDT, offset));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a zoned date-time formed from the instant represented by this
     * date-time and the specified time-zone.
     * <p>
     * This conversion will ignore the visible local date-time and use the underlying instant instead.
     * This avoids any problems with local time-line gaps or overlaps.
     * The result might have different values for fields such as hour, minute an even day.
     * <p>
     * To attempt to retain the values of the fields, use {@link #atZoneSimilarLocal(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    @Override
    public ChronoZonedDateTime<C> atZoneSameInstant(ZoneId zone) {
        ZoneRules rules = zone.getRules();  // latest rules version
        // Add optimization to avoid toInstant
        ChronoOffsetDateTimeImpl<C> codt = this.withOffsetSameInstant(rules.getOffset(this.toInstant()));
        return ChronoZonedDateTimeImpl.of(codt, zone);
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#retainOffset() retain-offset} resolver.
     * This selects the date-time immediately after a gap and retains the offset in
     * overlaps where possible, selecting the earlier offset if not possible.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * {@link ChronoZonedDateTime#withLaterOffsetAtOverlap()} immediately after this method.
     * Alternately, pass a specific resolver to {@link #atZoneSimilarLocal(ZoneId, ZoneResolver)}.
     * <p>
     * To create a zoned date-time at the same instant irrespective of the local time-line,
     * use {@link #atZoneSameInstant(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date and the earliest valid time for the zone, not null
     */
    @Override
    public ChronoZonedDateTime<C> atZoneSimilarLocal(ZoneId zone) {
        return atZoneSimilarLocal(zone, ZoneResolvers.retainOffset());
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone
     * taking control of what occurs in time-line gaps and overlaps.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then the resolver is used to determine the resultant local time and offset.
     * <p>
     * To create a zoned date-time at the same instant irrespective of the local time-line,
     * use {@link #atZoneSameInstant(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @param resolver  the zone resolver to use for gaps and overlaps, not null
     * @return the zoned date-time formed from this date and the earliest valid time for the zone, not null
     * @throws DateTimeException if the date-time cannot be resolved
     */
    @Override
    public ChronoZonedDateTime<C> atZoneSimilarLocal(ZoneId zone, ZoneResolver resolver) {
        // Convert the ChronoLocalDate to LocalDate to work with Zone rules and then convert back
        ZoneRules rules = zone.getRules();
        LocalDate ld = LocalDate.from(dateTime.getDate());
        LocalDateTime ldt = LocalDateTime.of(ld, dateTime.getTime());
        OffsetDateTime odt = OffsetDateTime.of(ldt, offset);
        OffsetDateTime offsetDT = resolver.resolve(ldt, rules.getOffsetInfo(odt.getDateTime()), rules, zone, odt);
        ChronoOffsetDateTimeImpl<C> codt = this
                .with(EPOCH_DAY, offsetDT.getDate().toEpochDay())
                .with(NANO_OF_DAY, offsetDT.getTime().toNanoOfDay());
        return ChronoZonedDateTimeImpl.of(codt, zone);
    }

    //-----------------------------------------------------------------------
    @Override
    public DateTime doWithAdjustment(DateTime dateTime) {
        return dateTime
                .with(OFFSET_SECONDS, getOffset().getTotalSeconds())
                .with(EPOCH_DAY, getDate().toEpochDay())
                .with(NANO_OF_DAY, getTime().toNanoOfDay());
    }

    @Override
    public long periodUntil(DateTime endDateTime, PeriodUnit unit) {
        if (endDateTime instanceof ChronoOffsetDateTime == false) {
            throw new DateTimeException("Unable to calculate period between objects of two different types");
        }
//        ChronoOffsetDateTime<?> end = (ChronoOffsetDateTime<?>) endDateTime;
        if (unit instanceof ChronoUnit) {
//            ChronoUnit f = (ChronoUnit) unit;
//            long until = dateTime.periodUntil(end.getDateTime(), unit);
            // NYI Adjust for offsets
            throw new DateTimeException("nyi: ChronoOffsetDateTime.periodUntil");
        }
        return unit.between(this, endDateTime).getAmount();
    }

    //-----------------------------------------------------------------------
    @Override
    public ChronoLocalDateTime<C> getDateTime() {
        return dateTime;
    }

    //-----------------------------------------------------------------------
    @Override
    public long toEpochSecond() {
        long epochDay = dateTime.getDate().toEpochDay();
        long secs = epochDay * SECONDS_PER_DAY + dateTime.getTime().toSecondOfDay();
        secs -= offset.getTotalSeconds();
        return secs;
    }

}
