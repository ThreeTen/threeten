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

import java.util.Comparator;
import javax.time.LocalDate;
import javax.time.LocalTime;
import javax.time.calendrical.ChronoField;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.PeriodUnit;
import javax.time.format.CalendricalFormatter;

/**
 * A date without time-of-day or time-zone in an arbitrary chronology, intended
 * for advanced globalization use cases.
 * <p>
 * <b>Most applications should declare method signatures, fields and variables
 * as {@link LocalDate}, not this interface.</b>
 * <p>
 * A {@code ChronoLocalDate} is the abstract representation of a date where the
 * {@code Chrono chronology}, or calendar system, is pluggable.
 * The date is defined in terms of fields expressed by {@link DateTimeField},
 * where most common implementations are defined in {@link ChronoField}.
 * The chronology defines how the calendar system operates and the meaning of
 * the standard fields.
 *
 * <h4>When to use this interface</h4>
 * The design of the API encourages the use of {@code LocalDate} rather than this
 * interface, even in the case where the application needs to deal with multiple
 * calendar systems. The rationale for this is explored in the following documentation.
 * <p>
 * The primary use case where this interface should be used is where the generic
 * type parameter {@code <C>} is fully defined as a specific chronology.
 * In that case, the assumptions of that chronology are known at development
 * time and specified in the code.
 * <p>
 * When the chronology is defined in the generic type parameter as ? or otherwise
 * unknown at development time, the rest of the discussion below applies.
 * <p>
 * To emphasize the point, declaring a method signature, field or variable as this
 * interface type can initially seem like the sensible way to globalize an application,
 * however it is usually the wrong approach.
 * As such, it should be considered an application-wide architectural decision to choose
 * to use this interface as opposed to {@code LocalDate}.
 *
 * <h4>Architectural issues to consider</h4>
 * These are some of the points that must be considered before using this interface
 * throughout an application.
 * <p>
 * 1) Applications using this interface, as opposed to using just {@code LocalDate},
 * face a significantly higher probability of bugs. This is because the calendar system
 * in use is not known at development time. A key cause of bugs is where the developer
 * applies assumptions from their day-to-day knowledge of the ISO calendar system
 * to code that is intended to deal with any arbitrary calendar system.
 * The section below outlines how those assumptions can cause problems
 * The primary mechanism for reducing this increased risk of bugs is a strong code review process.
 * This should also be considered a extra cost in maintenance for the lifetime of the code.
 * <p>
 * 2) This interface does not enforce immutability of implementations.
 * While the implementation notes indicate that all implementations must be immutable
 * there is nothing in the code or type system to enforce this. Any method declared
 * to accept a {@code ChronoLocalDate} could therefore be passed a poorly or
 * maliciously written mutable implementation.
 * <p>
 * 3) Applications using this interface  must consider the impact of eras.
 * {@code LocalDate} shields users from the concept of eras, by ensuring that {@code getYear()}
 * returns the proleptic year. That decision ensures that developers can think of
 * {@code LocalDate} instances as consisting of three fields - year, month-of-year and day-of-month.
 * By contrast, users of this interface must think of dates as consisting of four fields -
 * era, year-of-era, month-of-year and day-of-month. The extra era field is frequently
 * forgotten, yet it is of vital importance to dates in an arbitrary calendar system.
 * For example, in the Japanese calendar system, the era represents the reign of an Emperor.
 * Whenever one reign ends and another starts, the year-of-era is reset to one.
 * <p>
 * 4) The only agreed international standard for passing a date between two systems
 * is the ISO-8601 standard which requires the ISO calendar system. Using this interface
 * throughout the application will inevitably lead to the requirement to pass the date
 * across a network or component boundary, requiring an application specific protocol or format.
 * <p>
 * 5) Long term persistence, such as a database, will almost always only accept dates in the
 * ISO-8601 calendar system (or the related Julian-Gregorian). Passing around dates in other
 * calendar systems increases the complications of interacting with persistence.
 * <p>
 * 6) Most of the time, passing a {@code ChronoLocalDate} throughout an application
 * is unnecessary, as discussed in the last section below.
 *
 * <h4>False assumptions causing bugs in multi-calendar system code</h4>
 * As indicated above, there are many issues to consider when try to use and manipulate a
 * date in an arbitrary calendar system. These are some of the key issues.
 * <p>
 * Code that queries the day-of-month and assumes that the value will never be more than
 * 31 is invalid. Some calendar systems have more than 31 days in some months.
 * <p>
 * Code that adds 12 months to a date and assumes that a year has been added is invalid.
 * Some calendar systems have a different number of months, such as 13 in the Coptic or Ethiopic.
 * <p>
 * Code that adds one month to a date and assumes that the month-of-year value will increase
 * by one or wrap to the next year is invalid. Some calendar systems have a variable number
 * of months in a year, such as the Hebrew.
 * <p>
 * Code that adds one month, then adds a second one month and assumes that the day-of-month
 * will remain close to its original value is invalid. Some calendar systems have a large difference
 * between the length of the longest month and the length of the shortest month.
 * For example, the Coptic or Ethiopic have 12 months of 30 days and 1 month of 5 days.
 * <p>
 * Code that adds seven days and assumes that a week has been added is invalid.
 * Some calendar systems have weeks of other than seven days, such as the French Revolutionary.
 * <p>
 * Code that assumes that because the year of {@code date1} is greater than the year of {@code date2}
 * then {@code date1} is after {@code date2} is invalid. This is invalid for all calendar systems
 * when referring to the year-of-era, and especially untrue of the Japanese calendar system
 * where the year-of-era restarts with the reign of every new Emperor.
 * <p>
 * Code that treats month-of-year one and day-of-month one as the start of the year is invalid.
 * Not all calendar systems start the year when the month value is one.
 * <p>
 * In general, manipulating a date, and even querying a date, is wide open to bugs when the
 * calendar system is unknown at development time. This is why it is essential that code using
 * this interface is subjected to additional code reviews. It is also why an architectural
 * decision to avoid this interface type is usually the correct one.
 *
 * <h4>Using LocalDate instead</h4>
 * The primary alternative to using this interface throughout your application is as follows.
 * <p><ul>
 * <li>Declare all method signatures referring to dates in terms of {@code LocalDate}.
 * <li>Either store the chronology (calendar system) in the user profile or lookup
 *  the chronology from the user locale
 * <li>Convert the ISO {@code LocalDate} to and from the user's preferred calendar system during
 *  printing and parsing
 * </ul><p>
 * This approach treats the problem of globalized calendar systems as a localization issue
 * and confines it to the UI layer. This approach is in keeping with other localization
 * issues in the java platform.
 * <p>
 * As discussed above, performing calculations on a date where the rules of the calendar system
 * are pluggable requires skill and is not recommended.
 * Fortunately, the need to perform calculations on a date in an arbitrary calendar system
 * is extremely rare. For example, it is highly unlikely that the business rules of a library
 * book rental scheme will allow rentals to be for one month, where meaning of the month
 * is dependent on the user's preferred calendar system.
 * <p>
 * A key use case for calculations on a date in an arbitrary calendar system is producing
 * a month-by-month calendar for display and user interaction. Again, this is a UI issue,
 * and use of this interface solely within a few methods of the UI layer may be justified.
 * <p>
 * In any other part of the system, where a date must be manipulated in a calendar system
 * other than ISO, the use case will generally specify the calendar system to use.
 * For example, an application may need to calculate the next Islamic or Hebrew holiday
 * which may require manipulating the date.
 * This kind of use case can be handled as follows:
 * <p><ul>
 * <li>start from the ISO {@code LocalDate} being passed to the method
 * <li>convert the date to the alternate calendar system, which for this use case is known
 *  rather than arbitrary
 * <li>perform the calculation
 * <li>convert back to {@code LocalDate}
 * </ul>
 *
 * <h4>Implementation notes</h4>
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 * <p>
 * Additional calendar systems may be added to the system.
 * See {@link Chrono} for more details.
 *
 * @param <C> the chronology of this date
 */
public interface ChronoLocalDate<C extends Chrono<C>>
        extends DateTime, WithAdjuster, Comparable<ChronoLocalDate<?>> {

    /**
     * Comparator for two {@code ChronoLocalDate}s ignoring the chronology.
     * <p>
     * This comparator differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using {@code Long.compare(date1.toEpochDay(),  date2.toEpochDay())}.
     * 
     * @see #isAfter
     * @see #isBefore
     * @see #isEqual
     */
    public static final Comparator<ChronoLocalDate<?>> DATE_COMPARATOR =
            new Comparator<ChronoLocalDate<?>>() {
        @Override
        public int compare(ChronoLocalDate<?> date1, ChronoLocalDate<?> date2) {
            return Long.compare(date1.toEpochDay(), date2.toEpochDay());
        }
    };

    /**
     * Gets the chronology of this date.
     * <p>
     * The {@code Chrono} represents the calendar system in use.
     * The era and other fields in {@link ChronoField} are defined by the chronology.
     *
     * @return the chronology, not null
     */
    C getChrono();

    /**
     * Gets the era, as defined by the chronology.
     * <p>
     * The era is, conceptually, the largest division of the time-line.
     * Most calendar systems have a single epoch dividing the time-line into two eras.
     * However, some have multiple eras, such as one for the reign of each leader.
     * The exact meaning is determined by the {@code Chrono}.
     * <p>
     * All correctly implemented {@code Era} classes are singletons, thus it
     * is valid code to write {@code date.getEra() == SomeChrono.ERA_NAME)}.
     *
     * @return the chronology specific era constant applicable at this date, not null
     */
    Era<C> getEra();

    //-----------------------------------------------------------------------
    /**
     * Checks if the year is a leap year, as defined by the calendar system.
     * <p>
     * A leap-year is a year of a longer length than normal.
     * The exact meaning is determined by the chronology with the constraint that
     * a leap-year must imply a year-length longer than a non leap-year.
     * <p>
     * The default implementation uses {@link Chrono#isLeapYear(long)}.
     *
     * @return true if this date is in a leap year, false otherwise
     */
    boolean isLeapYear();

    /**
     * Returns the length of the month represented by this date, as defined by the calendar system.
     * <p>
     * This returns the length of the month in days.
     *
     * @return the length of the month in days
     */
    int lengthOfMonth();

    /**
     * Returns the length of the year represented by this date, as defined by the calendar system.
     * <p>
     * This returns the length of the year in days.
     * <p>
     * The default implementation uses {@link #isLeapYear()} and returns 365 or 366.
     *
     * @return the length of the year in days
     */
    int lengthOfYear();

    //-------------------------------------------------------------------------
    // override for covariant return type
    @Override
    ChronoLocalDate<C> with(WithAdjuster adjuster);

    @Override
    ChronoLocalDate<C> with(DateTimeField field, long newValue);

    @Override
    ChronoLocalDate<C> plus(PlusAdjuster adjuster);

    @Override
    ChronoLocalDate<C> plus(long amountToAdd, PeriodUnit unit);

    @Override
    ChronoLocalDate<C> minus(MinusAdjuster adjuster);

    @Override
    ChronoLocalDate<C> minus(long amountToSubtract, PeriodUnit unit);

    //-----------------------------------------------------------------------
    /**
     * Returns a date-time formed from this date at the specified time.
     * <p>
     * This merges the two objects - {@code this} and the specified time -
     * to form an instance of {@code ChronoLocalDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param localTime  the local time to use, not null
     * @return the local date-time formed from this date and the specified time, not null
     */
    ChronoLocalDateTime<C> atTime(LocalTime localTime);

    //-----------------------------------------------------------------------
    /**
     * Converts this date to the Epoch Day.
     * <p>
     * The {@link ChronoField#EPOCH_DAY Epoch Day count} is a simple
     * incrementing count of days where day 0 is 1970-01-01 (ISO).
     * This definition is the same for all chronologies, enabling conversion.
     *
     * @return the Epoch Day equivalent to this date
     */
    long toEpochDay();

    //-----------------------------------------------------------------------
    /**
     * Compares this date to another date, including the chronology.
     * <p>
     * The comparison is based first on the underlying time-line date, then
     * on the chronology.
     * It is "consistent with equals", as defined by {@link Comparable}.
     * <p>
     * For example, the following is the comparator order:
     * <ol>
     * <li>{@code 2012-12-03 (ISO)}</li>
     * <li>{@code 2012-12-04 (ISO)}</li>
     * <li>{@code 2555-12-04 (ThaiBuddhist)}</li>
     * <li>{@code 2012-12-05 (ISO)}</li>
     * </ol>
     * Values #2 and #3 represent the same date on the time-line.
     * When two values represent the same date, the chronology ID is compared to distinguish them.
     * This step is needed to make the ordering "consistent with equals".
     * <p>
     * If all the date objects being compared are in the same chronology, then the
     * additional chronology stage is not required and only the local date is used.
     * To compare the dates of two {@code DateTimeAccessor} instances, including dates
     * in two different chronologies, use {@link ChronoField#EPOCH_DAY} as a comparator.
     *
     * @param other  the other date to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    @Override
    int compareTo(ChronoLocalDate<?> other);

    //-----------------------------------------------------------------------
    /**
     * Checks if this date is after the specified date ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using {@code date1.toEpochDay() &gt; date2.toEpochDay()}.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is after the specified date
     */
    boolean isAfter(ChronoLocalDate<?> other);

    /**
     * Checks if this date is before the specified date ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using {@code date1.toEpochDay() &lt; date2.toEpochDay()}.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is before the specified date
     */
    boolean isBefore(ChronoLocalDate<?> other);

    /**
     * Checks if this date is equal to the specified date ignoring the chronology.
     * <p>
     * This method differs from the comparison in {@link #compareTo} in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using {@code date1.toEpochDay() == date2.toEpochDay()}.
     *
     * @param  other  the other date to compare to, not null
     * @return true if the underlying date is equal to the specified date
     */
    boolean isEqual(ChronoLocalDate<?> other);

    //-----------------------------------------------------------------------
    /**
     * Checks if this date is equal to another date, including the chronology.
     * <p>
     * Compares this date with another ensuring that the date and chronology are the same.
     * <p>
     * To compare the dates of two {@code DateTimeAccessor} instances, including dates
     * in two different chronologies, use {@link ChronoField#EPOCH_DAY} as a comparator.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other date
     */
    @Override
    boolean equals(Object obj);

    /**
     * A hash code for this date.
     *
     * @return a suitable hash code
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * Outputs this date as a {@code String}.
     * <p>
     * The output will include the full local date and the chronology ID.
     *
     * @return the formatted date, not null
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
