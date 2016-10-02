/*
 * Copyright (c) 2008-2010, Stephen Colebourne & Michael Nascimento Santos
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

import static javax.time.calendrical.LocalPeriodUnit.DAYS;
import static javax.time.calendrical.LocalPeriodUnit.ERAS;
import static javax.time.calendrical.LocalPeriodUnit.FOREVER;
import static javax.time.calendrical.LocalPeriodUnit.HOURS;
import static javax.time.calendrical.LocalPeriodUnit.MINUTES;
import static javax.time.calendrical.LocalPeriodUnit.MONTHS;
import static javax.time.calendrical.LocalPeriodUnit.NANOS;
import static javax.time.calendrical.LocalPeriodUnit.QUARTER_YEARS;
import static javax.time.calendrical.LocalPeriodUnit.SECONDS;
import static javax.time.calendrical.LocalPeriodUnit.WEEK_BASED_YEARS;
import static javax.time.calendrical.LocalPeriodUnit.YEARS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.time.calendrical.LocalPeriodUnit;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ISOPeriod.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 * @author Richard Warburton
 */
@Test
public class TestISOPeriod {

    private static final BigInteger MAX_BINT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger BINT_24 = BigInteger.valueOf(24);
    private static final BigInteger BINT_60 = BigInteger.valueOf(60);
    private static final BigInteger BINT_1BN = BigInteger.valueOf(1000000000L);

    //-----------------------------------------------------------------------
    // basics
    //-----------------------------------------------------------------------

    @DataProvider(name="serialization")
    Object[][] data_serialization() {
        return new Object[][] {
            {ISOPeriod.ZERO},
            {ISOPeriod.ofDays(0)},
            {ISOPeriod.ofDays(1)},
            {ISOPeriod.of(1, 2, 3, 4, 5, 6)},
        };
    }

    @Test(dataProvider="serialization")
    public void test_serialization(ISOPeriod ISOPeriod) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ISOPeriod);
        oos.close();
        
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                baos.toByteArray()));
        if (ISOPeriod.isZero()) {
            assertSame(ois.readObject(), ISOPeriod);
        } else {
            assertEquals(ois.readObject(), ISOPeriod);
        }
    }

    public void test_immutable() {
        Class<ISOPeriod> cls = ISOPeriod.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) == false && !Modifier.isTransient(field.getModifiers())) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()));
            }
        }
        Constructor<?>[] cons = cls.getDeclaredConstructors();
        for (Constructor<?> con : cons) {
            assertTrue(Modifier.isPrivate(con.getModifiers()));
        }
    }

    //-----------------------------------------------------------------------
    // factories
    //-----------------------------------------------------------------------
    public void factory_zeroSingleton() {
        assertSame(ISOPeriod.ZERO, ISOPeriod.ZERO);
        assertSame(ISOPeriod.of(0, 0, 0, 0, 0, 0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.of(0, 0, 0, 0, 0, 0, 0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofDateFields(0, 0, 0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofTimeFields(0, 0, 0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofTimeFields(0, 0, 0, 0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofYears(0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofMonths(0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofDays(0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofHours(0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofMinutes(0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofSeconds(0), ISOPeriod.ZERO);
        assertSame(ISOPeriod.ofNanos(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // of(ISOPeriodProvider)
    //-----------------------------------------------------------------------
    public void factory_of_ints() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6), 1, 2, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(0, 2, 3, 4, 5, 6), 0, 2, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 0, 0, 0, 0, 0), 1, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.of(0, 0, 0, 0, 0, 0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.of(-1, -2, -3, -4, -5, -6), -1, -2, -3, -4, -5, -6, 0);
    }

    //-----------------------------------------------------------------------
    // ofDateFields
    //-----------------------------------------------------------------------
    public void factory_ofDateFields_ints() {
        assertPeriod(ISOPeriod.ofDateFields(1, 2, 3), 1, 2, 3, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDateFields(0, 2, 3), 0, 2, 3, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDateFields(1, 0, 0), 1, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDateFields(0, 0, 0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDateFields(-1, -2, -3), -1, -2, -3, 0, 0, 0, 0);
    }

    //-----------------------------------------------------------------------
    // ofTimeFields
    //-----------------------------------------------------------------------
    public void factory_ofTimeFields_3ints() {
        assertPeriod(ISOPeriod.ofTimeFields(1, 2, 3), 0, 0, 0, 1, 2, 3, 0);
        assertPeriod(ISOPeriod.ofTimeFields(0, 2, 3), 0, 0, 0, 0, 2, 3, 0);
        assertPeriod(ISOPeriod.ofTimeFields(1, 0, 0), 0, 0, 0, 1, 0, 0, 0);
        assertPeriod(ISOPeriod.ofTimeFields(0, 0, 0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofTimeFields(-1, -2, -3), 0, 0, 0, -1, -2, -3, 0);
    }

    public void factory_ofTimeFields_4ints() {
        assertPeriod(ISOPeriod.ofTimeFields(1, 2, 3, 4), 0, 0, 0, 1, 2, 3, 4);
        assertPeriod(ISOPeriod.ofTimeFields(0, 2, 3, 4), 0, 0, 0, 0, 2, 3, 4);
        assertPeriod(ISOPeriod.ofTimeFields(1, 0, 0, 0), 0, 0, 0, 1, 0, 0, 0);
        assertPeriod(ISOPeriod.ofTimeFields(0, 0, 0, 0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofTimeFields(-1, -2, -3, -4), 0, 0, 0, -1, -2, -3, -4);
    }

    //-----------------------------------------------------------------------
    // of one field
    //-----------------------------------------------------------------------
    public void test_factory_of_intPeriodUnit() {
        assertEquals(ISOPeriod.of(1, YEARS), ISOPeriod.ofYears(1));
        assertEquals(ISOPeriod.of(2, MONTHS), ISOPeriod.ofMonths(2));
        assertEquals(ISOPeriod.of(3, DAYS), ISOPeriod.ofDays(3));
        assertEquals(ISOPeriod.of(Integer.MAX_VALUE, HOURS), ISOPeriod.ofHours(Integer.MAX_VALUE));
        assertEquals(ISOPeriod.of(-1, MINUTES), ISOPeriod.ofMinutes(-1));
        assertEquals(ISOPeriod.of(-2, SECONDS), ISOPeriod.ofSeconds(-2));
        assertEquals(ISOPeriod.of(Integer.MIN_VALUE, NANOS), ISOPeriod.ofNanos(Integer.MIN_VALUE));
    }

    public void test_factory_of_intPeriodUnit_convert() {
        assertEquals(ISOPeriod.of(2, QUARTER_YEARS), ISOPeriod.ofMonths(6));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_of_intPeriodUnit_forever() {
        ISOPeriod.of(1, FOREVER);
    }
    
    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_of_intPeriodUnit_eras() {
        ISOPeriod.of(1, ERAS);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_of_intPeriodUnit_week_based_years() {
        ISOPeriod.of(1, WEEK_BASED_YEARS);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_of_intPeriodUnit_null() {
        ISOPeriod.of(1, null);
    }

    //-----------------------------------------------------------------------
    public void factory_years() {
        assertPeriod(ISOPeriod.ofYears(1), 1, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofYears(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofYears(-1), -1, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofYears(Integer.MAX_VALUE), Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofYears(Integer.MIN_VALUE), Integer.MIN_VALUE, 0, 0, 0, 0, 0, 0);
    }

    public void factory_months() {
        assertPeriod(ISOPeriod.ofMonths(1), 0, 1, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofMonths(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofMonths(-1), 0, -1, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofMonths(Integer.MAX_VALUE), 0, Integer.MAX_VALUE, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofMonths(Integer.MIN_VALUE), 0, Integer.MIN_VALUE, 0, 0, 0, 0, 0);
    }

    public void factory_days() {
        assertPeriod(ISOPeriod.ofDays(1), 0, 0, 1, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDays(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDays(-1), 0, 0, -1, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDays(Integer.MAX_VALUE), 0, 0, Integer.MAX_VALUE, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofDays(Integer.MIN_VALUE), 0, 0, Integer.MIN_VALUE, 0, 0, 0, 0);
    }

    public void factory_hours() {
        assertPeriod(ISOPeriod.ofHours(1), 0, 0, 0, 1, 0, 0, 0);
        assertPeriod(ISOPeriod.ofHours(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofHours(-1), 0, 0, 0, -1, 0, 0, 0);
        assertPeriod(ISOPeriod.ofHours(Integer.MAX_VALUE), 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0);
        assertPeriod(ISOPeriod.ofHours(Integer.MIN_VALUE), 0, 0, 0, Integer.MIN_VALUE, 0, 0, 0);
    }

    public void factory_minutes() {
        assertPeriod(ISOPeriod.ofMinutes(1), 0, 0, 0, 0, 1, 0, 0);
        assertPeriod(ISOPeriod.ofMinutes(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofMinutes(-1), 0, 0, 0, 0, -1, 0, 0);
        assertPeriod(ISOPeriod.ofMinutes(Integer.MAX_VALUE), 0, 0, 0, 0, Integer.MAX_VALUE, 0, 0);
        assertPeriod(ISOPeriod.ofMinutes(Integer.MIN_VALUE), 0, 0, 0, 0, Integer.MIN_VALUE, 0, 0);
    }

    public void factory_seconds() {
        assertPeriod(ISOPeriod.ofSeconds(1), 0, 0, 0, 0, 0, 1, 0);
        assertPeriod(ISOPeriod.ofSeconds(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofSeconds(-1), 0, 0, 0, 0, 0, -1, 0);
        assertPeriod(ISOPeriod.ofSeconds(Integer.MAX_VALUE), 0, 0, 0, 0, 0, Integer.MAX_VALUE, 0);
        assertPeriod(ISOPeriod.ofSeconds(Integer.MIN_VALUE), 0, 0, 0, 0, 0, Integer.MIN_VALUE, 0);
    }

    public void factory_nanos() {
        assertPeriod(ISOPeriod.ofNanos(1), 0, 0, 0, 0, 0, 0, 1);
        assertPeriod(ISOPeriod.ofNanos(0), 0, 0, 0, 0, 0, 0, 0);
        assertPeriod(ISOPeriod.ofNanos(-1), 0, 0, 0, 0, 0, 0, -1);
        assertPeriod(ISOPeriod.ofNanos(Long.MAX_VALUE), 0, 0, 0, 0, 0, 0, Long.MAX_VALUE);
        assertPeriod(ISOPeriod.ofNanos(Long.MIN_VALUE), 0, 0, 0, 0, 0, 0, Long.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // of(Duration)
    //-----------------------------------------------------------------------
    public void factory_duration() {
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(2, 3)), 0, 0, 0, 0, 0, 2, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(59, 3)), 0, 0, 0, 0, 0, 59, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(60, 3)), 0, 0, 0, 0, 1, 0, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(61, 3)), 0, 0, 0, 0, 1, 1, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(3599, 3)), 0, 0, 0, 0, 59, 59, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(3600, 3)), 0, 0, 0, 1, 0, 0, 3);
    }

    public void factory_duration_negative() {
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(-2, 3)), 0, 0, 0, 0, 0, -2, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(-59, 3)), 0, 0, 0, 0, 0, -59, 3);
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(-60, 3)), 0, 0, 0, 0, -1, 0, 3);
        
        assertPeriod(ISOPeriod.of(Duration.ofSeconds(2, -3)), 0, 0, 0, 0, 0, 1, 999999997);
    }

    public void factory_duration_big() {
        Duration dur = Duration.ofSeconds(2, Long.MAX_VALUE);
        long secs = Long.MAX_VALUE / 1000000000 + 2;
        long nanos = Long.MAX_VALUE % 1000000000;
        assertPeriod(ISOPeriod.of(dur), 0, 0, 0, (int) (secs / 3600), (int) ((secs % 3600) / 60), (int) (secs % 60), nanos);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_duration_null() {
        ISOPeriod.of((Duration) null);
    }

    //-----------------------------------------------------------------------
    // between
    //-----------------------------------------------------------------------
    @DataProvider(name="Between")
    Object[][] data_between() {
        return new Object[][] {
            {2010, 1, 1, 2010, 1, 1, 0, 0, 0},
            {2010, 1, 1, 2010, 1, 2, 0, 0, 1},
            {2010, 1, 1, 2010, 1, 31, 0, 0, 30},
            {2010, 1, 1, 2010, 2, 1, 0, 1, 0},
            {2010, 1, 1, 2010, 2, 28, 0, 1, 27},
            {2010, 1, 1, 2010, 3, 1, 0, 2, 0},
            {2010, 1, 1, 2010, 12, 31, 0, 11, 30},
            {2010, 1, 1, 2011, 1, 1, 1, 0, 0},
            {2010, 1, 1, 2011, 12, 31, 1, 11, 30},
            {2010, 1, 1, 2012, 1, 1, 2, 0, 0},
            
            {2010, 1, 10, 2010, 1, 1, 0, 0, -9},
            {2010, 1, 10, 2010, 1, 2, 0, 0, -8},
            {2010, 1, 10, 2010, 1, 9, 0, 0, -1},
            {2010, 1, 10, 2010, 1, 10, 0, 0, 0},
            {2010, 1, 10, 2010, 1, 11, 0, 0, 1},
            {2010, 1, 10, 2010, 1, 31, 0, 0, 21},
            {2010, 1, 10, 2010, 2, 1, 0, 0, 22},
            {2010, 1, 10, 2010, 2, 9, 0, 0, 30},
            {2010, 1, 10, 2010, 2, 10, 0, 1, 0},
            {2010, 1, 10, 2010, 2, 28, 0, 1, 18},
            {2010, 1, 10, 2010, 3, 1, 0, 1, 19},
            {2010, 1, 10, 2010, 3, 9, 0, 1, 27},
            {2010, 1, 10, 2010, 3, 10, 0, 2, 0},
            {2010, 1, 10, 2010, 12, 31, 0, 11, 21},
            {2010, 1, 10, 2011, 1, 1, 0, 11, 22},
            {2010, 1, 10, 2011, 1, 9, 0, 11, 30},
            {2010, 1, 10, 2011, 1, 10, 1, 0, 0},
            
            {2010, 3, 30, 2011, 5, 1, 1, 1, 1},
            {2010, 4, 30, 2011, 5, 1, 1, 0, 1},
            
            {2010, 2, 28, 2012, 2, 27, 1, 11, 30},
            {2010, 2, 28, 2012, 2, 28, 2, 0, 0},
            {2010, 2, 28, 2012, 2, 29, 2, 0, 1},
            
            {2012, 2, 28, 2014, 2, 27, 1, 11, 30},
            {2012, 2, 28, 2014, 2, 28, 2, 0, 0},
            {2012, 2, 28, 2014, 3, 1, 2, 0, 1},
            
            {2012, 2, 29, 2014, 2, 28, 1, 11, 30},
            {2012, 2, 29, 2014, 3, 1, 2, 0, 1},
            {2012, 2, 29, 2014, 3, 2, 2, 0, 2},
            
            {2012, 2, 29, 2016, 2, 28, 3, 11, 30},
            {2012, 2, 29, 2016, 2, 29, 4, 0, 0},
            {2012, 2, 29, 2016, 3, 1, 4, 0, 1},
            
            {2010, 1, 1, 2009, 12, 31, 0, 0, -1},
            {2010, 1, 1, 2009, 12, 30, 0, 0, -2},
            {2010, 1, 1, 2009, 12, 2, 0, 0, -30},
            {2010, 1, 1, 2009, 12, 1, 0, -1, 0},
            {2010, 1, 1, 2009, 11, 30, 0, -1, -1},
            {2010, 1, 1, 2009, 11, 2, 0, -1, -29},
            {2010, 1, 1, 2009, 11, 1, 0, -2, 0},
            {2010, 1, 1, 2009, 1, 2, 0, -11, -30},
            {2010, 1, 1, 2009, 1, 1, -1, 0, 0},
            
            {2010, 1, 15, 2010, 1, 15, 0, 0, 0},
            {2010, 1, 15, 2010, 1, 14, 0, 0, -1},
            {2010, 1, 15, 2010, 1, 1, 0, 0, -14},
            {2010, 1, 15, 2009, 12, 31, 0, 0, -15},
            {2010, 1, 15, 2009, 12, 16, 0, 0, -30},
            {2010, 1, 15, 2009, 12, 15, 0, -1, 0},
            {2010, 1, 15, 2009, 12, 14, 0, -1, -1},
            
            {2010, 2, 28, 2009, 3, 1, 0, -11, -27},
            {2010, 2, 28, 2009, 2, 28, -1, 0, 0},
            {2010, 2, 28, 2009, 2, 27, -1, 0, -1},
            
            {2010, 2, 28, 2008, 2, 29, -1, -11, -28},
            {2010, 2, 28, 2008, 2, 28, -2, 0, 0},
            {2010, 2, 28, 2008, 2, 27, -2, 0, -1},
            
            {2012, 2, 29, 2009, 3, 1, -2, -11, -28},
            {2012, 2, 29, 2009, 2, 28, -3, 0, -1},
            {2012, 2, 29, 2009, 2, 27, -3, 0, -2},
            
            {2012, 2, 29, 2008, 3, 1, -3, -11, -28},
            {2012, 2, 29, 2008, 2, 29, -4, 0, 0},
            {2012, 2, 29, 2008, 2, 28, -4, 0, -1},
        };
    }

    @Test(dataProvider="Between")
    public void factory_between(int y1, int m1, int d1, int y2, int m2, int d2, int ye, int me, int de) {
        LocalDate start = LocalDate.of(y1, m1, d1);
        LocalDate end = LocalDate.of(y2, m2, d2);
        ISOPeriod test = ISOPeriod.between(start, end);
        assertPeriod(test, ye, me, de, 0, 0, 0, 0);
        // TODO:
//        assertEquals(start.plus(test), end);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_between_nullFirst() {
        ISOPeriod.between(null, LocalDate.of(2010, 1, 1));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_between_nullSecond() {
        ISOPeriod.between(LocalDate.of(2010, 1, 1), null);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="YearsBetween")
    Object[][] data_yearsBetween() {
        return new Object[][] {
            {2010, 1, 1, 2010, 1, 1, 0},
            {2010, 1, 1, 2010, 1, 2, 0},
            {2010, 1, 1, 2010, 12, 31, 0},
            {2010, 1, 1, 2011, 1, 1, 1},
            {2010, 1, 1, 2011, 12, 31, 1},
            {2010, 1, 1, 2012, 1, 1, 2},
            
            {2010, 2, 28, 2012, 2, 27, 1},
            {2010, 2, 28, 2012, 2, 28, 2},
            {2010, 2, 28, 2012, 2, 29, 2},
            
            {2012, 2, 29, 2014, 2, 28, 1},
            {2012, 2, 29, 2014, 3, 1, 2},
            
            {2012, 2, 29, 2016, 2, 28, 3},
            {2012, 2, 29, 2016, 2, 29, 4},
            {2012, 2, 29, 2016, 3, 1, 4},
            
            {2010, 1, 1, 2009, 12, 31, 0},
            {2010, 1, 1, 2009, 2, 1, 0},
            {2010, 1, 1, 2009, 1, 2, 0},
            {2010, 1, 1, 2009, 1, 1, -1},
            
            {2010, 1, 1, 2008, 12, 31, -1},
            {2010, 1, 1, 2008, 2, 1, -1},
            {2010, 1, 1, 2008, 1, 2, -1},
            {2010, 1, 1, 2008, 1, 1, -2},
            
            {2010, 2, 28, 2009, 3, 1, 0},
            {2010, 2, 28, 2009, 2, 28, -1},
            {2010, 2, 28, 2009, 2, 27, -1},
            
            {2010, 2, 28, 2008, 2, 29, -1},
            {2010, 2, 28, 2008, 2, 28, -2},
            {2010, 2, 28, 2008, 2, 27, -2},
            
            {2012, 2, 29, 2009, 3, 1, -2},
            {2012, 2, 29, 2009, 2, 28, -3},
            {2012, 2, 29, 2009, 2, 27, -3},
            
            {2012, 2, 29, 2008, 3, 1, -3},
            {2012, 2, 29, 2008, 2, 29, -4},
            {2012, 2, 29, 2008, 2, 28, -4},
        };
    }

    @Test(dataProvider="YearsBetween")
    public void factory_yearsBetween(int y1, int m1, int d1, int y2, int m2, int d2, int expected) {
        ISOPeriod test = ISOPeriod.yearsBetween(LocalDate.of(y1, m1, d1), LocalDate.of(y2, m2, d2));
        assertPeriod(test, expected, 0, 0, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_yearsBetween_nullFirst() {
        ISOPeriod.yearsBetween(null, LocalDate.of(2010, 1, 1));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_yearsBetween_nullSecond() {
        ISOPeriod.yearsBetween(LocalDate.of(2010, 1, 1), null);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="MonthsBetween")
    Object[][] data_monthsBetween() {
        return new Object[][] {
            {2010, 1, 1, 2010, 1, 1, 0},
            {2010, 1, 1, 2010, 1, 2, 0},
            {2010, 1, 1, 2010, 1, 31, 0},
            {2010, 1, 1, 2010, 2, 1, 1},
            {2010, 1, 1, 2010, 2, 28, 1},
            {2010, 1, 1, 2010, 3, 1, 2},
            {2010, 1, 1, 2010, 12, 31, 11},
            {2010, 1, 1, 2011, 1, 1, 12},
            
            {2010, 2, 28, 2012, 2, 27, 23},
            {2010, 2, 28, 2012, 2, 28, 24},
            {2010, 2, 28, 2012, 2, 29, 24},
            
            {2012, 2, 29, 2014, 2, 28, 23},
            {2012, 2, 29, 2014, 3, 1, 24},
            
            {2012, 2, 29, 2016, 2, 28, 47},
            {2012, 2, 29, 2016, 2, 29, 48},
            {2012, 2, 29, 2016, 3, 1, 48},
            
            {2010, 1, 1, 2009, 12, 31, 0},
            {2010, 1, 1, 2009, 12, 2, 0},
            {2010, 1, 1, 2009, 12, 1, -1},
            {2010, 1, 1, 2009, 11, 30, -1},
            
            {2010, 1, 1, 2009, 1, 2, -11},
            {2010, 1, 1, 2009, 1, 1, -12},
            {2010, 1, 1, 2008, 12, 31, -12},
            {2010, 1, 1, 2008, 12, 2, -12},
            {2010, 1, 1, 2008, 12, 1, -13},
            {2010, 1, 1, 2008, 1, 2, -23},
            {2010, 1, 1, 2008, 1, 1, -24},
            
            {2010, 2, 28, 2009, 3, 1, -11},
            {2010, 2, 28, 2009, 2, 28, -12},
            {2010, 2, 28, 2009, 2, 27, -12},
            
            {2010, 2, 28, 2008, 2, 29, -23},
            {2010, 2, 28, 2008, 2, 28, -24},
            {2010, 2, 28, 2008, 2, 27, -24},
            
            {2012, 2, 29, 2009, 3, 1, -35},
            {2012, 2, 29, 2009, 2, 28, -36},
            {2012, 2, 29, 2009, 2, 27, -36},
            
            {2012, 2, 29, 2008, 3, 1, -47},
            {2012, 2, 29, 2008, 2, 29, -48},
            {2012, 2, 29, 2008, 2, 28, -48},
        };
    }

    @Test(dataProvider="MonthsBetween")
    public void factory_monthsBetween(int y1, int m1, int d1, int y2, int m2, int d2, int expected) {
        ISOPeriod test = ISOPeriod.monthsBetween(LocalDate.of(y1, m1, d1), LocalDate.of(y2, m2, d2));
        assertPeriod(test, 0, expected, 0, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_monthsBetween_nullFirst() {
        ISOPeriod.monthsBetween(null, LocalDate.of(2010, 1, 1));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_monthsBetween_nullSecond() {
        ISOPeriod.monthsBetween(LocalDate.of(2010, 1, 1), null);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="DaysBetween")
    Object[][] data_daysBetween() {
        return new Object[][] {
            {2010, 1, 1, 2010, 1, 1, 0},
            {2010, 1, 1, 2010, 1, 2, 1},
            {2010, 1, 1, 2010, 1, 10, 9},
            {2010, 1, 1, 2010, 1, 31, 30},
            {2010, 1, 1, 2010, 2, 1, 31},
            {2010, 1, 1, 2010, 2, 28, 58},
            {2010, 1, 1, 2010, 3, 1, 59},
            {2010, 1, 1, 2010, 12, 31, 364},
            {2010, 1, 1, 2011, 1, 1, 365},
            {2010, 1, 1, 2011, 2, 28, 365 + 58},
            {2010, 1, 1, 2011, 3, 1, 365 + 59},
            {2010, 1, 1, 2012, 1, 1, 365 + 365},
            {2010, 1, 1, 2012, 2, 28, 365 + 365 + 58},
            {2010, 1, 1, 2012, 2, 29, 365 + 365 + 59},
            {2010, 1, 1, 2012, 3, 1, 365 + 365 + 60},
            
            {2010, 1, 1, 2009, 12, 31, -1},
        };
    }

    @Test(dataProvider="DaysBetween")
    public void factory_daysBetween(int y1, int m1, int d1, int y2, int m2, int d2, int expected) {
        ISOPeriod test = ISOPeriod.daysBetween(LocalDate.of(y1, m1, d1), LocalDate.of(y2, m2, d2));
        assertPeriod(test, 0, 0, expected, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_daysBetween_nullFirst() {
        ISOPeriod.daysBetween(null, LocalDate.of(2010, 1, 1));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_daysBetween_nullSecond() {
        ISOPeriod.daysBetween(LocalDate.of(2010, 1, 1), null);
    }

    //-----------------------------------------------------------------------
    // parse()
    //-----------------------------------------------------------------------
    // TODO:
//    @Test(dataProvider="toStringAndParse")
//    public void test_parse(ISOPeriod test, String expected) {
//        if (Math.signum(test.getSeconds()) == Math.signum(test.getNanos())) {
//            assertEquals(test, ISOPeriod.parse(expected));
//        }
//    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_nullText() {
        ISOPeriod.parse((String) null);
    }

    //-----------------------------------------------------------------------
    // isZero()
    //-----------------------------------------------------------------------
    public void test_isZero() {
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).isZero(), false);
        assertEquals(ISOPeriod.of(1, 2, 3, 0, 0, 0, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 5, 6, 7).isZero(), false);
        assertEquals(ISOPeriod.of(1, 0, 0, 0, 0, 0, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 2, 0, 0, 0, 0, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 0, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 0, 0, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 5, 0, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 6, 0).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, 7).isZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0).isZero(), true);
    }

    //-----------------------------------------------------------------------
    // isPositive()
    //-----------------------------------------------------------------------
    public void test_isPositive() {
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).isPositive(), true);
        assertEquals(ISOPeriod.of(1, 2, 3, 0, 0, 0, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 5, 6, 7).isPositive(), true);
        assertEquals(ISOPeriod.of(1, 0, 0, 0, 0, 0, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 2, 0, 0, 0, 0, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 0, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 0, 0, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 5, 0, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 6, 0).isPositive(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, 7).isPositive(), true);
        assertEquals(ISOPeriod.of(-1, -2, -3, -4, -5, -6, -7).isPositive(), false);
        assertEquals(ISOPeriod.of(-1, -2, 3, 4, -5, -6, -7).isPositive(), false);
        assertEquals(ISOPeriod.of(-1, 0, 0, 0, 0, 0, 0).isPositive(), false);
        assertEquals(ISOPeriod.of(0, -2, 0, 0, 0, 0, 0).isPositive(), false);
        assertEquals(ISOPeriod.of(0, 0, -3, 0, 0, 0, 0).isPositive(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, -4, 0, 0, 0).isPositive(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, -5, 0, 0).isPositive(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -6, 0).isPositive(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, -7).isPositive(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0).isPositive(), false);
    }

    //-----------------------------------------------------------------------
    // isPositiveOrZero()
    //-----------------------------------------------------------------------
    public void test_isPositiveOrZero() {
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(1, 2, 3, 0, 0, 0, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 5, 6, 7).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(1, 0, 0, 0, 0, 0, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 2, 0, 0, 0, 0, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 0, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 0, 0, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 5, 0, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 6, 0).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, 7).isPositiveOrZero(), true);
        assertEquals(ISOPeriod.of(-1, -2, -3, -4, -5, -6, -7).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(-1, -2, 3, 4, -5, -6, -7).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(-1, 0, 0, 0, 0, 0, 0).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, -2, 0, 0, 0, 0, 0).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, 0, -3, 0, 0, 0, 0).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, -4, 0, 0, 0).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, -5, 0, 0).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -6, 0).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, -7).isPositiveOrZero(), false);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0).isPositiveOrZero(), true);
    }

    //-----------------------------------------------------------------------
    // getNanosInt()
    //-----------------------------------------------------------------------
    public void test_getNanosInt() {
        ISOPeriod test = ISOPeriod.ofNanos(Integer.MAX_VALUE);
        assertEquals(test.getNanosInt(), Integer.MAX_VALUE);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_getNanosInt_tooBig() {
        ISOPeriod test = ISOPeriod.ofNanos(Integer.MAX_VALUE + 1L);
        test.getNanosInt();
    }

    //-----------------------------------------------------------------------
    // withYears()
    //-----------------------------------------------------------------------
    public void test_withYears() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withYears(10), 10, 2, 3, 4, 5, 6, 7);
    }

    public void test_withYears_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withYears(1), test);
    }

    public void test_withYears_toZero() {
        ISOPeriod test = ISOPeriod.ofYears(1);
        assertSame(test.withYears(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withMonths()
    //-----------------------------------------------------------------------
    public void test_withMonths() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withMonths(10), 1, 10, 3, 4, 5, 6, 7);
    }

    public void test_withMonths_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withMonths(2), test);
    }

    public void test_withMonths_toZero() {
        ISOPeriod test = ISOPeriod.ofMonths(1);
        assertSame(test.withMonths(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withDays()
    //-----------------------------------------------------------------------
    public void test_withDays() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withDays(10), 1, 2, 10, 4, 5, 6, 7);
    }

    public void test_withDays_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withDays(3), test);
    }

    public void test_withDays_toZero() {
        ISOPeriod test = ISOPeriod.ofDays(1);
        assertSame(test.withDays(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withHours()
    //-----------------------------------------------------------------------
    public void test_withHours() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withHours(10), 1, 2, 3, 10, 5, 6, 7);
    }

    public void test_withHours_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withHours(4), test);
    }

    public void test_withHours_toZero() {
        ISOPeriod test = ISOPeriod.ofHours(1);
        assertSame(test.withHours(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withMinutes()
    //-----------------------------------------------------------------------
    public void test_withMinutes() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withMinutes(10), 1, 2, 3, 4, 10, 6, 7);
    }

    public void test_withMinutes_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withMinutes(5), test);
    }

    public void test_withMinutes_toZero() {
        ISOPeriod test = ISOPeriod.ofMinutes(1);
        assertSame(test.withMinutes(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withSeconds()
    //-----------------------------------------------------------------------
    public void test_withSeconds() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withSeconds(10), 1, 2, 3, 4, 5, 10, 7);
    }

    public void test_withSeconds_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withSeconds(6), test);
    }

    public void test_withSeconds_toZero() {
        ISOPeriod test = ISOPeriod.ofSeconds(1);
        assertSame(test.withSeconds(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withNanos()
    //-----------------------------------------------------------------------
    public void test_withNanos() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withNanos(10), 1, 2, 3, 4, 5, 6, 10);
    }

    public void test_withNanos_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.withNanos(7), test);
    }

    public void test_withNanos_toZero() {
        ISOPeriod test = ISOPeriod.ofNanos(1);
        assertSame(test.withNanos(0), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withDateFieldsOnly()
    //-----------------------------------------------------------------------
    public void test_withDateFieldsOnly() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withDateFieldsOnly(), 1, 2, 3, 0, 0, 0, 0);
    }

    public void test_withDateFieldsOnly_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 0, 0, 0, 0);
        assertSame(test.withDateFieldsOnly(), test);
    }

    public void test_withDateFieldsOnly_toZero() {
        ISOPeriod test = ISOPeriod.of(0, 0, 0, 4, 5, 6, 7);
        assertSame(test.withDateFieldsOnly(), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // withTimeFieldsOnly()
    //-----------------------------------------------------------------------
    public void test_withTimeFieldsOnly() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.withTimeFieldsOnly(), 0, 0, 0, 4, 5, 6, 7);
    }

    public void test_withTimeFieldsOnly_noChange() {
        ISOPeriod test = ISOPeriod.of(0, 0, 0, 4, 5, 6, 7);
        assertSame(test.withTimeFieldsOnly(), test);
    }

    public void test_withTimeFieldsOnly_toZero() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 0, 0, 0, 0);
        assertSame(test.withTimeFieldsOnly(), ISOPeriod.ZERO);
    }

    //-----------------------------------------------------------------------
    // plus(ISOPeriodProvider)
    //-----------------------------------------------------------------------
    public void test_plus_provider() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        ISOPeriod provider = ISOPeriod.of(3, 3, 3, 3, 3, 3, 3);
        assertPeriod(test.plus(provider), 4, 5, 6, 7, 8, 9, 10);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plus_provider_null() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        test.plus(null);
    }

    //-----------------------------------------------------------------------
    // plusYears()
    //-----------------------------------------------------------------------
    public void test_plusYears() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusYears(10), 11, 2, 3, 4, 5, 6, 7);
    }

    public void test_plusYears_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusYears(0), test);
    }

    public void test_plusYears_toZero() {
        ISOPeriod test = ISOPeriod.ofYears(-1);
        assertSame(test.plusYears(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusYears_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofYears(Integer.MAX_VALUE);
        test.plusYears(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusYears_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofYears(Integer.MIN_VALUE);
        test.plusYears(-1);
    }

    //-----------------------------------------------------------------------
    // plusMonths()
    //-----------------------------------------------------------------------
    public void test_plusMonths() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusMonths(10), 1, 12, 3, 4, 5, 6, 7);
    }

    public void test_plusMonths_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusMonths(0), test);
    }

    public void test_plusMonths_toZero() {
        ISOPeriod test = ISOPeriod.ofMonths(-1);
        assertSame(test.plusMonths(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusMonths_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofMonths(Integer.MAX_VALUE);
        test.plusMonths(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusMonths_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofMonths(Integer.MIN_VALUE);
        test.plusMonths(-1);
    }

    //-----------------------------------------------------------------------
    // plusDays()
    //-----------------------------------------------------------------------
    public void test_plusDays() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusDays(10), 1, 2, 13, 4, 5, 6, 7);
    }

    public void test_plusDays_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusDays(0), test);
    }

    public void test_plusDays_toZero() {
        ISOPeriod test = ISOPeriod.ofDays(-1);
        assertSame(test.plusDays(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusDays_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofDays(Integer.MAX_VALUE);
        test.plusDays(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusDays_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofDays(Integer.MIN_VALUE);
        test.plusDays(-1);
    }

    //-----------------------------------------------------------------------
    // plusHours()
    //-----------------------------------------------------------------------
    public void test_plusHours() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusHours(10), 1, 2, 3, 14, 5, 6, 7);
    }

    public void test_plusHours_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusHours(0), test);
    }

    public void test_plusHours_toZero() {
        ISOPeriod test = ISOPeriod.ofHours(-1);
        assertSame(test.plusHours(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusHours_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofHours(Integer.MAX_VALUE);
        test.plusHours(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusHours_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofHours(Integer.MIN_VALUE);
        test.plusHours(-1);
    }

    //-----------------------------------------------------------------------
    // plusMinutes()
    //-----------------------------------------------------------------------
    public void test_plusMinutes() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusMinutes(10), 1, 2, 3, 4, 15, 6, 7);
    }

    public void test_plusMinutes_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusMinutes(0), test);
    }

    public void test_plusMinutes_toZero() {
        ISOPeriod test = ISOPeriod.ofMinutes(-1);
        assertSame(test.plusMinutes(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusMinutes_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofMinutes(Integer.MAX_VALUE);
        test.plusMinutes(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusMinutes_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofMinutes(Integer.MIN_VALUE);
        test.plusMinutes(-1);
    }

    //-----------------------------------------------------------------------
    // plusSeconds()
    //-----------------------------------------------------------------------
    public void test_plusSeconds() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusSeconds(10), 1, 2, 3, 4, 5, 16, 7);
    }

    public void test_plusSeconds_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusSeconds(0), test);
    }

    public void test_plusSeconds_toZero() {
        ISOPeriod test = ISOPeriod.ofSeconds(-1);
        assertSame(test.plusSeconds(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusSeconds_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofSeconds(Integer.MAX_VALUE);
        test.plusSeconds(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusSeconds_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofSeconds(Integer.MIN_VALUE);
        test.plusSeconds(-1);
    }

    //-----------------------------------------------------------------------
    // plusNanos()
    //-----------------------------------------------------------------------
    public void test_plusNanos() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.plusNanos(10), 1, 2, 3, 4, 5, 6, 17);
    }

    public void test_plusNanos_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.plusNanos(0), test);
    }

    public void test_plusNanos_toZero() {
        ISOPeriod test = ISOPeriod.ofNanos(-1);
        assertSame(test.plusNanos(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusNanos_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofNanos(Long.MAX_VALUE);
        test.plusNanos(1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plusNanos_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofNanos(Long.MIN_VALUE);
        test.plusNanos(-1);
    }

    //-----------------------------------------------------------------------
    // minus(ISOPeriodProvider)
    //-----------------------------------------------------------------------
    public void test_minus_provider() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        ISOPeriod provider = ISOPeriod.of(3, 3, 3, 3, 3, 3, 3);
        assertPeriod(test.minus(provider), -2, -1, 0, 1, 2, 3, 4);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minus_provider_null() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        test.minus(null);
    }

    //-----------------------------------------------------------------------
    // minusYears()
    //-----------------------------------------------------------------------
    public void test_minusYears() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusYears(10), -9, 2, 3, 4, 5, 6, 7);
    }

    public void test_minusYears_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusYears(0), test);
    }

    public void test_minusYears_toZero() {
        ISOPeriod test = ISOPeriod.ofYears(1);
        assertSame(test.minusYears(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusYears_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofYears(Integer.MAX_VALUE);
        test.minusYears(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusYears_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofYears(Integer.MIN_VALUE);
        test.minusYears(1);
    }

    //-----------------------------------------------------------------------
    // minusMonths()
    //-----------------------------------------------------------------------
    public void test_minusMonths() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusMonths(10), 1, -8, 3, 4, 5, 6, 7);
    }

    public void test_minusMonths_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusMonths(0), test);
    }

    public void test_minusMonths_toZero() {
        ISOPeriod test = ISOPeriod.ofMonths(1);
        assertSame(test.minusMonths(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusMonths_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofMonths(Integer.MAX_VALUE);
        test.minusMonths(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusMonths_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofMonths(Integer.MIN_VALUE);
        test.minusMonths(1);
    }

    //-----------------------------------------------------------------------
    // minusDays()
    //-----------------------------------------------------------------------
    public void test_minusDays() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusDays(10), 1, 2, -7, 4, 5, 6, 7);
    }

    public void test_minusDays_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusDays(0), test);
    }

    public void test_minusDays_toZero() {
        ISOPeriod test = ISOPeriod.ofDays(1);
        assertSame(test.minusDays(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusDays_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofDays(Integer.MAX_VALUE);
        test.minusDays(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusDays_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofDays(Integer.MIN_VALUE);
        test.minusDays(1);
    }

    //-----------------------------------------------------------------------
    // minusHours()
    //-----------------------------------------------------------------------
    public void test_minusHours() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusHours(10), 1, 2, 3, -6, 5, 6, 7);
    }

    public void test_minusHours_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusHours(0), test);
    }

    public void test_minusHours_toZero() {
        ISOPeriod test = ISOPeriod.ofHours(1);
        assertSame(test.minusHours(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusHours_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofHours(Integer.MAX_VALUE);
        test.minusHours(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusHours_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofHours(Integer.MIN_VALUE);
        test.minusHours(1);
    }

    //-----------------------------------------------------------------------
    // minusMinutes()
    //-----------------------------------------------------------------------
    public void test_minusMinutes() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusMinutes(10), 1, 2, 3, 4, -5, 6, 7);
    }

    public void test_minusMinutes_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusMinutes(0), test);
    }

    public void test_minusMinutes_toZero() {
        ISOPeriod test = ISOPeriod.ofMinutes(1);
        assertSame(test.minusMinutes(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusMinutes_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofMinutes(Integer.MAX_VALUE);
        test.minusMinutes(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusMinutes_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofMinutes(Integer.MIN_VALUE);
        test.minusMinutes(1);
    }

    //-----------------------------------------------------------------------
    // minusSeconds()
    //-----------------------------------------------------------------------
    public void test_minusSeconds() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusSeconds(10), 1, 2, 3, 4, 5, -4, 7);
    }

    public void test_minusSeconds_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusSeconds(0), test);
    }

    public void test_minusSeconds_toZero() {
        ISOPeriod test = ISOPeriod.ofSeconds(1);
        assertSame(test.minusSeconds(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusSeconds_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofSeconds(Integer.MAX_VALUE);
        test.minusSeconds(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusSeconds_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofSeconds(Integer.MIN_VALUE);
        test.minusSeconds(1);
    }

    //-----------------------------------------------------------------------
    // minusNanos()
    //-----------------------------------------------------------------------
    public void test_minusNanos() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.minusNanos(10), 1, 2, 3, 4, 5, 6, -3);
    }

    public void test_minusNanos_noChange() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.minusNanos(0), test);
    }

    public void test_minusNanos_toZero() {
        ISOPeriod test = ISOPeriod.ofNanos(1);
        assertSame(test.minusNanos(1), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusNanos_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofNanos(Long.MAX_VALUE);
        test.minusNanos(-1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minusNanos_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofNanos(Long.MIN_VALUE);
        test.minusNanos(1);
    }

    //-----------------------------------------------------------------------
    // multipliedBy()
    //-----------------------------------------------------------------------
    public void test_multipliedBy() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.multipliedBy(2), 2, 4, 6, 8, 10, 12, 14);
        assertPeriod(test.multipliedBy(-3), -3, -6, -9, -12, -15, -18, -21);
    }

    public void test_multipliedBy_zeroBase() {
        assertSame(ISOPeriod.ZERO.multipliedBy(2), ISOPeriod.ZERO);
    }

    public void test_multipliedBy_zero() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.multipliedBy(0), ISOPeriod.ZERO);
    }

    public void test_multipliedBy_one() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertSame(test.multipliedBy(1), test);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_multipliedBy_overflowTooBig() {
        ISOPeriod test = ISOPeriod.ofYears(Integer.MAX_VALUE / 2 + 1);
        test.multipliedBy(2);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_multipliedBy_overflowTooSmall() {
        ISOPeriod test = ISOPeriod.ofYears(Integer.MIN_VALUE / 2 - 1);
        test.multipliedBy(2);
    }

    //-----------------------------------------------------------------------
    // dividedBy()
    //-----------------------------------------------------------------------
    public void test_dividedBy() {
        ISOPeriod test = ISOPeriod.of(12, 12, 12, 12, 12, 11, 9);
        assertSame(ISOPeriod.ZERO.dividedBy(2), ISOPeriod.ZERO);
        assertSame(test.dividedBy(1), test);
        assertPeriod(test.dividedBy(2), 6, 6, 6, 6, 6, 5, 4);
        assertPeriod(test.dividedBy(-3), -4, -4, -4, -4, -4, -3, -3);
    }

    public void test_dividedBy_zeroBase() {
        assertSame(ISOPeriod.ZERO.dividedBy(2), ISOPeriod.ZERO);
    }

    public void test_dividedBy_one() {
        ISOPeriod test = ISOPeriod.of(12, 12, 12, 12, 12, 11, 11);
        assertSame(test.dividedBy(1), test);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_dividedBy_divideByZero() {
        ISOPeriod test = ISOPeriod.of(12, 12, 12, 12, 12, 12, 12);
        test.dividedBy(0);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_dividedBy_zeroBase_divideByZero() {
        ISOPeriod.ZERO.dividedBy(0);
    }

    //-----------------------------------------------------------------------
    // negated()
    //-----------------------------------------------------------------------
    public void test_negated() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6, 7);
        assertPeriod(test.negated(), -1, -2, -3, -4, -5, -6, -7);
    }

    public void test_negated_zero() {
        assertSame(ISOPeriod.ZERO.negated(), ISOPeriod.ZERO);
    }

    public void test_negated_max() {
        assertPeriod(ISOPeriod.ofYears(Integer.MAX_VALUE).negated(), -Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_negated_overflow() {
        ISOPeriod.ofYears(Integer.MIN_VALUE).negated();
    }

    //-----------------------------------------------------------------------
    // normalized()
    //-----------------------------------------------------------------------
    // TODO: Test negatives
    public void test_normalized() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).normalized(), 1, 2, 3, 4, 5, 6, 7);
    }

    public void test_normalized_months() {
        assertPeriod(ISOPeriod.of(1, 11, 3, 4, 5, 6).normalized(), 1, 11, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 12, 3, 4, 5, 6).normalized(), 2, 0, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 23, 3, 4, 5, 6).normalized(), 2, 11, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 24, 3, 4, 5, 6).normalized(), 3, 0, 3, 4, 5, 6, 0);
    }

    public void test_normalized_nanos() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6, 999999999).normalized(), 1, 2, 3, 4, 5, 6, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6, 1000000000).normalized(), 1, 2, 3, 4, 5, 7, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 59, 999999999).normalized(), 1, 2, 3, 4, 5, 59, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 59, 1000000000).normalized(), 1, 2, 3, 4, 6, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 59, 999999999).normalized(), 1, 2, 3, 4, 59, 59, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 59, 1000000000).normalized(), 1, 2, 3, 5, 0, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 59, 999999999).normalized(), 1, 2, 3, 23, 59, 59, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 59, 1000000000).normalized(), 1, 2, 3, 24, 0, 0, 0);
    }

    public void test_normalized_seconds() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 59).normalized(), 1, 2, 3, 4, 5, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 60).normalized(), 1, 2, 3, 4, 6, 0, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 119).normalized(), 1, 2, 3, 4, 6, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 120).normalized(), 1, 2, 3, 4, 7, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 59).normalized(), 1, 2, 3, 4, 59, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 60).normalized(), 1, 2, 3, 5, 0, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 59).normalized(), 1, 2, 3, 23, 59, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 60).normalized(), 1, 2, 3, 24, 0, 0, 0);
    }

    public void test_normalized_minutes() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 6).normalized(), 1, 2, 3, 4, 59, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 60, 6).normalized(), 1, 2, 3, 5, 0, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 119, 6).normalized(), 1, 2, 3, 5, 59, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 120, 6).normalized(), 1, 2, 3, 6, 0, 6, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 6).normalized(), 1, 2, 3, 23, 59, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 60, 6).normalized(), 1, 2, 3, 24, 0, 6, 0);
    }

    public void test_normalized_hours() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 5, 6).normalized(), 1, 2, 3, 23, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 24, 5, 6).normalized(), 1, 2, 3, 24, 5, 6, 0);
    }

    public void test_normalized_zero() {
        assertSame(ISOPeriod.ZERO.normalized(), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_normalized_max() {
        ISOPeriod base = ISOPeriod.of(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        base.normalized();
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_normalized_maxTime() {
        ISOPeriod base = ISOPeriod.of(0, 0, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        base.normalized();
    }

    //-----------------------------------------------------------------------
    // normalizedWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_normalizedWith24HourDays() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6).normalizedWith24HourDays(), 1, 2, 3, 4, 5, 6, 0);
    }

    public void test_normalizedWith24HourDays_months() {
        assertPeriod(ISOPeriod.of(1, 11, 3, 4, 5, 6).normalizedWith24HourDays(), 1, 11, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 12, 3, 4, 5, 6).normalizedWith24HourDays(), 2, 0, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 23, 3, 4, 5, 6).normalizedWith24HourDays(), 2, 11, 3, 4, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 24, 3, 4, 5, 6).normalizedWith24HourDays(), 3, 0, 3, 4, 5, 6, 0);
    }

    public void test_normalizedWith24HourDays_nanos() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6, 999999999).normalizedWith24HourDays(), 1, 2, 3, 4, 5, 6, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 6, 1000000000).normalizedWith24HourDays(), 1, 2, 3, 4, 5, 7, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 59, 999999999).normalizedWith24HourDays(), 1, 2, 3, 4, 5, 59, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 59, 1000000000).normalizedWith24HourDays(), 1, 2, 3, 4, 6, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 59, 999999999).normalizedWith24HourDays(), 1, 2, 3, 4, 59, 59, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 59, 1000000000).normalizedWith24HourDays(), 1, 2, 3, 5, 0, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 59, 999999999).normalizedWith24HourDays(), 1, 2, 3, 23, 59, 59, 999999999);
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 59, 1000000000).normalizedWith24HourDays(), 1, 2, 4, 0, 0, 0, 0);
    }

    public void test_normalizedWith24HourDays_seconds() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 59).normalizedWith24HourDays(), 1, 2, 3, 4, 5, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 60).normalizedWith24HourDays(), 1, 2, 3, 4, 6, 0, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 119).normalizedWith24HourDays(), 1, 2, 3, 4, 6, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 5, 120).normalizedWith24HourDays(), 1, 2, 3, 4, 7, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 59).normalizedWith24HourDays(), 1, 2, 3, 4, 59, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 60).normalizedWith24HourDays(), 1, 2, 3, 5, 0, 0, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 59).normalizedWith24HourDays(), 1, 2, 3, 23, 59, 59, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 60).normalizedWith24HourDays(), 1, 2, 4, 0, 0, 0, 0);
    }

    public void test_normalizedWith24HourDays_minutes() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 59, 6).normalizedWith24HourDays(), 1, 2, 3, 4, 59, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 60, 6).normalizedWith24HourDays(), 1, 2, 3, 5, 0, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 119, 6).normalizedWith24HourDays(), 1, 2, 3, 5, 59, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 4, 120, 6).normalizedWith24HourDays(), 1, 2, 3, 6, 0, 6, 0);
        
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 59, 6).normalizedWith24HourDays(), 1, 2, 3, 23, 59, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 60, 6).normalizedWith24HourDays(), 1, 2, 4, 0, 0, 6, 0);
    }

    public void test_normalizedWith24HourDays_hours() {
        assertPeriod(ISOPeriod.of(1, 2, 3, 23, 5, 6).normalizedWith24HourDays(), 1, 2, 3, 23, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 24, 5, 6).normalizedWith24HourDays(), 1, 2, 4, 0, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 47, 5, 6).normalizedWith24HourDays(), 1, 2, 4, 23, 5, 6, 0);
        assertPeriod(ISOPeriod.of(1, 2, 3, 48, 5, 6).normalizedWith24HourDays(), 1, 2, 5, 0, 5, 6, 0);
    }

    public void test_normalizedWith24HourDays_zero() {
        assertSame(ISOPeriod.ZERO.normalizedWith24HourDays(), ISOPeriod.ZERO);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_normalizedWith24HourDays_max() {
        ISOPeriod base = ISOPeriod.of(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        base.normalizedWith24HourDays();
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_normalizedWith24HourDays_maxTime() {
        ISOPeriod base = ISOPeriod.of(0, 0, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        base.normalizedWith24HourDays();
    }

    //-----------------------------------------------------------------------
    // totalYears()
    //-----------------------------------------------------------------------
    public void test_totalYears() {
        assertEquals(ISOPeriod.ZERO.totalYears(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalYears(), 1);
        assertEquals(ISOPeriod.ofDateFields(3, 0, 1000).totalYears(), 3);
        assertEquals(ISOPeriod.ofDateFields(3, 11, 0).totalYears(), 3);
        assertEquals(ISOPeriod.ofDateFields(3, 12, 0).totalYears(), 4);
        assertEquals(ISOPeriod.ofDateFields(3, -11, 0).totalYears(), 3);
        assertEquals(ISOPeriod.ofDateFields(3, -12, 0).totalYears(), 2);
        assertEquals(ISOPeriod.ofDateFields(-3, 11, 0).totalYears(), -3);
        assertEquals(ISOPeriod.ofDateFields(-3, 12, 0).totalYears(), -2);
        assertEquals(ISOPeriod.ofDateFields(-3, -11, 0).totalYears(), -3);
        assertEquals(ISOPeriod.ofDateFields(-3, -12, 0).totalYears(), -4);
    }

    public void test_totalYears_big() {
        BigInteger calc = MAX_BINT.divide(BigInteger.valueOf(12)).add(MAX_BINT);
        long y = new BigDecimal(calc).longValueExact();
        assertEquals(ISOPeriod.ofDateFields(Integer.MAX_VALUE, Integer.MAX_VALUE, 0).totalYears(), y);
    }

    //-----------------------------------------------------------------------
    // totalMonths()
    //-----------------------------------------------------------------------
    public void test_totalMonths() {
        assertEquals(ISOPeriod.ZERO.totalMonths(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalMonths(), 14);
        assertEquals(ISOPeriod.ofDateFields(3, 0, 1000).totalMonths(), 36);
        assertEquals(ISOPeriod.ofDateFields(3, 11, 0).totalMonths(), 47);
        assertEquals(ISOPeriod.ofDateFields(3, 12, 0).totalMonths(), 48);
        assertEquals(ISOPeriod.ofDateFields(3, -11, 0).totalMonths(), 25);
        assertEquals(ISOPeriod.ofDateFields(3, -12, 0).totalMonths(), 24);
        assertEquals(ISOPeriod.ofDateFields(-3, 11, 0).totalMonths(), -25);
        assertEquals(ISOPeriod.ofDateFields(-3, 12, 0).totalMonths(), -24);
        assertEquals(ISOPeriod.ofDateFields(-3, -11, 0).totalMonths(), -47);
        assertEquals(ISOPeriod.ofDateFields(-3, -12, 0).totalMonths(), -48);
    }

    public void test_totalMonths_big() {
        BigInteger calc = MAX_BINT.multiply(BigInteger.valueOf(12)).add(MAX_BINT);
        long m = new BigDecimal(calc).longValueExact();
        assertEquals(ISOPeriod.ofDateFields(Integer.MAX_VALUE, Integer.MAX_VALUE, 0).totalMonths(), m);
    }

    //-----------------------------------------------------------------------
    // totalDaysWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_totalDaysWith24HourDays() {
        assertEquals(ISOPeriod.ZERO.totalDaysWith24HourDays(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalDaysWith24HourDays(), 3);
    }

    public void test_totalDaysWith24HourDays_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 0, 0).totalDaysWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 3, 23, 0, 0, 0).totalDaysWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 3, 24, 0, 0, 0).totalDaysWith24HourDays(), 4);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 24 * 60 - 1, 0, 0).totalDaysWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 24 * 60, 0, 0).totalDaysWith24HourDays(), 4);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 24 * 60 * 60 - 1, 0).totalDaysWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 24 * 60 * 60, 0).totalDaysWith24HourDays(), 4);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 0, 24L * 60L * 60L * 1000000000L - 1).totalDaysWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 3, 0, 0, 0, 24L * 60L * 60L * 1000000000L).totalDaysWith24HourDays(), 4);
    }

    public void test_totalDaysWith24HourDays_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 3, 24, 0, 0).totalDaysWith24HourDays(), 4);
        assertEquals(ISOPeriod.of(0, 0, 3, -24, 0, 0).totalDaysWith24HourDays(), 2);
        
        assertEquals(ISOPeriod.of(0, 0, -3, 24, 0, 0).totalDaysWith24HourDays(), -2);
        assertEquals(ISOPeriod.of(0, 0, -3, -24, 0, 0).totalDaysWith24HourDays(), -4);
    }

    public void test_totalDaysWith24HourDays_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT).divide(BINT_24)
                            .add(MAX_BINT);
        long d = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalDaysWith24HourDays(), d);
    }

    //-----------------------------------------------------------------------
    // totalHours()
    //-----------------------------------------------------------------------
    public void test_totalHours() {
        assertEquals(ISOPeriod.ZERO.totalHours(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalHours(), 4);
    }

    public void test_totalHours_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 0, 0, 0).totalHours(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 59, 0, 0).totalHours(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 60, 0, 0).totalHours(), 4);
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 0, 3599, 0).totalHours(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 0, 3600, 0).totalHours(), 4);
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 0, 0, 3600L * 1000000000L - 1).totalHours(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 3, 0, 0, 3600L * 1000000000L).totalHours(), 4);
    }

    public void test_totalHours_negatives() {
        assertEquals(ISOPeriod.ofTimeFields(3, 60, 0).totalHours(), 4);
        assertEquals(ISOPeriod.ofTimeFields(3, -60, 0).totalHours(), 2);
        assertEquals(ISOPeriod.ofTimeFields(-3, 60, 0).totalHours(), -2);
        assertEquals(ISOPeriod.ofTimeFields(-3, -60, 0).totalHours(), -4);
    }

    public void test_totalHours_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT);
        long h = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalHours(), h);
    }

    //-----------------------------------------------------------------------
    // totalHoursWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_totalHoursWith24HourDays() {
        assertEquals(ISOPeriod.ZERO.totalHoursWith24HourDays(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalHoursWith24HourDays(), 76);
    }

    public void test_totalHoursWith24HourDays_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 0, 0, 0).totalHoursWith24HourDays(), 27);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 59, 0, 0).totalHoursWith24HourDays(), 27);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 60, 0, 0).totalHoursWith24HourDays(), 28);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 0, 3599, 0).totalHoursWith24HourDays(), 27);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 0, 3600, 0).totalHoursWith24HourDays(), 28);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 0, 0, 3600L * 1000000000L - 1).totalHoursWith24HourDays(), 27);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 0, 0, 3600L * 1000000000L).totalHoursWith24HourDays(), 28);
    }

    public void test_totalHoursWith24HourDays_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 1, 3, 60, 0).totalHoursWith24HourDays(), 28);
        assertEquals(ISOPeriod.of(0, 0, 1, 3, -60, 0).totalHoursWith24HourDays(), 26);
        assertEquals(ISOPeriod.of(0, 0, 1, -3, 60, 0).totalHoursWith24HourDays(), 22);
        assertEquals(ISOPeriod.of(0, 0, 1, -3, -60, 0).totalHoursWith24HourDays(), 20);
        
        assertEquals(ISOPeriod.of(0, 0, -1, 3, 60, 0).totalHoursWith24HourDays(), -20);
        assertEquals(ISOPeriod.of(0, 0, -1, 3, -60, 0).totalHoursWith24HourDays(), -22);
        assertEquals(ISOPeriod.of(0, 0, -1, -3, 60, 0).totalHoursWith24HourDays(), -26);
        assertEquals(ISOPeriod.of(0, 0, -1, -3, -60, 0).totalHoursWith24HourDays(), -28);
    }

    public void test_totalHoursWith24HourDays_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_24));
        long h = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalHoursWith24HourDays(), h);
    }

    //-----------------------------------------------------------------------
    // totalMinutes()
    //-----------------------------------------------------------------------
    public void test_totalMinutes() {
        assertEquals(ISOPeriod.ZERO.totalMinutes(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalMinutes(), 4 * 60 + 5);
    }

    public void test_totalMinutes_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 0, 0, 0).totalMinutes(), 60);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 59, 0, 0).totalMinutes(), 119);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 60, 0, 0).totalMinutes(), 120);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 59, 0).totalMinutes(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 60, 0).totalMinutes(), 4);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 0, 60L * 1000000000L - 1).totalMinutes(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 0, 60L * 1000000000L).totalMinutes(), 4);
    }

    public void test_totalMinutes_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 3, 60).totalMinutes(), 64);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 3, -60).totalMinutes(), 62);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, -3, 60).totalMinutes(), 58);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, -3, -60).totalMinutes(), 56);
        
        assertEquals(ISOPeriod.of(0, 0, 0, -1, 3, 60).totalMinutes(), -56);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, 3, -60).totalMinutes(), -58);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, -3, 60).totalMinutes(), -62);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, -3, -60).totalMinutes(), -64);
    }

    public void test_totalMinutes_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_60));
        long m = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalMinutes(), m);
    }

    //-----------------------------------------------------------------------
    // totalMinutesWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_totalMinutesWith24HourDays() {
        assertEquals(ISOPeriod.ZERO.totalMinutesWith24HourDays(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalMinutesWith24HourDays(), 3 * 24 * 60 + 4 * 60 + 5);
    }

    public void test_totalMinutesWith24HourDays_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 0, 0, 0).totalMinutesWith24HourDays(), 60);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 59, 0, 0).totalMinutesWith24HourDays(), 119);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 60, 0, 0).totalMinutesWith24HourDays(), 120);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 59, 0).totalMinutesWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 60, 0).totalMinutesWith24HourDays(), 4);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 0, 60L * 1000000000L - 1).totalMinutesWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 3, 0, 60L * 1000000000L).totalMinutesWith24HourDays(), 4);
        
        assertEquals(ISOPeriod.of(0, 0, 1, 1, 0, 0, 0).totalMinutesWith24HourDays(), 24 * 60 + 60);
        assertEquals(ISOPeriod.of(0, 0, 1, 1, 59, 0, 0).totalMinutesWith24HourDays(), 24 * 60 + 119);
        assertEquals(ISOPeriod.of(0, 0, 1, 1, 60, 0, 0).totalMinutesWith24HourDays(), 24 * 60 + 120);
        assertEquals(ISOPeriod.of(0, 0, 1, 0, 3, 59, 0).totalMinutesWith24HourDays(), 24 * 60 + 3);
        assertEquals(ISOPeriod.of(0, 0, 1, 0, 3, 60, 0).totalMinutesWith24HourDays(), 24 * 60 + 4);
        assertEquals(ISOPeriod.of(0, 0, 1, 0, 3, 0, 60L * 1000000000L - 1).totalMinutesWith24HourDays(), 24 * 60 + 3);
        assertEquals(ISOPeriod.of(0, 0, 1, 0, 3, 0, 60L * 1000000000L).totalMinutesWith24HourDays(), 24 * 60 + 4);
    }

    public void test_totalMinutesWith24HourDays_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 3, 60).totalMinutesWith24HourDays(), 64);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 3, -60).totalMinutesWith24HourDays(), 62);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, -3, 60).totalMinutesWith24HourDays(), 58);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, -3, -60).totalMinutesWith24HourDays(), 56);
        
        assertEquals(ISOPeriod.of(0, 0, 0, -1, 3, 60).totalMinutesWith24HourDays(), -56);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, 3, -60).totalMinutesWith24HourDays(), -58);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, -3, 60).totalMinutesWith24HourDays(), -62);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, -3, -60).totalMinutesWith24HourDays(), -64);
        
        assertEquals(ISOPeriod.of(0, 0, -1, 0, 0, 0).totalMinutesWith24HourDays(), -24 * 60);
    }

    public void test_totalMinutesWith24HourDays_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT).divide(BINT_60)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_24).add(MAX_BINT).multiply(BINT_60));
        long m = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalMinutesWith24HourDays(), m);
    }

    //-----------------------------------------------------------------------
    // totalSeconds()
    //-----------------------------------------------------------------------
    public void test_totalSeconds() {
        assertEquals(ISOPeriod.ZERO.totalSeconds(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalSeconds(), (4 * 60 + 5) * 60L + 6);
    }

    public void test_totalSeconds_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 0, 2, 0, 0, 0).totalSeconds(), 2 * 3600);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 2, 0, 0).totalSeconds(), 120);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 0).totalSeconds(), 2);
        
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L - 1).totalSeconds(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L).totalSeconds(), 4);
    }

    public void test_totalSeconds_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 3, 1).totalSeconds(), 3600 + 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 3, -1).totalSeconds(), 3600 + 180 - 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, -3, 1).totalSeconds(), 3600 - 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, -3, -1).totalSeconds(), 3600 - 180 - 1);
        
        assertEquals(ISOPeriod.of(0, 0, 0, -1, 3, 1).totalSeconds(), -3600 + 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, 3, -1).totalSeconds(), -3600 + 180 - 1);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, -3, 1).totalSeconds(), -3600 - 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, -1, -3, -1).totalSeconds(), -3600 - 180 - 1);
    }

    public void test_totalSeconds_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_60).add(MAX_BINT).multiply(BINT_60));
        long s = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalSeconds(), s);
    }

    //-----------------------------------------------------------------------
    // totalSecondsWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_totalSecondsWith24HourDays() {
        assertEquals(ISOPeriod.ZERO.totalSecondsWith24HourDays(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalSecondsWith24HourDays(), ((3 * 24 + 4) * 60 + 5) * 60L + 6);
    }

    public void test_totalSecondsWith24HourDays_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 2, 0, 0, 0, 0).totalSecondsWith24HourDays(), 2 * 24 * 60 * 60);
        assertEquals(ISOPeriod.of(0, 0, 0, 2, 0, 0, 0).totalSecondsWith24HourDays(), 2 * 3600);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 2, 0, 0).totalSecondsWith24HourDays(), 120);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 0).totalSecondsWith24HourDays(), 2);
        
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L - 1).totalSecondsWith24HourDays(), 3);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L).totalSecondsWith24HourDays(), 4);
    }

    public void test_totalSecondsWith24HourDays_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 1, 1, 3, 1).totalSecondsWith24HourDays(), 24 * 60 * 60 + 3600 + 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 1, 1, 3, -1).totalSecondsWith24HourDays(), 24 * 60 * 60 + 3600 + 180 - 1);
        assertEquals(ISOPeriod.of(0, 0, 1, 1, -3, 1).totalSecondsWith24HourDays(), 24 * 60 * 60 + 3600 - 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 1, 1, -3, -1).totalSecondsWith24HourDays(), 24 * 60 * 60 + 3600 - 180 - 1);
        
        assertEquals(ISOPeriod.of(0, 0, 1, -1, 3, 1).totalSecondsWith24HourDays(), 24 * 60 * 60 - 3600 + 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 1, -1, 3, -1).totalSecondsWith24HourDays(), 24 * 60 * 60 - 3600 + 180 - 1);
        assertEquals(ISOPeriod.of(0, 0, 1, -1, -3, 1).totalSecondsWith24HourDays(), 24 * 60 * 60 - 3600 - 180 + 1);
        assertEquals(ISOPeriod.of(0, 0, 1, -1, -3, -1).totalSecondsWith24HourDays(), 24 * 60 * 60 - 3600 - 180 - 1);
    }

    public void test_totalSecondsWith24HourDays_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_24).add(MAX_BINT).multiply(BINT_60).add(MAX_BINT).multiply(BINT_60));
        long s = new BigDecimal(calc).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.totalSecondsWith24HourDays(), s);
    }

    //-----------------------------------------------------------------------
    // totalNanos()
    //-----------------------------------------------------------------------
    public void test_totalNanos() {
        assertEquals(ISOPeriod.ZERO.totalNanos(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalNanos(), ((4 * 60 + 5) * 60L + 6) * 1000000000L + 7);
    }

    public void test_totalNanos_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 0, 2, 0, 0, 0).totalNanos(), 2L * 60L * 60L * 1000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 2, 0, 0).totalNanos(), 2L * 60L * 1000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 0).totalNanos(), 2000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, 2).totalNanos(), 2);
    }

    public void test_totalNanos_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 1, 1).totalNanos(), 1000000000L + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 1, -1).totalNanos(), 1000000000L - 1);

        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -1, 1).totalNanos(), -1000000000L + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -1, -1).totalNanos(), -1000000000L - 1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_totalNanos_big() {
        ISOPeriod test = ISOPeriod.of(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        test.totalNanos();
    }

    public void test_totalNanos_quiteBig() {
        BigInteger nanos = BigInteger.valueOf(1).multiply(BINT_60)
                                .add(BigInteger.valueOf(1000000)).multiply(BINT_60)
                                .add(MAX_BINT).multiply(BINT_1BN);
        long n = new BigDecimal(nanos).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, 0, 1, 1000000, Integer.MAX_VALUE, 0);
        assertEquals(test.totalNanos(), n);
    }

    //-----------------------------------------------------------------------
    // totalNanosWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_totalNanosWith24HourDays() {
        assertEquals(ISOPeriod.ZERO.totalNanosWith24HourDays(), 0);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6, 7).totalNanosWith24HourDays(), (((3L * 24 + 4) * 60 + 5) * 60L + 6) * 1000000000L + 7);
    }

    public void test_totalNanosWith24HourDays_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 2, 0, 0, 0, 0).totalNanosWith24HourDays(), 2L * 24L * 60L * 60L * 1000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 2, 0, 0, 0).totalNanosWith24HourDays(), 2L * 60L * 60L * 1000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 2, 0, 0).totalNanosWith24HourDays(), 2L * 60L * 1000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 0).totalNanosWith24HourDays(), 2000000000L);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 0, 2).totalNanosWith24HourDays(), 2);
    }

    public void test_totalNanosWith24HourDays_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 1, 1).totalNanosWith24HourDays(), 1000000000L + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 1, -1).totalNanosWith24HourDays(), 1000000000L - 1);

        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -1, 1).totalNanosWith24HourDays(), -1000000000L + 1);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -1, -1).totalNanosWith24HourDays(), -1000000000L - 1);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_totalNanosWith24HourDays_big() {
        ISOPeriod test = ISOPeriod.of(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        test.totalNanosWith24HourDays();
    }

    public void test_totalNanosWith24HourDays_quiteBig() {
        BigInteger nanos = BigInteger.valueOf(1).multiply(BINT_60)
                                .add(BigInteger.valueOf(1000000)).multiply(BINT_60)
                                .add(MAX_BINT).multiply(BINT_1BN);
        long n = new BigDecimal(nanos).longValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, 0, 1, 1000000, Integer.MAX_VALUE, 0);
        assertEquals(test.totalNanosWith24HourDays(), n);
    }
    
    @Test(expectedExceptions=ArithmeticException.class)
    public void test_factory_multipleOverflow_Millenia() {
    	ISOPeriod.of(Integer.MAX_VALUE, LocalPeriodUnit.MILLENNIA);
    }
    
    @Test(expectedExceptions=ArithmeticException.class)
    public void test_factory_multipleOverflow_Weeks() {
    	ISOPeriod.of(Integer.MAX_VALUE, LocalPeriodUnit.WEEKS);
    }

    //-----------------------------------------------------------------------
    // toEstimatedDuration()
    //-----------------------------------------------------------------------
    // TODO:
//    public void test_toEstimatedDuration() {
//        assertEquals(ISOPeriod.ZERO.toEstimatedDuration(), Duration.ofSeconds(0));
//        assertEquals(ISOPeriod.of(0, 0, 0, 4, 5, 6, 7).toEstimatedDuration(), Duration.ofSeconds((4 * 60 + 5) * 60L + 6, 7));
//        assertEquals(ISOPeriod.of(0, 0, 0, -4, -5, -6, -7).toEstimatedDuration(), Duration.ofSeconds((-4 * 60 - 5) * 60L - 6, -7));
//    }
//
//    public void test_toEstimatedDuration_Days() {
//        assertEquals(ISOPeriod.ZERO.toEstimatedDuration(), Duration.ofSeconds(0));
//        assertEquals(ISOPeriod.ofDays(2).toEstimatedDuration(), LocalPeriodUnit.DAYS.getDuration().multipliedBy(2));
//        assertEquals(ISOPeriod.ofMonths(2).toEstimatedDuration(), LocalPeriodUnit.MONTHS.getDuration().multipliedBy(2));
//    }

    //-----------------------------------------------------------------------
    // toDuration()
    //-----------------------------------------------------------------------
    public void test_toDuration() {
        assertEquals(ISOPeriod.ZERO.toDuration(), Duration.ofSeconds(0));
        assertEquals(ISOPeriod.of(0, 0, 0, 4, 5, 6, 7).toDuration(), Duration.ofSeconds((4 * 60 + 5) * 60L + 6, 7));
    }

    public void test_toDuration_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 0, 2, 0, 0, 0).toDuration(), Duration.ofSeconds(2 * 3600));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 2, 0, 0).toDuration(), Duration.ofSeconds(120));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 0).toDuration(), Duration.ofSeconds(2));
        
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L - 1).toDuration(), Duration.ofSeconds(3, 999999999));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L).toDuration(), Duration.ofSeconds(4, 0));
    }

    public void test_toDuration_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 1).toDuration(), Duration.ofSeconds(2, 1));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, -1).toDuration(), Duration.ofSeconds(1, 999999999));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -2, 1).toDuration(), Duration.ofSeconds(-2, 1));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -2, -1).toDuration(), Duration.ofSeconds(-3, 999999999));
    }

    public void test_toDuration_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_60).add(MAX_BINT).multiply(BINT_60));
        long s = new BigDecimal(calc).longValueExact();
        calc = BigInteger.valueOf(Long.MAX_VALUE).remainder(BINT_1BN);
        int n = new BigDecimal(calc).intValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.toDuration(), Duration.ofSeconds(s, n));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_toDuration_years() {
        ISOPeriod.of(1, 0, 0, 4, 5, 6, 7).toDuration();
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_toDuration_months() {
        ISOPeriod.of(0, 1, 0, 4, 5, 6, 7).toDuration();
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_toDuration_days() {
        ISOPeriod.of(0, 0, 1, 4, 5, 6, 7).toDuration();
    }

    //-----------------------------------------------------------------------
    // toDurationWith24HourDays()
    //-----------------------------------------------------------------------
    public void test_toDurationWith24HourDays() {
        assertEquals(ISOPeriod.ZERO.toDurationWith24HourDays(), Duration.ofSeconds(0));
        assertEquals(ISOPeriod.of(0, 0, 3, 4, 5, 6, 7).toDurationWith24HourDays(), Duration.ofSeconds(((3 * 24 + 4) * 60 + 5) * 60L + 6, 7));
    }

    public void test_toDurationWith24HourDays_calculation() {
        assertEquals(ISOPeriod.of(0, 0, 2, 0, 0, 0, 0).toDurationWith24HourDays(), Duration.ofSeconds(2 * 24 * 3600));
        assertEquals(ISOPeriod.of(0, 0, 0, 2, 0, 0, 0).toDurationWith24HourDays(), Duration.ofSeconds(2 * 3600));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 2, 0, 0).toDurationWith24HourDays(), Duration.ofSeconds(120));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 0).toDurationWith24HourDays(), Duration.ofSeconds(2));
        
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L - 1).toDurationWith24HourDays(), Duration.ofSeconds(3, 999999999));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 3, 1000000000L).toDurationWith24HourDays(), Duration.ofSeconds(4, 0));
    }

    public void test_toDurationWith24HourDays_negatives() {
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, 1).toDurationWith24HourDays(), Duration.ofSeconds(2, 1));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 2, -1).toDurationWith24HourDays(), Duration.ofSeconds(1, 999999999));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -2, 1).toDurationWith24HourDays(), Duration.ofSeconds(-2, 1));
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, -2, -1).toDurationWith24HourDays(), Duration.ofSeconds(-3, 999999999));
    }

    public void test_toDurationWith24HourDays_big() {
        BigInteger calc = BigInteger.valueOf(Long.MAX_VALUE).divide(BINT_1BN)
                            .add(MAX_BINT)
                            .add(MAX_BINT.multiply(BINT_24).add(MAX_BINT).multiply(BINT_60).add(MAX_BINT).multiply(BINT_60));
        long s = new BigDecimal(calc).longValueExact();
        calc = BigInteger.valueOf(Long.MAX_VALUE).remainder(BINT_1BN);
        int n = new BigDecimal(calc).intValueExact();
        ISOPeriod test = ISOPeriod.of(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(test.toDurationWith24HourDays(), Duration.ofSeconds(s, n));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_toDurationWith24HourDays_years() {
        ISOPeriod.of(1, 0, 0, 4, 5, 6, 7).toDurationWith24HourDays();
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_toDurationWith24HourDays_months() {
        ISOPeriod.of(0, 1, 0, 4, 5, 6, 7).toDurationWith24HourDays();
    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals() {
        assertEquals(ISOPeriod.of(1, 0, 0, 0, 0, 0).equals(ISOPeriod.ofYears(1)), true);
        assertEquals(ISOPeriod.of(0, 1, 0, 0, 0, 0).equals(ISOPeriod.ofMonths(1)), true);
        assertEquals(ISOPeriod.of(0, 0, 1, 0, 0, 0).equals(ISOPeriod.ofDays(1)), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 0, 0).equals(ISOPeriod.ofHours(1)), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 1, 0).equals(ISOPeriod.ofMinutes(1)), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 0, 0, 1).equals(ISOPeriod.ofSeconds(1)), true);
        assertEquals(ISOPeriod.of(1, 2, 3, 0, 0, 0).equals(ISOPeriod.ofDateFields(1, 2, 3)), true);
        assertEquals(ISOPeriod.of(0, 0, 0, 1, 2, 3).equals(ISOPeriod.ofTimeFields(1, 2, 3)), true);
        assertEquals(ISOPeriod.of(1, 2, 3, 4, 5, 6).equals(ISOPeriod.of(1, 2, 3, 4, 5, 6)), true);
        
        assertEquals(ISOPeriod.ofYears(1).equals(ISOPeriod.ofYears(1)), true);
        assertEquals(ISOPeriod.ofYears(1).equals(ISOPeriod.ofYears(2)), false);
        
        assertEquals(ISOPeriod.ofMonths(1).equals(ISOPeriod.ofMonths(1)), true);
        assertEquals(ISOPeriod.ofMonths(1).equals(ISOPeriod.ofMonths(2)), false);
        
        assertEquals(ISOPeriod.ofDays(1).equals(ISOPeriod.ofDays(1)), true);
        assertEquals(ISOPeriod.ofDays(1).equals(ISOPeriod.ofDays(2)), false);
        
        assertEquals(ISOPeriod.ofHours(1).equals(ISOPeriod.ofHours(1)), true);
        assertEquals(ISOPeriod.ofHours(1).equals(ISOPeriod.ofHours(2)), false);
        
        assertEquals(ISOPeriod.ofMinutes(1).equals(ISOPeriod.ofMinutes(1)), true);
        assertEquals(ISOPeriod.ofMinutes(1).equals(ISOPeriod.ofMinutes(2)), false);
        
        assertEquals(ISOPeriod.ofSeconds(1).equals(ISOPeriod.ofSeconds(1)), true);
        assertEquals(ISOPeriod.ofSeconds(1).equals(ISOPeriod.ofSeconds(2)), false);
        
        assertEquals(ISOPeriod.ofDateFields(1, 2, 3).equals(ISOPeriod.ofDateFields(1, 2, 3)), true);
        assertEquals(ISOPeriod.ofDateFields(1, 2, 3).equals(ISOPeriod.ofDateFields(0, 2, 3)), false);
        assertEquals(ISOPeriod.ofDateFields(1, 2, 3).equals(ISOPeriod.ofDateFields(1, 0, 3)), false);
        assertEquals(ISOPeriod.ofDateFields(1, 2, 3).equals(ISOPeriod.ofDateFields(1, 2, 0)), false);
        
        assertEquals(ISOPeriod.ofTimeFields(1, 2, 3).equals(ISOPeriod.ofTimeFields(1, 2, 3)), true);
        assertEquals(ISOPeriod.ofTimeFields(1, 2, 3).equals(ISOPeriod.ofTimeFields(0, 2, 3)), false);
        assertEquals(ISOPeriod.ofTimeFields(1, 2, 3).equals(ISOPeriod.ofTimeFields(1, 0, 3)), false);
        assertEquals(ISOPeriod.ofTimeFields(1, 2, 3).equals(ISOPeriod.ofTimeFields(1, 2, 0)), false);
    }

    public void test_equals_self() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6);
        assertEquals(test.equals(test), true);
    }

    public void test_equals_null() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6);
        assertEquals(test.equals(null), false);
    }

    public void test_equals_otherClass() {
        ISOPeriod test = ISOPeriod.of(1, 2, 3, 4, 5, 6);
        assertEquals(test.equals(""), false);
    }

    //-----------------------------------------------------------------------
    public void test_hashCode() {
        ISOPeriod test5 = ISOPeriod.ofDays(5);
        ISOPeriod test6 = ISOPeriod.ofDays(6);
        assertEquals(test5.hashCode() == test5.hashCode(), true);
        assertEquals(test5.hashCode() == test6.hashCode(), false);
    }

    // test too slow and runs out of memory
//    public void test_hashCode_unique() {
//        // spec requires unique hash codes
//        // years 0-31, months 0-11, days 0-31, hours 0-23, minutes 0-59, seconds 0-59
//        boolean[] pos = new boolean[Integer.MAX_VALUE / 128];  // added 128 to avoid out of memory
//        boolean[] neg = new boolean[Integer.MAX_VALUE / 128];
//        for (int y = 0; y <= 31; y++) {
//            for (int mo = 0; mo <= 11; mo++) {
//                System.out.print(".");
//                for (int d = 0; d <= 31; d++) {
//                    for (int h = 0; h <= 23; h++) {
//                        for (int mn = 0; mn <= 59; mn++) {
//                            for (int s = 0; s <= 50; s++) {
//                                ISOPeriod test = ISOPeriod.period(y, mo, d, h, mn, s);
//                                int hashCode = test.hashCode();
//                                if (hashCode >= 0) {
//                                    if (hashCode < pos.length) {
//                                        assertEquals(pos[hashCode], false);
//                                        pos[hashCode] = true;
//                                    }
//                                } else {
//                                    hashCode = -(hashCode + 1);
//                                    if (hashCode < neg.length) {
//                                        assertEquals(neg[hashCode], false);
//                                        neg[hashCode] = true;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
    public void test_hashCode_unique_workaroundSlowTest() {
        // spec requires unique hash codes
        // years 0-31, months 0-11, days 0-31, hours 0-23, minutes 0-59, seconds 0-59
        
        // -37 nanos removes the effect of the nanos
        int yearsBits = 0;
        for (int i = 0; i <= 31; i++) {
            ISOPeriod test = ISOPeriod.ofYears(i).withNanos(-37);
            yearsBits |= test.hashCode();
        }
        assertEquals(Integer.bitCount(yearsBits), 5);
        int monthsBits = 0;
        for (int i = 0; i <= 11; i++) {
            ISOPeriod test = ISOPeriod.ofMonths(i).withNanos(-37);
            monthsBits |= test.hashCode();
        }
        assertEquals(Integer.bitCount(monthsBits), 4);
        assertEquals(yearsBits & monthsBits, 0);
        int daysBits = 0;
        for (int i = 0; i <= 31; i++) {
            ISOPeriod test = ISOPeriod.ofDays(i).withNanos(-37);
            daysBits |= test.hashCode();
        }
        assertEquals(Integer.bitCount(daysBits), 5);
        assertEquals(yearsBits & monthsBits & daysBits, 0);
        int hoursBits = 0;
        for (int i = 0; i <= 23; i++) {
            ISOPeriod test = ISOPeriod.ofHours(i).withNanos(-37);
            hoursBits |= test.hashCode();
        }
        assertEquals(Integer.bitCount(hoursBits), 5);
        assertEquals(yearsBits & monthsBits & daysBits & hoursBits, 0);
        int minutesBits = 0;
        for (int i = 0; i <= 59; i++) {
            ISOPeriod test = ISOPeriod.ofMinutes(i).withNanos(-37);
            minutesBits |= test.hashCode();
        }
        assertEquals(Integer.bitCount(minutesBits), 6);
        assertEquals(yearsBits & minutesBits & daysBits & hoursBits & minutesBits, 0);
        int secondsBits = 0;
        for (int i = 0; i <= 59; i++) {
            ISOPeriod test = ISOPeriod.ofSeconds(i).withNanos(-37);
            secondsBits |= test.hashCode();
        }
        assertEquals(Integer.bitCount(secondsBits), 6);
        assertEquals(yearsBits & secondsBits & daysBits & hoursBits & minutesBits & secondsBits, 0);
        
        // make common overflows not same hash code
        assertTrue(ISOPeriod.ofMonths(16).hashCode() != ISOPeriod.ofYears(1).hashCode());
        assertTrue(ISOPeriod.ofDays(32).hashCode() != ISOPeriod.ofMonths(1).hashCode());
        assertTrue(ISOPeriod.ofHours(32).hashCode() != ISOPeriod.ofDays(1).hashCode());
        assertTrue(ISOPeriod.ofMinutes(64).hashCode() != ISOPeriod.ofHours(1).hashCode());
        assertTrue(ISOPeriod.ofSeconds(64).hashCode() != ISOPeriod.ofMinutes(1).hashCode());
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="toStringAndParse")
    Object[][] data_toString() {
        return new Object[][] {
            {ISOPeriod.ZERO, "PT0S"},
            {ISOPeriod.ofDays(0), "PT0S"},
            {ISOPeriod.ofYears(1), "P1Y"},
            {ISOPeriod.ofMonths(1), "P1M"},
            {ISOPeriod.ofDays(1), "P1D"},
            {ISOPeriod.ofHours(1), "PT1H"},
            {ISOPeriod.ofMinutes(1), "PT1M"},
            {ISOPeriod.ofSeconds(1), "PT1S"},
            {ISOPeriod.of(1, 2, 3, 4, 5, 6), "P1Y2M3DT4H5M6S"},
            {ISOPeriod.of(1, 2, 3, 4, 5, 6, 700000000), "P1Y2M3DT4H5M6.7S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, 100000000), "PT0.1S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, -100000000), "PT-0.1S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 1, -900000000), "PT0.1S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, -1, 900000000), "PT-0.1S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 1, 100000000), "PT1.1S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 1, -100000000), "PT0.9S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, -1, 100000000), "PT-0.9S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, -1, -100000000), "PT-1.1S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, 10000000), "PT0.01S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, -10000000), "PT-0.01S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, 1000000), "PT0.001S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, -1000000), "PT-0.001S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, 1000), "PT0.000001S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, -1000), "PT-0.000001S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, 1), "PT0.000000001S"},
            {ISOPeriod.of(0, 0, 0, 0, 0, 0, -1), "PT-0.000000001S"},
        };
    }

    @Test(dataProvider="toStringAndParse")
    public void test_toString(ISOPeriod test, String expected) {
        assertEquals(test.toString(), expected);
        assertSame(test.toString(), test.toString());  // repeat to check caching
    }

    //-----------------------------------------------------------------------
    private void assertPeriod(ISOPeriod test, int y, int mo, int d, int h, int mn, int s, long n) {
        assertEquals(test.getYears(), y);
        assertEquals(test.getMonths(), mo);
        assertEquals(test.getDays(), d);
        assertEquals(test.getHours(), h);
        assertEquals(test.getMinutes(), mn);
        assertEquals(test.getSeconds(), s);
        assertEquals(test.getNanos(), n);
    }

}


