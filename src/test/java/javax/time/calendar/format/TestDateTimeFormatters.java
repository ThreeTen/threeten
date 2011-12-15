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
package javax.time.calendar.format;

import static javax.time.calendar.ISODateTimeRule.DAY_OF_MONTH;
import static javax.time.calendar.ISODateTimeRule.DAY_OF_WEEK;
import static javax.time.calendar.ISODateTimeRule.DAY_OF_YEAR;
import static javax.time.calendar.ISODateTimeRule.HOUR_OF_DAY;
import static javax.time.calendar.ISODateTimeRule.MINUTE_OF_HOUR;
import static javax.time.calendar.ISODateTimeRule.MONTH_OF_YEAR;
import static javax.time.calendar.ISODateTimeRule.NANO_OF_SECOND;
import static javax.time.calendar.ISODateTimeRule.SECOND_OF_MINUTE;
import static javax.time.calendar.ISODateTimeRule.WEEK_BASED_YEAR;
import static javax.time.calendar.ISODateTimeRule.WEEK_OF_WEEK_BASED_YEAR;
import static javax.time.calendar.ISODateTimeRule.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import javax.time.CalendricalException;
import javax.time.calendar.Calendrical;
import javax.time.calendar.CalendricalRule;
import javax.time.calendar.CalendricalRuleException;
import javax.time.calendar.DateTimeFields;
import javax.time.calendar.DateTimeRule;
import javax.time.LocalDate;
import javax.time.LocalDateTime;
import javax.time.OffsetDateTime;
import javax.time.Year;
import javax.time.YearMonth;
import javax.time.ZoneId;
import javax.time.ZoneOffset;
import javax.time.ZonedDateTime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test DateTimeFormatters.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestDateTimeFormatters {

    @BeforeMethod
    public void setUp() {
    }

    @SuppressWarnings("rawtypes")
    public void test_constructor() throws Exception {
        for (Constructor constructor : DateTimeFormatters.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance(Collections.nCopies(constructor.getParameterTypes().length, null).toArray());
        }
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_nullCalendrical() {
        DateTimeFormatters.isoDate().print((Calendrical) null);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_pattern_String() {
        DateTimeFormatter test = DateTimeFormatters.pattern("d MMM yyyy");
        assertEquals(test.toString(), "Value(DayOfMonth)' 'Text(MonthOfYear,SHORT)' 'Value(Year,4,19,EXCEEDS_PAD)");
        assertEquals(test.getLocale(), Locale.getDefault());
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_pattern_String_invalid() {
        DateTimeFormatters.pattern("p");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_pattern_String_null() {
        DateTimeFormatters.pattern(null);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_pattern_StringLocale() {
        DateTimeFormatter test = DateTimeFormatters.pattern("d MMM yyyy", Locale.UK);
        assertEquals(test.toString(), "Value(DayOfMonth)' 'Text(MonthOfYear,SHORT)' 'Value(Year,4,19,EXCEEDS_PAD)");
        assertEquals(test.getLocale(), Locale.UK);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_pattern_StringLocale_invalid() {
        DateTimeFormatters.pattern("p", Locale.UK);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_pattern_StringLocale_nullPattern() {
        DateTimeFormatters.pattern(null, Locale.UK);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_pattern_StringLocale_nullLocale() {
        DateTimeFormatters.pattern("yyyy", null);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoLocalDate")
    Object[][] provider_sample_isoLocalDate() {
        return new Object[][]{
                {2008, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, CalendricalException.class},
                {null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, null, null, null, null, CalendricalException.class},
                {null, 6, 30, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, null, null,                   "2008-06-30", null},
                {2008, 6, 30, "+01:00", null,               "2008-06-30", null},
                {2008, 6, 30, "+01:00", "Europe/Paris",     "2008-06-30", null},
                {2008, 6, 30, null, "Europe/Paris",         "2008-06-30", null},
                
                {123456, 6, 30, null, null,                 "+123456-06-30", null},
        };
    }

    @Test(dataProvider="sample_isoLocalDate")
    public void test_print_isoLocalDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDate(year, month, day);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoLocalDate().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoLocalDate().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoLocalDate")
    public void test_parse_isoLocalDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDate(year, month, day);
            // offset/zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoLocalDate().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    @Test
    public void test_parse_isoLocalDate_999999999() {
        MockSimpleCalendrical expected = createDate(999999999, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToContext("+999999999-08-06", new ParsePosition(0)), expected);
        assertEquals(LocalDate.parse("+999999999-08-06"), LocalDate.of(999999999, 8, 6));
    }

    @Test
    public void test_parse_isoLocalDate_1000000000() {
        MockSimpleCalendrical expected = createDate(1000000000, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToContext("+1000000000-08-06", new ParsePosition(0)), expected);
    }

    @Test(expectedExceptions = CalendricalException.class)
    public void test_parse_isoLocalDate_1000000000_failedCreate() {
        LocalDate.parse("+1000000000-08-06");
    }

    @Test
    public void test_parse_isoLocalDate_M999999999() {
        MockSimpleCalendrical expected = createDate(-999999999, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToContext("-999999999-08-06", new ParsePosition(0)), expected);
        assertEquals(LocalDate.parse("-999999999-08-06"), LocalDate.of(-999999999, 8, 6));
    }

    @Test
    public void test_parse_isoLocalDate_M1000000000() {
        MockSimpleCalendrical expected = createDate(-1000000000, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToContext("-1000000000-08-06", new ParsePosition(0)), expected);
    }

    @Test(expectedExceptions = CalendricalException.class)
    public void test_parse_isoLocalDate_M1000000000_failedCreate() {
        LocalDate.parse("-1000000000-08-06");
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoOffsetDate")
    Object[][] provider_sample_isoOffsetDate() {
        return new Object[][]{
                {2008, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, CalendricalException.class},
                {null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, null, null, null, null, CalendricalException.class},
                {null, 6, 30, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, null, null,                   null, CalendricalException.class},
                {2008, 6, 30, "+01:00", null,               "2008-06-30+01:00", null},
                {2008, 6, 30, "+01:00", "Europe/Paris",     "2008-06-30+01:00", null},
                {2008, 6, 30, null, "Europe/Paris",         null, CalendricalException.class},
                
                {123456, 6, 30, "+01:00", null,             "+123456-06-30+01:00", null},
        };
    }

    @Test(dataProvider="sample_isoOffsetDate")
    public void test_print_isoOffsetDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDate(year, month, day);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoOffsetDate().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoOffsetDate().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoOffsetDate")
    public void test_parse_isoOffsetDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDate(year, month, day);
            buildCalendrical(expected, offsetId, null);  // zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoOffsetDate().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoDate")
    Object[][] provider_sample_isoDate() {
        return new Object[][]{
                {2008, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, CalendricalException.class},
                {null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, null, null, null, null, CalendricalException.class},
                {null, 6, 30, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, null, null,                   "2008-06-30", null},
                {2008, 6, 30, "+01:00", null,               "2008-06-30+01:00", null},
                {2008, 6, 30, "+01:00", "Europe/Paris",     "2008-06-30+01:00[Europe/Paris]", null},
                {2008, 6, 30, null, "Europe/Paris",         "2008-06-30", null},
                
                {123456, 6, 30, "+01:00", "Europe/Paris",   "+123456-06-30+01:00[Europe/Paris]", null},
        };
    }

    @Test(dataProvider="sample_isoDate")
    public void test_print_isoDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDate(year, month, day);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoDate().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoDate().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoDate")
    public void test_parse_isoDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDate(year, month, day);
            if (offsetId != null) {
                expected.put(ZoneOffset.rule(), ZoneOffset.of(offsetId));
                if (zoneId != null) {
                    expected.put(ZoneId.rule(), ZoneId.of(zoneId));
                }
            }
            assertParseMatch(DateTimeFormatters.isoDate().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoLocalTime")
    Object[][] provider_sample_isoLocalTime() {
        return new Object[][]{
                {11, null, null, null, null, null, null, CalendricalException.class},
                {null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, CalendricalException.class},
                {null, null, null, 1, null, null, null, CalendricalException.class},
                {null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                
                {11, 5, null, null, null, null,     "11:05", null},
                {11, 5, 30, null, null, null,       "11:05:30", null},
                {11, 5, 30, 500000000, null, null,  "11:05:30.5", null},
                {11, 5, 30, 1, null, null,          "11:05:30.000000001", null},
                
                {11, 5, null, null, "+01:00", null,     "11:05", null},
                {11, 5, 30, null, "+01:00", null,       "11:05:30", null},
                {11, 5, 30, 500000000, "+01:00", null,  "11:05:30.5", null},
                {11, 5, 30, 1, "+01:00", null,          "11:05:30.000000001", null},
                
                {11, 5, null, null, "+01:00", "Europe/Paris",       "11:05", null},
                {11, 5, 30, null, "+01:00", "Europe/Paris",         "11:05:30", null},
                {11, 5, 30, 500000000, "+01:00", "Europe/Paris",    "11:05:30.5", null},
                {11, 5, 30, 1, "+01:00", "Europe/Paris",            "11:05:30.000000001", null},
                
                {11, 5, null, null, null, "Europe/Paris",       "11:05", null},
                {11, 5, 30, null, null, "Europe/Paris",         "11:05:30", null},
                {11, 5, 30, 500000000, null, "Europe/Paris",    "11:05:30.5", null},
                {11, 5, 30, 1, null, "Europe/Paris",            "11:05:30.000000001", null},
        };
    }

    @Test(dataProvider="sample_isoLocalTime")
    public void test_print_isoLocalTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createTime(hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoLocalTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoLocalTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoLocalTime")
    public void test_parse_isoLocalTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createTime(hour, min, sec, nano);
            // offset/zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoLocalTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoOffsetTime")
    Object[][] provider_sample_isoOffsetTime() {
        return new Object[][]{
                {11, null, null, null, null, null, null, CalendricalException.class},
                {null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, CalendricalException.class},
                {null, null, null, 1, null, null, null, CalendricalException.class},
                {null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                
                {11, 5, null, null, null, null,     null, CalendricalException.class},
                {11, 5, 30, null, null, null,       null, CalendricalException.class},
                {11, 5, 30, 500000000, null, null,  null, CalendricalException.class},
                {11, 5, 30, 1, null, null,          null, CalendricalException.class},
                
                {11, 5, null, null, "+01:00", null,     "11:05+01:00", null},
                {11, 5, 30, null, "+01:00", null,       "11:05:30+01:00", null},
                {11, 5, 30, 500000000, "+01:00", null,  "11:05:30.5+01:00", null},
                {11, 5, 30, 1, "+01:00", null,          "11:05:30.000000001+01:00", null},
                
                {11, 5, null, null, "+01:00", "Europe/Paris",       "11:05+01:00", null},
                {11, 5, 30, null, "+01:00", "Europe/Paris",         "11:05:30+01:00", null},
                {11, 5, 30, 500000000, "+01:00", "Europe/Paris",    "11:05:30.5+01:00", null},
                {11, 5, 30, 1, "+01:00", "Europe/Paris",            "11:05:30.000000001+01:00", null},
                
                {11, 5, null, null, null, "Europe/Paris",       null, CalendricalException.class},
                {11, 5, 30, null, null, "Europe/Paris",         null, CalendricalException.class},
                {11, 5, 30, 500000000, null, "Europe/Paris",    null, CalendricalException.class},
                {11, 5, 30, 1, null, "Europe/Paris",            null, CalendricalException.class},
        };
    }

    @Test(dataProvider="sample_isoOffsetTime")
    public void test_print_isoOffsetTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createTime(hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoOffsetTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoOffsetTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoOffsetTime")
    public void test_parse_isoOffsetTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createTime(hour, min, sec, nano);
            buildCalendrical(expected, offsetId, null);  // zoneId is not expected from parse
            assertParseMatch(DateTimeFormatters.isoOffsetTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoTime")
    Object[][] provider_sample_isoTime() {
        return new Object[][]{
                {11, null, null, null, null, null, null, CalendricalException.class},
                {null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, CalendricalException.class},
                {null, null, null, 1, null, null, null, CalendricalException.class},
                {null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                
                {11, 5, null, null, null, null,     "11:05", null},
                {11, 5, 30, null, null, null,       "11:05:30", null},
                {11, 5, 30, 500000000, null, null,  "11:05:30.5", null},
                {11, 5, 30, 1, null, null,          "11:05:30.000000001", null},
                
                {11, 5, null, null, "+01:00", null,     "11:05+01:00", null},
                {11, 5, 30, null, "+01:00", null,       "11:05:30+01:00", null},
                {11, 5, 30, 500000000, "+01:00", null,  "11:05:30.5+01:00", null},
                {11, 5, 30, 1, "+01:00", null,          "11:05:30.000000001+01:00", null},
                
                {11, 5, null, null, "+01:00", "Europe/Paris",       "11:05+01:00[Europe/Paris]", null},
                {11, 5, 30, null, "+01:00", "Europe/Paris",         "11:05:30+01:00[Europe/Paris]", null},
                {11, 5, 30, 500000000, "+01:00", "Europe/Paris",    "11:05:30.5+01:00[Europe/Paris]", null},
                {11, 5, 30, 1, "+01:00", "Europe/Paris",            "11:05:30.000000001+01:00[Europe/Paris]", null},
                
                {11, 5, null, null, null, "Europe/Paris",       "11:05", null},
                {11, 5, 30, null, null, "Europe/Paris",         "11:05:30", null},
                {11, 5, 30, 500000000, null, "Europe/Paris",    "11:05:30.5", null},
                {11, 5, 30, 1, null, "Europe/Paris",            "11:05:30.000000001", null},
        };
    }

    @Test(dataProvider="sample_isoTime")
    public void test_print_isoTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createTime(hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoTime")
    public void test_parse_isoTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createTime(hour, min, sec, nano);
            if (offsetId != null) {
                expected.put(ZoneOffset.rule(), ZoneOffset.of(offsetId));
                if (zoneId != null) {
                    expected.put(ZoneId.rule(), ZoneId.of(zoneId));
                }
            }
            assertParseMatch(DateTimeFormatters.isoTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoLocalDateTime")
    Object[][] provider_sample_isoLocalDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, 11, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, CalendricalException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, CalendricalException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, CalendricalException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, null, null,                    "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         "2008-06-30T11:05:30.000000001", null},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     "2008-06-30T11:05:30.000000001", null},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001", null},
                
                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               "2008-06-30T11:05:30.000000001", null},
                
                {123456, 6, 30, 11, 5, null, null, null, null,                  "+123456-06-30T11:05", null},
        };
    }

    @Test(dataProvider="sample_isoLocalDateTime")
    public void test_print_isoLocalDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoLocalDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoLocalDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoLocalDateTime")
    public void test_parse_isoLocalDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDateTime(year, month, day, hour, min, sec, nano);
            assertParseMatch(DateTimeFormatters.isoLocalDateTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoOffsetDateTime")
    Object[][] provider_sample_isoOffsetDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, 11, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, CalendricalException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, CalendricalException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, CalendricalException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, null, null,                    null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                "2008-06-30T11:05+01:00", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  "2008-06-30T11:05:30+01:00", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             "2008-06-30T11:05:30.5+01:00", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     "2008-06-30T11:05:30.000000001+01:00", null},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05+01:00", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30+01:00", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5+01:00", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001+01:00", null},
                
                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               null, CalendricalException.class},
                
                {123456, 6, 30, 11, 5, null, null, "+01:00", null,              "+123456-06-30T11:05+01:00", null},
        };
    }

    @Test(dataProvider="sample_isoOffsetDateTime")
    public void test_print_isoOffsetDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoOffsetDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoOffsetDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoOffsetDateTime")
    public void test_parse_isoOffsetDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDateTime(year, month, day, hour, min, sec, nano);
            buildCalendrical(expected, offsetId, null);  // zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoOffsetDateTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoZonedDateTime")
    Object[][] provider_sample_isoZonedDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, 11, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, CalendricalException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, CalendricalException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, CalendricalException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, null, null,                    null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001+01:00[Europe/Paris]", null},
                
                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       null, CalendricalException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               null, CalendricalException.class},
                
                {123456, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",    "+123456-06-30T11:05+01:00[Europe/Paris]", null},
        };
    }

    @Test(dataProvider="sample_isoZonedDateTime")
    public void test_print_isoZonedDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoZonedDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoZonedDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoZonedDateTime")
    public void test_parse_isoZonedDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDateTime(year, month, day, hour, min, sec, nano);
            buildCalendrical(expected, offsetId, zoneId);
            assertParseMatch(DateTimeFormatters.isoZonedDateTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoDateTime")
    Object[][] provider_sample_isoDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, 6, null, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, 30, null, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, 11, null, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, 5, null, null, null, null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, CalendricalException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, CalendricalException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, CalendricalException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, CalendricalException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, CalendricalException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, CalendricalException.class},
                
                {2008, 6, 30, 11, 5, null, null, null, null,                    "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         "2008-06-30T11:05:30.000000001", null},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                "2008-06-30T11:05+01:00", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  "2008-06-30T11:05:30+01:00", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             "2008-06-30T11:05:30.5+01:00", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     "2008-06-30T11:05:30.000000001+01:00", null},
                
                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001+01:00[Europe/Paris]", null},
                
                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               "2008-06-30T11:05:30.000000001", null},
                
                {123456, 6, 30, 11, 5, null, null, null, null,                  "+123456-06-30T11:05", null},
        };
    }

    @Test(dataProvider="sample_isoDateTime")
    public void test_print_isoDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        MockSimpleCalendrical test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoDateTime")
    public void test_parse_isoDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            MockSimpleCalendrical expected = createDateTime(year, month, day, hour, min, sec, nano);
            if (offsetId != null) {
                expected.put(ZoneOffset.rule(), ZoneOffset.of(offsetId));
                if (zoneId != null) {
                    expected.put(ZoneId.rule(), ZoneId.of(zoneId));
                }
            }
            assertParseMatch(DateTimeFormatters.isoDateTime().parseToContext(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_print_isoOrdinalDate() {
        Calendrical test = LocalDateTime.of(2008, 6, 3, 11, 5, 30);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-155");
    }

    public void test_print_isoOrdinalDate_offset() {
        Calendrical test = OffsetDateTime.of(2008, 6, 3, 11, 5, 30, ZoneOffset.UTC);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-155Z");
    }

    public void test_print_isoOrdinalDate_zoned() {
        Calendrical test = ZonedDateTime.of(LocalDateTime.of(2008, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-155Z[UTC]");
    }

    public void test_print_isoOrdinalDate_zoned_largeYear() {
        Calendrical test = ZonedDateTime.of(LocalDateTime.of(123456, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "+123456-155Z[UTC]");
    }

    public void test_print_isoOrdinalDate_fields() {
        Calendrical test = DateTimeFields.of(YEAR, 2008, DAY_OF_YEAR, 231);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-231");
    }

    public void test_print_isoOrdinalDate_missingField() {
        try {
            Calendrical test = Year.of(2008);
            DateTimeFormatters.isoOrdinalDate().print(test);
            fail();
        } catch (CalendricalRuleException ex) {
            assertEquals(ex.getRule(), DAY_OF_YEAR);
        }
    }

    //-----------------------------------------------------------------------
    public void test_parse_isoOrdinalDate() {
        MockSimpleCalendrical expected = new MockSimpleCalendrical(YEAR, YEAR.field(2008), DAY_OF_YEAR, DAY_OF_YEAR.field(123));
        assertParseMatch(DateTimeFormatters.isoOrdinalDate().parseToContext("2008-123", new ParsePosition(0)), expected);
    }

    public void test_parse_isoOrdinalDate_largeYear() {
        MockSimpleCalendrical expected = new MockSimpleCalendrical(YEAR, YEAR.field(123456), DAY_OF_YEAR, DAY_OF_YEAR.field(123));
        assertParseMatch(DateTimeFormatters.isoOrdinalDate().parseToContext("+123456-123", new ParsePosition(0)), expected);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_print_basicIsoDate() {
        Calendrical test = LocalDateTime.of(2008, 6, 3, 11, 5, 30);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080603");
    }

    public void test_print_basicIsoDate_offset() {
        Calendrical test = OffsetDateTime.of(2008, 6, 3, 11, 5, 30, ZoneOffset.UTC);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080603Z");
    }

    public void test_print_basicIsoDate_zoned() {
        Calendrical test = ZonedDateTime.of(LocalDateTime.of(2008, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080603Z[UTC]");
    }

    @Test(expectedExceptions=CalendricalPrintException.class)
    public void test_print_basicIsoDate_largeYear() {
        try {
            Calendrical test = ZonedDateTime.of(LocalDateTime.of(123456, 6, 3, 11, 5, 30), ZoneId.UTC);
            DateTimeFormatters.basicIsoDate().print(test);
        } catch (CalendricalPrintException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    public void test_print_basicIsoDate_fields() {
        Calendrical test = LocalDate.of(2008, 6, 30);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080630");
    }

    public void test_print_basicIsoDate_missingField() {
        try {
            Calendrical test = YearMonth.of(2008, 6);
            DateTimeFormatters.basicIsoDate().print(test);
            fail();
        } catch (CalendricalRuleException ex) {
            assertEquals(ex.getRule(), DAY_OF_MONTH);
        }
    }

    //-----------------------------------------------------------------------
    public void test_parse_basicIsoDate() {
        LocalDate expected = LocalDate.of(2008, 6, 3);
        assertEquals(DateTimeFormatters.basicIsoDate().parse("20080603", LocalDate.rule()), expected);
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parse_basicIsoDate_largeYear() {
        try {
            LocalDate expected = LocalDate.of(123456, 6, 3);
            assertEquals(DateTimeFormatters.basicIsoDate().parse("+1234560603", LocalDate.rule()), expected);
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getErrorIndex(), 0);
            assertEquals(ex.getParsedString(), "+1234560603");
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="weekDate")
    Iterator<Object[]> weekDate() {
        return new Iterator<Object[]>() {
            private ZonedDateTime date = ZonedDateTime.of(LocalDateTime.of(2003, 12, 29, 11, 5, 30), ZoneId.UTC);
            private ZonedDateTime endDate = date.withDate(2005, 1, 2);
            private int week = 1;
            private int day = 1;

            public boolean hasNext() {
                return !date.isAfter(endDate);
            }
            public Object[] next() {
                StringBuilder sb = new StringBuilder("2004-W");
                if (week < 10) {
                    sb.append('0');
                }
                sb.append(week).append('-').append(day).append("Z[UTC]");
                Object[] ret = new Object[] {date, sb.toString()};
                date = date.plusDays(1);
                day += 1;
                if (day == 8) {
                    day = 1;
                    week++;
                }
                return ret;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test(dataProvider="weekDate")
    public void test_print_isoWeekDate(Calendrical test, String expected) {
        assertEquals(DateTimeFormatters.isoWeekDate().print(test), expected);
    }

    public void test_print_isoWeekDate_zoned_largeYear() {
        Calendrical test = ZonedDateTime.of(LocalDateTime.of(123456, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.isoWeekDate().print(test), "+123456-W23-2Z[UTC]");
    }

    public void test_print_isoWeekDate_fields() {
        MockSimpleCalendrical test = new MockSimpleCalendrical();
        test.put(WEEK_BASED_YEAR, WEEK_BASED_YEAR.field(2004));
        test.put(WEEK_OF_WEEK_BASED_YEAR, WEEK_OF_WEEK_BASED_YEAR.field(5));
        test.put(DAY_OF_WEEK, DAY_OF_WEEK.field(2));
        assertEquals(DateTimeFormatters.isoWeekDate().print(test), "2004-W05-2");
    }

    public void test_print_isoWeekDate_missingField() {
        try {
            Calendrical test = DateTimeFields.of(WEEK_BASED_YEAR, 2004, WEEK_OF_WEEK_BASED_YEAR, 1);
            DateTimeFormatters.isoWeekDate().print(test);
            fail();
        } catch (CalendricalRuleException ex) {
            assertEquals(ex.getRule(), DAY_OF_WEEK);
        }
    }

    //-----------------------------------------------------------------------
    public void test_parse_weekDate() {
        MockSimpleCalendrical expected = new MockSimpleCalendrical();
        expected.put(WEEK_BASED_YEAR, WEEK_BASED_YEAR.field(2004));
        expected.put(WEEK_OF_WEEK_BASED_YEAR, WEEK_OF_WEEK_BASED_YEAR.field(1));
        expected.put(DAY_OF_WEEK, DAY_OF_WEEK.field(1));
        assertParseMatch(DateTimeFormatters.isoWeekDate().parseToContext("2004-W01-1", new ParsePosition(0)), expected);
    }

    public void test_parse_weekDate_largeYear() {
        MockSimpleCalendrical expected = new MockSimpleCalendrical();
        expected.put(WEEK_BASED_YEAR, WEEK_BASED_YEAR.field(123456));
        expected.put(WEEK_OF_WEEK_BASED_YEAR, WEEK_OF_WEEK_BASED_YEAR.field(4));
        expected.put(DAY_OF_WEEK, DAY_OF_WEEK.field(5));
        assertParseMatch(DateTimeFormatters.isoWeekDate().parseToContext("+123456-W04-5", new ParsePosition(0)), expected);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_print_rfc1123() {
        Calendrical test = ZonedDateTime.of(LocalDateTime.of(2008, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.rfc1123().print(test), "Tue, 03 Jun 2008 11:05:30 Z");
    }

    public void test_print_rfc1123_missingField() {
        try {
            Calendrical test = YearMonth.of(2008, 6);
            DateTimeFormatters.rfc1123().print(test);
            fail();
        } catch (CalendricalRuleException ex) {
            assertEquals(ex.getRule(), DAY_OF_WEEK);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    private MockSimpleCalendrical createDate(Integer year, Integer month, Integer day) {
        MockSimpleCalendrical test = new MockSimpleCalendrical();
        if (year != null) {
            test.put(YEAR, YEAR.field(year));
        }
        if (month != null) {
            test.put(MONTH_OF_YEAR, MONTH_OF_YEAR.field(month));
        }
        if (day != null) {
            test.put(DAY_OF_MONTH, DAY_OF_MONTH.field(day));
        }
        return test;
    }

    private MockSimpleCalendrical createTime(Integer hour, Integer min, Integer sec, Integer nano) {
        MockSimpleCalendrical test = new MockSimpleCalendrical();
        if (hour != null) {
            test.put(HOUR_OF_DAY, HOUR_OF_DAY.field(hour));
        }
        if (min != null) {
            test.put(MINUTE_OF_HOUR, MINUTE_OF_HOUR.field(min));
        }
        if (sec != null) {
            test.put(SECOND_OF_MINUTE, SECOND_OF_MINUTE.field(sec));
        }
        if (nano != null) {
            test.put(NANO_OF_SECOND, NANO_OF_SECOND.field(nano));
        }
        return test;
    }

    private MockSimpleCalendrical createDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano) {
        MockSimpleCalendrical test = new MockSimpleCalendrical();
        if (year != null) {
            test.put(YEAR, YEAR.field(year));
        }
        if (month != null) {
            test.put(MONTH_OF_YEAR, MONTH_OF_YEAR.field(month));
        }
        if (day != null) {
            test.put(DAY_OF_MONTH, DAY_OF_MONTH.field(day));
        }
        if (hour != null) {
            test.put(HOUR_OF_DAY, HOUR_OF_DAY.field(hour));
        }
        if (min != null) {
            test.put(MINUTE_OF_HOUR, MINUTE_OF_HOUR.field(min));
        }
        if (sec != null) {
            test.put(SECOND_OF_MINUTE, SECOND_OF_MINUTE.field(sec));
        }
        if (nano != null) {
            test.put(NANO_OF_SECOND, NANO_OF_SECOND.field(nano));
        }
        return test;
    }

    private void buildCalendrical(MockSimpleCalendrical cal, String offsetId, String zoneId) {
        if (offsetId != null) {
            cal.put(ZoneOffset.rule(), ZoneOffset.of(offsetId));
        }
        if (zoneId != null) {
            cal.put(ZoneId.rule(), ZoneId.of(zoneId));
        }
    }

    private void assertParseMatch(DateTimeParseContext merger, MockSimpleCalendrical expected) {
        for (CalendricalRule<?> rule : expected.rules()) {
            if (rule instanceof DateTimeRule) {
                assertEquals(merger.getParsed((DateTimeRule) rule), ((DateTimeRule) rule).getValue(expected), "Failed on rule: " + rule.getName());
            } else {
                assertEquals(merger.getParsed(rule.getType()), expected.get(rule), "Failed on rule: " + rule.getName());
            }
        }
    }

}
