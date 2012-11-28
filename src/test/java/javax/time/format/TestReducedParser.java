/*
 * Copyright (c) 2010-2012, Stephen Colebourne & Michael Nascimento Santos
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

import static javax.time.calendrical.ChronoField.DAY_OF_YEAR;
import static javax.time.calendrical.ChronoField.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParsePosition;

import javax.time.calendrical.DateTimeBuilder;
import javax.time.calendrical.DateTimeField;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ReducedPrinterParser.
 */
@Test(groups={"implementation"})
public class TestReducedParser extends AbstractTestPrinterParser {

    private DateTimeFormatter getFormatter0(DateTimeField field, int width, int baseValue) {
        return builder.appendValueReduced(field, width, baseValue).toFormatter(locale).withSymbols(symbols);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="error")
    Object[][] data_error() {
        return new Object[][] {
            {YEAR, 2, 2010, "12", -1, IndexOutOfBoundsException.class},
            {YEAR, 2, 2010, "12", 3, IndexOutOfBoundsException.class},
        };
    }

    @Test(dataProvider="error")
    public void test_parse_error(DateTimeField field, int width, int baseValue, String text, int pos, Class<?> expected) {
        try {
            getFormatter0(field, width, baseValue).parseToBuilder(text, new ParsePosition(pos));
        } catch (RuntimeException ex) {
            assertTrue(expected.isInstance(ex));
        }
    }

    //-----------------------------------------------------------------------
    public void test_parse_fieldRangeIgnored() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        DateTimeBuilder dtb = getFormatter0(DAY_OF_YEAR, 3, 10).parseToBuilder("456", pos);
        assertEquals(pos.getIndex(), 3);
        assertParsed(dtb, DAY_OF_YEAR, 456L);  // parsed dayOfYear=456
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="Parse")
    Object[][] provider_parse() {
        return new Object[][] {
             // negative zero
            {YEAR, 1, 2010, "-0", 0, 0, null},

            // general
            {YEAR, 2, 2010, "Xxx12Xxx", 3, 5, 2012},
            {YEAR, 2, 2010, "12345", 0, 2, 2012},
            {YEAR, 2, 2010, "12-45", 0, 2, 2012},

            // insufficient digits
            {YEAR, 2, 2010, "0", 0, 0, null},
            {YEAR, 2, 2010, "1", 0, 0, null},
            {YEAR, 2, 2010, "1", 1, 1, null},
            {YEAR, 2, 2010, "1-2", 0, 0, null},
            {YEAR, 2, 2010, "9", 0, 0, null},

            // other junk
            {YEAR, 2, 2010, "A0", 0, 0, null},
            {YEAR, 2, 2010, "0A", 0, 0, null},
            {YEAR, 2, 2010, "  1", 0, 0, null},
            {YEAR, 2, 2010, "-1", 0, 0, null},
            {YEAR, 2, 2010, "-10", 0, 0, null},

            // parse OK 1
            {YEAR, 1, 2010, "0", 0, 1, 2010},
            {YEAR, 1, 2010, "9", 0, 1, 2019},
            {YEAR, 1, 2010, "10", 0, 1, 2011},

            {YEAR, 1, 2005, "0", 0, 1, 2010},
            {YEAR, 1, 2005, "4", 0, 1, 2014},
            {YEAR, 1, 2005, "5", 0, 1, 2005},
            {YEAR, 1, 2005, "9", 0, 1, 2009},
            {YEAR, 1, 2005, "10", 0, 1, 2011},

            // parse OK 2
            {YEAR, 2, 2010, "00", 0, 2, 2100},
            {YEAR, 2, 2010, "09", 0, 2, 2109},
            {YEAR, 2, 2010, "10", 0, 2, 2010},
            {YEAR, 2, 2010, "99", 0, 2, 2099},
            {YEAR, 2, 2010, "100", 0, 2, 2010},

            // parse OK 2
            {YEAR, 2, -2005, "05", 0, 2, -2005},
            {YEAR, 2, -2005, "00", 0, 2, -2000},
            {YEAR, 2, -2005, "99", 0, 2, -1999},
            {YEAR, 2, -2005, "06", 0, 2, -1906},
            {YEAR, 2, -2005, "100", 0, 2, -1910},
       };
    }

    @Test(dataProvider="Parse")
    public void test_parse(DateTimeField field, int width, int baseValue, String input, int pos, int parseLen, Integer parseVal) {
        ParsePosition ppos = new ParsePosition(pos);
        DateTimeBuilder dtb = getFormatter0(field, width, baseValue).parseToBuilder(input, ppos);
        if (ppos.getErrorIndex() != -1) {
            assertEquals(ppos.getErrorIndex(), parseLen);
        } else {
            assertEquals(ppos.getIndex(), parseLen);
            assertParsed(dtb, YEAR, parseVal != null ? (long) parseVal : null);
        }
    }

    @Test(dataProvider="Parse")
    public void test_parseLenient(DateTimeField field, int width, int baseValue, String input, int pos, int parseLen, Integer parseVal) {
        setStrict(false);
        ParsePosition ppos = new ParsePosition(pos);
        DateTimeBuilder dtb = getFormatter0(field, width, baseValue).parseToBuilder(input, ppos);
        if (ppos.getErrorIndex() != -1) {
            assertEquals(ppos.getErrorIndex(), parseLen);
        } else {
            assertEquals(ppos.getIndex(), parseLen);
            assertParsed(dtb, YEAR, parseVal != null ? (long) parseVal : null);
        }
    }

    private void assertParsed(DateTimeBuilder dtb, DateTimeField field, Long value) {
        if (value == null) {
            assertEquals(dtb, null);
        } else {
            assertEquals(dtb.getLong(field), (long)value);
        }
    }

}
