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

import javax.time.format.DateTimeFormatters;
import javax.time.format.CalendricalPrintException;
import javax.time.format.DateTimePrinter;
import javax.time.format.CompositePrinterParser;
import javax.time.format.DateTimeParseContext;
import javax.time.format.NumberPrinterParser;
import javax.time.format.DateTimeFormatter;
import javax.time.format.CalendricalParseException;
import javax.time.format.SignStyle;
import javax.time.format.StringLiteralPrinterParser;
import javax.time.format.DateTimeFormatSymbols;
import javax.time.format.DateTimeParser;
import static javax.time.chronology.ISODateTimeRule.DAY_OF_MONTH;
import static javax.time.chronology.ISODateTimeRule.MONTH_OF_YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.time.CalendricalException;
import javax.time.chronology.Calendrical;
import javax.time.chronology.CalendricalEngine;
import javax.time.chronology.CalendricalRule;
import javax.time.chronology.DateTimeField;
import javax.time.LocalDate;
import javax.time.LocalTime;
import javax.time.OffsetDate;
import javax.time.ZoneOffset;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test DateTimeFormatter.
 *
 * @author Stephen Colebourne
 */
@Test
public class TestDateTimeFormatter {

    private List<DateTimePrinter> printers;
    private List<DateTimeParser> parsers;
    private StringLiteralPrinterParser stringPP;
    private NumberPrinterParser numberPP;
    private CompositePrinterParser compPP;

    @BeforeMethod
    public void setUp() {
        printers = new ArrayList<DateTimePrinter>();
        parsers = new ArrayList<DateTimeParser>();
        stringPP = new StringLiteralPrinterParser("ONE");
        numberPP = new NumberPrinterParser(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE);
        printers.add(stringPP);
        printers.add(numberPP);
        parsers.add(stringPP);
        parsers.add(numberPP);
        compPP = new CompositePrinterParser(printers, parsers, false);
    }

    //-----------------------------------------------------------------------
    public void test_constructor() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        assertEquals(test.isPrintSupported(), true);
        assertEquals(test.isParseSupported(), true);
        assertEquals(test.getLocale(), Locale.ENGLISH);
    }

    public void test_constructor_empty() throws Exception {
        printers.clear();
        parsers.clear();
        DateTimeFormatter test = new DateTimeFormatter(Locale.FRANCE, DateTimeFormatSymbols.STANDARD, compPP);
        assertEquals(test.isPrintSupported(), true);
        assertEquals(test.isParseSupported(), true);
        assertEquals(test.getLocale(), Locale.FRANCE);
    }

    public void test_constructor_cannotPrint() throws Exception {
        printers.set(1, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.GERMANY, DateTimeFormatSymbols.STANDARD, compPP);
        assertEquals(test.isPrintSupported(), false);
        assertEquals(test.isParseSupported(), true);
        assertEquals(test.getLocale(), Locale.GERMANY);
    }

    public void test_constructor_cannotParse() throws Exception {
        parsers.set(1, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.US, DateTimeFormatSymbols.STANDARD, compPP);
        assertEquals(test.isPrintSupported(), true);
        assertEquals(test.isParseSupported(), false);
        assertEquals(test.getLocale(), Locale.US);
    }

    public void test_constructor_cannotPrintParse() throws Exception {
        printers.set(0, null);
        parsers.set(1, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.UK, DateTimeFormatSymbols.STANDARD, compPP);
        assertEquals(test.isPrintSupported(), false);
        assertEquals(test.isParseSupported(), false);
        assertEquals(test.getLocale(), Locale.UK);
    }

    //-----------------------------------------------------------------------
    public void test_withLocale() throws Exception {
        DateTimeFormatter base = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        DateTimeFormatter test = base.withLocale(Locale.GERMAN);
        assertEquals(test.getLocale(), Locale.GERMAN);
    }

    public void test_withLocale_same() throws Exception {
        DateTimeFormatter base = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        DateTimeFormatter test = base.withLocale(Locale.ENGLISH);
        assertSame(test, base);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withLocale_null() throws Exception {
        DateTimeFormatter base = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        base.withLocale((Locale) null);
    }

    //-----------------------------------------------------------------------
    // print
    //-----------------------------------------------------------------------
    public void test_print_Calendrical() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        String result = test.print(LocalDate.of(2008, 6, 30));
        assertEquals(result, "ONE30");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_print_Calendrical_noSuchField() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.print(LocalTime.of(11, 30));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_Calendrical_null() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.print((Calendrical) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_print_Calendrical_noPrinting() throws Exception {
        printers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.print(LocalDate.of(2008, 6, 30));
    }

    //-----------------------------------------------------------------------
    public void test_print_CalendricalAppendable() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        StringBuilder buf = new StringBuilder();
        test.printTo(LocalDate.of(2008, 6, 30), buf);
        assertEquals(buf.toString(), "ONE30");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_print_CalendricalAppendable_noSuchField() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        StringBuilder buf = new StringBuilder();
        test.printTo(LocalTime.of(11, 30), buf);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_CalendricalAppendable_nullCalendrical() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        StringBuilder buf = new StringBuilder();
        test.printTo((Calendrical) null, buf);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_CalendricalAppendable_nullAppendable() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.printTo(LocalDate.of(2008, 6, 30), (Appendable) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_print_CalendricalAppendable_noPrinting() throws Exception {
        printers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.printTo(LocalDate.of(2008, 6, 30), new StringBuilder());
    }

    @Test(expectedExceptions=IOException.class)  // IOException
    public void test_print_CalendricalAppendable_ioError() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.printTo(LocalDate.of(2008, 6, 30), new MockIOExceptionAppendable());
        } catch (CalendricalPrintException ex) {
            assertEquals(ex.getCause() instanceof IOException, true);
            ex.rethrowIOException();
        }
    }

    //-----------------------------------------------------------------------
    // parse
    //-----------------------------------------------------------------------
    public void test_parse_Rule_String() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        DateTimeField result = test.parse("ONE30", DAY_OF_MONTH);
        assertEquals(result, DAY_OF_MONTH.field(30L));
    }

    public void test_parse_Rule_CharSequence() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        DateTimeField result = test.parse(new StringBuilder("ONE30"), DAY_OF_MONTH);
        assertEquals(result, DAY_OF_MONTH.field(30L));
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parse_Rule_String_parseError() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parse("ONEXXX", DAY_OF_MONTH);
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONEXXX"), true);
            assertEquals(ex.getParsedString(), "ONEXXX");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parse_Rule_String_parseErrorLongText() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parse("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789", DAY_OF_MONTH);
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parse_Rule_String_parseIncomplete() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parse("ONE30SomethingElse", DAY_OF_MONTH);
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONE30SomethingElse"), true);
            assertEquals(ex.getParsedString(), "ONE30SomethingElse");
            assertEquals(ex.getErrorIndex(), 5);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_Rule_String_nullText() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parse((String) null, DAY_OF_MONTH);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_Rule_String_nullRule() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parse("30", (CalendricalRule<?>) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_parse_Rule_String_noParsing() throws Exception {
        parsers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parse("ONE30", DAY_OF_MONTH);
    }

    //-----------------------------------------------------------------------
    public void test_parseBest_firstOption() throws Exception {
        DateTimeFormatter test = DateTimeFormatters.pattern("yyyy-MM-dd[ZZZ]");
        Calendrical result = test.parseBest("2011-06-30+03:00", OffsetDate.rule(), LocalDate.rule());
        assertEquals(result, OffsetDate.of(2011, 6, 30, ZoneOffset.ofHours(3)));
    }

    public void test_parseBest_secondOption() throws Exception {
        DateTimeFormatter test = DateTimeFormatters.pattern("yyyy-MM-dd[ZZZ]");
        Calendrical result = test.parseBest("2011-06-30", OffsetDate.rule(), LocalDate.rule());
        assertEquals(result, LocalDate.of(2011, 6, 30));
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parseBest_String_parseError() throws Exception {
        DateTimeFormatter test = DateTimeFormatters.pattern("yyyy-MM-dd[ZZZ]");
        try {
            test.parseBest("2011-06-XX", OffsetDate.rule(), LocalDate.rule());
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("XX"), true);
            assertEquals(ex.getParsedString(), "2011-06-XX");
            assertEquals(ex.getErrorIndex(), 8);
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parseBest_String_parseErrorLongText() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parseBest("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789", DAY_OF_MONTH, MONTH_OF_YEAR);
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parseBest_String_parseIncomplete() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parseBest("ONE30SomethingElse", DAY_OF_MONTH, MONTH_OF_YEAR);
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONE30SomethingElse"), true);
            assertEquals(ex.getParsedString(), "ONE30SomethingElse");
            assertEquals(ex.getErrorIndex(), 5);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseBest_String_nullText() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseBest((String) null, DAY_OF_MONTH, MONTH_OF_YEAR);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseBest_String_nullRules() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseBest("30", (CalendricalRule<?>[]) null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_parseBest_String_zeroRules() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseBest("30", new CalendricalRule<?>[0]);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_parseBest_String_oneRule() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseBest("30", DAY_OF_MONTH);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_parseBest_Rule_String_noParsing() throws Exception {
        parsers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseBest("ONE30", OffsetDate.rule(), LocalDate.rule());
    }

    //-----------------------------------------------------------------------
    public void test_parseToEngine_String() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        CalendricalEngine result = test.parseToEngine("ONE30");
        assertEquals(result.getInput().size(), 1);
        assertEquals(result.getInput().get(0), DAY_OF_MONTH.field(30L));
    }

    public void test_parseToEngine_CharSequence() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        CalendricalEngine result = test.parseToEngine(new StringBuilder("ONE30"));
        assertEquals(result.getInput().size(), 1);
        assertEquals(result.getInput().get(0), DAY_OF_MONTH.field(30L));
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parseToEngine_String_parseError() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parseToEngine("ONEXXX");
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("ONEXXX"), true);
            assertEquals(ex.getParsedString(), "ONEXXX");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parseToEngine_String_parseErrorLongText() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parseToEngine("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalParseException.class)
    public void test_parseToEngine_String_parseIncomplete() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        try {
            test.parseToEngine("ONE30SomethingElse");
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("ONE30SomethingElse"), true);
            assertEquals(ex.getParsedString(), "ONE30SomethingElse");
            assertEquals(ex.getErrorIndex(), 5);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseToEngine_String_null() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseToEngine((String) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_parseToEngine_String_noParsing() throws Exception {
        parsers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseToEngine("ONE30");
    }

    //-----------------------------------------------------------------------
    public void test_parseToContext_StringParsePosition() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        ParsePosition pos = new ParsePosition(0);
        DateTimeParseContext result = test.parseToContext("ONE30XXX", pos);
        assertEquals(pos.getIndex(), 5);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.getParsed().size(), 1);
        assertEquals(result.getParsed(DAY_OF_MONTH), DAY_OF_MONTH.field(30L));
    }

    public void test_parseToContext_StringParsePosition_parseError() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        ParsePosition pos = new ParsePosition(0);
        DateTimeParseContext result = test.parseToContext("ONEXXX", pos);
        assertEquals(pos.getIndex(), 0);  // TODO: is this right?
        assertEquals(pos.getErrorIndex(), 3);
        assertEquals(result, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseToContext_StringParsePosition_nullString() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        ParsePosition pos = new ParsePosition(0);
        test.parseToContext((String) null, pos);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseToContext_StringParsePosition_nullParsePosition() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseToContext("ONE30", (ParsePosition) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_parseToContext_StringParsePosition_noParsing() throws Exception {
        parsers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.parseToContext("ONE30", new ParsePosition(0));
    }

    @Test(expectedExceptions=IndexOutOfBoundsException.class)
    public void test_parseToContext_StringParsePosition_invalidPosition() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        ParsePosition pos = new ParsePosition(6);
        test.parseToContext("ONE30", pos);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_toFormat_format() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        String result = format.format(LocalDate.of(2008, 6, 30));
        assertEquals(result, "ONE30");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_format_null() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.format(null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_toFormat_format_notCalendrical() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.format("Not a Calendrical");
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_toFormat_format_noPrinting() throws Exception {
        printers.set(1, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.format(LocalDate.of(2008, 6, 30));
    }

    //-----------------------------------------------------------------------
    public void test_toFormat_parseObject_String() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        CalendricalEngine result = (CalendricalEngine) format.parseObject("ONE30");
        assertEquals(result.getInput().size(), 1);
        assertEquals(result.getInput().get(0), DAY_OF_MONTH.field(30L));
    }

    @Test(expectedExceptions=ParseException.class)
    public void test_toFormat_parseObject_String_parseError() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        try {
            format.parseObject("ONEXXX");
        } catch (ParseException ex) {
            assertEquals(ex.getMessage().contains("ONEXXX"), true);
            assertEquals(ex.getErrorOffset(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=ParseException.class)
    public void test_toFormat_parseObject_String_parseErrorLongText() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        try {
            format.parseObject("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
        } catch (CalendricalParseException ex) {
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_parseObject_String_null() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.parseObject((String) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_toFormat_parseObject_String_noParsing() throws Exception {
        parsers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.parseObject("ONE30");
    }

    //-----------------------------------------------------------------------
    public void test_toFormat_parseObject_StringParsePosition() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        ParsePosition pos = new ParsePosition(0);
        CalendricalEngine result = (CalendricalEngine) format.parseObject("ONE30XXX", pos);
        assertEquals(pos.getIndex(), 5);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.getInput().size(), 1);
        assertEquals(result.getInput().get(0), DAY_OF_MONTH.field(30));
    }

    public void test_toFormat_parseObject_StringParsePosition_parseError() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        ParsePosition pos = new ParsePosition(0);
        Calendrical result = (Calendrical) format.parseObject("ONEXXX", pos);
        assertEquals(pos.getIndex(), 0);  // TODO: is this right?
        assertEquals(pos.getErrorIndex(), 3);
        assertEquals(result, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_parseObject_StringParsePosition_nullString() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        ParsePosition pos = new ParsePosition(0);
        format.parseObject((String) null, pos);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_parseObject_StringParsePosition_nullParsePosition() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.parseObject("ONE30", (ParsePosition) null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_toFormat_parseObject_StringParsePosition_noParsing() throws Exception {
        parsers.set(0, null);
        compPP = new CompositePrinterParser(printers, parsers, false);
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        format.parseObject("ONE30", new ParsePosition(0));
    }

    @Test(expectedExceptions=IndexOutOfBoundsException.class)
    public void test_toFormat_parseObject_StringParsePosition_invalidPosition() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        ParsePosition pos = new ParsePosition(6);
        Format format = test.toFormat();
        format.parseObject("ONE30", pos);
    }

    //-----------------------------------------------------------------------
    public void test_toFormat_Rule_format() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat();
        String result = format.format(LocalDate.of(2008, 6, 30));
        assertEquals(result, "ONE30");
    }

    public void test_toFormat_Rule_parseObject_String() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        Format format = test.toFormat(DAY_OF_MONTH);
        DateTimeField result = (DateTimeField) format.parseObject("ONE30");
        assertEquals(result, DAY_OF_MONTH.field(30L));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_Rule() throws Exception {
        DateTimeFormatter test = new DateTimeFormatter(Locale.ENGLISH, DateTimeFormatSymbols.STANDARD, compPP);
        test.toFormat(null);
    }

}
