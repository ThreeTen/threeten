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

import static javax.time.calendrical.LocalDateTimeField.OFFSET_SECONDS;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.jdk8.DefaultInterfaceDateTimeAccessor;
import javax.time.zone.ZoneOffsetInfo;

/**
 * A time-zone offset from Greenwich/UTC, such as {@code +02:00}.
 * <p>
 * A time-zone offset is the period of time that a time-zone differs from Greenwich/UTC.
 * This is usually a fixed number of hours and minutes.
 * <p>
 * Different parts of the world have different time-zone offsets.
 * The rules for how offsets vary by place and time of year are captured in the
 * {@link ZoneId} class.
 * <p>
 * For example, Paris is one hour ahead of Greenwich/UTC in winter and two hours
 * ahead in summer. The {@code ZoneId} instance for Paris will reference two
 * {@code ZoneOffset} instances - a {@code +01:00} instance for winter,
 * and a {@code +02:00} instance for summer.
 * <p>
 * In 2008, time-zone offsets around the world extended from -12:00 to +14:00.
 * To prevent any problems with that range being extended, yet still provide
 * validation, the range of offsets is restricted to -18:00 to 18:00 inclusive.
 * <p>
 * This class is designed for use with the ISO calendar system
 * The fields of hours, minutes and seconds make assumptions that are valid for the
 * standard ISO definitions of those fields. This class may be used with other
 * calendar systems providing the definition of the time fields matches those
 * of the ISO calendar system.
 * <p>
 * Instances of {@code ZoneOffset} must be compared using {@link #equals}.
 * Implementations may choose to cache certain common offsets, however
 * applications must not rely on such caching.
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class ZoneOffset
        extends DefaultInterfaceDateTimeAccessor
        implements DateTimeAccessor, WithAdjuster, ZoneOffsetInfo, Comparable<ZoneOffset>, Serializable {

    /** Cache of time-zone offset by offset in seconds. */
    private static final ConcurrentMap<Integer, ZoneOffset> SECONDS_CACHE = new ConcurrentHashMap<Integer, ZoneOffset>(16, 0.75f, 4);
    /** Cache of time-zone offset by ID. */
    private static final ConcurrentMap<String, ZoneOffset> ID_CACHE = new ConcurrentHashMap<String, ZoneOffset>(16, 0.75f, 4);

    /**
     * The time-zone offset for UTC, with an ID of 'Z'.
     */
    public static final ZoneOffset UTC = ofHoursMinutesSeconds(0, 0, 0);
    /**
     * The number of seconds per hour.
     */
    private static final int SECONDS_PER_HOUR = 60 * 60;
    /**
     * The number of seconds per minute.
     */
    private static final int SECONDS_PER_MINUTE = 60;
    /**
     * The number of minutes per hour.
     */
    private static final int MINUTES_PER_HOUR = 60;
    /**
     * The abs maximum seconds.
     */
    private static final int MAX_SECONDS = 18 * SECONDS_PER_HOUR;
    /**
     * Serialization version.
     */
   private static final long serialVersionUID = 2357656521762053153L;

    /**
     * The total offset in seconds.
     */
    private final int totalSeconds;
    /**
     * The string form of the time-zone offset.
     */
    private final transient String id;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZoneOffset} using the ID.
     * <p>
     * This method parses the string ID of a {@code ZoneOffset} to
     * return an instance. The parsing accepts all the formats generated by
     * {@link #getId()}, plus some additional formats:
     * <ul>
     * <li>{@code Z} - for UTC
     * <li>{@code +hh:mm}
     * <li>{@code -hh:mm}
     * <li>{@code +hhmm}
     * <li>{@code -hhmm}
     * <li>{@code +hh:mm:ss}
     * <li>{@code -hh:mm:ss}
     * <li>{@code +hhmmss}
     * <li>{@code -hhmmss}
     * </ul>
     * Note that &plusmn; means either the plus or minus symbol.
     * <p>
     * The ID of the returned offset will be normalized to one of the formats
     * described by {@link #getId()}.
     * <p>
     * The maximum supported range is from +18:00 to -18:00 inclusive.
     *
     * @param offsetId  the offset ID, not null
     * @return the zone-offset, not null
     * @throws IllegalArgumentException if the offset ID is invalid
     */
    public static ZoneOffset of(String offsetId) {
        Objects.requireNonNull(offsetId, "Offset ID");
        // "Z" is always in the cache
        ZoneOffset offset = ID_CACHE.get(offsetId);
        if (offset != null) {
            return offset;
        }
        
        // parse - +hh, +hhmm, +hh:mm, +hhmmss, +hh:mm:ss
        final int hours, minutes, seconds;
        int len = offsetId.length();
        switch (len) {
            case 3:
                hours = parseNumber(offsetId, 1, false);
                minutes = 0;
                seconds = 0;
                break;
            case 5:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 3, false);
                seconds = 0;
                break;
            case 6:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 4, true);
                seconds = 0;
                break;
            case 7:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 3, false);
                seconds = parseNumber(offsetId, 5, false);
                break;
            case 9:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 4, true);
                seconds = parseNumber(offsetId, 7, true);
                break;
            default:
                throw new IllegalArgumentException("Zone offset ID '" + offsetId + "' is invalid");
        }
        char first = offsetId.charAt(0);
        if (first != '+' && first != '-') {
            throw new IllegalArgumentException("Zone offset ID '" + offsetId + "' is invalid: Plus/minus not found when expected");
        }
        if (first == '-') {
            return ofHoursMinutesSeconds(-hours, -minutes, -seconds);
        } else {
            return ofHoursMinutesSeconds(hours, minutes, seconds);
        }
    }

    /**
     * Parse a two digit zero-prefixed number.
     *
     * @param offsetId  the offset ID, not null
     * @param pos  the position to parse, valid
     * @param precededByColon  should this number be prefixed by a precededByColon
     * @return the parsed number, from 0 to 99
     */
    private static int parseNumber(CharSequence offsetId, int pos, boolean precededByColon) {
        if (precededByColon && offsetId.charAt(pos - 1) != ':') {
            throw new IllegalArgumentException("Zone offset ID '" + offsetId + "' is invalid: Colon not found when expected");
        }
        char ch1 = offsetId.charAt(pos);
        char ch2 = offsetId.charAt(pos + 1);
        if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
            throw new IllegalArgumentException("Zone offset ID '" + offsetId + "' is invalid: Non numeric characters found");
        }
        return (ch1 - 48) * 10 + (ch2 - 48);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZoneOffset} using an offset in hours.
     *
     * @param hours  the time-zone offset in hours, from -18 to +18
     * @return the zone-offset, not null
     * @throws IllegalArgumentException if the offset is not in the required range
     */
    public static ZoneOffset ofHours(int hours) {
        return ofHoursMinutesSeconds(hours, 0, 0);
    }

    /**
     * Obtains an instance of {@code ZoneOffset} using an offset in
     * hours and minutes.
     * <p>
     * The sign of the hours and minutes components must match.
     * Thus, if the hours is negative, the minutes must be negative or zero.
     * If the hours is zero, the minutes may be positive, negative or zero.
     *
     * @param hours  the time-zone offset in hours, from -18 to +18
     * @param minutes  the time-zone offset in minutes, from 0 to &plusmn;59, sign matches hours
     * @return the zone-offset, not null
     * @throws IllegalArgumentException if the offset is not in the required range
     */
    public static ZoneOffset ofHoursMinutes(int hours, int minutes) {
        return ofHoursMinutesSeconds(hours, minutes, 0);
    }

    /**
     * Obtains an instance of {@code ZoneOffset} using an offset in
     * hours, minutes and seconds.
     * <p>
     * The sign of the hours, minutes and seconds components must match.
     * Thus, if the hours is negative, the minutes and seconds must be negative or zero.
     *
     * @param hours  the time-zone offset in hours, from -18 to +18
     * @param minutes  the time-zone offset in minutes, from 0 to &plusmn;59, sign matches hours and seconds
     * @param seconds  the time-zone offset in seconds, from 0 to &plusmn;59, sign matches hours and minutes
     * @return the zone-offset, not null
     * @throws IllegalArgumentException if the offset is not in the required range
     */
    public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds) {
        validate(hours, minutes, seconds);
        int totalSeconds = totalSeconds(hours, minutes, seconds);
        return ofTotalSeconds(totalSeconds);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZoneOffset} from a date-time object.
     * <p>
     * A {@code DateTimeAccessor} represents some form of date and time information.
     * This factory converts the arbitrary date-time object to an instance of {@code ZoneOffset}.
     * 
     * @param dateTime  the date-time object to convert, not null
     * @return the zone-offset, not null
     * @throws DateTimeException if unable to convert to an {@code ZoneOffset}
     */
    public static ZoneOffset from(DateTimeAccessor dateTime) {
        return ofTotalSeconds(dateTime.get(OFFSET_SECONDS));
    }

    //-----------------------------------------------------------------------
    /**
     * Validates the offset fields.
     *
     * @param hours  the time-zone offset in hours, from -18 to +18
     * @param minutes  the time-zone offset in minutes, from 0 to &plusmn;59
     * @param seconds  the time-zone offset in seconds, from 0 to &plusmn;59
     * @throws IllegalArgumentException if the offset is not in the required range
     */
    private static void validate(int hours, int minutes, int seconds) {
        if (hours < -18 || hours > 18) {
            throw new IllegalArgumentException("Zone offset hours not in valid range: value " + hours +
                    " is not in the range -18 to 18");
        }
        if (hours > 0) {
            if (minutes < 0 || seconds < 0) {
                throw new IllegalArgumentException("Zone offset minutes and seconds must be positive because hours is positive");
            }
        } else if (hours < 0) {
            if (minutes > 0 || seconds > 0) {
                throw new IllegalArgumentException("Zone offset minutes and seconds must be negative because hours is negative");
            }
        } else if ((minutes > 0 && seconds < 0) || (minutes < 0 && seconds > 0)) {
            throw new IllegalArgumentException("Zone offset minutes and seconds must have the same sign");
        }
        if (Math.abs(minutes) > 59) {
            throw new IllegalArgumentException("Zone offset minutes not in valid range: abs(value) " +
                    Math.abs(minutes) + " is not in the range 0 to 59");
        }
        if (Math.abs(seconds) > 59) {
            throw new IllegalArgumentException("Zone offset seconds not in valid range: abs(value) " +
                    Math.abs(seconds) + " is not in the range 0 to 59");
        }
        if (Math.abs(hours) == 18 && (Math.abs(minutes) > 0 || Math.abs(seconds) > 0)) {
            throw new IllegalArgumentException("Zone offset not in valid range: -18:00 to +18:00");
        }
    }

    /**
     * Calculates the total offset in seconds.
     *
     * @param hours  the time-zone offset in hours, from -18 to +18
     * @param minutes  the time-zone offset in minutes, from 0 to &plusmn;59, sign matches hours and seconds
     * @param seconds  the time-zone offset in seconds, from 0 to &plusmn;59, sign matches hours and minutes
     * @return the total in seconds
     */
    private static int totalSeconds(int hours, int minutes, int seconds) {
        return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds;
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZoneOffset} specifying the total offset in seconds
     * <p>
     * The offset must be in the range {@code -18:00} to {@code +18:00}, which corresponds to -64800 to +64800.
     *
     * @param totalSeconds  the total time-zone offset in seconds, from -64800 to +64800
     * @return the ZoneOffset, not null
     * @throws IllegalArgumentException if the offset is not in the required range
     */
    public static ZoneOffset ofTotalSeconds(int totalSeconds) {
        if (Math.abs(totalSeconds) > MAX_SECONDS) {
            throw new IllegalArgumentException("Zone offset not in valid range: -18:00 to +18:00");
        }
        if (totalSeconds % (15 * SECONDS_PER_MINUTE) == 0) {
            Integer totalSecs = totalSeconds;
            ZoneOffset result = SECONDS_CACHE.get(totalSecs);
            if (result == null) {
                result = new ZoneOffset(totalSeconds);
                SECONDS_CACHE.putIfAbsent(totalSecs, result);
                result = SECONDS_CACHE.get(totalSecs);
                ID_CACHE.putIfAbsent(result.getId(), result);
            }
            return result;
        } else {
            return new ZoneOffset(totalSeconds);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param totalSeconds  the total time-zone offset in seconds, from -64800 to +64800
     */
    private ZoneOffset(int totalSeconds) {
        super();
        this.totalSeconds = totalSeconds;
        id = buildId(totalSeconds);
    }

    private static String buildId(int totalSeconds) {
        if (totalSeconds == 0) {
            return "Z";
        } else {
            int absTotalSeconds = Math.abs(totalSeconds);
            StringBuilder buf = new StringBuilder();
            int absHours = absTotalSeconds / SECONDS_PER_HOUR;
            int absMinutes = (absTotalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
            buf.append(totalSeconds < 0 ? "-" : "+")
                .append(absHours < 10 ? "0" : "").append(absHours)
                .append(absMinutes < 10 ? ":0" : ":").append(absMinutes);
            int absSeconds = absTotalSeconds % SECONDS_PER_MINUTE;
            if (absSeconds != 0) {
                buf.append(absSeconds < 10 ? ":0" : ":").append(absSeconds);
            }
            return buf.toString();
        }
    }

    /**
     * Resolves singletons.
     *
     * @return the singleton instance
     */
    private Object readResolve() {
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the total zone offset in seconds.
     * <p>
     * This is the primary way to access the offset amount.
     * It returns the total of the hours, minutes and seconds fields as a
     * single offset that can be added to a time.
     *
     * @return the total zone offset amount in seconds
     */
    public int getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * Gets the normalized zone offset ID.
     * <p>
     * The ID is minor variation to the standard ISO-8601 formatted string
     * for the offset. There are three formats:
     * <ul>
     * <li>{@code Z} - for UTC (ISO-8601)
     * <li>{@code +hh:mm} or {@code -hh:mm} - if the seconds are zero (ISO-8601)
     * <li>{@code +hh:mm:ss} or {@code -hh:mm:ss} - if the seconds are non-zero (not ISO-8601)
     * </ul>
     *
     * @return the zone offset ID, not null
     */
    public String getId() {
        return id;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            return field == OFFSET_SECONDS;
        }
        return field != null && field.doIsSupported(this);
    }

    @Override
    public DateTimeValueRange range(DateTimeField field) {
        if (field == OFFSET_SECONDS) {
            return field.range();
        }
        return super.range(field);
    }

    @Override
    public int get(DateTimeField field) {
        if (field == OFFSET_SECONDS) {
            return totalSeconds;
        }
        return super.get(field);
    }

    @Override
    public long getLong(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            switch ((LocalDateTimeField) field) {
                case OFFSET_SECONDS: return totalSeconds;
            }
            throw new DateTimeException("Unsupported field: " + field.getName());
        }
        return field.doGet(this);
    }

    @Override
    public ZoneOffset with(DateTimeField field, long newValue) {
        if (field instanceof LocalDateTimeField) {
            LocalDateTimeField f = (LocalDateTimeField) field;
            switch (f) {
                case OFFSET_SECONDS: return ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue));
            }
            throw new DateTimeException("Unsupported field: " + field.getName());
        }
        return field.doSet(this, newValue);
    }

    @Override
    public DateTime doWithAdjustment(DateTime dateTime) {
        return dateTime.with(OFFSET_SECONDS, totalSeconds);
    }

    /**
     * Checks if this offset equals the specified offset.
     * <p>
     * This checks to see if the given offset equals this offset.
     * This method exists to implement the {@link ZoneOffsetInfo} contract.
     *
     * @param offset  the offset to check, null returns false
     * @return true if the specified offset equals this offset
     */
    @Override
    public boolean isValidOffset(ZoneOffset offset) {
        return equals(offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this offset to a time-zone.
     * <p>
     * The returned time-zone will use this offset for all instants.
     *
     * @return the time-zone, not null
     */
    public ZoneId toZoneId() {
        return ZoneId.of(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this offset to another offset in descending order.
     * <p>
     * The offsets are compared in the order that they occur for the same time
     * of day around the world. Thus, an offset of {@code +10:00} comes before an
     * offset of {@code +09:00} and so on down to {@code -18:00}.
     *
     * @param other  the other date to compare to, not null
     * @return the comparator value, negative if less, postive if greater
     * @throws NullPointerException if {@code other} is null
     */
    public int compareTo(ZoneOffset other) {
        return other.totalSeconds - totalSeconds;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this offset is equal to another offset.
     * <p>
     * The comparison is based on the amount of the offset in seconds.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other offset
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof ZoneOffset) {
            return totalSeconds == ((ZoneOffset) obj).totalSeconds;
        }
        return false;
    }

    /**
     * A hash code for this offset.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return totalSeconds;
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this offset as a {@code String}, using the normalized ID.
     *
     * @return a string representation of this offset, not null
     */
    @Override
    public String toString() {
        return id;
    }

}
