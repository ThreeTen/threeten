/*
 * Copyright (c) 2007-2011, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.calendar.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.InstantProvider;
import javax.time.LocalDateTime;
import javax.time.ZoneOffset;

/**
 * Implementation of zone rules for fixed offsets.
 * <p>
 * This class allows an offset, normally represented by {@link ZoneOffset}, to be
 * represented as a time-zone.
 * <p>
 * This class is immutable and thread-safe.
 *
 * @author Stephen Colebourne
 */
final class FixedZoneRules extends ZoneRules implements Serializable {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The fixed offset.
     */
    private final ZoneOffset offset;

    /**
     * Creates an instance wrapping the offset.
     *
     * @param offset  the zone offset, not null
     */
    FixedZoneRules(ZoneOffset offset) {
        ZoneRules.checkNotNull(offset, "ZoneOffset must not be null");
        this.offset = offset;
    }

    //-----------------------------------------------------------------------
    /**
     * Uses a serialization delegate.
     *
     * @return the replacing object, not null
     */
    private Object writeReplace() {
        return new Ser(Ser.FZR, this);
    }

    /**
     * Writes the state to the stream.
     *
     * @param out  the output stream, not null
     * @throws IOException if an error occurs
     */
    void writeExternal(DataOutput out) throws IOException {
        Ser.writeOffset(offset, out);
    }

    /**
     * Reads the state from the stream.
     *
     * @param in  the input stream, not null
     * @return the created object, not null
     * @throws IOException if an error occurs
     */
    static FixedZoneRules readExternal(DataInput in) throws IOException {
        ZoneOffset offset = Ser.readOffset(in);
        return new FixedZoneRules(offset);
    }

    //-----------------------------------------------------------------------
    /** {@inheritDoc} */
    @Override
    public ZoneOffset getOffset(InstantProvider instant) {
        return offset;
    }

    /** {@inheritDoc} */
    @Override
    public ZoneOffsetInfo getOffsetInfo(LocalDateTime dateTime) {
        return new ZoneOffsetInfo(dateTime, offset, null);
    }

    /** {@inheritDoc} */
    @Override
    public ZoneOffset getStandardOffset(InstantProvider instant) {
        return offset;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFixedOffset() {
        return true;
    }

    //-----------------------------------------------------------------------
    /** {@inheritDoc} */
    @Override
    public ZoneOffsetTransition nextTransition(InstantProvider instantProvider) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ZoneOffsetTransition previousTransition(InstantProvider instantProvider) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<ZoneOffsetTransition> getTransitions() {
        return new ArrayList<ZoneOffsetTransition>();
    }

    /** {@inheritDoc} */
    @Override
    public List<ZoneOffsetTransitionRule> getTransitionRules() {
        return new ArrayList<ZoneOffsetTransitionRule>();
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this object equals another, comparing the offset.
     * <p>
     * The entire state of the object is compared.
     *
     * @param obj  the object to check, null returns false
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof FixedZoneRules) {
            return offset.equals(((FixedZoneRules) obj).offset);
        }
        return false;
    }

    /**
     * Returns a suitable hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return offset.hashCode() + 1;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string describing this object.
     *
     * @return a string for debugging, not null
     */
    @Override
    public String toString() {
        return offset == ZoneOffset.UTC ? "UTC" : "UTC" + offset.getID();
    }

}
