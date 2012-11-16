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

import java.util.Comparator;
import javax.time.DateTimeException;
import javax.time.LocalDateTime;
import javax.time.LocalTime;
import javax.time.ZoneId;
import javax.time.ZoneOffset;
import javax.time.calendrical.ChronoField;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.PeriodUnit;
import javax.time.format.CalendricalFormatter;
import javax.time.zone.ZoneResolver;
import javax.time.zone.ZoneResolvers;

/**
 * A date-time without a time-zone in an arbitrary chronology, intended
 * for advanced globalization use cases.
 * <p>
 * <b>Most applications should declare method signatures, fields and variables
 * as {@link LocalDateTime}, not this interface.</b>
 * <p>
 * A {@code ChronoLocalDateTime} is the abstract representation of a local date-time
 * where the {@code Chrono chronology}, or calendar system, is pluggable.
 * The date-time is defined in terms of fields expressed by {@link DateTimeField},
 * where most common implementations are defined in {@link ChronoField}.
 * The chronology defines how the calendar system operates and the meaning of
 * the standard fields.
 *
 * <h4>When to use this interface</h4>
 * The design of the API encourages the use of {@code LocalDateTime} rather than this
 * interface, even in the case where the application needs to deal with multiple
 * calendar systems. The rationale for this is explored in detail in {@link ChronoLocalDate}.
 * <p>
 * Ensure that the discussion in {@code ChronoLocalDate} has been read and understood
 * before using this interface.
 *
 * <h4>Implementation notes</h4>
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 *
 * @param <C> the chronology of this date-time
 */
public interface ChronoLocalDateTime<C extends Chrono<C>>
        extends  DateTime, WithAdjuster, Comparable<ChronoLocalDateTime<?>> {

   /**
     * Comparator for two {@code ChronoLocalDateTime}s ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * 
     * @see #isAfter
     * @see #isBefore
     * @see #isEqual
     */
    public static final Comparator<ChronoLocalDateTime<?>> DATE_TIME_COMPARATOR =
            new Comparator<ChronoLocalDateTime<?>>() {
        @Override
        public int compare(ChronoLocalDateTime<?> datetime1, ChronoLocalDateTime<?> datetime2) {
            int cmp = Long.compare(datetime1.getDate().toEpochDay(), datetime2.getDate().toEpochDay());
            if (cmp == 0) {
                cmp = Long.compare(datetime1.getTime().toNanoOfDay(), datetime2.getTime().toNanoOfDay());
            }
            return cmp;
        }
    };

    /**
     * Gets the local date part of this date-time.
     * <p>
     * This returns a local date with the same year, month and day
     * as this date-time.
     *
     * @return the date part of this date-time, not null
     */
    ChronoLocalDate<C> getDate() ;

    /**
     * Gets the local time part of this date-time.
     * <p>
     * This returns a local time with the same hour, minute, second and
     * nanosecond as this date-time.
     *
     * @return the time part of this date-time, not null
     */
    LocalTime getTime();

    //-------------------------------------------------------------------------
    // override for covariant return type
    @Override
    ChronoLocalDateTime<C> with(WithAdjuster adjuster);

    @Override
    ChronoLocalDateTime<C> with(DateTimeField field, long newValue);

    @Override
    ChronoLocalDateTime<C> plus(PlusAdjuster adjuster);

    @Override
    ChronoLocalDateTime<C> plus(long amountToAdd, PeriodUnit unit);

    @Override
    ChronoLocalDateTime<C> minus(MinusAdjuster adjuster);

    @Override
    ChronoLocalDateTime<C> minus(long amountToSubtract, PeriodUnit unit);

    //-----------------------------------------------------------------------
    /**
     * Returns an offset date-time formed from this date-time and the specified offset.
     * <p>
     * The result will be the combination of this date-time and the specified offset.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the offset to use, not null
     * @return the offset date-time formed from this date-time and the specified offset, not null
     */
    ChronoOffsetDateTime<C> atOffset(ZoneOffset offset);

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#postGapPreOverlap() post-gap pre-overlap} resolver.
     * This selects the date-time immediately after a gap and the earlier offset in overlaps.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * {@link javax.time.ZonedDateTime#withLaterOffsetAtOverlap()} immediately after this method.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    ChronoZonedDateTime<C> atZone(ZoneId zone);

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#postGapPreOverlap() post-gap pre-overlap} resolver.
     * This selects the date-time immediately after a gap and the earlier offset in overlaps.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * {@link javax.time.ZonedDateTime#withLaterOffsetAtOverlap()} immediately after this method.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    ChronoZonedDateTime<C> atZone(ZoneId zone, ZoneResolver resolver);

    //-----------------------------------------------------------------------
    /**
     * Compares this date-time to another date-time, including the chronology.
     * <p>
     * The comparison is based first on the underlying time-line date-time, then
     * on the chronology.
     * It is "consistent with equals", as defined by {@link Comparable}.
     * <p>
     * For example, the following is the comparator order:
     * <ol>
     * <li>{@code 2012-12-03T12:00 (ISO)}</li>
     * <li>{@code 2012-12-04T12:00 (ISO)}</li>
     * <li>{@code 2555-12-04T12:00 (ThaiBuddhist)}</li>
     * <li>{@code 2012-12-05T12:00 (ISO)}</li>
     * </ol>
     * Values #2 and #3 represent the same date-time on the time-line.
     * When two values represent the same date-time, the chronology ID is compared to distinguish them.
     * This step is needed to make the ordering "consistent with equals".
     * <p>
     * If all the date-time objects being compared are in the same chronology, then the
     * additional chronology stage is not required and only the local date-time is used.
     *
     * @param other  the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    @Override
    int compareTo(ChronoLocalDateTime<?> other);

    /**
     * Checks if this date-time is after the specified date-time ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date-time and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is after the specified date-time
     */
    boolean isAfter(ChronoLocalDateTime<?> other);

    /**
     * Checks if this date-time is before the specified date-time ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date-time and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     *
     * @param  other  the other date-time to compare to, not null
     * @return true if this is before the specified date-time
     */
    boolean isBefore(ChronoLocalDateTime<?> other);

    /**
     * Checks if this date-time is equal to the specified date-time ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date and time and not the chronology.
     * This allows date-times in different calendar systems to be compared based
     * on the time-line position.
     *
     * @param  other  the other date-time to compare to, not null
     * @return true if the underlying date-time is equal to the specified date-time on the timeline
     */
    boolean isEqual(ChronoLocalDateTime<?> other);

    //-----------------------------------------------------------------------
    /**
     * Checks if this date-time is equal to another date-time, including the chronology.
     * <p>
     * Compares this date-time with another ensuring that the date-time and chronology are the same.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other date
     */
    @Override
    boolean equals(Object obj);

    /**
     * A hash code for this date-time.
     *
     * @return a suitable hash code
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * Outputs this date-time as a {@code String}.
     * <p>
     * The output will include the full local date-time and the chronology ID.
     *
     * @return a string representation of this date-time, not null
     */
    @Override
    String toString();

    /**
     * Outputs this date-time as a {@code String} using the formatter.
     *
     * @param formatter  the formatter to use, not null
     * @return the formatted date-time string, not null
     * @throws DateTimeException if an error occurs during printing
     */
    String toString(CalendricalFormatter formatter);

}
