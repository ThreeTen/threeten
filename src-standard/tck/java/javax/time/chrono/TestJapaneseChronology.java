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

import static org.testng.Assert.assertEquals;

import javax.time.DateTimeException;
import javax.time.LocalDate;
import javax.time.LocalDateTime;
import javax.time.calendrical.DateTimeAdjusters;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class TestJapaneseChronology {

    //-----------------------------------------------------------------------
    // Chrono.ofName("Japanese")  Lookup by name
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_chrono_byName() {
        Chronology c = JapaneseChronology.INSTANCE;
        Chronology japanese = Chronology.of("Japanese");
        Assert.assertNotNull(japanese, "The Japanese calendar could not be found byName");
        Assert.assertEquals(japanese.getId(), "Japanese", "Name mismatch");
    }

    //-----------------------------------------------------------------------
    // creation, toLocalDate()
    //-----------------------------------------------------------------------
    @DataProvider(name="samples")
    Object[][] data_samples() {
        return new Object[][] {
            {JapaneseChronology.INSTANCE.date(1, 1, 1), LocalDate.of(1, 1, 1)},
            {JapaneseChronology.INSTANCE.date(1, 1, 2), LocalDate.of(1, 1, 2)},
            {JapaneseChronology.INSTANCE.date(1, 1, 3), LocalDate.of(1, 1, 3)},

            {JapaneseChronology.INSTANCE.date(2, 1, 1), LocalDate.of(2, 1, 1)},
            {JapaneseChronology.INSTANCE.date(3, 1, 1), LocalDate.of(3, 1, 1)},
            {JapaneseChronology.INSTANCE.date(3, 12, 6), LocalDate.of(3, 12, 6)},
            {JapaneseChronology.INSTANCE.date(4, 1, 1), LocalDate.of(4, 1, 1)},
            {JapaneseChronology.INSTANCE.date(4, 7, 3), LocalDate.of(4, 7, 3)},
            {JapaneseChronology.INSTANCE.date(4, 7, 4), LocalDate.of(4, 7, 4)},
            {JapaneseChronology.INSTANCE.date(5, 1, 1), LocalDate.of(5, 1, 1)},
            {JapaneseChronology.INSTANCE.date(1662, 3, 3), LocalDate.of(1662, 3, 3)},
            {JapaneseChronology.INSTANCE.date(1728, 10, 28), LocalDate.of(1728, 10, 28)},
            {JapaneseChronology.INSTANCE.date(1728, 10, 29), LocalDate.of(1728, 10, 29)},
        };
    }

    @Test(dataProvider="samples", groups={"tck"})
    public void test_toLocalDate(ChronoLocalDate jdate, LocalDate iso) {
        assertEquals(LocalDate.from(jdate), iso);
    }

    @Test(dataProvider="samples", groups={"tck"})
    public void test_fromCalendrical(ChronoLocalDate jdate, LocalDate iso) {
        assertEquals(JapaneseChronology.INSTANCE.date(iso), jdate);
    }

    @DataProvider(name="badDates")
    Object[][] data_badDates() {
        return new Object[][] {
            {1728, 0, 0},

            {1728, -1, 1},
            {1728, 0, 1},
            {1728, 14, 1},
            {1728, 15, 1},

            {1728, 1, -1},
            {1728, 1, 0},
            {1728, 1, 32},

            {1728, 12, -1},
            {1728, 12, 0},
            {1728, 12, 32},
        };
    }

    @Test(dataProvider="badDates", groups={"tck"}, expectedExceptions=DateTimeException.class)
    public void test_badDates(int year, int month, int dom) {
        JapaneseChronology.INSTANCE.date(year, month, dom);
    }

    //-----------------------------------------------------------------------
    // with(WithAdjuster)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_adjust1() {
        ChronoLocalDate base = JapaneseChronology.INSTANCE.date(1728, 10, 29);
        ChronoLocalDate test = base.with(DateTimeAdjusters.lastDayOfMonth());
        assertEquals(test, JapaneseChronology.INSTANCE.date(1728, 10, 31));
    }

    @Test(groups={"tck"})
    public void test_adjust2() {
        ChronoLocalDate base = JapaneseChronology.INSTANCE.date(1728, 12, 2);
        ChronoLocalDate test = base.with(DateTimeAdjusters.lastDayOfMonth());
        assertEquals(test, JapaneseChronology.INSTANCE.date(1728, 12, 31));
    }

    //-----------------------------------------------------------------------
    // JapaneseDate.with(Local*)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_adjust_toLocalDate() {
        ChronoLocalDate jdate = JapaneseChronology.INSTANCE.date(1726, 1, 4);
        ChronoLocalDate test = jdate.with(LocalDate.of(2012, 7, 6));
        assertEquals(test, JapaneseChronology.INSTANCE.date(2012, 7, 6));
    }

//    @Test(groups={"tck"}, expectedExceptions=DateTimeException.class)
//    public void test_adjust_toMonth() {
//        ChronoLocalDate jdate = JapaneseChronology.INSTANCE.date(1726, 1, 4);
//        jdate.with(Month.APRIL);
//    }  // TODO: shouldn't really accept ISO Month

    //-----------------------------------------------------------------------
    // LocalDate.with(JapaneseDate)
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_LocalDate_adjustToJapaneseDate() {
        ChronoLocalDate jdate = JapaneseChronology.INSTANCE.date(1728, 10, 29);
        LocalDate test = LocalDate.MIN_DATE.with(jdate);
        assertEquals(test, LocalDate.of(1728, 10, 29));
    }

    @Test(groups={"tck"})
    public void test_LocalDateTime_adjustToJapaneseDate() {
        ChronoLocalDate jdate = JapaneseChronology.INSTANCE.date(1728, 10, 29);
        LocalDateTime test = LocalDateTime.MIN_DATE_TIME.with(jdate);
        assertEquals(test, LocalDateTime.of(1728, 10, 29, 0, 0));
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="toString")
    Object[][] data_toString() {
        return new Object[][] {
            {JapaneseChronology.INSTANCE.date(0001,  1,  1), "0001-01-01"},
            {JapaneseChronology.INSTANCE.date(1728, 10, 28), "1728-10-28"},
            {JapaneseChronology.INSTANCE.date(1728, 10, 29), "1728-10-29"},
            {JapaneseChronology.INSTANCE.date(1727, 12,  5), "1727-12-05"},
            {JapaneseChronology.INSTANCE.date(1727, 12,  6), "1727-12-06"},
            {JapaneseChronology.INSTANCE.date(1868,  9,  8), "M1-09-08"},
            {JapaneseChronology.INSTANCE.date(1912,  7, 29), "M45-07-29"},
            {JapaneseChronology.INSTANCE.date(1912,  7, 30), "T1-07-30"},
            {JapaneseChronology.INSTANCE.date(1926, 12, 24), "T15-12-24"},
            {JapaneseChronology.INSTANCE.date(1926, 12, 25), "S1-12-25"},
            {JapaneseChronology.INSTANCE.date(1989,  1,  7), "S64-01-07"},
            {JapaneseChronology.INSTANCE.date(1989,  1,  8), "H1-01-08"},
            {JapaneseChronology.INSTANCE.date(2012, 12,  6), "H24-12-06"},
        };
    }

    @Test(dataProvider="toString", groups={"tck"})
    public void test_toString(ChronoLocalDate jdate, String expected) {
        assertEquals(jdate.toString(), expected);
    }


}
