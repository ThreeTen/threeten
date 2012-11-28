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

import static javax.time.calendrical.ChronoField.NANO_OF_SECOND;
import static javax.time.calendrical.ChronoField.SECOND_OF_MINUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.text.ParsePosition;

import javax.time.DateTimeException;
import javax.time.LocalTime;
import javax.time.calendrical.DateTimeBuilder;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.MockFieldValue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test FractionPrinterParser.
 */
@Test(groups={"implementation"})
public class TestFractionPrinterParser extends AbstractTestPrinterParser {

    private DateTimeFormatter getFormatter(DateTimeField field, int minWidth, int maxWidth) {
        return builder.appendFraction(field, minWidth, maxWidth).toFormatter(locale).withSymbols(symbols);
    }

    //-----------------------------------------------------------------------
    // print
    //-----------------------------------------------------------------------
    @Test(expectedExceptions=DateTimeException.class)
    public void test_print_emptyCalendrical() throws Exception {
        getFormatter(NANO_OF_SECOND, 0, 9).printTo(EMPTY_DTA, buf);
    }

    public void test_print_append() throws Exception {
        buf.append("EXISTING");
        getFormatter(NANO_OF_SECOND, 0, 9).printTo(LocalTime.of(12, 30, 40, 3), buf);
        assertEquals(buf.toString(), "EXISTING.000000003");
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="Nanos")
    Object[][] provider_nanos() {
        return new Object[][] {
            {0, 9, 0,           ""},
            {0, 9, 2,           ".000000002"},
            {0, 9, 20,          ".00000002"},
            {0, 9, 200,         ".0000002"},
            {0, 9, 2000,        ".000002"},
            {0, 9, 20000,       ".00002"},
            {0, 9, 200000,      ".0002"},
            {0, 9, 2000000,     ".002"},
            {0, 9, 20000000,    ".02"},
            {0, 9, 200000000,   ".2"},
            {0, 9, 1,           ".000000001"},
            {0, 9, 12,          ".000000012"},
            {0, 9, 123,         ".000000123"},
            {0, 9, 1234,        ".000001234"},
            {0, 9, 12345,       ".000012345"},
            {0, 9, 123456,      ".000123456"},
            {0, 9, 1234567,     ".001234567"},
            {0, 9, 12345678,    ".012345678"},
            {0, 9, 123456789,   ".123456789"},

            {1, 9, 0,           ".0"},
            {1, 9, 2,           ".000000002"},
            {1, 9, 20,          ".00000002"},
            {1, 9, 200,         ".0000002"},
            {1, 9, 2000,        ".000002"},
            {1, 9, 20000,       ".00002"},
            {1, 9, 200000,      ".0002"},
            {1, 9, 2000000,     ".002"},
            {1, 9, 20000000,    ".02"},
            {1, 9, 200000000,   ".2"},

            {2, 3, 0,           ".00"},
            {2, 3, 2,           ".000"},
            {2, 3, 20,          ".000"},
            {2, 3, 200,         ".000"},
            {2, 3, 2000,        ".000"},
            {2, 3, 20000,       ".000"},
            {2, 3, 200000,      ".000"},
            {2, 3, 2000000,     ".002"},
            {2, 3, 20000000,    ".02"},
            {2, 3, 200000000,   ".20"},
            {2, 3, 1,           ".000"},
            {2, 3, 12,          ".000"},
            {2, 3, 123,         ".000"},
            {2, 3, 1234,        ".000"},
            {2, 3, 12345,       ".000"},
            {2, 3, 123456,      ".000"},
            {2, 3, 1234567,     ".001"},
            {2, 3, 12345678,    ".012"},
            {2, 3, 123456789,   ".123"},

            {6, 6, 0,           ".000000"},
            {6, 6, 2,           ".000000"},
            {6, 6, 20,          ".000000"},
            {6, 6, 200,         ".000000"},
            {6, 6, 2000,        ".000002"},
            {6, 6, 20000,       ".000020"},
            {6, 6, 200000,      ".000200"},
            {6, 6, 2000000,     ".002000"},
            {6, 6, 20000000,    ".020000"},
            {6, 6, 200000000,   ".200000"},
            {6, 6, 1,           ".000000"},
            {6, 6, 12,          ".000000"},
            {6, 6, 123,         ".000000"},
            {6, 6, 1234,        ".000001"},
            {6, 6, 12345,       ".000012"},
            {6, 6, 123456,      ".000123"},
            {6, 6, 1234567,     ".001234"},
            {6, 6, 12345678,    ".012345"},
            {6, 6, 123456789,   ".123456"},
       };
    }

    @Test(dataProvider="Nanos")
    public void test_print_nanos(int minWidth, int maxWidth, int value, String result) throws Exception {
        getFormatter(NANO_OF_SECOND,  minWidth, maxWidth).printTo(new MockFieldValue(NANO_OF_SECOND, value), buf);
        if (result == null) {
            fail("Expected exception");
        }
        assertEquals(buf.toString(), result);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="Seconds")
    Object[][] provider_seconds() {
        return new Object[][] {
            {0, 9, 0,  ""},
            {0, 9, 3,  ".05"},
            {0, 9, 6,  ".1"},
            {0, 9, 9,  ".15"},
            {0, 9, 12, ".2"},
            {0, 9, 15, ".25"},
            {0, 9, 30, ".5"},
            {0, 9, 45, ".75"},

            {2, 2, 0,  ".00"},
            {2, 2, 3,  ".05"},
            {2, 2, 6,  ".10"},
            {2, 2, 9,  ".15"},
            {2, 2, 12, ".20"},
            {2, 2, 15, ".25"},
            {2, 2, 30, ".50"},
            {2, 2, 45, ".75"},
        };
    }

    @Test(dataProvider="Seconds")
    public void test_print_seconds(int minWidth, int maxWidth, int value, String result) throws Exception {
        getFormatter(SECOND_OF_MINUTE, minWidth, maxWidth).printTo(new MockFieldValue(SECOND_OF_MINUTE, value), buf);
        if (result == null) {
            fail("Expected exception");
        }
        assertEquals(buf.toString(), result);
    }

    //-----------------------------------------------------------------------
    // parse
    //-----------------------------------------------------------------------
    @Test(dataProvider="Nanos")
    public void test_reverseParse(int minWidth, int maxWidth, int value, String result) throws Exception {
        ParsePosition pos = new ParsePosition(0);
        int expectedValue = fixParsedValue(maxWidth, value);
        DateTimeBuilder dtb = getFormatter(NANO_OF_SECOND, minWidth, maxWidth).parseToBuilder(result, pos);
        assertEquals(pos.getIndex(), result.length());
        assertParsed(dtb, NANO_OF_SECOND, value == 0 && minWidth == 0 ? null : (long) expectedValue);
    }

    @Test(dataProvider="Nanos")
    public void test_reverseParse_followedByNonDigit(int minWidth, int maxWidth, int value, String result) throws Exception {
        ParsePosition pos = new ParsePosition(0);
        int expectedValue = fixParsedValue(maxWidth, value);
        DateTimeBuilder dtb = getFormatter(NANO_OF_SECOND, minWidth, maxWidth).parseToBuilder(result + " ", pos);
        assertEquals(pos.getIndex(), result.length());
        assertParsed(dtb, NANO_OF_SECOND, value == 0 && minWidth == 0 ? null : (long) expectedValue);
    }

    @Test(dataProvider="Nanos")
    public void test_reverseParse_preceededByNonDigit(int minWidth, int maxWidth, int value, String result) throws Exception {
        ParsePosition pos = new ParsePosition(1);
        int expectedValue = fixParsedValue(maxWidth, value);
        DateTimeBuilder dtb = getFormatter(NANO_OF_SECOND, minWidth, maxWidth).parseToBuilder(" " + result, pos);
        assertEquals(pos.getIndex(), result.length() + 1);
        assertParsed(dtb, NANO_OF_SECOND, value == 0 && minWidth == 0 ? null : (long) expectedValue);
    }

    private int fixParsedValue(int maxWidth, int value) {
        if (maxWidth < 9) {
            int power = (int) Math.pow(10, (9 - maxWidth));
            value = (value / power) * power;
        }
        return value;
    }

    @Test(dataProvider="Seconds")
    public void test_reverseParse_seconds(int minWidth, int maxWidth, int value, String result) throws Exception {
        ParsePosition pos = new ParsePosition(0);
        DateTimeBuilder dtb = getFormatter(SECOND_OF_MINUTE, minWidth, maxWidth).parseToBuilder(result, pos);
        assertEquals(pos.getIndex(), result.length());
        assertParsed(dtb, SECOND_OF_MINUTE, value == 0 && minWidth == 0 ? null : (long) value);
    }

    private void assertParsed(DateTimeBuilder dtb, DateTimeField field, Long value) {
        if (value == null) {
            assertEquals(dtb.containsFieldValue(field), false);
        } else {
            assertEquals(dtb.getLong(field), (long) value);
        }
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="ParseNothing")
    Object[][] provider_parseNothing() {
        return new Object[][] {
            {NANO_OF_SECOND, 3, 6, "", 0, 0},
            {NANO_OF_SECOND, 3, 6, "A", 0, 0},
            {NANO_OF_SECOND, 3, 6, ".", 0, 1},
            {NANO_OF_SECOND, 3, 6, ".5", 0, 1},
            {NANO_OF_SECOND, 3, 6, ".51", 0, 1},
            {NANO_OF_SECOND, 3, 6, ".A23456", 0, 1},
            {NANO_OF_SECOND, 3, 6, ".1A3456", 0, 1},
        };
    }

    @Test(dataProvider="ParseNothing")
    public void test_parse_nothing(DateTimeField field, int min, int max, String text, int pos, int expected) {
        ParsePosition ppos = new ParsePosition(pos);
        DateTimeBuilder dtb = getFormatter(field, min, max).parseToBuilder(text, ppos);
        assertNull(dtb);
        assertEquals(ppos.getErrorIndex(), expected);
    }

    //-----------------------------------------------------------------------
    public void test_toString() throws Exception {
        assertEquals(getFormatter(NANO_OF_SECOND, 3, 6).toString(), "Fraction(NanoOfSecond,3,6)");
    }

}
