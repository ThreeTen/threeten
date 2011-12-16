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

import javax.time.chronology.TestUTCInstant;
import javax.time.chronology.TestTAIInstant;
import javax.time.chronology.TestUTCRules;
import java.util.Arrays;

import javax.time.chronology.TestAmPmOfDay;
import javax.time.chronology.TestCalendricalMatchers;
import javax.time.chronology.TestCalendricalNomalizer;
import javax.time.chronology.TestCalendricalRule;
import javax.time.chronology.TestClock;
import javax.time.chronology.TestClock_TimeSourceClock;
import javax.time.chronology.TestDateAdjusters;
import javax.time.chronology.TestDateResolvers;
import javax.time.chronology.TestDateTimeField;
import javax.time.chronology.TestDateTimeFields;
import javax.time.chronology.TestDateTimeRule;
import javax.time.chronology.TestDateTimeRuleRange;
import javax.time.chronology.TestDayOfWeek;
import javax.time.chronology.TestISOAlignedWeekOfMonthRule;
import javax.time.chronology.TestISOAlignedWeekOfYearRule;
import javax.time.chronology.TestISOAmPmOfDayRule;
import javax.time.chronology.TestISOChronology;
import javax.time.chronology.TestISOClockHourOfAmPmRule;
import javax.time.chronology.TestISOClockHourOfDayRule;
import javax.time.chronology.TestISODayOfMonthRule;
import javax.time.chronology.TestISODayOfWeekRule;
import javax.time.chronology.TestISODayOfYearRule;
import javax.time.chronology.TestISOHourOfAmPmRule;
import javax.time.chronology.TestISOHourOfDayRule;
import javax.time.chronology.TestISOMinuteOfDayRule;
import javax.time.chronology.TestISOMonthOfQuarterRule;
import javax.time.chronology.TestISOMonthOfYearRule;
import javax.time.chronology.TestISONanoOfDayRule;
import javax.time.chronology.TestISOQuarterOfYearRule;
import javax.time.chronology.TestISOWeekBasedYearRule;
import javax.time.chronology.TestISOWeekOfWeekBasedYearRule;
import javax.time.chronology.TestISOYearRule;
import javax.time.chronology.TestISOZeroEpochMonthRule;
import javax.time.chronology.TestLocalDate;
import javax.time.chronology.TestLocalDateTime;
import javax.time.chronology.TestLocalTime;
import javax.time.chronology.TestMonthDay;
import javax.time.chronology.TestMonthOfYear;
import javax.time.chronology.TestOffsetDate;
import javax.time.chronology.TestOffsetDateTime;
import javax.time.chronology.TestOffsetDateTime_instants;
import javax.time.chronology.TestOffsetTime;
import javax.time.chronology.TestPeriod;
import javax.time.chronology.TestPeriodField;
import javax.time.chronology.TestPeriodFields;
import javax.time.chronology.TestPeriodUnit;
import javax.time.chronology.TestQuarterOfYear;
import javax.time.chronology.TestWeekRules;
import javax.time.chronology.TestYear;
import javax.time.chronology.TestYearMonth;
import javax.time.chronology.TestZoneId;
import javax.time.chronology.TestZoneOffset;
import javax.time.chronology.TestZoneResolvers;
import javax.time.chronology.TestZonedDateTime;
import javax.time.format.TestCalendricalPrintException;
import javax.time.format.TestCaseSensitivePrinterParser;
import javax.time.format.TestCharLiteralParser;
import javax.time.format.TestCharLiteralPrinter;
import javax.time.format.TestDateTimeFormatSymbols;
import javax.time.format.TestDateTimeFormatter;
import javax.time.format.TestDateTimeFormatterBuilder;
import javax.time.format.TestDateTimeFormatters;
import javax.time.format.TestDateTimeParseContext;
import javax.time.format.TestFractionPrinterParser;
import javax.time.format.TestNumberParser;
import javax.time.format.TestNumberPrinter;
import javax.time.format.TestPadParserDecorator;
import javax.time.format.TestPadPrinterDecorator;
import javax.time.format.TestSimpleDateTimeTextProvider;
import javax.time.format.TestStrictLenientPrinterParser;
import javax.time.format.TestStringLiteralParser;
import javax.time.format.TestStringLiteralPrinter;
import javax.time.format.TestTextParser;
import javax.time.format.TestTextPrinter;
import javax.time.format.TestZoneIdParser;
import javax.time.format.TestZoneOffsetParser;
import javax.time.format.TestZoneOffsetPrinter;
import javax.time.i18n.TestCopticChronology;
import javax.time.i18n.TestCopticDate;
import javax.time.i18n.TestInterCalendarSystem;
import javax.time.zone.TestFixedZoneRules;
import javax.time.zone.TestStandardZoneRules;
import javax.time.zone.TestTZDBZoneRulesCompiler;
import javax.time.zone.TestZoneOffsetInfo;
import javax.time.zone.TestZoneOffsetTransition;
import javax.time.zone.TestZoneOffsetTransitionRule;
import javax.time.zone.TestZoneRules;
import javax.time.zone.TestZoneRulesBuilder;
import javax.time.zone.TestZoneRulesGroup;

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
        
        testng.run();
    }

}
