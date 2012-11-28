/*
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
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
import static org.testng.Assert.assertTrue;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.time.ZoneId;
import javax.time.calendrical.DateTimeBuilder;
import javax.time.zone.ZoneRulesProvider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ZonePrinterParser.
 */
@Test(groups={"implementation"})
public class TestZoneIdParser extends AbstractTestPrinterParser {

    private static final String AMERICA_DENVER = "America/Denver";
    private static final ZoneId TIME_ZONE_DENVER = ZoneId.of(AMERICA_DENVER);

    private DateTimeFormatter getFormatter0(TextStyle style) {
        if (style == null)
            return builder.appendZoneId().toFormatter(locale).withSymbols(symbols);
        return builder.appendZoneText(style).toFormatter(locale).withSymbols(symbols);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="error")
    Object[][] data_error() {
        return new Object[][] {
            {null, "hello", -1, IndexOutOfBoundsException.class},
            {null, "hello", 6, IndexOutOfBoundsException.class},
        };
    }

    @Test(dataProvider="error")
    public void test_parse_error(TextStyle style, String text, int pos, Class<?> expected) {
        try {
            getFormatter0(style).parseToBuilder(text, new ParsePosition(pos));
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(expected.isInstance(ex));
        }
    }

    //-----------------------------------------------------------------------
    public void test_parse_exactMatch_Denver() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder(AMERICA_DENVER, pos);
        assertEquals(pos.getIndex(), AMERICA_DENVER.length());
        assertParsed(dtb, TIME_ZONE_DENVER);
    }

    public void test_parse_startStringMatch_Denver() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder(AMERICA_DENVER + "OTHER", pos);
        assertEquals(pos.getIndex(), AMERICA_DENVER.length());
        assertParsed(dtb, TIME_ZONE_DENVER);
    }

    public void test_parse_midStringMatch_Denver() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHER" + AMERICA_DENVER + "OTHER", pos);
        assertEquals(pos.getIndex(), 5 + AMERICA_DENVER.length());
        assertParsed(dtb, TIME_ZONE_DENVER);
    }

    public void test_parse_endStringMatch_Denver() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHER" + AMERICA_DENVER, pos);
        assertEquals(pos.getIndex(), 5 + AMERICA_DENVER.length());
        assertParsed(dtb, TIME_ZONE_DENVER);
    }

    public void test_parse_partialMatch() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHERAmerica/Bogusville", pos);
        assertEquals(pos.getErrorIndex(), 5);  // TBD: -6 ?
        assertEquals(dtb, null);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="zones")
    Object[][] populateTestData() {
        List<String> ids = new ArrayList<>();
        Set<String> groupIds = ZoneRulesProvider.getAvailableGroupIds();
        for (String groupId : groupIds) {
            Set<String> regionIds = ZoneRulesProvider.getProvider(groupId).getAvailableRegionIds();
            for (String regionId : regionIds) {
                ids.add(groupId + ":" + regionId);
                if (groupId.equals("TZDB")) {
                    ids.add(regionId);
                }
            }
        }
        Object[][] rtnval = new Object[ids.size()][];
        int i = 0;
        for (String id : ids) {
            rtnval[i++] = new Object[] { id, ZoneId.of(id) };
        }
        return rtnval;
    }

    @Test(dataProvider="zones")
    public void test_parse_exactMatch(String parse, ZoneId expected) throws Exception {
        ParsePosition pos = new ParsePosition(0);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder(parse, pos);
        assertEquals(pos.getIndex(), parse.length());
        assertParsed(dtb, expected);
    }

    //-----------------------------------------------------------------------
    public void test_parse_endStringMatch_utc() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHERUTC", pos);
        assertEquals(pos.getIndex(), 8);
        assertParsed(dtb, ZoneId.UTC);
    }

    public void test_parse_endStringMatch_utc_plus1() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHERUTC+01:00", pos);
        assertEquals(pos.getIndex(), 14);
        assertParsed(dtb, ZoneId.of("UTC+01:00"));
    }

    //-----------------------------------------------------------------------
    public void test_parse_midStringMatch_utc() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHERUTCOTHER", pos);
        assertEquals(pos.getIndex(), 8);
        assertParsed(dtb, ZoneId.UTC);
    }

    public void test_parse_midStringMatch_utc_plus1() throws Exception {
        ParsePosition pos = new ParsePosition(5);
        DateTimeBuilder dtb = getFormatter0(null).parseToBuilder("OTHERUTC+01:00OTHER", pos);
        assertEquals(pos.getIndex(), 14);
        assertParsed(dtb, ZoneId.of("UTC+01:00"));
    }

    //-----------------------------------------------------------------------
    public void test_toString_id() {
        assertEquals(getFormatter0(null).toString(), "ZoneId()");
    }

    public void test_toString_text() {
        assertEquals(getFormatter0(TextStyle.FULL).toString(), "ZoneText(FULL)");
    }

    private void assertParsed(DateTimeBuilder dtb, ZoneId expectedZone) {
        assertEquals(dtb.extract(ZoneId.class), expectedZone);
    }

}
