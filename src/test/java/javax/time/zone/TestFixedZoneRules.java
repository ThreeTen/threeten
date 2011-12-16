/*
 * Copyright (c) 2010, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.zone;

import javax.time.zone.FixedZoneRules;
import javax.time.zone.ZoneOffsetInfo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.time.Instant;
import javax.time.LocalDateTime;
import javax.time.OffsetDateTime;
import javax.time.Period;
import javax.time.ZoneOffset;

import org.testng.annotations.Test;

/**
 * Test FixedZoneRules.
 *
 * @author Stephen Colebourne
 */
@Test
public class TestFixedZoneRules {

    private static final ZoneOffset OFFSET_PONE = ZoneOffset.ofHours(1);
    private static final ZoneOffset OFFSET_PTWO = ZoneOffset.ofHours(2);
    private static final LocalDateTime LDT = LocalDateTime.of(2010, 12, 3, 11, 30);
    private static final OffsetDateTime ODT = OffsetDateTime.of(2010, 12, 3, 11, 30, OFFSET_PONE);
    private static final Instant INSTANT = ODT.toInstant();

    //-----------------------------------------------------------------------
    // Basics
    //-----------------------------------------------------------------------
    public void test_interfaces() {
        assertTrue(Serializable.class.isAssignableFrom(FixedZoneRules.class));
    }

    public void test_immutable() {
        Class<FixedZoneRules> cls = FixedZoneRules.class;
        assertFalse(Modifier.isPublic(cls.getModifiers()));
        assertFalse(Modifier.isProtected(cls.getModifiers()));
        assertFalse(Modifier.isPrivate(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()) ||
                        (Modifier.isVolatile(field.getModifiers()) && Modifier.isTransient(field.getModifiers())), "" + field);
            }
        }
    }

    public void test_serialization() throws Exception {
        FixedZoneRules test = new FixedZoneRules(OFFSET_PONE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        FixedZoneRules result = (FixedZoneRules) in.readObject();
        
        assertEquals(result, test);
    }

    //-----------------------------------------------------------------------
    // basics
    //-----------------------------------------------------------------------
    public void test_data() {
        FixedZoneRules test = new FixedZoneRules(OFFSET_PONE);
        assertEquals(test.getDaylightSavings(INSTANT), Period.ZERO);
        assertEquals(test.getOffset(INSTANT), OFFSET_PONE);
        assertEquals(test.getOffsetInfo(LDT), new ZoneOffsetInfo(LDT, OFFSET_PONE, null));
        assertEquals(test.getOffsetInfo(INSTANT), new ZoneOffsetInfo(LDT, OFFSET_PONE, null));
        assertEquals(test.getStandardOffset(INSTANT), OFFSET_PONE);
        assertEquals(test.getTransitions().size(), 0);
        assertEquals(test.getTransitionRules().size(), 0);
        assertEquals(test.nextTransition(INSTANT), null);
        assertEquals(test.previousTransition(INSTANT), null);
    }

    public void test_isValidDateTime_same_offset() {
        FixedZoneRules test = new FixedZoneRules(OFFSET_PONE);
        assertEquals(test.isValidDateTime(ODT), true);
    }

    public void test_isValidDateTime_diff_offset() {
        FixedZoneRules test = new FixedZoneRules(OFFSET_PTWO);
        assertEquals(test.isValidDateTime(ODT), false);
    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals() {
        FixedZoneRules a = new FixedZoneRules(OFFSET_PONE);
        FixedZoneRules b = new FixedZoneRules(OFFSET_PTWO);
        
        assertEquals(a.equals(a), true);
        assertEquals(a.equals(b), false);
        assertEquals(b.equals(a), false);
        assertEquals(b.equals(b), true);
        
        assertEquals(a.equals("Rubbish"), false);
        assertEquals(a.equals(null), false);
        
        assertEquals(a.hashCode() == a.hashCode(), true);
        assertEquals(b.hashCode() == b.hashCode(), true);
    }

}
