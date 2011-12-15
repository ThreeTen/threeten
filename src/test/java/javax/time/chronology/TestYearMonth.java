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
package javax.time.calendar;

import javax.time.YearMonth;
import javax.time.Year;
import javax.time.ZoneOffset;
import javax.time.Clock;
import javax.time.ZoneId;
import javax.time.LocalTime;
import javax.time.DateResolver;
import javax.time.Period;
import javax.time.OffsetDateTime;
import javax.time.LocalDate;
import javax.time.DateResolvers;
import javax.time.TimeSource;
import static javax.time.calendar.ISODateTimeRule.DAY_OF_MONTH;
import static javax.time.calendar.ISODateTimeRule.MONTH_OF_QUARTER;
import static javax.time.calendar.ISODateTimeRule.MONTH_OF_YEAR;
import static javax.time.calendar.ISODateTimeRule.YEAR;
import static javax.time.ISOPeriodUnit.DAYS;
import static javax.time.ISOPeriodUnit.DECADES;
import static javax.time.ISOPeriodUnit.MONTHS;
import static javax.time.ISOPeriodUnit.YEARS;
import static javax.time.MonthOfYear.FEBRUARY;
import static javax.time.MonthOfYear.JULY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.time.CalendricalException;
import javax.time.Instant;
import javax.time.MonthOfYear;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatters;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test YearMonth.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestYearMonth {

    private YearMonth TEST_2008_06;

    @BeforeMethod
    public void setUp() {
        TEST_2008_06 = YearMonth.of(2008, 6);
    }

    //-----------------------------------------------------------------------
    public void test_interfaces() {
        Object obj = TEST_2008_06;
        assertTrue(obj instanceof Calendrical);
        assertTrue(obj instanceof Serializable);
        assertTrue(obj instanceof Comparable<?>);
        assertTrue(obj instanceof DateAdjuster);
        assertTrue(obj instanceof CalendricalMatcher);
    }

    public void test_serialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(TEST_2008_06);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                baos.toByteArray()));
        assertEquals(ois.readObject(), TEST_2008_06);
    }

    public void test_immutable() {
        Class<YearMonth> cls = YearMonth.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            assertTrue(Modifier.isPrivate(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    //-----------------------------------------------------------------------
    void check(YearMonth test, int y, int m) {
        assertEquals(test.getYear(), y);
        assertEquals(test.getMonthOfYear().getValue(), m);
    }

    //-----------------------------------------------------------------------
    // now()
    //-----------------------------------------------------------------------
    public void now() {
        YearMonth expected = YearMonth.now(Clock.systemDefaultZone());
        YearMonth test = YearMonth.now();
        for (int i = 0; i < 100; i++) {
            if (expected.equals(test)) {
                return;
            }
            expected = YearMonth.now(Clock.systemDefaultZone());
            test = YearMonth.now();
        }
        assertEquals(test, expected);
    }

    //-----------------------------------------------------------------------
    // now(Clock)
    //-----------------------------------------------------------------------
    public void now_Clock() {
        Instant instant = Instant.of(OffsetDateTime.of(2010, 12, 31, 0, 0, ZoneOffset.UTC));
        Clock clock = Clock.clock(TimeSource.fixed(instant), ZoneId.UTC);
        YearMonth test = YearMonth.now(clock);
        assertEquals(test.getYear(), 2010);
        assertEquals(test.getMonthOfYear(), MonthOfYear.DECEMBER);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void now_Clock_nullClock() {
        YearMonth.now(null);
    }

    //-----------------------------------------------------------------------
    public void factory_intsMonth() {
        YearMonth test = YearMonth.of(2008, MonthOfYear.FEBRUARY);
        check(test, 2008, 2);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_factory_intsMonth_yearTooLow() {
        try {
            YearMonth.of(Year.MIN_YEAR - 1, MonthOfYear.JANUARY);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_factory_intsMonth_dayTooHigh() {
        try {
            YearMonth.of(Year.MAX_YEAR + 1, MonthOfYear.JANUARY);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_intsMonth_nullMonth() {
        YearMonth.of(2008, null);
    }

    //-----------------------------------------------------------------------
    public void factory_ints() {
        YearMonth test = YearMonth.of(2008, 2);
        check(test, 2008, 2);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_factory_ints_yearTooLow() {
        try {
            YearMonth.of(Year.MIN_YEAR - 1, 2);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_factory_ints_dayTooHigh() {
        try {
            YearMonth.of(Year.MAX_YEAR + 1, 2);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_factory_ints_monthTooLow() {
        try {
            YearMonth.of(2008, 0);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), MONTH_OF_YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_factory_ints_monthTooHigh() {
        try {
            YearMonth.of(2008, 13);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), MONTH_OF_YEAR);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_factory_Calendricals() {
        assertEquals(YearMonth.from(LocalDate.of(2007, 7, 15)), YearMonth.of(2007, 7));
        assertEquals(YearMonth.from(MockCenturyFieldRule.INSTANCE.field(20), MockYearOfCenturyFieldRule.INSTANCE.field(7), JULY), YearMonth.of(2007, 7));
        assertEquals(YearMonth.from(Year.of(2007), JULY), YearMonth.of(2007, 7));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_Calendricals_invalid_clash() {
        YearMonth.from(Year.of(2007), JULY, FEBRUARY);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_Calendricals_invalid_noDerive() {
        YearMonth.from(LocalTime.of(12, 30));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_Calendricals_invalid_empty() {
        YearMonth.from();
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_Calendricals_nullArray() {
        YearMonth.from((Calendrical[]) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_Calendricals_null() {
        YearMonth.from((Calendrical) null);
    }

    //-----------------------------------------------------------------------
    // parse()
    //-----------------------------------------------------------------------
    @DataProvider(name="goodParseData")
    Object[][] provider_goodParseData() {
        return new Object[][] {
                {"0000-01", YearMonth.of(0, 1)},
                {"0000-12", YearMonth.of(0, 12)},
                {"9999-12", YearMonth.of(9999, 12)},
                {"2000-01", YearMonth.of(2000, 1)},
                {"2000-02", YearMonth.of(2000, 2)},
                {"2000-03", YearMonth.of(2000, 3)},
                {"2000-04", YearMonth.of(2000, 4)},
                {"2000-05", YearMonth.of(2000, 5)},
                {"2000-06", YearMonth.of(2000, 6)},
                {"2000-07", YearMonth.of(2000, 7)},
                {"2000-08", YearMonth.of(2000, 8)},
                {"2000-09", YearMonth.of(2000, 9)},
                {"2000-10", YearMonth.of(2000, 10)},
                {"2000-11", YearMonth.of(2000, 11)},
                {"2000-12", YearMonth.of(2000, 12)},
                
                {"+12345678-03", YearMonth.of(12345678, 3)},
                {"+123456-03", YearMonth.of(123456, 3)},
                {"0000-03", YearMonth.of(0, 3)},
                {"-1234-03", YearMonth.of(-1234, 3)},
                {"-12345678-03", YearMonth.of(-12345678, 3)},
                
                {"+" + Year.MAX_YEAR + "-03", YearMonth.of(Year.MAX_YEAR, 3)},
                {Year.MIN_YEAR + "-03", YearMonth.of(Year.MIN_YEAR, 3)},
        };
    }

    @Test(dataProvider="goodParseData")
    public void factory_parse_success(String text, YearMonth expected) {
        YearMonth yearMonth = YearMonth.parse(text);
        assertEquals(yearMonth, expected);
    }

//    @Test(dataProvider="goodParseData")
//    public void factory_parse_success_noDash(String text, YearMonth expected) {
//        text = text.substring(0, text.lastIndexOf('-')) + text.substring(text.lastIndexOf('-') + 1);
//        YearMonth yearMonth = YearMonth.parse(text);
//        assertEquals(yearMonth, expected);
//    }

    //-----------------------------------------------------------------------
    @DataProvider(name="badParseData")
    Object[][] provider_badParseData() {
        return new Object[][] {
                {"", 0},
                {"-00", 1},
                {"--01-0", 1},
                {"A01-3", 0},
                {"200-01", 0},
                {"2009/12", 4},
                
                {"-0000-10", 0},
                {"-12345678901-10", 11},
                {"+1-10", 1},
                {"+12-10", 1},
                {"+123-10", 1},
                {"+1234-10", 0},
                {"12345-10", 0},
                {"+12345678901-10", 11},
        };
    }

    @Test(dataProvider="badParseData", expectedExceptions=CalendricalParseException.class)
    public void factory_parse_fail(String text, int pos) {
        try {
            YearMonth.parse(text);
            fail(String.format("Parse should have failed for %s at position %d", text, pos));
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getParsedString(), text);
            assertEquals(ex.getErrorIndex(), pos);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=CalendricalParseException.class)
    public void factory_parse_illegalValue_Month() {
        YearMonth.parse("2008-13");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_parse_nullText() {
        YearMonth.parse(null);
    }

    //-----------------------------------------------------------------------
    // parse(DateTimeFormatter)
    //-----------------------------------------------------------------------
    public void factory_parse_formatter() {
        YearMonth t = YearMonth.parse("2010 12", DateTimeFormatters.pattern("yyyy MM"));
        assertEquals(t, YearMonth.of(2010, 12));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_parse_formatter_nullText() {
        YearMonth.parse((String) null, DateTimeFormatters.basicIsoDate());
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_parse_formatter_nullFormatter() {
        YearMonth.parse("2010 12", null);
    }

    //-----------------------------------------------------------------------
    // get(CalendricalRule)
    //-----------------------------------------------------------------------
    public void test_get_CalendricalRule() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.get(YearMonth.rule()), test);
        assertEquals(test.get(Chronology.rule()), ISOChronology.INSTANCE);
        assertEquals(test.get(YEAR).getValue(), 2008);
        assertEquals(test.get(MONTH_OF_YEAR).getValue(), 6);
        assertEquals(test.get(MONTH_OF_QUARTER).getValue(), 3);
    }

    @Test(expectedExceptions=NullPointerException.class )
    public void test_get_CalendricalRule_null() {
        YearMonth test = YearMonth.of(2008, 6);
        test.get((CalendricalRule<?>) null);
    }

    public void test_get_unsupported() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.get(MockRuleNoValue.INSTANCE), null);
    }

    //-----------------------------------------------------------------------
    // get*()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleDates")
    Object[][] provider_sampleDates() {
        return new Object[][] {
            {2008, 1},
            {2008, 2},
            {-1, 3},
            {0, 12},
        };
    }

    //-----------------------------------------------------------------------
    // with(Year)
    //-----------------------------------------------------------------------
    public void test_with_Year() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.with(Year.of(2000)), YearMonth.of(2000, 6));
    }

    public void test_with_Year_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.with(Year.of(2008)), test);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_with_Year_null() {
        YearMonth test = YearMonth.of(2008, 6);
        test.with((Year) null);
    }

    //-----------------------------------------------------------------------
    // with(MonthOfYear)
    //-----------------------------------------------------------------------
    public void test_with_MonthOfYear() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.with(MonthOfYear.JANUARY), YearMonth.of(2008, 1));
    }

    public void test_with_MonthOfYear_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.with(MonthOfYear.JUNE), test);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_with_MonthOfYear_null() {
        YearMonth test = YearMonth.of(2008, 6);
        test.with((MonthOfYear) null);
    }

    //-----------------------------------------------------------------------
    // withYear()
    //-----------------------------------------------------------------------
    public void test_withYear() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.withYear(1999), YearMonth.of(1999, 6));
    }

    public void test_withYear_int_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.withYear(2008), test);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withYear_tooLow() {
        YearMonth test = YearMonth.of(2008, 6);
        try {
            test.withYear(Year.MIN_YEAR - 1);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withYear_tooHigh() {
        YearMonth test = YearMonth.of(2008, 6);
        try {
            test.withYear(Year.MAX_YEAR + 1);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), YEAR);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // withMonthOfYear()
    //-----------------------------------------------------------------------
    public void test_withMonthOfYear() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.withMonthOfYear(1), YearMonth.of(2008, 1));
    }

    public void test_withMonthOfYear_int_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.withMonthOfYear(6), test);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withMonthOfYear_tooLow() {
        YearMonth test = YearMonth.of(2008, 6);
        try {
            test.withMonthOfYear(0);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), MONTH_OF_YEAR);
            throw ex;
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withMonthOfYear_tooHigh() {
        YearMonth test = YearMonth.of(2008, 6);
        try {
            test.withMonthOfYear(13);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), MONTH_OF_YEAR);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // plus(PeriodProvider)
    //-----------------------------------------------------------------------
    public void test_plus_PeriodProvider() {
        PeriodProvider provider = Period.of(1, 2, 3, 4, 5, 6, 7);
        YearMonth test = YearMonth.of(2008, 6).plus(provider);
        assertEquals(test, YearMonth.of(2009, 8));
    }

    public void test_plus_PeriodProvider_normalized() {
        PeriodProvider provider = PeriodFields.of(5, DECADES).with(3, YEARS).with(25, MONTHS).with(90, DAYS);
        assertEquals(YearMonth.of(2007, 6).plus(provider), YearMonth.of(2007 + 50 + 3 + 2, 7));
    }

    public void test_plus_PeriodProvider_timeIgnored() {
        PeriodProvider provider = Period.of(1, 2, Integer.MAX_VALUE, Integer.MAX_VALUE, 5, 6, 7);
        YearMonth test = YearMonth.of(2008, 6).plus(provider);
        assertEquals(test, YearMonth.of(2009, 8));
    }

    public void test_plus_PeriodProvider_zero() {
        YearMonth base = YearMonth.of(2008, 6);
        YearMonth test = base.plus(Period.ZERO);
        assertSame(test, base);
    }

    public void test_plus_PeriodProvider_previousValidResolver_oneMonth() {
        PeriodProvider provider = Period.ofMonths(1);
        YearMonth test = YearMonth.of(2008, 6).plus(provider);
        assertEquals(test, YearMonth.of(2008, 7));
    }

    public void test_plus_PeriodProvider_previousValidResolver_oneYear() {
        PeriodProvider provider = Period.ofYears(1);
        YearMonth test = YearMonth.of(2008, 6).plus(provider);
        assertEquals(test, YearMonth.of(2009, 6));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plus_PeriodProvider_invalidPeriod() {
        PeriodProvider provider = PeriodField.of(20, MockOtherChronology.OTHER_MONTHS);
        YearMonth.of(2010, 6).plus(provider);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plus_PeriodProvider_bigPeriod() {
        long years = 20L + Integer.MAX_VALUE;
        PeriodProvider provider = PeriodField.of(years, YEARS);
        YearMonth.of(-40, 6).plus(provider);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plus_PeriodProvider_null() {
        YearMonth.of(2008, 6).plus((PeriodProvider) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plus_PeriodProvider_badProvider() {
        YearMonth.of(2008, 6).plus(new MockPeriodProviderReturnsNull());
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plus_PeriodProvider_invalidTooLarge() {
        PeriodProvider provider = Period.ofYears(1);
        YearMonth.of(Year.MAX_YEAR, 6).plus(provider);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plus_PeriodProvider_invalidTooSmall() {
        PeriodProvider provider = Period.ofYears(-1);
        YearMonth.of(Year.MIN_YEAR, 6).plus(provider);
    }

    //-----------------------------------------------------------------------
    // plusYears()
    //-----------------------------------------------------------------------
    public void test_plusYears_long() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.plusYears(1), YearMonth.of(2009, 6));
    }

    public void test_plusYears_long_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.plusYears(0), test);
    }

    public void test_plusYears_long_negative() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.plusYears(-1), YearMonth.of(2007, 6));
    }

    public void test_plusYears_long_big() {
        YearMonth test = YearMonth.of(-40, 6);
        assertEquals(test.plusYears(20L + Year.MAX_YEAR), YearMonth.of((int) (-40L + 20L + Year.MAX_YEAR), 6));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_long_invalidTooLarge() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 6);
        test.plusYears(1);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_long_invalidTooLargeMaxAddMax() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.plusYears(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_long_invalidTooLargeMaxAddMin() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.plusYears(Long.MIN_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_long_invalidTooSmall() {
        YearMonth test = YearMonth.of(Year.MIN_YEAR, 6);
        test.plusYears(-1);
    }

    //-----------------------------------------------------------------------
    // plusMonths()
    //-----------------------------------------------------------------------
    public void test_plusMonths_long() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.plusMonths(1), YearMonth.of(2008, 7));
    }

    public void test_plusMonths_long_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.plusMonths(0), test);
    }

    public void test_plusMonths_long_overYears() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.plusMonths(7), YearMonth.of(2009, 1));
    }

    public void test_plusMonths_long_negative() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.plusMonths(-1), YearMonth.of(2008, 5));
    }

    public void test_plusMonths_long_negativeOverYear() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.plusMonths(-6), YearMonth.of(2007, 12));
    }

    public void test_plusMonths_long_big() {
        YearMonth test = YearMonth.of(-40, 6);
        long months = 20L + Integer.MAX_VALUE;
        assertEquals(test.plusMonths(months), YearMonth.of((int) (-40L + months / 12), 6 + (int) (months % 12)));
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusMonths_long_invalidTooLarge() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.plusMonths(1);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusMonths_long_invalidTooLargeMaxAddMax() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.plusMonths(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusMonths_long_invalidTooLargeMaxAddMin() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.plusMonths(Long.MIN_VALUE);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusMonths_long_invalidTooSmall() {
        YearMonth test = YearMonth.of(Year.MIN_YEAR, 1);
        test.plusMonths(-1);
    }

    //-----------------------------------------------------------------------
    // minus(PeriodProvider)
    //-----------------------------------------------------------------------
    public void test_minus_PeriodProvider() {
        PeriodProvider provider = Period.of(1, 2, 3, 4, 5, 6, 7);
        YearMonth test = YearMonth.of(2008, 6).minus(provider);
        assertEquals(test, YearMonth.of(2007, 4));
    }

    public void test_minus_PeriodProvider_normalized() {
        PeriodProvider provider = PeriodFields.of(5, DECADES).with(3, YEARS).with(25, MONTHS).with(90, DAYS);
        assertEquals(YearMonth.of(2007, 6).minus(provider), YearMonth.of(2007 - 50 - 3 - 2, 5));
    }

    public void test_minus_PeriodProvider_timeIgnored() {
        PeriodProvider provider = Period.of(1, 2, Integer.MAX_VALUE, Integer.MAX_VALUE, 5, 6, 7);
        YearMonth test = YearMonth.of(2008, 6).minus(provider);
        assertEquals(test, YearMonth.of(2007, 4));
    }

    public void test_minus_PeriodProvider_zero() {
        YearMonth base = YearMonth.of(2008, 6);
        YearMonth test = base.minus(Period.ZERO);
        assertSame(test, base);
    }

    public void test_minus_PeriodProvider_previousValidResolver_oneMonth() {
        PeriodProvider provider = Period.ofMonths(1);
        YearMonth test = YearMonth.of(2008, 6).minus(provider);
        assertEquals(test, YearMonth.of(2008, 5));
    }

    public void test_minus_PeriodProvider_previousValidResolver_oneYear() {
        PeriodProvider provider = Period.ofYears(1);
        YearMonth test = YearMonth.of(2008, 6).minus(provider);
        assertEquals(test, YearMonth.of(2007, 6));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minus_PeriodProvider_invalidPeriod() {
        PeriodProvider provider = PeriodField.of(20, MockOtherChronology.OTHER_MONTHS);
        YearMonth.of(2010, 6).minus(provider);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minus_PeriodProvider_bigPeriod() {
        long years = 20L + Integer.MAX_VALUE;
        PeriodProvider provider = PeriodField.of(years, YEARS);
        YearMonth.of(40, 6).minus(provider);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minus_PeriodProvider_null() {
        YearMonth.of(2008, 6).minus((PeriodProvider) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minus_PeriodProvider_badProvider() {
        YearMonth.of(2008, 6).minus(new MockPeriodProviderReturnsNull());
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minus_PeriodProvider_invalidTooLarge() {
        PeriodProvider provider = Period.ofYears(-1);
        YearMonth.of(Year.MAX_YEAR, 6).minus(provider);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minus_PeriodProvider_invalidTooSmall() {
        PeriodProvider provider = Period.ofYears(1);
        YearMonth.of(Year.MIN_YEAR, 6).minus(provider);
    }

    //-----------------------------------------------------------------------
    // minusYears()
    //-----------------------------------------------------------------------
    public void test_minusYears_long() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.minusYears(1), YearMonth.of(2007, 6));
    }

    public void test_minusYears_long_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.minusYears(0), test);
    }

    public void test_minusYears_long_negative() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.minusYears(-1), YearMonth.of(2009, 6));
    }

    public void test_minusYears_long_big() {
        YearMonth test = YearMonth.of(40, 6);
        assertEquals(test.minusYears(20L + Year.MAX_YEAR), YearMonth.of((int) (40L - 20L - Year.MAX_YEAR), 6));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_long_invalidTooLarge() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 6);
        test.minusYears(-1);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_long_invalidTooLargeMaxSubtractMax() {
        YearMonth test = YearMonth.of(Year.MIN_YEAR, 12);
        test.minusYears(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_long_invalidTooLargeMaxSubtractMin() {
        YearMonth test = YearMonth.of(Year.MIN_YEAR, 12);
        test.minusYears(Long.MIN_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_long_invalidTooSmall() {
        YearMonth test = YearMonth.of(Year.MIN_YEAR, 6);
        test.minusYears(1);
    }

    //-----------------------------------------------------------------------
    // minusMonths()
    //-----------------------------------------------------------------------
    public void test_minusMonths_long() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.minusMonths(1), YearMonth.of(2008, 5));
    }

    public void test_minusMonths_long_noChange() {
        YearMonth test = YearMonth.of(2008, 6);
        assertSame(test.minusMonths(0), test);
    }

    public void test_minusMonths_long_overYears() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.minusMonths(6), YearMonth.of(2007, 12));
    }

    public void test_minusMonths_long_negative() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.minusMonths(-1), YearMonth.of(2008, 7));
    }

    public void test_minusMonths_long_negativeOverYear() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.minusMonths(-7), YearMonth.of(2009, 1));
    }

    public void test_minusMonths_long_big() {
        YearMonth test = YearMonth.of(40, 6);
        long months = 20L + Integer.MAX_VALUE;
        assertEquals(test.minusMonths(months), YearMonth.of((int) (40L - months / 12), 6 - (int) (months % 12)));
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusMonths_long_invalidTooLarge() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.minusMonths(-1);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusMonths_long_invalidTooLargeMaxSubtractMax() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.minusMonths(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusMonths_long_invalidTooLargeMaxSubtractMin() {
        YearMonth test = YearMonth.of(Year.MAX_YEAR, 12);
        test.minusMonths(Long.MIN_VALUE);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusMonths_long_invalidTooSmall() {
        YearMonth test = YearMonth.of(Year.MIN_YEAR, 1);
        test.minusMonths(1);
    }

    //-----------------------------------------------------------------------
    // adjustDate()
    //-----------------------------------------------------------------------
    public void test_adjustDate() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2007, 1, 1);
        assertEquals(test.adjustDate(date), LocalDate.of(2008, 6, 1));
    }

    public void test_adjustDate_same() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2008, 6, 30);
        assertSame(test.adjustDate(date), date);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_adjustDate_null() {
        TEST_2008_06.adjustDate((LocalDate) null);
    }

    //-----------------------------------------------------------------------
    public void test_adjustDate_DateResolver() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2007, 1, 1);
        assertEquals(test.adjustDate(date, DateResolvers.nextValid()), LocalDate.of(2008, 6, 1));
    }

    public void test_adjustDate_DateResolver_same() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2008, 6, 30);
        assertSame(test.adjustDate(date, DateResolvers.nextValid()), date);
    }

    public void test_adjustDate_DateResolver_resolve() {
        YearMonth test = YearMonth.of(2008, 2);
        LocalDate date = LocalDate.of(2007, 1, 31);
        assertEquals(test.adjustDate(date, DateResolvers.nextValid()), LocalDate.of(2008, 3, 1));
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void test_adjustDate_DateResolver_invalid() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2007, 1, 31);
        try {
            test.adjustDate(date, DateResolvers.strict());
        } catch (InvalidCalendarFieldException ex) {
            assertEquals(ex.getRule(), DAY_OF_MONTH);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_adjustDate_DateResolver_nullDate() {
        TEST_2008_06.adjustDate((LocalDate) null, DateResolvers.nextValid());
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_adjustDate_DateResolver_nullResolver() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2008, 6, 30);  // same year-month, should still NPE
        test.adjustDate(date, (DateResolver) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_adjustDate_DateResolver_badResolver() {
        YearMonth test = YearMonth.of(2008, 6);
        LocalDate date = LocalDate.of(2007, 6, 30);
        test.adjustDate(date, new MockDateResolverReturnsNull());
    }

    //-----------------------------------------------------------------------
    // matchesDate()
    //-----------------------------------------------------------------------
    public void test_matchesDate() {
        assertEquals(YearMonth.of(2008, 6).matchesCalendrical(LocalDate.of(2008, 6, 1)), true);
        assertEquals(YearMonth.of(2007, 6).matchesCalendrical(LocalDate.of(2007, 6, 1)), true);
        assertEquals(YearMonth.of(2008, 5).matchesCalendrical(LocalDate.of(2008, 5, 1)), true);
        
        assertEquals(YearMonth.of(2007, 6).matchesCalendrical(LocalDate.of(2008, 6, 1)), false);
        assertEquals(YearMonth.of(2008, 5).matchesCalendrical(LocalDate.of(2008, 6, 1)), false);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesDate_null() {
        TEST_2008_06.matchesCalendrical(null);
    }

    //-----------------------------------------------------------------------
    // lengthInDays()
    //-----------------------------------------------------------------------
    public void test_lengthInDays_june() {
        YearMonth test = YearMonth.of(2007, 6);
        assertEquals(test.lengthInDays(), 30);
    }

    public void test_lengthInDays_febNonLeap() {
        YearMonth test = YearMonth.of(2007, 2);
        assertEquals(test.lengthInDays(), 28);
    }

    public void test_lengthInDays_febLeap() {
        YearMonth test = YearMonth.of(2008, 2);
        assertEquals(test.lengthInDays(), 29);
    }

    //-----------------------------------------------------------------------
    // isValidDay(int)
    //-----------------------------------------------------------------------
    public void test_isValidDay_int_june() {
        YearMonth test = YearMonth.of(2007, 6);
        assertEquals(test.isValidDay(1), true);
        assertEquals(test.isValidDay(30), true);
        
        assertEquals(test.isValidDay(-1), false);
        assertEquals(test.isValidDay(0), false);
        assertEquals(test.isValidDay(31), false);
        assertEquals(test.isValidDay(32), false);
    }

    public void test_isValidDay_int_febNonLeap() {
        YearMonth test = YearMonth.of(2007, 2);
        assertEquals(test.isValidDay(1), true);
        assertEquals(test.isValidDay(28), true);
        
        assertEquals(test.isValidDay(-1), false);
        assertEquals(test.isValidDay(0), false);
        assertEquals(test.isValidDay(29), false);
        assertEquals(test.isValidDay(32), false);
    }

    public void test_isValidDay_int_febLeap() {
        YearMonth test = YearMonth.of(2008, 2);
        assertEquals(test.isValidDay(1), true);
        assertEquals(test.isValidDay(29), true);
        
        assertEquals(test.isValidDay(-1), false);
        assertEquals(test.isValidDay(0), false);
        assertEquals(test.isValidDay(30), false);
        assertEquals(test.isValidDay(32), false);
    }

    //-----------------------------------------------------------------------
    // atDay(int)
    //-----------------------------------------------------------------------
    public void test_atDay_int() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.atDay(30), LocalDate.of(2008, 6, 30));
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void test_atDay_int_invalidDay() {
        YearMonth test = YearMonth.of(2008, 6);
        try {
            test.atDay(31);
        } catch (InvalidCalendarFieldException ex) {
            assertEquals(ex.getRule(), DAY_OF_MONTH);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // atDay(int,DateResolver)
    //-----------------------------------------------------------------------
    public void test_atDay_intDateResolver() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.atDay(30, DateResolvers.strict()), LocalDate.of(2008, 6, 30));
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void test_atDay_intDateResolver_invalidDay() {
        YearMonth test = YearMonth.of(2008, 6);
        try {
            test.atDay(31, DateResolvers.strict());
        } catch (InvalidCalendarFieldException ex) {
            assertEquals(ex.getRule(), DAY_OF_MONTH);
            throw ex;
        }
    }

    public void test_atDay_intDateResolver_resolved() {
        YearMonth test = YearMonth.of(2008, 6);
        assertEquals(test.atDay(31, DateResolvers.previousValid()), LocalDate.of(2008, 6, 30));
    }

    //-----------------------------------------------------------------------
    // toFields()
    //-----------------------------------------------------------------------
    public void test_toFields() {
        assertEquals(YearMonth.of(2010, 6).toFields(), DateTimeFields.of(YEAR, 2010, MONTH_OF_YEAR, 6));
    }

    //-----------------------------------------------------------------------
    // compareTo()
    //-----------------------------------------------------------------------
    public void test_comparisons() {
        doTest_comparisons_YearMonth(
            YearMonth.of(-1, 1),
            YearMonth.of(0, 1),
            YearMonth.of(0, 12),
            YearMonth.of(1, 1),
            YearMonth.of(1, 2),
            YearMonth.of(1, 12),
            YearMonth.of(2008, 1),
            YearMonth.of(2008, 6),
            YearMonth.of(2008, 12)
        );
    }

    void doTest_comparisons_YearMonth(YearMonth... localDates) {
        for (int i = 0; i < localDates.length; i++) {
            YearMonth a = localDates[i];
            for (int j = 0; j < localDates.length; j++) {
                YearMonth b = localDates[j];
                if (i < j) {
                    assertTrue(a.compareTo(b) < 0, a + " <=> " + b);
                    assertEquals(a.isBefore(b), true, a + " <=> " + b);
                    assertEquals(a.isAfter(b), false, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else if (i > j) {
                    assertTrue(a.compareTo(b) > 0, a + " <=> " + b);
                    assertEquals(a.isBefore(b), false, a + " <=> " + b);
                    assertEquals(a.isAfter(b), true, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else {
                    assertEquals(a.compareTo(b), 0, a + " <=> " + b);
                    assertEquals(a.isBefore(b), false, a + " <=> " + b);
                    assertEquals(a.isAfter(b), false, a + " <=> " + b);
                    assertEquals(a.equals(b), true, a + " <=> " + b);
                }
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_compareTo_ObjectNull() {
        TEST_2008_06.compareTo(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_isBefore_ObjectNull() {
        TEST_2008_06.isBefore(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_isAfter_ObjectNull() {
        TEST_2008_06.isAfter(null);
    }

    //-----------------------------------------------------------------------
    // equals()
    //-----------------------------------------------------------------------
    public void test_equals() {
        YearMonth a = YearMonth.of(2008, 6);
        YearMonth b = YearMonth.of(2008, 6);
        YearMonth c = YearMonth.of(2007, 6);
        YearMonth d = YearMonth.of(2008, 5);
        
        assertEquals(a.equals(a), true);
        assertEquals(a.equals(b), true);
        assertEquals(a.equals(c), false);
        assertEquals(a.equals(d), false);
        
        assertEquals(b.equals(a), true);
        assertEquals(b.equals(b), true);
        assertEquals(b.equals(c), false);
        assertEquals(b.equals(d), false);
        
        assertEquals(c.equals(a), false);
        assertEquals(c.equals(b), false);
        assertEquals(c.equals(c), true);
        assertEquals(c.equals(d), false);
        
        assertEquals(d.equals(a), false);
        assertEquals(d.equals(b), false);
        assertEquals(d.equals(c), false);
        assertEquals(d.equals(d), true);
    }

    public void test_equals_itself_true() {
        assertEquals(TEST_2008_06.equals(TEST_2008_06), true);
    }

    public void test_equals_string_false() {
        assertEquals(TEST_2008_06.equals("2007-07-15"), false);
    }

    public void test_equals_null_false() {
        assertEquals(TEST_2008_06.equals(null), false);
    }

    //-----------------------------------------------------------------------
    // hashCode()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_hashCode(int y, int m) {
        YearMonth a = YearMonth.of(y, m);
        assertEquals(a.hashCode(), a.hashCode());
        YearMonth b = YearMonth.of(y, m);
        assertEquals(a.hashCode(), b.hashCode());
    }

    public void test_hashCode_unique() {
        Set<Integer> uniques = new HashSet<Integer>(201 * 12);
        for (int i = 1900; i <= 2100; i++) {
            for (int j = 1; j <= 12; j++) {
                assertTrue(uniques.add(YearMonth.of(i, j).hashCode()));
            }
        }
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleToString")
    Object[][] provider_sampleToString() {
        return new Object[][] {
            {2008, 1, "2008-01"},
            {2008, 12, "2008-12"},
            {7, 5, "0007-05"},
            {0, 5, "0000-05"},
            {-1, 1, "-0001-01"},
        };
    }

    @Test(dataProvider="sampleToString")
    public void test_toString(int y, int m, String expected) {
        YearMonth test = YearMonth.of(y, m);
        String str = test.toString();
        assertEquals(str, expected);
    }

    //-----------------------------------------------------------------------
    // toString(DateTimeFormatter)
    //-----------------------------------------------------------------------
    public void test_toString_formatter() {
        String t = YearMonth.of(2010, 12).toString(DateTimeFormatters.pattern("yyyy MM"));
        assertEquals(t, "2010 12");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toString_formatter_null() {
        YearMonth.of(2010, 12).toString(null);
    }

}
