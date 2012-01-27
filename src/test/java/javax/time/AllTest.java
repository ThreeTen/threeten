/*
 * Copyright (c) 2007-2011, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time;

import java.util.Arrays;

import javax.time.calendar.TestAmPmOfDay;
import javax.time.calendar.TestCalendricalMatchers;
import javax.time.calendar.TestCalendricalNomalizer;
import javax.time.calendar.TestCalendricalRule;
import javax.time.calendar.TestClock;
import javax.time.calendar.TestClock_TimeSourceClock;
import javax.time.calendar.TestDateAdjusters;
import javax.time.calendar.TestDateResolvers;
import javax.time.calendar.TestDateTimeField;
import javax.time.calendar.TestDateTimeFields;
import javax.time.calendar.TestDateTimeRule;
import javax.time.calendar.TestDateTimeRuleRange;
import javax.time.calendar.TestDayOfWeek;
import javax.time.calendar.TestISOAlignedWeekOfMonthRule;
import javax.time.calendar.TestISOAlignedWeekOfYearRule;
import javax.time.calendar.TestISOAmPmOfDayRule;
import javax.time.calendar.TestISOChronology;
import javax.time.calendar.TestISOClockHourOfAmPmRule;
import javax.time.calendar.TestISOClockHourOfDayRule;
import javax.time.calendar.TestISODayOfMonthRule;
import javax.time.calendar.TestISODayOfWeekRule;
import javax.time.calendar.TestISODayOfYearRule;
import javax.time.calendar.TestISOHourOfAmPmRule;
import javax.time.calendar.TestISOHourOfDayRule;
import javax.time.calendar.TestISOMinuteOfDayRule;
import javax.time.calendar.TestISOMonthOfQuarterRule;
import javax.time.calendar.TestISOMonthOfYearRule;
import javax.time.calendar.TestISONanoOfDayRule;
import javax.time.calendar.TestISOQuarterOfYearRule;
import javax.time.calendar.TestISOWeekBasedYearRule;
import javax.time.calendar.TestISOWeekOfWeekBasedYearRule;
import javax.time.calendar.TestISOYearRule;
import javax.time.calendar.TestISOZeroEpochMonthRule;
import javax.time.calendar.TestLocalDate;
import javax.time.calendar.TestLocalDateTime;
import javax.time.calendar.TestLocalTime;
import javax.time.calendar.TestMonthDay;
import javax.time.calendar.TestMonthOfYear;
import javax.time.calendar.TestOffsetDate;
import javax.time.calendar.TestOffsetDateTime;
import javax.time.calendar.TestOffsetDateTime_instants;
import javax.time.calendar.TestOffsetTime;
import javax.time.calendar.TestPeriod;
import javax.time.calendar.TestPeriodField;
import javax.time.calendar.TestPeriodFields;
import javax.time.calendar.TestPeriodParser;
import javax.time.calendar.TestPeriodUnit;
import javax.time.calendar.TestQuarterOfYear;
import javax.time.calendar.TestWeekRules;
import javax.time.calendar.TestYear;
import javax.time.calendar.TestYearMonth;
import javax.time.calendar.TestZoneId;
import javax.time.calendar.TestZoneOffset;
import javax.time.calendar.TestZoneResolvers;
import javax.time.calendar.TestZonedDateTime;
import javax.time.calendar.format.TestCalendricalPrintException;
import javax.time.calendar.format.TestCaseSensitivePrinterParser;
import javax.time.calendar.format.TestCharLiteralParser;
import javax.time.calendar.format.TestCharLiteralPrinter;
import javax.time.calendar.format.TestDateTimeFormatSymbols;
import javax.time.calendar.format.TestDateTimeFormatter;
import javax.time.calendar.format.TestDateTimeFormatterBuilder;
import javax.time.calendar.format.TestDateTimeFormatters;
import javax.time.calendar.format.TestDateTimeParseContext;
import javax.time.calendar.format.TestFractionPrinterParser;
import javax.time.calendar.format.TestNumberParser;
import javax.time.calendar.format.TestNumberPrinter;
import javax.time.calendar.format.TestPadParserDecorator;
import javax.time.calendar.format.TestPadPrinterDecorator;
import javax.time.calendar.format.TestSimpleDateTimeTextProvider;
import javax.time.calendar.format.TestStrictLenientPrinterParser;
import javax.time.calendar.format.TestStringLiteralParser;
import javax.time.calendar.format.TestStringLiteralPrinter;
import javax.time.calendar.format.TestTextParser;
import javax.time.calendar.format.TestTextPrinter;
import javax.time.calendar.format.TestZoneIdParser;
import javax.time.calendar.format.TestZoneOffsetParser;
import javax.time.calendar.format.TestZoneOffsetPrinter;
import javax.time.calendar.i18n.TestCopticChronology;
import javax.time.calendar.i18n.TestCopticDate;
import javax.time.calendar.i18n.TestInterCalendarSystem;
import javax.time.calendar.zone.TestFixedZoneRules;
import javax.time.calendar.zone.TestStandardZoneRules;
import javax.time.calendar.zone.TestTZDBZoneRulesCompiler;
import javax.time.calendar.zone.TestZoneOffsetInfo;
import javax.time.calendar.zone.TestZoneOffsetTransition;
import javax.time.calendar.zone.TestZoneOffsetTransitionRule;
import javax.time.calendar.zone.TestZoneRules;
import javax.time.calendar.zone.TestZoneRulesBuilder;
import javax.time.calendar.zone.TestZoneRulesGroup;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.internal.Utils;

/**
 * Test class.
 * 
 * @author Stephen Colebourne
 */
public class AllTest {

    public static void main(String[] args) {
        TestNG testng = getTestSuite();
        testng.run();
    }

	static TestNG getTestSuite() {
		TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] {
            // main classes
            TestDuration.class,
            TestInstant.class,
            TestMathUtils.class,
            TestTAIInstant.class,
            TestTimeSource_Fixed.class,
            TestTimeSource_OffsetSystem.class,
            TestTimeSource_System.class,
            TestUTCInstant.class,
            TestUTCRules.class,
            // calendar classes
            TestAmPmOfDay.class,
            TestCalendricalMatchers.class,
            TestCalendricalNomalizer.class,
            TestCalendricalRule.class,
            TestClock.class,
            TestClock_TimeSourceClock.class,
            TestDateAdjusters.class,
            TestDateResolvers.class,
            TestDateTimeField.class,
            TestDateTimeFields.class,
            TestDateTimeRule.class,
            TestDateTimeRuleRange.class,
            TestDayOfWeek.class,
            TestISOAlignedWeekOfMonthRule.class,
            TestISOAlignedWeekOfYearRule.class,
            TestISOAmPmOfDayRule.class,
            TestISOChronology.class,
            TestISOClockHourOfAmPmRule.class,
            TestISOClockHourOfDayRule.class,
            TestISODayOfMonthRule.class,
            TestISODayOfWeekRule.class,
            TestISODayOfYearRule.class,
            TestISOHourOfAmPmRule.class,
            TestISOHourOfDayRule.class,
            TestISOMinuteOfDayRule.class,
            TestISOMonthOfQuarterRule.class,
            TestISOMonthOfYearRule.class,
            TestISONanoOfDayRule.class,
            TestISOQuarterOfYearRule.class,
            TestISOWeekBasedYearRule.class,
            TestISOWeekOfWeekBasedYearRule.class,
            TestISOYearRule.class,
            TestISOZeroEpochMonthRule.class,
            TestLocalDate.class,
            TestLocalDateTime.class,
            TestLocalTime.class,
            TestMonthDay.class,
            TestMonthOfYear.class,
            TestOffsetDate.class,
            TestOffsetDateTime.class,
            TestOffsetDateTime_instants.class,
            TestOffsetTime.class,
            TestPeriod.class,
            TestPeriodParser.class,
            TestPeriodField.class,
            TestPeriodFields.class,
            TestPeriodUnit.class,
            TestQuarterOfYear.class,
            TestWeekRules.class,
            TestYear.class,
            TestYearMonth.class,
            TestZonedDateTime.class,
            TestZoneId.class,
            TestZoneOffset.class,
            TestZoneResolvers.class,
            // format
            TestCalendricalPrintException.class,
            TestCaseSensitivePrinterParser.class,
            TestCharLiteralParser.class,
            TestCharLiteralPrinter.class,
            TestDateTimeFormatSymbols.class,
            TestDateTimeFormatter.class,
            TestDateTimeFormatters.class,
            TestDateTimeParseContext.class,
            TestDateTimeFormatters.class,
            TestDateTimeFormatterBuilder.class,
            TestFractionPrinterParser.class,
            TestNumberParser.class,
            TestNumberPrinter.class,
            TestPadParserDecorator.class,
            TestPadPrinterDecorator.class,
            TestSimpleDateTimeTextProvider.class,
            TestStrictLenientPrinterParser.class,
            TestStringLiteralPrinter.class,
            TestStringLiteralParser.class,
            TestTextPrinter.class,
            TestTextParser.class,
            TestZoneOffsetPrinter.class,
            TestZoneOffsetParser.class,
            TestZoneIdParser.class,
            // i18n
            TestCopticChronology.class,
            TestCopticDate.class,
            TestInterCalendarSystem.class,
            // zone
            TestFixedZoneRules.class,
            TestStandardZoneRules.class,
            TestTZDBZoneRulesCompiler.class,
            TestZoneOffsetInfo.class,
            TestZoneOffsetTransition.class,
            TestZoneOffsetTransitionRule.class,
            TestZoneRules.class,
            TestZoneRulesBuilder.class,
            TestZoneRulesGroup.class,
        });
//        testng.addListener(new DotTestListener());
//        testng.addListener(new TextReporter("All", 2));
        testng.addListener(new TestListenerAdapter() {
            private int count = 0;
            private void log() {
                // log dot every 25 tests
                if ((getPassedTests().size() + getFailedTests().size()) % 25 == 0) {
                    System.out.print('.');
                    if (++count == 40) {
                        count = 0;
                        System.out.println();
                    }
                }
            }
            @Override
            public void onTestSuccess(ITestResult tr) {
                super.onTestSuccess(tr);
                log();
            }
            @Override
            public void onTestFailure(ITestResult tr) {
                super.onTestFailure(tr);
                log();
                Throwable throwable = tr.getThrowable();
                String params = "";
                if (tr.getParameters() != null && tr.getParameters().length > 0) {
                    params = " " + Arrays.toString(tr.getParameters());
                    if (tr.getMethod().getMethod().getParameterTypes().length != tr.getParameters().length) {
                        params = " Method has wrong number of arguments for data provider";
                        throwable = null;
                    }
                }
                String desc = tr.getMethod().getDescription() == null ? "" : " " + tr.getMethod().getDescription();
                System.out.println("FAILED: " + tr.getName() + desc + params);
                if (throwable != null) {
                    System.out.println(Utils.stackTrace(throwable, false)[0]);
                }
            }
        });
		return testng;
	}

}
