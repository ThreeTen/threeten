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

import static org.testng.Assert.assertEquals;

import javax.time.LocalDate;

import org.testng.annotations.Test;

/**
 * Test PadPrinterDecorator.
 */
@Test(groups={"implementation"})
public class TestPadPrinterDecorator extends AbstractTestPrinterParser {

    //-----------------------------------------------------------------------
    public void test_print_emptyCalendrical() throws Exception {
        builder.padNext(3, '-').appendLiteral('Z');
        getFormatter().printTo(EMPTY_DTA, buf);
        assertEquals(buf.toString(), "--Z");
    }

    public void test_print_fullDateTime() throws Exception {
        builder.padNext(3, '-').appendLiteral('Z');
        getFormatter().printTo(LocalDate.of(2008, 12, 3), buf);
        assertEquals(buf.toString(), "--Z");
    }

    public void test_print_append() throws Exception {
        buf.append("EXISTING");
        builder.padNext(3, '-').appendLiteral('Z');
        getFormatter().printTo(EMPTY_DTA, buf);
        assertEquals(buf.toString(), "EXISTING--Z");
    }

    //-----------------------------------------------------------------------
    public void test_print_noPadRequiredSingle() throws Exception {
        builder.padNext(1, '-').appendLiteral('Z');
        getFormatter().printTo(EMPTY_DTA, buf);
        assertEquals(buf.toString(), "Z");
    }

    public void test_print_padRequiredSingle() throws Exception {
        builder.padNext(5, '-').appendLiteral('Z');
        getFormatter().printTo(EMPTY_DTA, buf);
        assertEquals(buf.toString(), "----Z");
    }

    public void test_print_noPadRequiredMultiple() throws Exception {
        builder.padNext(4, '-').appendLiteral("WXYZ");
        getFormatter().printTo(EMPTY_DTA, buf);
        assertEquals(buf.toString(), "WXYZ");
    }

    public void test_print_padRequiredMultiple() throws Exception {
        builder.padNext(5, '-').appendLiteral("WXYZ");
        getFormatter().printTo(EMPTY_DTA, buf);
        assertEquals(buf.toString(), "-WXYZ");
    }

    @Test(expectedExceptions=DateTimePrintException.class)
    public void test_print_overPad() throws Exception {
        builder.padNext(3, '-').appendLiteral("WXYZ");
        getFormatter().printTo(EMPTY_DTA, buf);
    }

    //-----------------------------------------------------------------------
    public void test_toString1() throws Exception {
        builder.padNext(5, ' ').appendLiteral('Y');
        assertEquals(getFormatter().toString(), "Pad('Y',5)");
    }

    public void test_toString2() throws Exception {
        builder.padNext(5, '-').appendLiteral('Y');
        assertEquals(getFormatter().toString(), "Pad('Y',5,'-')");
    }

}
