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
package javax.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test UTCInstant.
 *
 * @author Stephen Colebourne
 */
@Test
public class TestUTCInstant {

    private static final long SECS_PER_DAY = 24L * 60 * 60;
    private static final long NANOS_PER_SEC = 1000000000L;

    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_interfaces() {
        assertTrue(Serializable.class.isAssignableFrom(Duration.class));
        assertTrue(Comparable.class.isAssignableFrom(Duration.class));
    }

    //-----------------------------------------------------------------------
    // serialization
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_deserialization() throws Exception {
        UTCInstant orginal = UTCInstant.ofModifiedJulianDay(2, 3);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(orginal);
        out.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        UTCInstant ser = (UTCInstant) in.readObject();
        assertEquals(UTCInstant.ofModifiedJulianDay(2, 3), ser);
    }

//    //-----------------------------------------------------------------------
//    // nowClock()
//    //-----------------------------------------------------------------------
//    @Test(expectedExceptions=NullPointerException.class)
//    public void now_Clock_nullClock() {
//        TAIInstant.now(null);
//    }
//
//    public void now_TimeSource_allSecsInDay_utc() {
//        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
//            TAIInstant expected = TAIInstant.ofEpochSecond(i).plusNanos(123456789L);
//            TimeSource clock = TimeSource.fixed(expected);
//            TAIInstant test = TAIInstant.now(clock);
//            assertEquals(test, expected);
//        }
//    }
//
//    public void now_TimeSource_allSecsInDay_beforeEpoch() {
//        for (int i =-1; i >= -(24 * 60 * 60); i--) {
//            TAIInstant expected = TAIInstant.ofEpochSecond(i).plusNanos(123456789L);
//            TimeSource clock = TimeSource.fixed(expected);
//            TAIInstant test = TAIInstant.now(clock);
//            assertEquals(test, expected);
//        }
//    }
//
//    //-----------------------------------------------------------------------
//    // nowSystemClock()
//    //-----------------------------------------------------------------------
//    public void nowSystemClock() {
//        TAIInstant expected = TAIInstant.now(TimeSource.system());
//        TAIInstant test = TAIInstant.nowSystemClock();
//        BigInteger diff = test.toEpochNano().subtract(expected.toEpochNano()).abs();
//        if (diff.compareTo(BigInteger.valueOf(100000000)) >= 0) {
//            // may be date change
//            expected = TAIInstant.now(TimeSource.system());
//            test = TAIInstant.nowSystemClock();
//            diff = test.toEpochNano().subtract(expected.toEpochNano()).abs();
//        }
//        assertTrue(diff.compareTo(BigInteger.valueOf(100000000)) < 0);  // less than 0.1 secs
//    }

    //-----------------------------------------------------------------------
    // ofModififiedJulianDay(long,long)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long() {
        for (long i = -2; i <= 2; i++) {
            for (int j = 0; j < 10; j++) {
                UTCInstant t = UTCInstant.ofModifiedJulianDay(i, j);
                assertEquals(t.getModifiedJulianDay(), i);
                assertEquals(t.getNanoOfDay(), j);
                assertEquals(t.getRules(), UTCRules.system());
                assertEquals(t.isLeapSecond(), false);
            }
        }
    }

    @Test(groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_setupLeap() {
        MockUTCRulesAlwaysLeap mockRules = new MockUTCRulesAlwaysLeap();
        UTCInstant t = UTCInstant.ofModifiedJulianDay(41683 - 1, SECS_PER_DAY * NANOS_PER_SEC + 2, mockRules);
        assertEquals(t.getModifiedJulianDay(), 41683 - 1);
        assertEquals(t.getNanoOfDay(), SECS_PER_DAY * NANOS_PER_SEC + 2);
        assertEquals(t.getRules(), mockRules);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_nanosNegative() {
        UTCInstant.ofModifiedJulianDay(2L, -1);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_nanosTooBigNotLeapDay() {
        UTCInstant.ofModifiedJulianDay(2L, SECS_PER_DAY * NANOS_PER_SEC);
    }

    //-----------------------------------------------------------------------
    // ofModififiedJulianDay(long,long,Rules)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_Rules() {
        MockUTCRulesAlwaysLeap mockRules = new MockUTCRulesAlwaysLeap();
        for (long i = -2; i <= 2; i++) {
            for (int j = 0; j < 10; j++) {
                UTCInstant t = UTCInstant.ofModifiedJulianDay(i, j, mockRules);
                assertEquals(t.getModifiedJulianDay(), i);
                assertEquals(t.getNanoOfDay(), j);
                assertEquals(t.getRules(), mockRules);
                assertEquals(t.isLeapSecond(), false);
            }
        }
    }

    @Test(groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_Rules_setupLeap() {
        MockUTCRulesAlwaysLeap mockRules = new MockUTCRulesAlwaysLeap();
        UTCInstant t = UTCInstant.ofModifiedJulianDay(0, SECS_PER_DAY * NANOS_PER_SEC + 2, mockRules);
        assertEquals(t.getModifiedJulianDay(), 0);
        assertEquals(t.getNanoOfDay(), SECS_PER_DAY * NANOS_PER_SEC + 2);
        assertEquals(t.getRules(), mockRules);
        assertEquals(t.isLeapSecond(), true);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_Rules_nanosNegative() {
        MockUTCRulesAlwaysLeap mockRules = new MockUTCRulesAlwaysLeap();
        UTCInstant.ofModifiedJulianDay(2L, -1, mockRules);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_Rules_nanosTooBigNotDoubleLeapDay() {
        MockUTCRulesAlwaysLeap mockRules = new MockUTCRulesAlwaysLeap();
        UTCInstant.ofModifiedJulianDay(2L, (SECS_PER_DAY + 1) * NANOS_PER_SEC, mockRules);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_ofModifiedJulianDay_long_long_Rules_null() {
        UTCInstant.ofModifiedJulianDay(0, 0, (UTCRules) null);
    }

    //-----------------------------------------------------------------------
    // of(Instant)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_Instant() {
        UTCInstant test = UTCInstant.of(Instant.ofEpochSecond(0, 2));  // 1970-01-01
        assertEquals(test.getModifiedJulianDay(), 40587);
        assertEquals(test.getNanoOfDay(), 2);
        assertEquals(test.getRules(), UTCRules.system());
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_Instant_null() {
        UTCInstant.of((Instant) null);
    }

    //-----------------------------------------------------------------------
    // of(Instant, LeapSecondRules)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_Instant_Rules() {
        MockUTCRulesAlwaysLeap mockRules = new MockUTCRulesAlwaysLeap();
        UTCInstant test = UTCInstant.of(Instant.ofEpochSecond(0, 2), mockRules);  // 1970-01-01
        assertEquals(test.getModifiedJulianDay(), 40587);
        assertEquals(test.getNanoOfDay(), 2);
        assertEquals(test.getRules(), mockRules);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_Instant_Rules_null() {
        UTCInstant.of(Instant.ofEpochSecond(0, 2), (UTCRules) null);
    }

    //-----------------------------------------------------------------------
    // of(TAIInstant)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_TAIInstant() {
        for (int i = -1000; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                UTCInstant expected = UTCInstant.ofModifiedJulianDay(36204 + i, j * NANOS_PER_SEC + 2L);
                TAIInstant tai = TAIInstant.ofTAISeconds(i * SECS_PER_DAY + j + 10, 2);
                assertEquals(UTCInstant.of(tai), expected);
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_TAIInstant_null() {
        UTCInstant.of((TAIInstant) null);
    }

    //-----------------------------------------------------------------------
    // of(TAIInstant, LeapSecondRules)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void factory_of_TAIInstant_Rules() {
        TAIInstant tai = TAIInstant.ofTAISeconds(2 * SECS_PER_DAY + 10, 2);
        UTCInstant test = UTCInstant.of(tai, UTCRules.system());
        assertEquals(test.getModifiedJulianDay(), 36204 + 2);
        assertEquals(test.getNanoOfDay(), 2);
        assertEquals(test.getRules(), UTCRules.system());
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void factory_of_TAIInstant_Rules_null() {
        UTCInstant.of(TAIInstant.ofTAISeconds(0, 2), (UTCRules) null);
    }

    //-----------------------------------------------------------------------
    // withModifiedJulianDay()
    //-----------------------------------------------------------------------
    @DataProvider(name="WithModifiedJulianDay")
    Object[][] provider_withModifiedJulianDay() {
        return new Object[][] {
            {0L, 12345L,  1L, 1L, 12345L},
            {0L, 12345L,  -1L, -1L, 12345L},
            {7L, 12345L,  2L, 2L, 12345L},
            {7L, 12345L,  -2L, -2L, 12345L},
            {-99L, 12345L,  3L, 3L, 12345L},
            {-99L, 12345L,  -3L, -3L, 12345L},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  999L, null, null},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  1000L, 1000L, NANOS_PER_SEC * SECS_PER_DAY},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  1001L, null, null},
       };
    }

    @Test(dataProvider="WithModifiedJulianDay") 
    public void test_withModifiedJulianDay(long mjd, long nanos, long newMjd, Long expectedMjd, Long expectedNanos) {
        UTCInstant i = UTCInstant.ofModifiedJulianDay(mjd, nanos, new MockUTCRulesLeapOn1000());
        if (expectedMjd != null) {
            i = i.withModifiedJulianDay(newMjd);
            assertEquals(i.getModifiedJulianDay(), expectedMjd.longValue());
            assertEquals(i.getNanoOfDay(), expectedNanos.longValue());
        } else {
            try {
                i = i.withModifiedJulianDay(newMjd);
                fail();
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
     }

    //-----------------------------------------------------------------------
    // withNanoOfDay()
    //-----------------------------------------------------------------------
    @DataProvider(name="WithNanoOfDay")
    Object[][] provider_withNanoOfDay() {
        return new Object[][] {
            {0L, 12345L,  1L, 0L, 1L},
            {0L, 12345L,  -1L, null, null},
            {7L, 12345L,  2L, 7L, 2L},
            {-99L, 12345L,  3L, -99L, 3L},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  NANOS_PER_SEC * SECS_PER_DAY - 1, 1000L, NANOS_PER_SEC * SECS_PER_DAY - 1},
       };
    }

    @Test(dataProvider="WithNanoOfDay", groups={"tck"})
    public void test_withNanoOfDay(long mjd, long nanos, long newNanoOfDay, Long expectedMjd, Long expectedNanos) {
        UTCInstant i = UTCInstant.ofModifiedJulianDay(mjd, nanos, new MockUTCRulesLeapOn1000());
        if (expectedMjd != null) {
            i = i.withNanoOfDay(newNanoOfDay);
            assertEquals(i.getModifiedJulianDay(), expectedMjd.longValue());
            assertEquals(i.getNanoOfDay(), expectedNanos.longValue());
        } else {
            try {
                i = i.withNanoOfDay(newNanoOfDay);
                fail();
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
     }

    //-----------------------------------------------------------------------
    // plus(Duration)
    //-----------------------------------------------------------------------
    @DataProvider(name="Plus")
    Object[][] provider_plus() {
        return new Object[][] {
            {0, 0,  -2 * SECS_PER_DAY, 5, -2, 5},
            {0, 0,  -1 * SECS_PER_DAY, 1, -1, 1},
            {0, 0,  -1 * SECS_PER_DAY, 0, -1, 0},
            {0, 0,  0,        -2, -1,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {0, 0,  0,        -1, -1,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {0, 0,  0,         0,  0,  0},
            {0, 0,  0,         1,  0,  1},
            {0, 0,  0,         2,  0,  2},
            {0, 0,  1,         0,  0,  1 * NANOS_PER_SEC},
            {0, 0,  2,         0,  0,  2 * NANOS_PER_SEC},
            {0, 0,  3, 333333333,  0,  3 * NANOS_PER_SEC + 333333333},
            {0, 0,  1 * SECS_PER_DAY, 0,  1, 0},
            {0, 0,  1 * SECS_PER_DAY, 1,  1, 1},
            {0, 0,  2 * SECS_PER_DAY, 5,  2, 5},
            
            {1, 0,  -2 * SECS_PER_DAY, 5, -1, 5},
            {1, 0,  -1 * SECS_PER_DAY, 1, 0, 1},
            {1, 0,  -1 * SECS_PER_DAY, 0, 0, 0},
            {1, 0,  0,        -2,  0,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {1, 0,  0,        -1,  0,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {1, 0,  0,         0,  1,  0},
            {1, 0,  0,         1,  1,  1},
            {1, 0,  0,         2,  1,  2},
            {1, 0,  1,         0,  1,  1 * NANOS_PER_SEC},
            {1, 0,  2,         0,  1,  2 * NANOS_PER_SEC},
            {1, 0,  3, 333333333,  1,  3 * NANOS_PER_SEC + 333333333},
            {1, 0,  1 * SECS_PER_DAY, 0,  2, 0},
            {1, 0,  1 * SECS_PER_DAY, 1,  2, 1},
            {1, 0,  2 * SECS_PER_DAY, 5,  3, 5},
       };
    }
    
    @Test(dataProvider="Plus", groups={"tck"})
    public void test_plus(long mjd, long nanos, long plusSeconds, int plusNanos, long expectedMjd, long expectedNanos) {
       UTCInstant i = UTCInstant.ofModifiedJulianDay(mjd, nanos).plus(Duration.ofSeconds(plusSeconds, plusNanos));
       assertEquals(i.getModifiedJulianDay(), expectedMjd);
       assertEquals(i.getNanoOfDay(), expectedNanos);
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_plus_overflowTooBig() {
       UTCInstant i = UTCInstant.ofModifiedJulianDay(Long.MAX_VALUE, SECS_PER_DAY * NANOS_PER_SEC - 1);
       i.plus(Duration.ofNanos(1));
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_plus_overflowTooSmall() {
       UTCInstant i = UTCInstant.ofModifiedJulianDay(Long.MIN_VALUE, 0);
       i.plus(Duration.ofNanos(-1));
    }

    //-----------------------------------------------------------------------
    // minus(Duration)
    //-----------------------------------------------------------------------
    @DataProvider(name="Minus")
    Object[][] provider_minus() {
        return new Object[][] {
            {0, 0,  2 * SECS_PER_DAY, -5, -2, 5},
            {0, 0,  1 * SECS_PER_DAY, -1, -1, 1},
            {0, 0,  1 * SECS_PER_DAY, 0, -1, 0},
            {0, 0,  0,          2, -1,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {0, 0,  0,          1, -1,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {0, 0,  0,          0,  0,  0},
            {0, 0,  0,         -1,  0,  1},
            {0, 0,  0,         -2,  0,  2},
            {0, 0,  -1,         0,  0,  1 * NANOS_PER_SEC},
            {0, 0,  -2,         0,  0,  2 * NANOS_PER_SEC},
            {0, 0,  -3, -333333333,  0,  3 * NANOS_PER_SEC + 333333333},
            {0, 0,  -1 * SECS_PER_DAY, 0,  1, 0},
            {0, 0,  -1 * SECS_PER_DAY, -1,  1, 1},
            {0, 0,  -2 * SECS_PER_DAY, -5,  2, 5},
            
            {1, 0,  2 * SECS_PER_DAY, -5, -1, 5},
            {1, 0,  1 * SECS_PER_DAY, -1, 0, 1},
            {1, 0,  1 * SECS_PER_DAY, 0, 0, 0},
            {1, 0,  0,          2,  0,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {1, 0,  0,          1,  0,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {1, 0,  0,          0,  1,  0},
            {1, 0,  0,         -1,  1,  1},
            {1, 0,  0,         -2,  1,  2},
            {1, 0,  -1,         0,  1,  1 * NANOS_PER_SEC},
            {1, 0,  -2,         0,  1,  2 * NANOS_PER_SEC},
            {1, 0,  -3, -333333333,  1,  3 * NANOS_PER_SEC + 333333333},
            {1, 0,  -1 * SECS_PER_DAY, 0,  2, 0},
            {1, 0,  -1 * SECS_PER_DAY, -1,  2, 1},
            {1, 0,  -2 * SECS_PER_DAY, -5,  3, 5},
       };
    }
    
    @Test(dataProvider="Minus", groups={"tck"})
    public void test_minus(long mjd, long nanos, long minusSeconds, int minusNanos, long expectedMjd, long expectedNanos) {
       UTCInstant i = UTCInstant.ofModifiedJulianDay(mjd, nanos).minus(Duration.ofSeconds(minusSeconds, minusNanos));
       assertEquals(i.getModifiedJulianDay(), expectedMjd);
       assertEquals(i.getNanoOfDay(), expectedNanos);
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_minus_overflowTooSmall() {
       UTCInstant i = UTCInstant.ofModifiedJulianDay(Long.MIN_VALUE, 0);
       i.minus(Duration.ofNanos(1));
    }

    @Test(expectedExceptions=ArithmeticException.class, groups={"tck"})
    public void test_minus_overflowTooBig() {
       UTCInstant i = UTCInstant.ofModifiedJulianDay(Long.MAX_VALUE, SECS_PER_DAY * NANOS_PER_SEC - 1);
       i.minus(Duration.ofNanos(-1));
    }

    //-----------------------------------------------------------------------
    // durationUntil()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_durationUntil_oneDayNoLeap() {
        UTCInstant utc1 = UTCInstant.ofModifiedJulianDay(41681, 0);  // 1972-12-30
        UTCInstant utc2 = UTCInstant.ofModifiedJulianDay(41682, 0);  // 1972-12-31
        Duration test = utc1.durationUntil(utc2);
        assertEquals(test.getSeconds(), 86400);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    @Test(groups={"tck"})
    public void test_durationUntil_oneDayLeap() {
        UTCInstant utc1 = UTCInstant.ofModifiedJulianDay(41682, 0);  // 1972-12-31
        UTCInstant utc2 = UTCInstant.ofModifiedJulianDay(41683, 0);  // 1973-01-01
        Duration test = utc1.durationUntil(utc2);
        assertEquals(test.getSeconds(), 86401);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    @Test(groups={"tck"})
    public void test_durationUntil_oneDayLeapNegative() {
        UTCInstant utc1 = UTCInstant.ofModifiedJulianDay(41683, 0);  // 1973-01-01
        UTCInstant utc2 = UTCInstant.ofModifiedJulianDay(41682, 0);  // 1972-12-31
        Duration test = utc1.durationUntil(utc2);
        assertEquals(test.getSeconds(), -86401);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    //-----------------------------------------------------------------------
    // toTAIInstant()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_toTAIInstant() {
        for (int i = -1000; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                UTCInstant utc = UTCInstant.ofModifiedJulianDay(36204 + i, j * NANOS_PER_SEC + 2L);
                TAIInstant test = utc.toTAIInstant();
                assertEquals(test.getTAISeconds(), i * SECS_PER_DAY + j + 10);
                assertEquals(test.getNanoOfSecond(), 2);
            }
        }
    }

    //-----------------------------------------------------------------------
    // toInstant()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_toInstant() {
        for (int i = -1000; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                Instant expected = Instant.ofEpochSecond(315532800 + i * SECS_PER_DAY + j).plusNanos(2);
                UTCInstant test = UTCInstant.ofModifiedJulianDay(44239 + i, j * NANOS_PER_SEC + 2);
                assertEquals(test.toInstant(), expected, "Loop " + i + " " + j);
            }
        }
    }

    //-----------------------------------------------------------------------
    // compareTo()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_comparisons() {
        doTest_comparisons_UTCInstant(
            UTCInstant.ofModifiedJulianDay(-2L, 0),
            UTCInstant.ofModifiedJulianDay(-2L, SECS_PER_DAY * NANOS_PER_SEC - 2),
            UTCInstant.ofModifiedJulianDay(-2L, SECS_PER_DAY * NANOS_PER_SEC - 1),
            UTCInstant.ofModifiedJulianDay(-1L, 0),
            UTCInstant.ofModifiedJulianDay(-1L, 1),
            UTCInstant.ofModifiedJulianDay(-1L, SECS_PER_DAY * NANOS_PER_SEC - 2),
            UTCInstant.ofModifiedJulianDay(-1L, SECS_PER_DAY * NANOS_PER_SEC - 1),
            UTCInstant.ofModifiedJulianDay(0L, 0),
            UTCInstant.ofModifiedJulianDay(0L, 1),
            UTCInstant.ofModifiedJulianDay(0L, 2),
            UTCInstant.ofModifiedJulianDay(0L, SECS_PER_DAY * NANOS_PER_SEC - 1),
            UTCInstant.ofModifiedJulianDay(1L, 0),
            UTCInstant.ofModifiedJulianDay(2L, 0)
        );
    }

    void doTest_comparisons_UTCInstant(UTCInstant... instants) {
        for (int i = 0; i < instants.length; i++) {
            UTCInstant a = instants[i];
            for (int j = 0; j < instants.length; j++) {
                UTCInstant b = instants[j];
                if (i < j) {
                    assertEquals(a.compareTo(b), -1, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else if (i > j) {
                    assertEquals(a.compareTo(b), 1, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else {
                    assertEquals(a.compareTo(b), 0, a + " <=> " + b);
                    assertEquals(a.equals(b), true, a + " <=> " + b);
                }
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_compareTo_ObjectNull() {
        UTCInstant a = UTCInstant.ofModifiedJulianDay(0L, 0);
        a.compareTo(null);
    }

    @Test(expectedExceptions=ClassCastException.class, groups={"tck"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void test_compareToNonUTCInstant() {
       Comparable c = UTCInstant.ofModifiedJulianDay(0L, 2);
       c.compareTo(new Object());
    }

    //-----------------------------------------------------------------------
    // equals()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_equals() {
        UTCInstant test5a = UTCInstant.ofModifiedJulianDay(5L, 20);
        UTCInstant test5b = UTCInstant.ofModifiedJulianDay(5L, 20);
        UTCInstant test5n = UTCInstant.ofModifiedJulianDay(5L, 30);
        UTCInstant test6 = UTCInstant.ofModifiedJulianDay(6L, 20);
        
        assertEquals(test5a.equals(test5a), true);
        assertEquals(test5a.equals(test5b), true);
        assertEquals(test5a.equals(test5n), false);
        assertEquals(test5a.equals(test6), false);
        
        assertEquals(test5b.equals(test5a), true);
        assertEquals(test5b.equals(test5b), true);
        assertEquals(test5b.equals(test5n), false);
        assertEquals(test5b.equals(test6), false);
        
        assertEquals(test5n.equals(test5a), false);
        assertEquals(test5n.equals(test5b), false);
        assertEquals(test5n.equals(test5n), true);
        assertEquals(test5n.equals(test6), false);
        
        assertEquals(test6.equals(test5a), false);
        assertEquals(test6.equals(test5b), false);
        assertEquals(test6.equals(test5n), false);
        assertEquals(test6.equals(test6), true);
    }

    @Test(groups={"tck"})
    public void test_equals_null() {
        UTCInstant test5 = UTCInstant.ofModifiedJulianDay(5L, 20);
        assertEquals(test5.equals(null), false);
    }

    @Test(groups={"tck"})
    public void test_equals_otherClass() {
        UTCInstant test5 = UTCInstant.ofModifiedJulianDay(5L, 20);
        assertEquals(test5.equals(""), false);
    }

    //-----------------------------------------------------------------------
    // hashCode()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_hashCode() {
        UTCInstant test5a = UTCInstant.ofModifiedJulianDay(5L, 20);
        UTCInstant test5b = UTCInstant.ofModifiedJulianDay(5L, 20);
        UTCInstant test5n = UTCInstant.ofModifiedJulianDay(5L, 30);
        UTCInstant test6 = UTCInstant.ofModifiedJulianDay(6L, 20);
        
        assertEquals(test5a.hashCode() == test5a.hashCode(), true);
        assertEquals(test5a.hashCode() == test5b.hashCode(), true);
        assertEquals(test5b.hashCode() == test5b.hashCode(), true);
        
        assertEquals(test5a.hashCode() == test5n.hashCode(), false);
        assertEquals(test5a.hashCode() == test6.hashCode(), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_toString() {
        assertEquals(UTCInstant.ofModifiedJulianDay(40587, 0).toString(), "1970-01-01T00:00:00.000000000(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(40588, 1).toString(), "1970-01-02T00:00:00.000000001(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(40618, 999999999).toString(), "1970-02-01T00:00:00.999999999(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(40619, 1000000000).toString(), "1970-02-02T00:00:01.000000000(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(40620, 60L * 1000000000L).toString(), "1970-02-03T00:01:00.000000000(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(40621, 60L * 60L * 1000000000L).toString(), "1970-02-04T01:00:00.000000000(UTC)");
    }

    @Test(groups={"tck"})
    public void test_toString_leap() {
        assertEquals(UTCInstant.ofModifiedJulianDay(41682, 24L * 60L * 60L * 1000000000L - 1000000000L).toString(), "1972-12-31T23:59:59.000000000(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(41682, 24L * 60L * 60L * 1000000000L).toString(), "1972-12-31T23:59:60.000000000(UTC)");
        assertEquals(UTCInstant.ofModifiedJulianDay(41683, 0).toString(), "1973-01-01T00:00:00.000000000(UTC)");
    }

}
