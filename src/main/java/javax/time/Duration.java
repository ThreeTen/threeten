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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import javax.time.calendrical.LocalPeriodUnit;
import javax.time.calendrical.PeriodUnit;
import javax.time.format.DateTimeParseException;

/**
 * A duration between two instants on the time-line.
 * <p>
 * This class models a duration of time and is not tied to any instant.
 * The model is of a directed duration, meaning that the duration may be negative.
 * <p>
 * A physical duration could be of infinite length.
 * For practicality, the duration is stored with constraints similar to {@link Instant}.
 * The duration uses nanosecond resolution with a maximum value of the seconds that can
 * be held in a {@code long}. This is greater than the current estimated age of the universe.
 * <p>
 * The range of a duration requires the storage of a number larger than a {@code long}.
 * To achieve this, the class stores a {@code long} representing seconds and an {@code int}
 * representing nanosecond-of-second, which will always be between 0 and 999,999,999.
 * <p>
 * The duration is measured in "seconds", but these are not necessarily identical to
 * the scientific "SI second" definition based on atomic clocks.
 * This difference only impacts durations measured near a leap-second and should not affect
 * most applications.
 * See {@link Instant} for a discussion as to the meaning of the second and time-scales.
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class Duration implements Comparable<Duration>, Serializable {

    /**
     * Constant for a duration of zero.
     */
    public static final Duration ZERO = new Duration(0, 0);
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Constant for nanos per second.
     */
    private static final int NANOS_PER_SECOND = 1000000000;
    /**
     * Constant for nanos per second.
     */
    private static final BigInteger BI_NANOS_PER_SECOND = BigInteger.valueOf(NANOS_PER_SECOND);

    /**
     * The number of seconds in the duration.
     */
    private final long seconds;
    /**
     * The number of nanoseconds in the duration, expressed as a fraction of the
     * number of seconds. This is always positive, and never exceeds 999,999,999.
     */
    private final int nanos;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} from a number of seconds.
     * <p>
     * The nanosecond in second field is set to zero.
     *
     * @param seconds  the number of seconds, positive or negative
     * @return a {@code Duration}, not null
     */
    public static Duration ofSeconds(long seconds) {
        return create(seconds, 0);
    }

    /**
     * Obtains an instance of {@code Duration} from a number of seconds
     * and an adjustment in nanoseconds.
     * <p>
     * This method allows an arbitrary number of nanoseconds to be passed in.
     * The factory will alter the values of the second and nanosecond in order
     * to ensure that the stored nanosecond is in the range 0 to 999,999,999.
     * For example, the following will result in the exactly the same duration:
     * <pre>
     *  Duration.ofSeconds(3, 1);
     *  Duration.ofSeconds(4, -999999999);
     *  Duration.ofSeconds(2, 1000000001);
     * </pre>
     *
     * @param seconds  the number of seconds, positive or negative
     * @param nanoAdjustment  the nanosecond adjustment to the number of seconds, positive or negative
     * @return a {@code Duration}, not null
     * @throws ArithmeticException if the adjustment causes the seconds to exceed the capacity of {@code Duration}
     */
    public static Duration ofSeconds(long seconds, long nanoAdjustment) {
        long secs = DateTimes.safeAdd(seconds, nanoAdjustment / NANOS_PER_SECOND);
        int nos = (int) (nanoAdjustment % NANOS_PER_SECOND);
        if (nos < 0) {
            nos += NANOS_PER_SECOND;
            secs = DateTimes.safeDecrement(secs);
        }
        return create(secs, nos);
    }

    /**
     * Obtains an instance of {@code Duration} from a number of seconds.
     * <p>
     * The seconds and nanoseconds are extracted from the specified {@code BigDecimal}.
     * If the decimal is larger than {@code Long.MAX_VALUE} or has more than 9 decimal
     * places then an exception is thrown.
     *
     * @param seconds  the number of seconds, up to scale 9, positive or negative
     * @return a {@code Duration}, not null
     * @throws ArithmeticException if the input seconds exceeds the capacity of a {@code Duration}
     */
    public static Duration ofSeconds(BigDecimal seconds) {
        DateTimes.checkNotNull(seconds, "Seconds must not be null");
        BigInteger nanos = seconds.movePointRight(9).toBigIntegerExact();
        BigInteger[] divRem = nanos.divideAndRemainder(BI_NANOS_PER_SECOND);
        if (divRem[0].bitLength() > 63) {
            throw new ArithmeticException("Exceeds capacity of Duration: " + nanos);
        }
        return ofSeconds(divRem[0].longValue(), divRem[1].intValue());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} from a number of milliseconds.
     * <p>
     * The seconds and nanoseconds are extracted from the specified milliseconds.
     *
     * @param millis  the number of milliseconds, positive or negative
     * @return a {@code Duration}, not null
     */
    public static Duration ofMillis(long millis) {
        long secs = millis / 1000;
        int mos = (int) (millis % 1000);
        if (mos < 0) {
            mos += 1000;
            secs--;
        }
        return create(secs, mos * 1000000);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} from a number of nanoseconds.
     * <p>
     * The seconds and nanoseconds are extracted from the specified nanoseconds.
     *
     * @param nanos  the number of nanoseconds, positive or negative
     * @return a {@code Duration}, not null
     */
    public static Duration ofNanos(long nanos) {
        long secs = nanos / NANOS_PER_SECOND;
        int nos = (int) (nanos % NANOS_PER_SECOND);
        if (nos < 0) {
            nos += NANOS_PER_SECOND;
            secs--;
        }
        return create(secs, nos);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} from a number of standard length minutes.
     * <p>
     * The seconds are calculated based on the standard definition of a minute,
     * where each minute is 60 seconds.
     * The nanosecond in second field is set to zero.
     *
     * @param minutes  the number of minutes, positive or negative
     * @return a {@code Duration}, not null
     * @throws ArithmeticException if the input minutes exceeds the capacity of {@code Duration}
     */
    public static Duration ofMinutes(long minutes) {
        return create(DateTimes.safeMultiply(minutes, 60), 0);
    }

    /**
     * Obtains an instance of {@code Duration} from a number of standard length hours.
     * <p>
     * The seconds are calculated based on the standard definition of an hour,
     * where each hour is 3600 seconds.
     * The nanosecond in second field is set to zero.
     *
     * @param hours  the number of hours, positive or negative
     * @return a {@code Duration}, not null
     * @throws ArithmeticException if the input hours exceeds the capacity of {@code Duration}
     */
    public static Duration ofHours(long hours) {
        return create(DateTimes.safeMultiply(hours, 3600), 0);
    }

    /**
     * Obtains an instance of {@code Duration} from a number of standard 24 hour days.
     * <p>
     * The seconds are calculated based on the standard definition of a day,
     * where each day is 86400 seconds which implies a 24 hour day.
     * The nanosecond in second field is set to zero.
     *
     * @param days  the number of days, positive or negative
     * @return a {@code Duration}, not null
     * @throws ArithmeticException if the input days exceeds the capacity of {@code Duration}
     */
    public static Duration ofDays(long days) {
        return create(DateTimes.safeMultiply(days, 86400), 0);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} from a duration in a specified unit.
     * <p>
     * The duration amount is measured in terms of the specified unit. For example:
     * <pre>
     *  Duration.of(3, SECONDS);
     *  Duration.of(465, HOURS);
     * </pre>
     * Only units with an {@link PeriodUnit#isDurationEstimated() exact duration}
     * are accepted by this method, other units throw an exception.
     *
     * @param amount  the amount of the period, measured in terms of the unit, positive or negative
     * @param unit  the unit that the period is measured in, must have an exact duration, not null
     * @return a {@code Duration}, not null
     * @throws DateTimeException if the period unit has an estimated duration
     * @throws ArithmeticException if a numeric overflow occurs
     */
    public static Duration of(long amount, PeriodUnit unit) {
        return ZERO.plus(amount, unit);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} representing the duration between two instants.
     * <p>
     * A {@code Duration} represents a directed distance between two points on the time-line.
     * As such, this method will return a negative duration if the end is before the start.
     * To guarantee to obtain a positive duration call {@link #abs()} on the result of this factory.
     *
     * @param startInclusive  the start instant, inclusive, not null
     * @param endExclusive  the end instant, exclusive, not null
     * @return a {@code Duration}, not null
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Duration}
     */
    public static Duration between(Instant startInclusive, Instant endExclusive) {
        long secs = DateTimes.safeSubtract(endExclusive.getEpochSecond(), startInclusive.getEpochSecond());
        int nanos = endExclusive.getNano() - startInclusive.getNano();
        if (nanos < 0) {
            nanos += NANOS_PER_SECOND;
            secs = DateTimes.safeDecrement(secs);
        }
        return create(secs, nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} by parsing a text string.
     * <p>
     * This will parse the string produced by {@link #toString()} which is
     * the ISO-8601 format {@code PTnS} where {@code n} is
     * the number of seconds with optional decimal part.
     * The number must consist of ASCII numerals.
     * There must only be a negative sign at the start of the number and it can
     * only be present if the value is less than zero.
     * There must be at least one digit before any decimal point.
     * There must be between 1 and 9 inclusive digits after any decimal point.
     * The letters (P, T and S) will be accepted in upper or lower case.
     * The decimal point may be either a dot or a comma.
     *
     * @param text  the text to parse, not null
     * @return a {@code Duration}, not null
     * @throws DateTimeParseException if the text cannot be parsed to a {@code Duration}
     */
    public static Duration parse(final CharSequence text) {
        DateTimes.checkNotNull(text, "Text to parse must not be null");
        int len = text.length();
        if (len < 4 ||
                (text.charAt(0) != 'P' && text.charAt(0) != 'p') ||
                (text.charAt(1) != 'T' && text.charAt(1) != 't') ||
                (text.charAt(len - 1) != 'S' && text.charAt(len - 1) != 's') ||
                (len == 5 && text.charAt(2) == '-' && text.charAt(3) == '0')) {
            throw new DateTimeParseException("Duration could not be parsed: " + text, text, 0);
        }
        String numberText = text.subSequence(2, len - 1).toString().replace(',', '.');
        if (numberText.charAt(0) == '+') {
            throw new DateTimeParseException("Duration could not be parsed: " + text, text, 2);
        }
        int dot = numberText.indexOf('.');
        try {
            if (dot == -1) {
                // no decimal places
                if (numberText.startsWith("-0")) {
                    throw new DateTimeParseException("Duration could not be parsed: " + text, text, 2);
                }
                return create(Long.parseLong(numberText), 0);
            }
            // decimal places
            boolean negative = false;
            if (numberText.charAt(0) == '-') {
                negative = true;
            }
            long secs = Long.parseLong(numberText.substring(0, dot));
            numberText = numberText.substring(dot + 1);
            len = numberText.length();
            if (len == 0 || len > 9 || numberText.charAt(0) == '-' || numberText.charAt(0) == '+') {
                throw new DateTimeParseException("Duration could not be parsed: " + text, text, 2);
            }
            int nanos = Integer.parseInt(numberText);
            switch (len) {
                case 1:
                    nanos *= 100000000;
                    break;
                case 2:
                    nanos *= 10000000;
                    break;
                case 3:
                    nanos *= 1000000;
                    break;
                case 4:
                    nanos *= 100000;
                    break;
                case 5:
                    nanos *= 10000;
                    break;
                case 6:
                    nanos *= 1000;
                    break;
                case 7:
                    nanos *= 100;
                    break;
                case 8:
                    nanos *= 10;
                    break;
            }
            return negative ? ofSeconds(secs, -nanos) : create(secs, nanos);
            
        } catch (ArithmeticException ex) {
            throw new DateTimeParseException("Duration could not be parsed: " + text, text, 2, ex);
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Duration could not be parsed: " + text, text, 2, ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Duration} using seconds and nanoseconds.
     *
     * @param seconds  the length of the duration in seconds, positive or negative
     * @param nanoAdjustment  the nanosecond adjustment within the second, from 0 to 999,999,999
     */
    private static Duration create(long seconds, int nanoAdjustment) {
        if ((seconds | nanoAdjustment) == 0) {
            return ZERO;
        }
        return new Duration(seconds, nanoAdjustment);
    }

    /**
     * Constructs an instance of {@code Duration} using seconds and nanoseconds.
     *
     * @param seconds  the length of the duration in seconds, positive or negative
     * @param nanos  the nanoseconds within the second, from 0 to 999,999,999
     */
    private Duration(long seconds, int nanos) {
        super();
        this.seconds = seconds;
        this.nanos = nanos;
    }

    /**
     * Resolves singletons.
     *
     * @return the resolved instance, not null
     */
    private Object readResolve() {
        return (seconds | nanos) == 0 ? ZERO : this;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this duration is zero length.
     * <p>
     * A {@code Duration} represents a directed distance between two points on
     * the time-line and can therefore be positive, zero or negative.
     * This method checks whether the length is zero.
     *
     * @return true if this duration has a total length equal to zero
     */
    public boolean isZero() {
        return (seconds | nanos) == 0;
    }

    /**
     * Checks if this duration is positive, excluding zero.
     * <p>
     * A {@code Duration} represents a directed distance between two points on
     * the time-line and can therefore be positive, zero or negative.
     * This method checks whether the length is greater than zero.
     *
     * @return true if this duration has a total length greater than zero
     */
    public boolean isPositive() {
        return seconds >= 0 && ((seconds | nanos) != 0);
    }

    /**
     * Checks if this duration is negative, excluding zero.
     * <p>
     * A {@code Duration} represents a directed distance between two points on
     * the time-line and can therefore be positive, zero or negative.
     * This method checks whether the length is less than zero.
     *
     * @return true if this duration has a total length less than zero
     */
    public boolean isNegative() {
        return seconds < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number of seconds in this duration.
     * <p>
     * The length of the duration is stored using two fields - seconds and nanoseconds.
     * The nanoseconds part is a value from 0 to 999,999,999 that is an adjustment to
     * the length in seconds.
     * The total duration is defined by calling this method and {@link #getNano()}.
     * <p>
     * A {@code Duration} represents a directed distance between two points on the time-line.
     * A negative duration is expressed by the negative sign of the seconds part.
     * A duration of -1 nanosecond is stored as -1 seconds plus 999,999,999 nanoseconds.
     *
     * @return the whole seconds part of the length of the duration, positive or negative
     */
    public long getSeconds() {
        return seconds;
    }

    /**
     * Gets the number of nanoseconds within the second in this duration.
     * <p>
     * The length of the duration is stored using two fields - seconds and nanoseconds.
     * The nanoseconds part is a value from 0 to 999,999,999 that is an adjustment to
     * the length in seconds.
     * The total duration is defined by calling this method and {@link #getSeconds()}.
     * <p>
     * A {@code Duration} represents a directed distance between two points on the time-line.
     * A negative duration is expressed by the negative sign of the seconds part.
     * A duration of -1 nanosecond is stored as -1 seconds plus 999,999,999 nanoseconds.
     *
     * @return the nanoseconds within the second part of the length of the duration, from 0 to 999,999,999
     */
    public int getNano() {
        return nanos;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to add, positive or negative, not null
     * @return a {@code Duration} based on this duration with the specified duration added, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration plus(Duration duration) {
        return plus(duration.getSeconds(), duration.getNano());
     }

    /**
     * Returns a copy of this duration with the specified duration added.
     * <p>
     * The duration amount is measured in terms of the specified unit.
     * Only units with an {@link PeriodUnit#isDurationEstimated() exact duration}
     * are accepted by this method, other units throw an exception.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount of the period, measured in terms of the unit, positive or negative
     * @param unit  the unit that the period is measured in, must have an exact duration, not null
     * @return a {@code Duration} based on this duration with the specified duration added, not null
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Duration}
     */
    public Duration plus(long amountToAdd, PeriodUnit unit) {
        if (unit.isDurationEstimated()) {
            throw new DateTimeException("Unit must not have an estimated duration");
        }
        if (amountToAdd == 0) {
            return this;
        }
        if (unit instanceof LocalPeriodUnit) {
            switch ((LocalPeriodUnit) unit) {
                case NANOS: return plusNanos(amountToAdd);
                case MICROS: return plusSeconds((amountToAdd / (1000000L * 1000)) * 1000).plusNanos((amountToAdd % (1000000L * 1000)) * 1000);
                case MILLIS: return plusMillis(amountToAdd);
                case SECONDS: return plusSeconds(amountToAdd);
            }
            return ofSeconds(DateTimes.safeMultiply(unit.getDuration().seconds, amountToAdd));
        }
        Duration duration = unit.getDuration().multipliedBy(amountToAdd);
        return plusSeconds(duration.getSeconds()).plusNanos(duration.getNano());
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration in seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToAdd  the seconds to add, positive or negative
     * @return a {@code Duration} based on this duration with the specified seconds added, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration plusSeconds(long secondsToAdd) {
        return plus(secondsToAdd, 0);
    }

    /**
     * Returns a copy of this duration with the specified duration in milliseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param millisToAdd  the milliseconds to add, positive or negative
     * @return a {@code Duration} based on this duration with the specified milliseconds added, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration plusMillis(long millisToAdd) {
        return plus(millisToAdd / 1000, (millisToAdd % 1000) * 1000000);
    }

    /**
     * Returns a copy of this duration with the specified duration in nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToAdd  the nanoseconds to add, positive or negative
     * @return a {@code Duration} based on this duration with the specified nanoseconds added, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration plusNanos(long nanosToAdd) {
        return plus(0, nanosToAdd);
    }

    /**
     * Returns a copy of this duration with the specified duration added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToAdd  the seconds to add, positive or negative
     * @param nanosToAdd  the nanos to add, positive or negative
     * @return a {@code Duration} based on this duration with the specified seconds added, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    private Duration plus(long secondsToAdd, long nanosToAdd) {
        if ((secondsToAdd | nanosToAdd) == 0) {
            return this;
        }
        long epochSec = DateTimes.safeAdd(seconds, secondsToAdd);
        epochSec = DateTimes.safeAdd(epochSec, nanosToAdd / NANOS_PER_SECOND);
        nanosToAdd = nanosToAdd % NANOS_PER_SECOND;
        long nanoAdjustment = nanos + nanosToAdd;  // safe int+NANOS_PER_SECOND
        return ofSeconds(epochSec, nanoAdjustment);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to subtract, positive or negative, not null
     * @return a {@code Duration} based on this duration with the specified duration subtracted, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration minus(Duration duration) {
        long secsToSubtract = duration.getSeconds();
        int nanosToSubtract = duration.getNano();
        if (secsToSubtract == Long.MIN_VALUE) {
            return plus(1, 0).plus(Long.MAX_VALUE, -nanosToSubtract);
        }
        return plus(-secsToSubtract, -nanosToSubtract);
     }

    /**
     * Returns a copy of this duration with the specified duration subtracted.
     * <p>
     * The duration amount is measured in terms of the specified unit.
     * Only units with an {@link PeriodUnit#isDurationEstimated() exact duration}
     * are accepted by this method, other units throw an exception.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount of the period, measured in terms of the unit, positive or negative
     * @param unit  the unit that the period is measured in, must have an exact duration, not null
     * @return a {@code Duration} based on this duration with the specified duration subtracted, not null
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Duration}
     */
    public Duration minus(long amountToSubtract, PeriodUnit unit) {
        if (amountToSubtract == Long.MIN_VALUE) {
            return plus(Long.MAX_VALUE, unit).plus(1, unit);
        }
        return plus(-amountToSubtract, unit);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration in seconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToSubtract  the seconds to subtract, positive or negative
     * @return a {@code Duration} based on this duration with the specified seconds subtracted, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration minusSeconds(long secondsToSubtract) {
        if (secondsToSubtract == Long.MIN_VALUE) {
            return plusSeconds(Long.MAX_VALUE).plusSeconds(1);
        }
        return plusSeconds(-secondsToSubtract);
    }

    /**
     * Returns a copy of this duration with the specified duration in milliseconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param millisToSubtract  the milliseconds to subtract, positive or negative
     * @return a {@code Duration} based on this duration with the specified milliseconds subtracted, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration minusMillis(long millisToSubtract) {
        if (millisToSubtract == Long.MIN_VALUE) {
            return plusMillis(Long.MAX_VALUE).plusMillis(1);
        }
        return plusMillis(-millisToSubtract);
    }

    /**
     * Returns a copy of this duration with the specified duration in nanoseconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToSubtract  the nanoseconds to subtract, positive or negative
     * @return a {@code Duration} based on this duration with the specified nanoseconds subtracted, not null
     * @throws ArithmeticException if the calculation exceeds the supported range
     */
    public Duration minusNanos(long nanosToSubtract) {
        if (nanosToSubtract == Long.MIN_VALUE) {
            return plusNanos(Long.MAX_VALUE).plusNanos(1);
        }
        return plusNanos(-nanosToSubtract);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration multiplied by the scalar.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param multiplicand  the value to multiply the duration by, positive or negative
     * @return a {@code Duration} based on this duration multiplied by the specified scalar, not null
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Duration}
     */
    public Duration multipliedBy(long multiplicand) {
        if (multiplicand == 0) {
            return ZERO;
        }
        if (multiplicand == 1) {
            return this;
        }
        return ofSeconds(toSeconds().multiply(BigDecimal.valueOf(multiplicand)));
     }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration divided by the specified value.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param divisor  the value to divide the duration by, positive or negative, not zero
     * @return a {@code Duration} based on this duration divided by the specified divisor, not null
     * @throws ArithmeticException if the divisor is zero
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Duration}
     */
    public Duration dividedBy(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        if (divisor == 1) {
            return this;
        }
        return ofSeconds(toSeconds().divide(BigDecimal.valueOf(divisor), RoundingMode.DOWN));
     }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the length negated.
     * <p>
     * This method swaps the sign of the total length of this duration.
     * For example, {@code PT1.3S} will be returned as {@code PT-1.3S}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code Duration} based on this period with the amount negated, not null
     * @throws ArithmeticException if the seconds part of the length is {@code Long.MIN_VALUE}
     */
    public Duration negated() {
        return multipliedBy(-1);
    }

    /**
     * Returns a copy of this duration with a positive length.
     * <p>
     * This method returns a positive duration by effectively removing the sign from any negative total length.
     * For example, {@code PT-1.3S} will be returned as {@code PT1.3S}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code Duration} based on this period with an absolute length, not null
     * @throws ArithmeticException if the seconds part of the length is {@code Long.MIN_VALUE}
     */
    public Duration abs() {
        return isNegative() ? negated() : this;
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this duration to the total length in seconds and
     * fractional nanoseconds expressed as a {@code BigDecimal}.
     *
     * @return the total length of the duration in seconds, with a scale of 9, not null
     */
    public BigDecimal toSeconds() {
        return BigDecimal.valueOf(seconds).add(BigDecimal.valueOf(nanos, 9));
    }

    /**
     * Converts this duration to the total length in milliseconds.
     * <p>
     * If this duration is too large to fit in a {@code long} milliseconds, then an
     * exception is thrown.
     * <p>
     * If this duration has greater than millisecond precision, then the conversion
     * will drop any excess precision information as though the amount in nanoseconds
     * was subject to integer division by one million.
     *
     * @return the total length of the duration in milliseconds
     * @throws ArithmeticException if the length exceeds the capacity of a {@code long}
     */
    public long toMillis() {
        long millis = DateTimes.safeMultiply(seconds, 1000);
        millis = DateTimes.safeAdd(millis, nanos / 1000000);
        return millis;
    }

    /**
     * Converts this duration to the total length in nanoseconds expressed as a {@code long}.
     * <p>
     * If this duration is too large to fit in a {@code long} nanoseconds, then an
     * exception is thrown.
     *
     * @return the total length of the duration in nanoseconds
     * @throws ArithmeticException if the length exceeds the capacity of a {@code long}
     */
    public long toNanos() {
        long millis = DateTimes.safeMultiply(seconds, 1000000000);
        millis = DateTimes.safeAdd(millis, nanos);
        return millis;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this duration to the specified {@code Duration}.
     * <p>
     * The comparison is based on the total length of the durations.
     *
     * @param otherDuration  the other duration to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    public int compareTo(Duration otherDuration) {
        int cmp = DateTimes.safeCompare(seconds, otherDuration.seconds);
        if (cmp != 0) {
            return cmp;
        }
        return DateTimes.safeCompare(nanos, otherDuration.nanos);
    }

    /**
     * Checks if this duration is greater than the specified {@code Duration}.
     * <p>
     * The comparison is based on the total length of the durations.
     *
     * @param otherDuration  the other duration to compare to, not null
     * @return true if this duration is greater than the specified duration
     */
    public boolean isGreaterThan(Duration otherDuration) {
        return compareTo(otherDuration) > 0;
    }

    /**
     * Checks if this duration is less than the specified {@code Duration}.
     * <p>
     * The comparison is based on the total length of the durations.
     *
     * @param otherDuration  the other duration to compare to, not null
     * @return true if this duration is less than the specified duration
     */
    public boolean isLessThan(Duration otherDuration) {
        return compareTo(otherDuration) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this duration is equal to the specified {@code Duration}.
     * <p>
     * The comparison is based on the total length of the durations.
     *
     * @param otherDuration  the other duration, null returns false
     * @return true if the other duration is equal to this one
     */
    @Override
    public boolean equals(Object otherDuration) {
        if (this == otherDuration) {
            return true;
        }
        if (otherDuration instanceof Duration) {
            Duration other = (Duration) otherDuration;
            return this.seconds == other.seconds &&
                   this.nanos == other.nanos;
        }
        return false;
    }

    /**
     * A hash code for this duration.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return ((int) (seconds ^ (seconds >>> 32))) + (51 * nanos);
    }

    //-----------------------------------------------------------------------
    /**
     * A string representation of this duration using ISO-8601 seconds
     * based representation, such as {@code PT12.345S}.
     * <p>
     * The format of the returned string will be {@code PTnS} where n is
     * the seconds and fractional seconds of the duration.
     *
     * @return an ISO-8601 representation of this duration, not null
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(24);
        buf.append("PT");
        if (seconds < 0 && nanos > 0) {
            if (seconds == -1) {
                buf.append("-0");
            } else {
                buf.append(seconds + 1);
            }
        } else {
            buf.append(seconds);
        }
        if (nanos > 0) {
            int pos = buf.length();
            if (seconds < 0) {
                buf.append(2 * NANOS_PER_SECOND - nanos);
            } else {
                buf.append(nanos + NANOS_PER_SECOND);
            }
            while (buf.charAt(buf.length() - 1) == '0') {
                buf.setLength(buf.length() - 1);
            }
            buf.setCharAt(pos, '.');
        }
        buf.append('S');
        return buf.toString();
    }

}
