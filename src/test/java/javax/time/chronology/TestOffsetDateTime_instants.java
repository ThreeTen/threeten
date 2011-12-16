/*
 * Copyright (c) 2008-2011, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.chronology;

import javax.time.chronology.IllegalCalendarFieldValueException;
import javax.time.Year;
import javax.time.MonthOfYear;
import javax.time.ZoneOffset;
import javax.time.OffsetDateTime;
import javax.time.LocalDate;
import static org.testng.Assert.assertEquals;

import javax.time.Instant;

import org.testng.annotations.Test;

/**
 * Test OffsetDate creation.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestOffsetDateTime_instants {

    private static final ZoneOffset OFFSET_PONE = ZoneOffset.ofHours(1);
    private static final ZoneOffset OFFSET_MAX = ZoneOffset.ofHours(18);
    private static final ZoneOffset OFFSET_MIN = ZoneOffset.ofHours(-18);

    //-----------------------------------------------------------------------
    private void check(OffsetDateTime test, int y, int mo, int d, int h, int m, int s, int n, ZoneOffset offset) {
        assertEquals(test.getYear(), y);
        assertEquals(test.getMonthOfYear().getValue(), mo);
        assertEquals(test.getDayOfMonth(), d);
        assertEquals(test.getHourOfDay(), h);
        assertEquals(test.getMinuteOfHour(), m);
        assertEquals(test.getSecondOfMinute(), s);
        assertEquals(test.getNanoOfSecond(), n);
        assertEquals(test.getOffset(), offset);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class)
    public void factoryUTC_ofInstant_InstantProvider_nullInstant() {
        OffsetDateTime.ofInstantUTC((Instant) null);
    }

    public void factoryUTC_ofInstant_InstantProvider() {
        Instant instant = Instant.ofEpochSecond(86400 + 5 * 3600 + 10 * 60 + 20);
        OffsetDateTime test = OffsetDateTime.ofInstantUTC(instant);
        check(test, 1970, 1, 2, 5, 10, 20, 0, ZoneOffset.UTC);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class)
    public void factory_ofInstant_InstantProvider_nullInstant() {
        OffsetDateTime.ofInstant((Instant) null, OFFSET_PONE);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_ofInstant_InstantProvider_nullOffset() {
        Instant instant = Instant.ofEpochSecond(0L);
        OffsetDateTime.ofInstant(instant, (ZoneOffset) null);
    }

    public void factory_ofInstant_InstantProvider_allSecsInDay() {
        for (int i = 0; i < (24 * 60 * 60); i++) {
            Instant instant = Instant.ofEpochSecond(i);
            OffsetDateTime test = OffsetDateTime.ofInstant(instant, OFFSET_PONE);
            assertEquals(test.getYear(), 1970);
            assertEquals(test.getMonthOfYear(), MonthOfYear.JANUARY);
            assertEquals(test.getDayOfMonth(), 1 + (i >= 23 * 60 * 60 ? 1 : 0));
            assertEquals(test.getHourOfDay(), ((i / (60 * 60)) + 1) % 24);
            assertEquals(test.getMinuteOfHour(), (i / 60) % 60);
            assertEquals(test.getSecondOfMinute(), i % 60);
        }
    }

    public void factory_ofInstant_InstantProvider_allDaysInCycle() {
        // sanity check using different algorithm
        OffsetDateTime expected = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        for (long i = 0; i < 146097; i++) {
            Instant instant = Instant.ofEpochSecond(i * 24L * 60L * 60L);
            OffsetDateTime test = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
            assertEquals(test, expected);
            expected = expected.plusDays(1);
        }
    }

    public void factory_ofInstant_InstantProvider_history() {
//        long start = System.currentTimeMillis();
        doTest_factory_ofInstant_InstantProvider_all(-2820, 2820);
//        long end = System.currentTimeMillis();
//        System.err.println(end - start);
    }

    //-----------------------------------------------------------------------
    public void factory_ofInstant_InstantProvider_minYear() {
        doTest_factory_ofInstant_InstantProvider_all(Year.MIN_YEAR, Year.MIN_YEAR + 420);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_ofInstant_InstantProvider_tooLow() {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        int year = Year.MIN_YEAR - 1;
        long days = (year * 365L + (year / 4 - year / 100 + year / 400)) - days_0000_to_1970;
        Instant instant = Instant.ofEpochSecond(days * 24L * 60L * 60L);
        OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public void factory_ofInstant_InstantProvider_maxYear() {
        doTest_factory_ofInstant_InstantProvider_all(Year.MAX_YEAR - 420, Year.MAX_YEAR);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_ofInstant_InstantProvider_tooBig() {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        long year = Year.MAX_YEAR + 1L;
        long days = (year * 365L + (year / 4 - year / 100 + year / 400)) - days_0000_to_1970;
        Instant instant = Instant.ofEpochSecond(days * 24L * 60L * 60L);
        OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    //-----------------------------------------------------------------------
    public void factory_ofInstant_InstantProvider_minWithMinOffset() {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        int year = Year.MIN_YEAR;
        long days = (year * 365L + (year / 4 - year / 100 + year / 400)) - days_0000_to_1970;
        Instant instant = Instant.ofEpochSecond(days * 24L * 60L * 60L - OFFSET_MIN.getAmountSeconds());
        OffsetDateTime test = OffsetDateTime.ofInstant(instant, OFFSET_MIN);
        assertEquals(test.getYear(), Year.MIN_YEAR);
        assertEquals(test.getMonthOfYear().getValue(), 1);
        assertEquals(test.getDayOfMonth(), 1);
        assertEquals(test.getOffset(), OFFSET_MIN);
        assertEquals(test.getHourOfDay(), 0);
        assertEquals(test.getMinuteOfHour(), 0);
        assertEquals(test.getSecondOfMinute(), 0);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    public void factory_ofInstant_InstantProvider_minWithMaxOffset() {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        int year = Year.MIN_YEAR;
        long days = (year * 365L + (year / 4 - year / 100 + year / 400)) - days_0000_to_1970;
        Instant instant = Instant.ofEpochSecond(days * 24L * 60L * 60L - OFFSET_MAX.getAmountSeconds());
        OffsetDateTime test = OffsetDateTime.ofInstant(instant, OFFSET_MAX);
        assertEquals(test.getYear(), Year.MIN_YEAR);
        assertEquals(test.getMonthOfYear().getValue(), 1);
        assertEquals(test.getDayOfMonth(), 1);
        assertEquals(test.getOffset(), OFFSET_MAX);
        assertEquals(test.getHourOfDay(), 0);
        assertEquals(test.getMinuteOfHour(), 0);
        assertEquals(test.getSecondOfMinute(), 0);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    public void factory_ofInstant_InstantProvider_maxWithMinOffset() {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        int year = Year.MAX_YEAR;
        long days = (year * 365L + (year / 4 - year / 100 + year / 400)) + 365 - days_0000_to_1970;
        Instant instant = Instant.ofEpochSecond((days + 1) * 24L * 60L * 60L - 1 - OFFSET_MIN.getAmountSeconds());
        OffsetDateTime test = OffsetDateTime.ofInstant(instant, OFFSET_MIN);
        assertEquals(test.getYear(), Year.MAX_YEAR);
        assertEquals(test.getMonthOfYear().getValue(), 12);
        assertEquals(test.getDayOfMonth(), 31);
        assertEquals(test.getOffset(), OFFSET_MIN);
        assertEquals(test.getHourOfDay(), 23);
        assertEquals(test.getMinuteOfHour(), 59);
        assertEquals(test.getSecondOfMinute(), 59);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    public void factory_ofInstant_InstantProvider_maxWithMaxOffset() {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        int year = Year.MAX_YEAR;
        long days = (year * 365L + (year / 4 - year / 100 + year / 400)) + 365 - days_0000_to_1970;
        Instant instant = Instant.ofEpochSecond((days + 1) * 24L * 60L * 60L - 1 - OFFSET_MAX.getAmountSeconds());
        OffsetDateTime test = OffsetDateTime.ofInstant(instant, OFFSET_MAX);
        assertEquals(test.getYear(), Year.MAX_YEAR);
        assertEquals(test.getMonthOfYear().getValue(), 12);
        assertEquals(test.getDayOfMonth(), 31);
        assertEquals(test.getOffset(), OFFSET_MAX);
        assertEquals(test.getHourOfDay(), 23);
        assertEquals(test.getMinuteOfHour(), 59);
        assertEquals(test.getSecondOfMinute(), 59);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_ofInstant_InstantProvider_maxInstantWithMaxOffset() {
        Instant instant = Instant.ofEpochSecond(Long.MAX_VALUE);
        OffsetDateTime.ofInstant(instant, OFFSET_MAX);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_ofInstant_InstantProvider_maxInstantWithMinOffset() {
        Instant instant = Instant.ofEpochSecond(Long.MAX_VALUE);
        OffsetDateTime.ofInstant(instant, OFFSET_MIN);
    }

    //-----------------------------------------------------------------------
    private void doTest_factory_ofInstant_InstantProvider_all(long minYear, long maxYear) {
        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
        int minOffset = (minYear <= 0 ? 0 : 3);
        int maxOffset = (maxYear <= 0 ? 0 : 3);
        long minDays = (minYear * 365L + ((minYear + minOffset) / 4L - (minYear + minOffset) / 100L + (minYear + minOffset) / 400L)) - days_0000_to_1970;
        long maxDays = (maxYear * 365L + ((maxYear + maxOffset) / 4L - (maxYear + maxOffset) / 100L + (maxYear + maxOffset) / 400L)) + 365L - days_0000_to_1970;
        
        final LocalDate maxDate = LocalDate.of(Year.MAX_YEAR, 12, 31);
        OffsetDateTime expected = OffsetDateTime.of((int) minYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        for (long i = minDays; i < maxDays; i++) {
            Instant instant = Instant.ofEpochSecond(i * 24L * 60L * 60L);
            try {
                OffsetDateTime test = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
                assertEquals(test, expected);
                if (expected.toLocalDate().equals(maxDate) == false) {
                    expected = expected.plusDays(1);
                }
            } catch (RuntimeException ex) {
                System.out.println("Error: " + i + " " + expected);
                throw ex;
            } catch (Error ex) {
                System.out.println("Error: " + i + " " + expected);
                throw ex;
            }
        }
    }

    // for performance testing
//    private void doTest_factory_ofInstant_InstantProvider_all(int minYear, int maxYear) {
//        long days_0000_to_1970 = (146097 * 5) - (30 * 365 + 7);
//        int minOffset = (minYear <= 0 ? 0 : 3);
//        int maxOffset = (maxYear <= 0 ? 0 : 3);
//        long minDays = (long) (minYear * 365L + ((minYear + minOffset) / 4L - (minYear + minOffset) / 100L + (minYear + minOffset) / 400L)) - days_0000_to_1970;
//        long maxDays = (long) (maxYear * 365L + ((maxYear + maxOffset) / 4L - (maxYear + maxOffset) / 100L + (maxYear + maxOffset) / 400L)) + 365L - days_0000_to_1970;
//        
//        OffsetDateTime expected = OffsetDateTime.dateTime(minYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
//        Date cutover = new Date(Long.MIN_VALUE);
//        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
//        cal.setGregorianChange(cutover);
//        for (long i = minDays; i < maxDays; i++) {
//            Instant instant = Instant.instant(i * 24L * 60L * 60L);
//            try {
//                cal.setTimeInMillis(instant.getEpochSecond() * 1000L);
//                assertEquals(cal.get(GregorianCalendar.MONTH), expected.getMonthOfYear().getValue() - 1);
//                assertEquals(cal.get(GregorianCalendar.DAY_OF_MONTH), expected.getDayOfMonth().getValue());
//                expected = expected.plusDays(1);
//            } catch (RuntimeException ex) {
//                System.out.println("Error: " + i + " " + expected);
//                throw ex;
//            } catch (Error ex) {
//                System.out.println("Error: " + i + " " + expected);
//                throw ex;
//            }
//        }
//    }

    //-----------------------------------------------------------------------
    public void factory_ofInstant_multiProvider_checkAmbiguous() {
        MockMultiProvider mmp = new MockMultiProvider(2008, 6, 30, 11, 30, 10, 500);
        OffsetDateTime test = OffsetDateTime.ofInstant(mmp, ZoneOffset.UTC);
        check(test, 2008, 6, 30, 11, 30, 10, 500, ZoneOffset.UTC);
    }

    //-----------------------------------------------------------------------
    public void test_toInstant_19700101() {
        OffsetDateTime dt = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        Instant test = dt.toInstant();
        assertEquals(test.getEpochSecond(), 0);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    public void test_toInstant_19700101_oneNano() {
        OffsetDateTime dt = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 1, ZoneOffset.UTC);
        Instant test = dt.toInstant();
        assertEquals(test.getEpochSecond(), 0);
        assertEquals(test.getNanoOfSecond(), 1);
    }

    public void test_toInstant_19700101_minusOneNano() {
        OffsetDateTime dt = OffsetDateTime.of(1969, 12, 31, 23, 59, 59, 999999999, ZoneOffset.UTC);
        Instant test = dt.toInstant();
        assertEquals(test.getEpochSecond(), -1);
        assertEquals(test.getNanoOfSecond(), 999999999);
    }

    public void test_toInstant_19700102() {
        OffsetDateTime dt = OffsetDateTime.of(1970, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        Instant test = dt.toInstant();
        assertEquals(test.getEpochSecond(), 24L * 60L * 60L);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    public void test_toInstant_19691231() {
        OffsetDateTime dt = OffsetDateTime.of(1969, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC);
        Instant test = dt.toInstant();
        assertEquals(test.getEpochSecond(), -24L * 60L * 60L);
        assertEquals(test.getNanoOfSecond(), 0);
    }

    //-----------------------------------------------------------------------
    public void test_toEpochSecond_19700101() {
        OffsetDateTime dt = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(dt.toEpochSecond(), 0);
    }

    public void test_toEpochSecond_19700101_oneNano() {
        OffsetDateTime dt = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 1, ZoneOffset.UTC);
        assertEquals(dt.toEpochSecond(), 0);
    }

    public void test_toEpochSecond_19700101_minusOneNano() {
        OffsetDateTime dt = OffsetDateTime.of(1969, 12, 31, 23, 59, 59, 999999999, ZoneOffset.UTC);
        assertEquals(dt.toEpochSecond(), -1);
    }

    public void test_toEpochSecond_19700102() {
        OffsetDateTime dt = OffsetDateTime.of(1970, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(dt.toEpochSecond(), 24L * 60L * 60L);
    }

    public void test_toEpochSecond_19691231() {
        OffsetDateTime dt = OffsetDateTime.of(1969, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(dt.toEpochSecond(), -24L * 60L * 60L);
    }

}
