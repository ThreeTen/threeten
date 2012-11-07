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
package javax.time.extra;

import static javax.time.calendrical.LocalDateTimeField.NANO_OF_SECOND;

import java.io.Serializable;

import javax.time.DateTimeException;
import javax.time.LocalTime;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.LocalDateTimeField;

/**
 * A representation of a nano-of-second in the ISO-8601 calendar system.
 * <p>
 * NanoOfSecond is an immutable time field that can only store a nano-of-second.
 * It is a type-safe way of representing a nano-of-second in an application.
 * <p>
 * Static factory methods allow you to construct instances.
 * The nano-of-second may be queried using getValue().
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class NanoOfSecond
        implements Comparable<NanoOfSecond>, WithAdjuster, Serializable {

    /**
     * A singleton instance for zero nanoseconds.
     */
    public static final NanoOfSecond ZERO = new NanoOfSecond(0);

    /**
     * A serialization identifier for this instance.
     */
    private static final long serialVersionUID = 7701185165999361415L;

    /**
     * The nano-of-second being represented.
     */
    private final int nanoOfSecond;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code NanoOfSecond}.
     *
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return the nano-of-second, not null
     * @throws DateTimeException if the nano-of-second is invalid
     */
    public static NanoOfSecond of(int nanoOfSecond) {
        LocalDateTimeField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        if (nanoOfSecond == 0) {
            return ZERO;
        }
        return new NanoOfSecond(nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code NanoOfSecond} from a date-time object.
     * <p>
     * A {@code DateTimeAccessor} represents some form of date and time information.
     * This factory converts the arbitrary date-time object to an instance of {@code NanoOfSecond}.
     * 
     * @param dateTime  the date-time object to convert, not null
     * @return the nano-of-second, not null
     * @throws DateTimeException if unable to convert to a {@code NanoOfSecond}
     */
    public static NanoOfSecond from(DateTimeAccessor dateTime) {
        LocalTime time = LocalTime.from(dateTime);
        return NanoOfSecond.of(time.getNano());
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs an instance with the specified nano-of-second.
     *
     * @param nanoOfSecond  the nano-of-second to represent
     */
    private NanoOfSecond(int nanoOfSecond) {
        this.nanoOfSecond = nanoOfSecond;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the field that defines how the nano-of-second field operates.
     * <p>
     * The field provides access to the minimum and maximum values, and a
     * generic way to access values within a date-time.
     *
     * @return the nano-of-second field, never null
     */
    public DateTimeField getField() {
        return LocalDateTimeField.NANO_OF_SECOND;
    }

    /**
     * Gets the nano-of-second value.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    public int getValue() {
        return nanoOfSecond;
    }

//    /**
//     * Gets the value as a fraction of a second between 0 and 1.
//     * <p>
//     * The fractional value is between 0 (inclusive) and 1 (exclusive).
//     * The fraction will have the minimum scale necessary to represent the fraction.
//     *
//     * @return the nano-of-second, from 0 to 0.999,999,999, never null
//     */
//    public BigDecimal getFractionalValue() {
//        return getField().convertToFraction(nanoOfSecond);
//    }

    //-----------------------------------------------------------------------
    /**
     * Adjusts a time to have the nano-of-second represented by this object,
     * returning a new time.
     * <p>
     * Only the nano-of-second field is adjusted in the result. The other time
     * fields are unaffected.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dateTime  the time to be adjusted, not null
     * @return the adjusted time, never null
     */
    @Override
    public DateTime doWithAdjustment(DateTime dateTime) {
        return dateTime.with(NANO_OF_SECOND, nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this nano-of-second instance to another.
     *
     * @param otherNanoOfSecond  the other nano-of-second instance, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if otherNanoOfSecond is null
     */
    public int compareTo(NanoOfSecond otherNanoOfSecond) {
        int thisValue = this.nanoOfSecond;
        int otherValue = otherNanoOfSecond.nanoOfSecond;
        return (thisValue < otherValue ? -1 : (thisValue == otherValue ? 0 : 1));
    }

    //-----------------------------------------------------------------------
    /**
     * Is this instance equal to that specified, evaluating the nano-of-second.
     *
     * @param otherNanoOfSecond  the other nano-of-second instance, null returns false
     * @return true if the nano-of-second is the same
     */
    @Override
    public boolean equals(Object otherNanoOfSecond) {
        if (this == otherNanoOfSecond) {
            return true;
        }
        if (otherNanoOfSecond instanceof NanoOfSecond) {
            return nanoOfSecond == ((NanoOfSecond) otherNanoOfSecond).nanoOfSecond;
        }
        return false;
    }

    /**
     * A hash code for the nano-of-second object.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return nanoOfSecond;
    }

    /**
     * A string describing the nano-of-second object.
     *
     * @return a string describing this object
     */
    @Override
    public String toString() {
        return "NanoOfSecond=" + getValue();
    }

}
