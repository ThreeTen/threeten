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
package javax.time.format;

import javax.time.format.CharLiteralPrinterParser;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test CharLiteralPrinterParser.
 *
 * @author Stephen Colebourne
 */
@Test
public class TestCharLiteralParser extends AbstractTestPrinterParser {

    @DataProvider(name="success")
    Object[][] data_success() {
        return new Object[][] {
            // match
            {new CharLiteralPrinterParser('a'), true, "a", 0, 1},
            {new CharLiteralPrinterParser('a'), true, "aOTHER", 0, 1},
            {new CharLiteralPrinterParser('a'), true, "OTHERaOTHER", 5, 6},
            {new CharLiteralPrinterParser('a'), true, "OTHERa", 5, 6},
            
            // no match
            {new CharLiteralPrinterParser('a'), true, "", 0, ~0},
            {new CharLiteralPrinterParser('a'), true, "a", 1, ~1},
            {new CharLiteralPrinterParser('a'), true, "A", 0, ~0},
            {new CharLiteralPrinterParser('a'), true, "b", 0, ~0},
            {new CharLiteralPrinterParser('a'), true, "OTHERbOTHER", 5, ~5},
            {new CharLiteralPrinterParser('a'), true, "OTHERb", 5, ~5},
            
            // case insensitive
            {new CharLiteralPrinterParser('a'), false, "a", 0, 1},
            {new CharLiteralPrinterParser('a'), false, "A", 0, 1},
        };
    }

    @Test(dataProvider="success")
    public void test_parse_success(CharLiteralPrinterParser pp, boolean caseSensitive, String text, int pos, int expectedPos) {
        parseContext.setCaseSensitive(caseSensitive);
        int result = pp.parse(parseContext, text, pos);
        assertEquals(result, expectedPos);
        assertEquals(parseContext.getParsed().size(), 0);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="error")
    Object[][] data_error() {
        return new Object[][] {
            {new CharLiteralPrinterParser('a'), "a", -1, IndexOutOfBoundsException.class},
            {new CharLiteralPrinterParser('a'), "a", 2, IndexOutOfBoundsException.class},
        };
    }

    @Test(dataProvider="error")
    public void test_parse_error(CharLiteralPrinterParser pp, String text, int pos, Class<?> expected) {
        try {
            pp.parse(parseContext, text, pos);
        } catch (RuntimeException ex) {
            assertTrue(expected.isInstance(ex));
            assertEquals(parseContext.getParsed().size(), 0);
        }
    }

}
