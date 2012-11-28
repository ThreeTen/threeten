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

import static javax.time.calendrical.ChronoField.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import javax.time.DateTimeException;
import javax.time.LocalDate;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.MockFieldValue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ReducedPrinterParser.
 */
@Test(groups={"implementation"})
public class TestReducedPrinter extends AbstractTestPrinterParser {

    private DateTimeFormatter getFormatter0(DateTimeField field, int width, int baseValue) {
        return builder.appendValueReduced(field, width, baseValue).toFormatter(locale).withSymbols(symbols);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=DateTimeException.class)
    public void test_print_emptyCalendrical() throws Exception {
        getFormatter0(YEAR, 2, 2010).printTo(EMPTY_DTA, buf);
    }

    //-----------------------------------------------------------------------
    public void test_print_append() throws Exception {
        buf.append("EXISTING");
        getFormatter0(YEAR, 2, 2010).printTo(LocalDate.of(2012, 1, 1), buf);
        assertEquals(buf.toString(), "EXISTING12");
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="Pivot")
    Object[][] provider_pivot() {
        return new Object[][] {
            {1, 2010, 2010, "0"},
            {1, 2010, 2011, "1"},
            {1, 2010, 2012, "2"},
            {1, 2010, 2013, "3"},
            {1, 2010, 2014, "4"},
            {1, 2010, 2015, "5"},
            {1, 2010, 2016, "6"},
            {1, 2010, 2017, "7"},
            {1, 2010, 2018, "8"},
            {1, 2010, 2019, "9"},
            {1, 2010, 2009, "9"},
            {1, 2010, 2020, "0"},

            {2, 2010, 2010, "10"},
            {2, 2010, 2011, "11"},
            {2, 2010, 2021, "21"},
            {2, 2010, 2099, "99"},
            {2, 2010, 2100, "00"},
            {2, 2010, 2109, "09"},
            {2, 2010, 2009, "09"},
            {2, 2010, 2110, "10"},

            {2, 2005, 2005, "05"},
            {2, 2005, 2099, "99"},
            {2, 2005, 2100, "00"},
            {2, 2005, 2104, "04"},
            {2, 2005, 2004, "04"},
            {2, 2005, 2105, "05"},

            {3, 2005, 2005, "005"},
            {3, 2005, 2099, "099"},
            {3, 2005, 2100, "100"},
            {3, 2005, 2999, "999"},
            {3, 2005, 3000, "000"},
            {3, 2005, 3004, "004"},
            {3, 2005, 2004, "004"},
            {3, 2005, 3005, "005"},

            {9, 2005, 2005, "000002005"},
            {9, 2005, 2099, "000002099"},
            {9, 2005, 2100, "000002100"},
            {9, 2005, 999999999, "999999999"},
            {9, 2005, 1000000000, "000000000"},
            {9, 2005, 1000002004, "000002004"},
            {9, 2005, 2004, "000002004"},
            {9, 2005, 1000002005, "000002005"},

            {2, -2005, -2005, "05"},
            {2, -2005, -2000, "00"},
            {2, -2005, -1999, "99"},
            {2, -2005, -1904, "04"},
            {2, -2005, -2006, "06"},
            {2, -2005, -1905, "05"},
       };
    }

    @Test(dataProvider="Pivot")
    public void test_pivot(int width, int baseValue, int value, String result) throws Exception {
        try {
            getFormatter0(YEAR, width, baseValue).printTo(new MockFieldValue(YEAR, value), buf);
            if (result == null) {
                fail("Expected exception");
            }
            assertEquals(buf.toString(), result);
        } catch (DateTimePrintException ex) {
            if (result == null || value < 0) {
                assertEquals(ex.getMessage().contains(YEAR.getName()), true);
            } else {
                throw ex;
            }
        }
    }

    //-----------------------------------------------------------------------
    public void test_toString() throws Exception {
        assertEquals(getFormatter0(YEAR, 2, 2005).toString(), "ReducedValue(Year,2,2005)");
    }

}
