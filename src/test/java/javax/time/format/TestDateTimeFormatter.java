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
import static org.testng.Assert.assertSame;

import java.util.Locale;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test DateTimeFormatter.
 */
@Test
public class TestDateTimeFormatter {
    // TODO these tests are not tck, as they refer to a non-public class
    // rewrite whole test case to use BASIC_FORMATTER or similar

    @BeforeMethod(groups={"tck"})
    public void setUp() {
    }

    @Test(groups={"implementation"})
    public void test_withLocale_same() throws Exception {
        DateTimeFormatter base =
            new DateTimeFormatterBuilder().appendLiteral("ONE")
                                          .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                                          .toFormatter(Locale.ENGLISH)
                                          .withSymbols(DateTimeFormatSymbols.STANDARD);
        DateTimeFormatter test = base.withLocale(Locale.ENGLISH);
        assertSame(test, base);
    }

}
