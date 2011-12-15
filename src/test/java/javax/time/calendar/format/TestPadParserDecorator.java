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

import static javax.time.chronology.ISODateTimeRule.MONTH_OF_YEAR;
import static org.testng.Assert.assertEquals;

import javax.time.chronology.DateTimeField;
import javax.time.chronology.DateTimeRule;

import org.testng.annotations.Test;

/**
 * Test PadPrinterParserDecorator.
 *
 * @author Stephen Colebourne
 */
@Test
public class TestPadParserDecorator extends AbstractTestPrinterParser {

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=IndexOutOfBoundsException.class)
    public void test_parse_negativePosition() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(null, new CharLiteralPrinterParser('Z'), 3, '-');
        pp.parse(parseContext, "--Z", -1);
    }

    @Test(expectedExceptions=IndexOutOfBoundsException.class)
    public void test_parse_offEndPosition() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(null, new CharLiteralPrinterParser('Z'), 3, '-');
        pp.parse(parseContext, "--Z", 4);
    }

    //-----------------------------------------------------------------------
    public void test_parse() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(
                null, new NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-');
        int result = pp.parse(parseContext, "--2", 0);
        assertEquals(result, 3);
        assertEquals(parseContext.getParsed().size(), 1);
        assertParsed(MONTH_OF_YEAR, 2L);
    }

    public void test_parse_noReadBeyond() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(
                null, new NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-');
        int result = pp.parse(parseContext, "--22", 0);
        assertEquals(result, 3);
        assertEquals(parseContext.getParsed().size(), 1);
        assertParsed(MONTH_OF_YEAR, 2L);
    }

    public void test_parse_textLessThanPadWidth() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(
                null, new NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-');
        int result = pp.parse(parseContext, "-1", 0);
        assertEquals(result, ~0);
        assertEquals(parseContext.getParsed().size(), 0);
    }

    public void test_parse_decoratedErrorPassedBack() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(
                null, new NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-');
        int result = pp.parse(parseContext, "--A", 0);
        assertEquals(result, ~2);
        assertEquals(parseContext.getParsed().size(), 0);
    }

    public void test_parse_decoratedDidNotParseToPadWidth() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(
                null, new NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-');
        int result = pp.parse(parseContext, "-1X", 0);
        assertEquals(result, ~0);
//        assertEquals(parseContext.getFieldValueMap().size(), 0);
    }

    //-----------------------------------------------------------------------
    public void test_parse_decoratedStartsWithPad() throws Exception {
        PadPrinterParserDecorator pp = new PadPrinterParserDecorator(
                null, new StringLiteralPrinterParser("-HELLO-"), 8, '-');
        int result = pp.parse(parseContext, "--HELLO-", 0);
        assertEquals(result, 8);
        assertEquals(parseContext.getParsed().size(), 0);
    }

    private void assertParsed(DateTimeRule rule, Number value) {
        if (value == null) {
            assertEquals(parseContext.getParsed(rule), null);
        } else {
            assertEquals(parseContext.getParsed(rule), DateTimeField.of(rule, value.longValue()));
        }
    }

}
