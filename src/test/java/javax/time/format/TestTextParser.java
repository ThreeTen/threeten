/*
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.format;

import static javax.time.calendrical.ChronoField.DAY_OF_MONTH;
import static javax.time.calendrical.ChronoField.DAY_OF_WEEK;
import static javax.time.calendrical.ChronoField.MONTH_OF_YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParsePosition;
import java.util.Locale;

import javax.time.calendrical.DateTimeField;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test TextPrinterParser.
 */
@Test(groups={"implementation"})
public class TestTextParser extends AbstractTestPrinterParser {

    //-----------------------------------------------------------------------
    @DataProvider(name="error")
    Object[][] data_error() {
        return new Object[][] {
            {DAY_OF_WEEK, TextStyle.FULL, "Monday", -1, IndexOutOfBoundsException.class},
            {DAY_OF_WEEK, TextStyle.FULL, "Monday", 7, IndexOutOfBoundsException.class},
        };
    }

    @Test(dataProvider="error")
    public void test_parse_error(DateTimeField field, TextStyle style, String text, int pos, Class<?> expected) {
        try {
            getFormatter(field, style).parseToBuilder(text, new ParsePosition(pos));
        } catch (RuntimeException ex) {
            assertTrue(expected.isInstance(ex));
        }
    }

    //-----------------------------------------------------------------------
    public void test_parse_midStr() throws Exception {
        ParsePosition pos = new ParsePosition(3);
        assertEquals(getFormatter(DAY_OF_WEEK, TextStyle.FULL)
                     .parseToBuilder("XxxMondayXxx", pos)
                     .getLong(DAY_OF_WEEK), 1L);
        assertEquals(pos.getIndex(), 9);
    }

    public void test_parse_remainderIgnored() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(DAY_OF_WEEK, TextStyle.SHORT)
                     .parseToBuilder("Wednesday", pos)
                     .getLong(DAY_OF_WEEK), 3L);
        assertEquals(pos.getIndex(), 3);
    }

    //-----------------------------------------------------------------------
    public void test_parse_noMatch1() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        getFormatter(DAY_OF_WEEK, TextStyle.FULL).parseToBuilder("Munday", pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    public void test_parse_noMatch2() throws Exception {
        ParsePosition pos = new ParsePosition(3);
        getFormatter(DAY_OF_WEEK, TextStyle.FULL).parseToBuilder("Monday", pos);
        assertEquals(pos.getErrorIndex(), 3);
    }

    public void test_parse_noMatch_atEnd() throws Exception {
        ParsePosition pos = new ParsePosition(6);
        getFormatter(DAY_OF_WEEK, TextStyle.FULL).parseToBuilder("Monday", pos);
        assertEquals(pos.getErrorIndex(), 6);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="parseText")
    Object[][] provider_text() {
        return new Object[][] {
            {DAY_OF_WEEK, TextStyle.FULL, 1, "Monday"},
            {DAY_OF_WEEK, TextStyle.FULL, 2, "Tuesday"},
            {DAY_OF_WEEK, TextStyle.FULL, 3, "Wednesday"},
            {DAY_OF_WEEK, TextStyle.FULL, 4, "Thursday"},
            {DAY_OF_WEEK, TextStyle.FULL, 5, "Friday"},
            {DAY_OF_WEEK, TextStyle.FULL, 6, "Saturday"},
            {DAY_OF_WEEK, TextStyle.FULL, 7, "Sunday"},

            {DAY_OF_WEEK, TextStyle.SHORT, 1, "Mon"},
            {DAY_OF_WEEK, TextStyle.SHORT, 2, "Tue"},
            {DAY_OF_WEEK, TextStyle.SHORT, 3, "Wed"},
            {DAY_OF_WEEK, TextStyle.SHORT, 4, "Thu"},
            {DAY_OF_WEEK, TextStyle.SHORT, 5, "Fri"},
            {DAY_OF_WEEK, TextStyle.SHORT, 6, "Sat"},
            {DAY_OF_WEEK, TextStyle.SHORT, 7, "Sun"},

            {MONTH_OF_YEAR, TextStyle.FULL, 1, "January"},
            {MONTH_OF_YEAR, TextStyle.FULL, 12, "December"},

            {MONTH_OF_YEAR, TextStyle.SHORT, 1, "Jan"},
            {MONTH_OF_YEAR, TextStyle.SHORT, 12, "Dec"},
       };
    }

    @DataProvider(name="parseNumber")
    Object[][] provider_number() {
        return new Object[][] {
            {DAY_OF_MONTH, TextStyle.FULL, 1, "1"},
            {DAY_OF_MONTH, TextStyle.FULL, 2, "2"},
            {DAY_OF_MONTH, TextStyle.FULL, 30, "30"},
            {DAY_OF_MONTH, TextStyle.FULL, 31, "31"},

            {DAY_OF_MONTH, TextStyle.SHORT, 1, "1"},
            {DAY_OF_MONTH, TextStyle.SHORT, 2, "2"},
            {DAY_OF_MONTH, TextStyle.SHORT, 30, "30"},
            {DAY_OF_MONTH, TextStyle.SHORT, 31, "31"},
       };
    }

    @Test(dataProvider="parseText")
    public void test_parseText(DateTimeField field, TextStyle style, int value, String input) throws Exception {
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(field, style).parseToBuilder(input, pos).getLong(field), (long) value);
        assertEquals(pos.getIndex(), input.length());
    }

    @Test(dataProvider="parseNumber")
    public void test_parseNumber(DateTimeField field, TextStyle style, int value, String input) throws Exception {
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(field, style).parseToBuilder(input, pos).getLong(field), (long) value);
        assertEquals(pos.getIndex(), input.length());
    }

    //-----------------------------------------------------------------------
    @Test(dataProvider="parseText")
    public void test_parse_strict_caseSensitive_parseUpper(DateTimeField field, TextStyle style, int value, String input) throws Exception {
        setCaseSensitive(true);
        ParsePosition pos = new ParsePosition(0);
        getFormatter(field, style).parseToBuilder(input.toUpperCase(), pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    @Test(dataProvider="parseText")
    public void test_parse_strict_caseInsensitive_parseUpper(DateTimeField field, TextStyle style, int value, String input) throws Exception {
        setCaseSensitive(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(field, style).parseToBuilder(input.toUpperCase(), pos).getLong(field), (long) value);
        assertEquals(pos.getIndex(), input.length());
    }

    //-----------------------------------------------------------------------
    @Test(dataProvider="parseText")
    public void test_parse_strict_caseSensitive_parseLower(DateTimeField field, TextStyle style, int value, String input) throws Exception {
        setCaseSensitive(true);
        ParsePosition pos = new ParsePosition(0);
        getFormatter(field, style).parseToBuilder(input.toLowerCase(), pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    @Test(dataProvider="parseText")
    public void test_parse_strict_caseInsensitive_parseLower(DateTimeField field, TextStyle style, int value, String input) throws Exception {
        setCaseSensitive(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(field, style).parseToBuilder(input.toLowerCase(), pos).getLong(field), (long) value);
        assertEquals(pos.getIndex(), input.length());
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_parse_full_strict_full_match() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.FULL).parseToBuilder("January", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 7);
    }

    public void test_parse_full_strict_short_noMatch() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        getFormatter(MONTH_OF_YEAR, TextStyle.FULL).parseToBuilder("Janua", pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    public void test_parse_full_strict_number_noMatch() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        getFormatter(MONTH_OF_YEAR, TextStyle.FULL).parseToBuilder("1", pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    //-----------------------------------------------------------------------
    public void test_parse_short_strict_full_match() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).parseToBuilder("January", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 3);
    }

    public void test_parse_short_strict_short_match() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).parseToBuilder("Janua", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 3);
    }

    public void test_parse_short_strict_number_noMatch() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).parseToBuilder("1", pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    //-----------------------------------------------------------------------
    public void test_parse_french_short_strict_full_noMatch() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).withLocale(Locale.FRENCH)
                                                    .parseToBuilder("janvier", pos);
        assertEquals(pos.getErrorIndex(), 0);
    }

    public void test_parse_french_short_strict_short_match() throws Exception {
        setStrict(true);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).withLocale(Locale.FRENCH)
                                                                 .parseToBuilder("janv.", pos)
                                                                 .getLong(MONTH_OF_YEAR),
                     1L);
        assertEquals(pos.getIndex(), 5);
    }

    //-----------------------------------------------------------------------
    public void test_parse_full_lenient_full_match() throws Exception {
        setStrict(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.FULL).parseToBuilder("January.", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 7);
    }

    public void test_parse_full_lenient_short_match() throws Exception {
        setStrict(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.FULL).parseToBuilder("Janua", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 3);
    }

    public void test_parse_full_lenient_number_match() throws Exception {
        setStrict(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.FULL).parseToBuilder("1", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 1);
    }

    //-----------------------------------------------------------------------
    public void test_parse_short_lenient_full_match() throws Exception {
        setStrict(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).parseToBuilder("January", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 7);
    }

    public void test_parse_short_lenient_short_match() throws Exception {
        setStrict(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).parseToBuilder("Janua", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 3);
    }

    public void test_parse_short_lenient_number_match() throws Exception {
        setStrict(false);
        ParsePosition pos = new ParsePosition(0);
        assertEquals(getFormatter(MONTH_OF_YEAR, TextStyle.SHORT).parseToBuilder("1", pos).getLong(MONTH_OF_YEAR), 1L);
        assertEquals(pos.getIndex(), 1);
    }

}
