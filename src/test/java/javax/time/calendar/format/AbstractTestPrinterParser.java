/*
 * Copyright (c) 2011, Stephen Colebourne & Michael Nascimento Santos
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

import java.util.Locale;

import javax.time.calendar.DateTimeFields;
import javax.time.calendar.ZoneId;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Abstract PrinterParser test.
 *
 * @author Stephen Colebourne
 */
@Test
public class AbstractTestPrinterParser {

    protected DateTimePrintContext printEmptyContext;
    protected DateTimePrintContext printContext;
    protected DateTimeParseContext parseContext;
    protected StringBuilder buf;

    @BeforeMethod(groups={"tck"})
    public void setUp() {
        printEmptyContext = new DateTimePrintContext(DateTimeFields.EMPTY, Locale.ENGLISH, DateTimeFormatSymbols.STANDARD);
        printContext = new DateTimePrintContext(ZonedDateTime.of(2011, 6, 30, 12, 30, 40, 0, ZoneId.of("Europe/Paris")), Locale.ENGLISH, DateTimeFormatSymbols.STANDARD);
        parseContext = new DateTimeParseContext(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD);
        buf = new StringBuilder();
    }

}
