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
package javax.time.calendar;

import static javax.time.calendar.DayOfWeek.MONDAY;
import static javax.time.calendar.DayOfWeek.TUESDAY;
import static javax.time.calendar.DayOfWeek.WEDNESDAY;
import static javax.time.calendar.ISODateTimeRule.DAY_OF_WEEK;
import static javax.time.calendar.ISODateTimeRule.MONTH_OF_YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Locale;

import javax.time.CalendricalException;
import javax.time.calendar.format.TextStyle;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test DayOfWeek.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestDayOfWeek {

    @BeforeMethod
    public void setUp() {
    }

    //-----------------------------------------------------------------------
    @Test(groups={"implementation"})
    public void test_interfaces() {
        assertTrue(Enum.class.isAssignableFrom(DayOfWeek.class));
        assertTrue(Serializable.class.isAssignableFrom(DayOfWeek.class));
        assertTrue(Comparable.class.isAssignableFrom(DayOfWeek.class));
        assertTrue(Calendrical.class.isAssignableFrom(DayOfWeek.class));
        assertTrue(CalendricalMatcher.class.isAssignableFrom(DayOfWeek.class));
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_rule() {
        assertEquals(DayOfWeek.rule().getName(), "DayOfWeek");
        assertEquals(DayOfWeek.rule().getType(), DayOfWeek.class);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_factory_int_singleton() {
        for (int i = 1; i <= 7; i++) {
            DayOfWeek test = DayOfWeek.of(i);
            assertEquals(test.getValue(), i);
            assertSame(DayOfWeek.of(i), test);
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class, groups={"tck"})
    public void test_factory_int_valueTooLow() {
        DayOfWeek.of(0);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class, groups={"tck"})
    public void test_factory_int_valueTooHigh() {
        DayOfWeek.of(8);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_factory_Calendricals() {
        assertEquals(DayOfWeek.from(LocalDate.of(2011, 6, 6)), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.from(DAY_OF_WEEK.field(4)), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.from(LocalDate.of(2011, 6, 6), MONDAY.toField()), DayOfWeek.MONDAY);
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_factory_Calendricals_invalid_clash() {
        DayOfWeek.from(TUESDAY, WEDNESDAY.toField());
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_factory_Calendricals_invalid_noDerive() {
        DayOfWeek.from(LocalTime.of(12, 30));
    }

    @Test(expectedExceptions=CalendricalException.class, groups={"tck"})
    public void test_factory_Calendricals_invalid_empty() {
        DayOfWeek.from();
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_factory_Calendricals_nullArray() {
        DayOfWeek.from((Calendrical[]) null);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_factory_Calendricals_null() {
        DayOfWeek.from((Calendrical) null);
    }

    //-----------------------------------------------------------------------
    // get()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_get() {
        assertEquals(DayOfWeek.MONDAY.get(DayOfWeek.rule()), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.FRIDAY.get(DayOfWeek.rule()), DayOfWeek.FRIDAY);
        
        assertEquals(DayOfWeek.MONDAY.get(DAY_OF_WEEK), DAY_OF_WEEK.field(1));
        assertEquals(DayOfWeek.THURSDAY.get(DAY_OF_WEEK), DAY_OF_WEEK.field(4));
        
        assertEquals(DayOfWeek.MONDAY.get(MONTH_OF_YEAR), null);
    }

    //-----------------------------------------------------------------------
    // getText()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_getText() {
        assertEquals(DayOfWeek.MONDAY.getText(TextStyle.SHORT, Locale.US), "Mon");
    }

    @Test(expectedExceptions = NullPointerException.class, groups={"tck"})
    public void test_getText_nullStyle() {
        DayOfWeek.MONDAY.getText(null, Locale.US);
    }

    @Test(expectedExceptions = NullPointerException.class, groups={"tck"})
    public void test_getText_nullLocale() {
        DayOfWeek.MONDAY.getText(TextStyle.FULL, null);
    }

    //-----------------------------------------------------------------------
    // next()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_next() {
        assertEquals(DayOfWeek.MONDAY.next(), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.TUESDAY.next(), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.WEDNESDAY.next(), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.THURSDAY.next(), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.FRIDAY.next(), DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.SATURDAY.next(), DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.SUNDAY.next(), DayOfWeek.MONDAY);
    }

    //-----------------------------------------------------------------------
    // previous()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_previous() {
        assertEquals(DayOfWeek.MONDAY.previous(), DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.TUESDAY.previous(), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.WEDNESDAY.previous(), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.THURSDAY.previous(), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.FRIDAY.previous(), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.SATURDAY.previous(), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.SUNDAY.previous(), DayOfWeek.SATURDAY);
    }

    //-----------------------------------------------------------------------
    // roll(int)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_plusDays_monday() {
        assertEquals(DayOfWeek.MONDAY.roll(-7), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.MONDAY.roll(-6), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.MONDAY.roll(-5), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.MONDAY.roll(-4), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.MONDAY.roll(-3), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.MONDAY.roll(-2), DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.MONDAY.roll(-1), DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.MONDAY.roll(0), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.MONDAY.roll(1), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.MONDAY.roll(2), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.MONDAY.roll(3), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.MONDAY.roll(4), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.MONDAY.roll(5), DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.MONDAY.roll(6), DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.MONDAY.roll(7), DayOfWeek.MONDAY);
    }

    @Test(groups={"tck"})
    public void test_roll_thursday() {
        assertEquals(DayOfWeek.THURSDAY.roll(-7), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(-6), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(-5), DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(-4), DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(-3), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(-2), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(-1), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(0), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(1), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(2), DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(3), DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(4), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(5), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(6), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.THURSDAY.roll(7), DayOfWeek.THURSDAY);
    }

    //-----------------------------------------------------------------------
    // matcher
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_matcher() {
        assertEquals(DayOfWeek.MONDAY.matchesCalendrical(DAY_OF_WEEK.field(1)), true);
        assertEquals(DayOfWeek.MONDAY.matchesCalendrical(MONDAY), true);
        assertEquals(DayOfWeek.MONDAY.matchesCalendrical(TUESDAY), false);
        
        assertEquals(DayOfWeek.MONDAY.matchesCalendrical(LocalDate.of(1970, 1, 5)), true);
        assertEquals(DayOfWeek.MONDAY.matchesCalendrical(LocalDate.of(1970, 1, 1)), false);
    }

    @Test(expectedExceptions = NullPointerException.class, groups={"tck"})
    public void test_matcher_null() {
        DayOfWeek.MONDAY.matchesCalendrical(null);
    }

    //-----------------------------------------------------------------------
    // toField()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_toField() {
        assertEquals(DayOfWeek.MONDAY.toField(), DAY_OF_WEEK.field(1));
        assertEquals(DayOfWeek.THURSDAY.toField(), DAY_OF_WEEK.field(4));
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_toString() {
        assertEquals(DayOfWeek.MONDAY.toString(), "MONDAY");
        assertEquals(DayOfWeek.TUESDAY.toString(), "TUESDAY");
        assertEquals(DayOfWeek.WEDNESDAY.toString(), "WEDNESDAY");
        assertEquals(DayOfWeek.THURSDAY.toString(), "THURSDAY");
        assertEquals(DayOfWeek.FRIDAY.toString(), "FRIDAY");
        assertEquals(DayOfWeek.SATURDAY.toString(), "SATURDAY");
        assertEquals(DayOfWeek.SUNDAY.toString(), "SUNDAY");
    }

    //-----------------------------------------------------------------------
    // generated methods
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_enum() {
        assertEquals(DayOfWeek.valueOf("MONDAY"), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.values()[0], DayOfWeek.MONDAY);
    }

}
