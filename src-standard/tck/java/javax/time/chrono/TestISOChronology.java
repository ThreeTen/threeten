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
package javax.time.chrono;

import static javax.time.calendrical.LocalDateTimeField.ERA;
import static javax.time.calendrical.LocalDateTimeField.YEAR;
import static javax.time.calendrical.LocalDateTimeField.YEAR_OF_ERA;
import static javax.time.chrono.ISOChronology.ERA_BCE;
import static javax.time.chrono.ISOChronology.ERA_CE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.time.DateTimeException;
import javax.time.LocalDate;
import javax.time.LocalDateTime;
import javax.time.calendrical.DateTimeAdjusters;
import javax.time.calendrical.LocalDateTimeField;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class TestISOChronology {

    //-----------------------------------------------------------------------
    // Chrono.ofName("ISO")  Lookup by name
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_chrono_byName() {
        Chronology ISO = Chronology.of("ISO");
        Assert.assertNotNull(ISO, "The ISO calendar could not be found byName");
        Assert.assertEquals(ISO.getId(), "ISO", "Name mismatch");
    }
    
    //-----------------------------------------------------------------------
    // Lookup by Singleton
    //-----------------------------------------------------------------------
    @Test(groups="tck")
    public void instanceNotNull() {
        assertNotNull(ISOChronology.INSTANCE);
    }
    
    //-----------------------------------------------------------------------
    // Era creation
    //-----------------------------------------------------------------------
    @Test(groups="tck")
    public void test_eraOf() {
        assertEquals(ISOChronology.INSTANCE.eraOf(0), ERA_BCE);
        assertEquals(ISOChronology.INSTANCE.eraOf(1), ERA_CE);
    }

    //-----------------------------------------------------------------------
    // creation, toLocalDate()
    //-----------------------------------------------------------------------
    @DataProvider(name="samples")
    Object[][] data_samples() {
        return new Object[][] {
            {ISOChronology.INSTANCE.date(1, 7, 8), LocalDate.of(1, 7, 8)},
            {ISOChronology.INSTANCE.date(1, 7, 20), LocalDate.of(1, 7, 20)},
            {ISOChronology.INSTANCE.date(1, 7, 21), LocalDate.of(1, 7, 21)},
            
            {ISOChronology.INSTANCE.date(2, 7, 8), LocalDate.of(2, 7, 8)},
            {ISOChronology.INSTANCE.date(3, 6, 27), LocalDate.of(3, 6, 27)},
            {ISOChronology.INSTANCE.date(3, 5, 23), LocalDate.of(3, 5, 23)},
            {ISOChronology.INSTANCE.date(4, 6, 16), LocalDate.of(4, 6, 16)},
            {ISOChronology.INSTANCE.date(4, 7, 3), LocalDate.of(4, 7, 3)},
            {ISOChronology.INSTANCE.date(4, 7, 4), LocalDate.of(4, 7, 4)},
            {ISOChronology.INSTANCE.date(5, 1, 1), LocalDate.of(5, 1, 1)},
            {ISOChronology.INSTANCE.date(1727, 3, 3), LocalDate.of(1727, 3, 3)},
            {ISOChronology.INSTANCE.date(1728, 10, 28), LocalDate.of(1728, 10, 28)},
            {ISOChronology.INSTANCE.date(2012, 10, 29), LocalDate.of(2012, 10, 29)},
        };
    }

    @Test(dataProvider="samples", groups={"tck"})
    public void test_toLocalDate(ChronoLocalDate ISODate, LocalDate iso) {
        assertEquals(LocalDate.from(ISODate), iso);
    }

    @Test(dataProvider="samples", groups={"tck"})
    public void test_fromCalendrical(ChronoLocalDate ISODate, LocalDate iso) {
        assertEquals(ISOChronology.INSTANCE.date(iso), ISODate);
    }

    @DataProvider(name="badDates")
    Object[][] data_badDates() {
        return new Object[][] {
            {2012, 0, 0},
            
            {2012, -1, 1},
            {2012, 0, 1},
            {2012, 14, 1},
            {2012, 15, 1},
            
            {2012, 1, -1},
            {2012, 1, 0},
            {2012, 1, 32},
            
            {2012, 12, -1},
            {2012, 12, 0},
            {2012, 12, 32},
        };
    }

    @Test(dataProvider="badDates", groups={"tck"}, expectedExceptions=DateTimeException.class)
    public void test_badDates(int year, int month, int dom) {
        ISOChronology.INSTANCE.date(year, month, dom);
    }
    
    @Test(groups="tck")
    public void test_date_withEra() {
        int year = 5;
        int month = 5;
        int dayOfMonth = 5;
        ChronoLocalDate ChronoLocalDate = ISOChronology.INSTANCE.date(ERA_BCE, year, month, dayOfMonth);
        assertEquals(ChronoLocalDate.getEra(), ERA_BCE);
        assertEquals(ChronoLocalDate.get(LocalDateTimeField.YEAR_OF_ERA), year);
        assertEquals(ChronoLocalDate.get(LocalDateTimeField.MONTH_OF_YEAR), month);
        assertEquals(ChronoLocalDate.get(LocalDateTimeField.DAY_OF_MONTH), dayOfMonth);
        
        assertEquals(ChronoLocalDate.get(YEAR), 1 + (-1 * year));
        assertEquals(ChronoLocalDate.get(ERA), 0);
        assertEquals(ChronoLocalDate.get(YEAR_OF_ERA), year);
    }

    @Test(expectedExceptions=DateTimeException.class, groups="tck")
    public void test_date_withEra_withWrongEra() {        
        ISOChronology.INSTANCE.date((Era)HijrahChronology.ERA_AH, 1, 1, 1);
    }

    //-----------------------------------------------------------------------
    // with(DateTimeAdjuster)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_adjust1() {
        ChronoLocalDate base = ISOChronology.INSTANCE.date(1728, 10, 28);
        ChronoLocalDate test = base.with(DateTimeAdjusters.lastDayOfMonth());
        assertEquals(test, ISOChronology.INSTANCE.date(1728, 10, 31));
    }

    @Test(groups={"tck"})
    public void test_adjust2() {
        ChronoLocalDate base = ISOChronology.INSTANCE.date(1728, 12, 2);
        ChronoLocalDate test = base.with(DateTimeAdjusters.lastDayOfMonth());
        assertEquals(test, ISOChronology.INSTANCE.date(1728, 12, 31));
    }

    //-----------------------------------------------------------------------
    // ISODate.with(Local*)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_adjust_toLocalDate() {
        ChronoLocalDate ISODate = ISOChronology.INSTANCE.date(1726, 1, 4);
        ChronoLocalDate test = ISODate.with(LocalDate.of(2012, 7, 6));
        assertEquals(test, ISOChronology.INSTANCE.date(2012, 7, 6));
    }

//    @Test(groups={"tck"}, expectedExceptions=DateTimeException.class)
//    public void test_adjust_toMonth() {
//        ChronoLocalDate ISODate = ISOChronology.INSTANCE.date(1726, 1, 4);
//        ISODate.with(Month.APRIL);
//    }  // TODO: shouldn't really accept ISO Month

    //-----------------------------------------------------------------------
    // LocalDate.with(ISODate)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_LocalDate_adjustToISODate() {
        ChronoLocalDate ISODate = ISOChronology.INSTANCE.date(1728, 10, 29);
        LocalDate test = LocalDate.MIN_DATE.with(ISODate);
        assertEquals(test, LocalDate.of(1728, 10, 29));
    }

    @Test(groups={"tck"})
    public void test_LocalDateTime_adjustToISODate() {
        ChronoLocalDate ISODate = ISOChronology.INSTANCE.date(1728, 10, 29);
        LocalDateTime test = LocalDateTime.MIN_DATE_TIME.with(ISODate);
        assertEquals(test, LocalDateTime.of(1728, 10, 29, 0, 0));
    }
    
    //-----------------------------------------------------------------------
    // isLeapYear()
    //-----------------------------------------------------------------------
    @DataProvider(name="leapYears")
    Object[][] leapYearInformation() {
        return new Object[][] {
                {2000, true},
                {1996, true},
                {1600, true},
                
                {1900, false},
                {2100, false},
        };
    }
    
    @Test(dataProvider="leapYears", groups="tck")
    public void test_isLeapYear(int year, boolean isLeapYear) {        
        assertEquals(ISOChronology.INSTANCE.isLeapYear(year), isLeapYear);
    }
    
    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @Test(groups="tck")
    public void test_now() {
        assertEquals(LocalDate.from(ISOChronology.INSTANCE.dateNow()), LocalDate.now());
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="toString")
    Object[][] data_toString() {
        return new Object[][] {
            {ISOChronology.INSTANCE.date(1, 1, 1), "0001-01-01"},
            {ISOChronology.INSTANCE.date(1728, 10, 28), "1728-10-28"},
            {ISOChronology.INSTANCE.date(1728, 10, 29), "1728-10-29"},
            {ISOChronology.INSTANCE.date(1727, 12, 5), "1727-12-05"},
            {ISOChronology.INSTANCE.date(1727, 12, 6), "1727-12-06"},
        };
    }

    @Test(dataProvider="toString", groups={"tck"})
    public void test_toString(ChronoLocalDate ISODate, String expected) {
        assertEquals(ISODate.toString(), expected);
    }
    
    //-----------------------------------------------------------------------
    // equals()
    //-----------------------------------------------------------------------
    @Test(groups="tck")
    public void test_equals_true() {        
        assertTrue(ISOChronology.INSTANCE.equals(ISOChronology.INSTANCE));
    }
    
    @Test(groups="tck")
    public void test_equals_false() {        
        assertFalse(ISOChronology.INSTANCE.equals(HijrahChronology.INSTANCE));
    }

    
}
