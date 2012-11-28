/*
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
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

import java.util.Locale;

import javax.time.DateTimeException;
import javax.time.ZoneId;
import javax.time.ZonedDateTime;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeField;
import javax.time.jdk8.DefaultInterfaceDateTimeAccessor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Abstract PrinterParser test.
 */
@Test
public class AbstractTestPrinterParser {

    protected StringBuilder buf;
    protected DateTimeFormatterBuilder builder;
    protected DateTimeAccessor dta;
    protected Locale locale;
    protected DateTimeFormatSymbols symbols;


    @BeforeMethod(groups={"tck"})
    public void setUp() {
        buf = new StringBuilder();
        builder = new DateTimeFormatterBuilder();
        dta = ZonedDateTime.of(2011, 6, 30, 12, 30, 40, 0, ZoneId.of("Europe/Paris"));
        locale = Locale.ENGLISH;
        symbols = DateTimeFormatSymbols.STANDARD;
    }

    protected void setCaseSensitive(boolean caseSensitive) {
        if (caseSensitive)
            builder.parseCaseSensitive();
        else
            builder.parseCaseInsensitive();
    }

    protected void setStrict(boolean strict) {
        if (strict)
            builder.parseStrict();
        else
            builder.parseLenient();
    }

    protected DateTimeFormatter getFormatter() {
        return builder.toFormatter(locale).withSymbols(symbols);
    }

    protected DateTimeFormatter getFormatter(char c) {
        return builder.appendLiteral(c).toFormatter(locale).withSymbols(symbols);
    }

    protected DateTimeFormatter getFormatter(String s) {
        return builder.appendLiteral(s).toFormatter(locale).withSymbols(symbols);
    }

    protected DateTimeFormatter getFormatter(DateTimeField field, TextStyle style) {
        return builder.appendText(field, style).toFormatter(locale).withSymbols(symbols);
    }

    protected DateTimeFormatter getFormatter(DateTimeField field, int minWidth, int maxWidth, SignStyle signStyle) {
        return builder.appendValue(field, minWidth, maxWidth, signStyle).toFormatter(locale).withSymbols(symbols);
    }

    protected DateTimeFormatter getFormatter(String noOffsetText, String pattern) {
        return builder.appendOffset(noOffsetText, pattern).toFormatter(locale).withSymbols(symbols);
    }

    protected static final DateTimeAccessor EMPTY_DTA = new DefaultInterfaceDateTimeAccessor() {
        public boolean isSupported(DateTimeField field) {
            return true;
        }
        @Override
        public long getLong(DateTimeField field) {
            throw new DateTimeException("Mock");
        }
    };
}
