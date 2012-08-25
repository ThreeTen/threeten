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
package javax.time.calendrical;

import static javax.time.calendrical.LocalDateTimeField.HOUR_OF_DAY;
import static javax.time.calendrical.LocalDateTimeField.MINUTE_OF_HOUR;
import static javax.time.calendrical.LocalDateTimeField.NANO_OF_SECOND;
import static javax.time.calendrical.LocalDateTimeField.SECOND_OF_MINUTE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.CalendricalException;
import javax.time.DateTimes;
import javax.time.ZoneId;
import javax.time.ZoneOffset;

/**
 * Builder that can holds date and time fields and related Calendrical objects.
 * <p>
 * The builder is used to hold onto different elements of date and time.
 * It is designed as two separate maps:
 * <ul>
 * <li>from {@link DateTimeField} to {@code long} value, where the value may be
 * outside the valid range for the field
 * <li>from {@code Class} to {@link DateTime}, holding larger scale objects
 * like {@code LocalDateTime}.
 * </ul>
 * 
 * <h4>Implementation notes</h4>
 * This class is mutable and not thread-safe.
 * It should only be used from a single thread.
 */
public final class DateTimeBuilder implements DateTime, Cloneable {

    /**
     * The map of other fields.
     */
    private Map<DateTimeField, Long> otherFields;
    /**
     * The map of date-time fields.
     */
    private final EnumMap<LocalDateTimeField, Long> standardFields = new EnumMap<LocalDateTimeField, Long>(LocalDateTimeField.class);
    /**
     * The list of calendrical objects by type.
     */
    private final List<Object> objects = new ArrayList<Object>(2);

    //-----------------------------------------------------------------------
    /**
     * Creates an empty instance of the builder.
     */
    public DateTimeBuilder() {
    }

    /**
     * Creates a new instance of the builder with a single field-value.
     * <p>
     * This is equivalent to using {@link #addFieldValue(DateTimeField, long)} on an empty builder.
     */
    public DateTimeBuilder(DateTimeField field, long value) {
        addFieldValue(field, value);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the map of field-value pairs in the builder.
     * 
     * @return a modifiable copy of the field-value map, not null
     */
    public Map<DateTimeField, Long> getFieldValueMap() {
        Map<DateTimeField, Long> map = new HashMap<DateTimeField, Long>(standardFields);
        if (otherFields != null) {
            map.putAll(otherFields);
        }
        return map;
    }

    /**
     * Checks whether the specified field is present in the builder.
     * 
     * @param field  the field to find in the field-value map, not null
     * @return true if the field is present
     */
    public boolean containsFieldValue(DateTimeField field) {
        DateTimes.checkNotNull(field, "Field cannot be null");
        return standardFields.containsKey(field) || (otherFields != null && otherFields.containsKey(field));
    }

    /**
     * Gets the value of the specified field from the builder.
     * 
     * @param field  the field to query in the field-value map, not null
     * @return the value of the field, may be out of range
     * @throws CalendricalException if the field is not present
     */
    public long getFieldValue(DateTimeField field) {
        DateTimes.checkNotNull(field, "Field cannot be null");
        Long value = getFieldValue0(field);
        if (value == null) {
            throw new CalendricalException("Field not found: " + field);
        }
        return value;
    }

    private Long getFieldValue0(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            return standardFields.get(field);
        } else if (otherFields != null) {
            return otherFields.get(field);
        }
        return null;
    }

    /**
     * Gets the value of the specified field from the builder ensuring it is valid.
     * 
     * @param field  the field to query in the field-value map, not null
     * @return the value of the field, may be out of range
     * @throws CalendricalException if the field is not present
     */
    public long getValidFieldValue(DateTimeField field) {
        long value = getFieldValue(field);
        return field.range().checkValidValue(value, field);
    }

    /**
     * Adds a field-value pair to the builder.
     * <p>
     * This adds a field to the builder.
     * If the field is not already present, then the field-value pair is added to the map.
     * If the field is already present and it has the same value as that specified, no action occurs.
     * If the field is already present and it has a different value to that specified, then
     * an exception is thrown.
     * 
     * @param field  the field to add, not null
     * @param value  the value to add, not null
     * @return {@code this}, for method chaining
     * @throws CalendricalException if the field is already present with a different value
     */
    public DateTimeBuilder addFieldValue(DateTimeField field, long value) {
        DateTimes.checkNotNull(field, "Field cannot be null");
        Long old = getFieldValue0(field);  // check first for better error message
        if (old != null && old.longValue() != value) {
            throw new CalendricalException("Conflict found: " + field + " " + old + " differs from " + field + " " + value + ": " + this);
        }
        return putFieldValue0(field, value);
    }

    private DateTimeBuilder putFieldValue0(DateTimeField field, long value) {
        if (field instanceof LocalDateTimeField) {
            standardFields.put((LocalDateTimeField) field, value);
        } else {
            if (otherFields == null) {
                otherFields = new LinkedHashMap<DateTimeField, Long>();
            }
            otherFields.put(field, value);
        }
        return this;
    }

    /**
     * Removes a field-value pair from the builder.
     * <p>
     * This removes a field, which must exist, from the builder.
     * See {@link #removeFieldValues(DateTimeField...)} for a version which does not throw an exception
     * 
     * @param field  the field to remove, not null
     * @return the previous value of the field
     * @throws CalendricalException if the field is not found
     */
    public long removeFieldValue(DateTimeField field) {
        DateTimes.checkNotNull(field, "Field cannot be null");
        Long value = null;
        if (field instanceof LocalDateTimeField) {
            value = standardFields.remove(field);
        } else if (otherFields != null) {
            value = otherFields.remove(field);
        }
        if (value == null) {
            throw new CalendricalException("Field not found: " + field);
        }
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Removes a list of fields from the builder.
     * <p>
     * This removes the specified fields from the builder.
     * No exception is thrown if the fields are not present.
     * 
     * @param fields  the fields to remove, not null
     */
    public void removeFieldValues(DateTimeField... fields) {
        for (DateTimeField field : fields) {
            if (field instanceof LocalDateTimeField) {
                standardFields.remove((LocalDateTimeField)field);
            } else if (otherFields != null) {
                otherFields.remove(field);
            }
        }
    }

    /**
     * Queries a list of fields from the builder.
     * <p>
     * This gets the value of the specified fields from the builder into
     * an array where the positions match the order of the fields.
     * If a field is not present, the array will contain null in that position.
     * 
     * @param fields  the fields to query, not null
     * @return the array of field values, not null
     */
    public Long[] queryFieldValues(DateTimeField... fields) {
        Long[] values = new Long[fields.length];
        int i = 0;
        for (DateTimeField field : fields) {
            values[i++] = getFieldValue0(field);
        }
        return values;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the list of calendrical date-time objects in the builder.
     * <p>
     * This map is intended for use with {@link ZoneOffset} and {@link ZoneId}.
     * The returned map is live and may be edited.
     * 
     * @return the editable list of calendrical date-time objects, not null
     */
    public List<Object> getCalendricalList() {
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <R> R getCalendrical(Class<R> type) {
        for (Object object : objects) {
            if (type.isInstance(object)) {
                return (R) object;
            }
        }
        return null;
    }

    /**
     * Adds a calendrical to the builder.
     * <p>
     * This adds a calendrical to the builder.
     * If the calendrical is a {@code DateTimeBuilder}, each field is added using {@link #addFieldValue}.
     * If the calendrical is not already present, then the calendrical is added to the map.
     * If the calendrical is already present and it is equal to that specified, no action occurs.
     * If the calendrical is already present and it is not equal to that specified, then an exception is thrown.
     * 
     * @param calendrical  the calendrical to add, not null
     * @return {@code this}, for method chaining
     * @throws CalendricalException if the field is already present with a different value
     */
    public DateTimeBuilder addCalendrical(Object calendrical) {
        objects.add(calendrical);
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the builder, evaluating the date and time.
     * <p>
     * This examines the contents of the builder and resolves it to produce the best
     * available date and time, throwing an exception if a problem occurs.
     * Calling this method changes the state of the builder.
     * 
     * @return {@code this}, for method chaining
     */
    public DateTimeBuilder resolve() {
        // handle unusual fields
        if (otherFields != null) {
            outer:
            while (true) {
                for (Entry<DateTimeField, Long> entry : otherFields.entrySet()) {
                    if (entry.getKey().resolve(this, entry.getValue())) {
                        continue outer;
                    }
                }
                break;
            }
        }
        // default standard time fields
        if (!standardFields.containsKey(HOUR_OF_DAY)) {
            addFieldValue(HOUR_OF_DAY, 0L);
        }
        if (!standardFields.containsKey(MINUTE_OF_HOUR)) {
            addFieldValue(MINUTE_OF_HOUR, 0L);
        }
        if (!standardFields.containsKey(SECOND_OF_MINUTE)) {
            addFieldValue(SECOND_OF_MINUTE, 0L);
        }
        if (!standardFields.containsKey(NANO_OF_SECOND)) {
            addFieldValue(NANO_OF_SECOND, 0L);
        }
        return this;
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <R> R extract(Class<R> type) {
        R result = null;
        for (Object obj : objects) {
            if (type.isInstance(obj)) {
                if (result != null && result.equals(obj) == false) {
                    throw new CalendricalException("Conflict found: " + type.getSimpleName() + " differs " + result + " vs " + obj + ": " + this);
                }
                result = (R) obj;
            }
        }
        return result;
    }

    /**
     * Invoked the class's {@code from(datetime)} method with a datetime
     * and returns the value.
     * @param <T>  The parameter type to return
     * @param type The type to invoke {@code from} on.
     * @param datetime the datetime to pass as the argument
     * @return the value returned from the {@code from} method, or {@code null} if any exception occurred
     */
    public static <T> T from(Class<T> type, DateTime datetime) {
       try {
            Method m = type.getDeclaredMethod("from", DateTime.class);
            return (T)type.cast(m.invoke(null, datetime));
        } catch (NoSuchMethodException nsm) {
            return null;
        } catch (ReflectiveOperationException ex) {
            if (false && ex.getCause() instanceof CalendricalException) {
                return null;
            }
            throw (CalendricalException)ex.getCause();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Clones this builder, creating a new independent copy referring to the
     * same map of fields and calendricals.
     * 
     * @return the cloned builder, not null
     */
    @Override
    public DateTimeBuilder clone() {
        DateTimeBuilder dtb = new DateTimeBuilder();
        dtb.objects.addAll(this.objects);
        dtb.standardFields.putAll(this.standardFields);
        dtb.standardFields.putAll(this.standardFields);
        if (this.otherFields != null) {
            dtb.otherFields.putAll(this.otherFields);
        }
        return dtb;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("DateTimeBuilder[");
        Map<DateTimeField, Long> fields = getFieldValueMap();
        if (fields.size() > 0) {
            buf.append("fields=").append(fields);
        }
        if (objects.size() > 0) {
            if (fields.size() > 0) {
                buf.append(", ");
            }
            buf.append("objects=").append(objects);
        }
        buf.append(']');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    @Override
    public long get(DateTimeField field) {
        return getFieldValue(field);
    }

    @Override
    public DateTime with(DateTimeField field, long newValue) {
        putFieldValue0(field, newValue);
        return this;
    }

}
