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
package javax.time.chronology;

import javax.time.chronology.Chronology;
import javax.time.chronology.Calendrical;
import javax.time.chronology.CalendricalRule;
import javax.time.YearMonth;
import javax.time.AmPmOfDay;
import javax.time.ZoneOffset;
import javax.time.LocalDateTime;
import javax.time.ZonedDateTime;
import javax.time.ZoneId;
import javax.time.LocalTime;
import javax.time.OffsetDateTime;
import javax.time.LocalDate;
import static javax.time.chronology.ISODateTimeRule.DAY_OF_MONTH;
import static javax.time.chronology.ISODateTimeRule.HOUR_OF_AMPM;
import static javax.time.chronology.ISODateTimeRule.HOUR_OF_DAY;
import static javax.time.chronology.ISODateTimeRule.MINUTE_OF_HOUR;
import static javax.time.chronology.ISODateTimeRule.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.time.CalendricalException;
import javax.time.Instant;
import javax.time.calendar.format.TextStyle;
import javax.time.calendar.zone.ZoneOffsetInfo;
import javax.time.calendar.zone.ZoneOffsetTransition;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test TimeZone.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestZoneId {

    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");
    public static final String LATEST_TZDB = "2010i";

    //-----------------------------------------------------------------------
    // Basics
    //-----------------------------------------------------------------------
    public void test_interfaces() {
        Object obj = ZoneId.UTC;
        assertTrue(obj instanceof Calendrical);
        assertTrue(obj instanceof Serializable);
    }

    public void test_immutable() {
        Class<ZoneId> cls = ZoneId.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()) ||
                        (Modifier.isVolatile(field.getModifiers()) && Modifier.isTransient(field.getModifiers())));
            }
        }
    }

    public void test_serialization_UTC() throws Exception {
        ZoneId test = ZoneId.UTC;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        ZoneId result = (ZoneId) in.readObject();
        
        assertSame(result, test);
    }

    public void test_serialization_fixed() throws Exception {
        ZoneId test = ZoneId.of("UTC+01:30");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        ZoneId result = (ZoneId) in.readObject();
        
        assertEquals(result, test);
    }

    public void test_serialization_Europe() throws Exception {
        ZoneId test = ZoneId.of("Europe/London");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        ZoneId result = (ZoneId) in.readObject();
        
        assertEquals(result, test);
    }

    public void test_serialization_America() throws Exception {
        ZoneId test = ZoneId.of("America/Chicago");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        ZoneId result = (ZoneId) in.readObject();
        
        assertEquals(result, test);
    }

    //-----------------------------------------------------------------------
    // UTC
    //-----------------------------------------------------------------------
    public void test_constant_UTC() {
        ZoneId test = ZoneId.UTC;
        assertEquals(test.getID(), "UTC");
        assertEquals(test.getGroupID(), "");
        assertEquals(test.getRegionID(), "UTC");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.getText(TextStyle.FULL, Locale.UK), "UTC");
        assertEquals(test.isFixed(), true);
        assertEquals(test.getRules().isFixedOffset(), true);
        assertEquals(test.getRules().getOffset(Instant.ofEpochSecond(0L)), ZoneOffset.UTC);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 6, 30));
        assertEquals(info.isTransition(), false);
        assertEquals(info.getTransition(), null);
        assertEquals(info.getOffset(), ZoneOffset.UTC);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.UTC);
        assertSame(test, ZoneId.of("UTC"));
        assertSame(test, ZoneId.of(ZoneOffset.UTC));
    }

    //-----------------------------------------------------------------------
    // OLD_IDS_PRE_2005
    //-----------------------------------------------------------------------
    public void test_constant_OLD_IDS_PRE_2005() {
        Map<String, String> ids = ZoneId.OLD_IDS_PRE_2005;
        assertEquals(ids.get("EST"), "America/Indianapolis");
        assertEquals(ids.get("MST"), "America/Phoenix");
        assertEquals(ids.get("HST"), "Pacific/Honolulu");
        assertEquals(ids.get("ACT"), "Australia/Darwin");
        assertEquals(ids.get("AET"), "Australia/Sydney");
        assertEquals(ids.get("AGT"), "America/Argentina/Buenos_Aires");
        assertEquals(ids.get("ART"), "Africa/Cairo");
        assertEquals(ids.get("AST"), "America/Anchorage");
        assertEquals(ids.get("BET"), "America/Sao_Paulo");
        assertEquals(ids.get("BST"), "Asia/Dhaka");
        assertEquals(ids.get("CAT"), "Africa/Harare");
        assertEquals(ids.get("CNT"), "America/St_Johns");
        assertEquals(ids.get("CST"), "America/Chicago");
        assertEquals(ids.get("CTT"), "Asia/Shanghai");
        assertEquals(ids.get("EAT"), "Africa/Addis_Ababa");
        assertEquals(ids.get("ECT"), "Europe/Paris");
        assertEquals(ids.get("IET"), "America/Indiana/Indianapolis");
        assertEquals(ids.get("IST"), "Asia/Kolkata");
        assertEquals(ids.get("JST"), "Asia/Tokyo");
        assertEquals(ids.get("MIT"), "Pacific/Apia");
        assertEquals(ids.get("NET"), "Asia/Yerevan");
        assertEquals(ids.get("NST"), "Pacific/Auckland");
        assertEquals(ids.get("PLT"), "Asia/Karachi");
        assertEquals(ids.get("PNT"), "America/Phoenix");
        assertEquals(ids.get("PRT"), "America/Puerto_Rico");
        assertEquals(ids.get("PST"), "America/Los_Angeles");
        assertEquals(ids.get("SST"), "Pacific/Guadalcanal");
        assertEquals(ids.get("VST"), "Asia/Ho_Chi_Minh");
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_constant_OLD_IDS_PRE_2005_immutable() {
        Map<String, String> ids = ZoneId.OLD_IDS_PRE_2005;
        ids.clear();
    }

    //-----------------------------------------------------------------------
    // OLD_IDS_POST_2005
    //-----------------------------------------------------------------------
    public void test_constant_OLD_IDS_POST_2005() {
        Map<String, String> ids = ZoneId.OLD_IDS_POST_2005;
        assertEquals(ids.get("EST"), "UTC-05:00");
        assertEquals(ids.get("MST"), "UTC-07:00");
        assertEquals(ids.get("HST"), "UTC-10:00");
        assertEquals(ids.get("ACT"), "Australia/Darwin");
        assertEquals(ids.get("AET"), "Australia/Sydney");
        assertEquals(ids.get("AGT"), "America/Argentina/Buenos_Aires");
        assertEquals(ids.get("ART"), "Africa/Cairo");
        assertEquals(ids.get("AST"), "America/Anchorage");
        assertEquals(ids.get("BET"), "America/Sao_Paulo");
        assertEquals(ids.get("BST"), "Asia/Dhaka");
        assertEquals(ids.get("CAT"), "Africa/Harare");
        assertEquals(ids.get("CNT"), "America/St_Johns");
        assertEquals(ids.get("CST"), "America/Chicago");
        assertEquals(ids.get("CTT"), "Asia/Shanghai");
        assertEquals(ids.get("EAT"), "Africa/Addis_Ababa");
        assertEquals(ids.get("ECT"), "Europe/Paris");
        assertEquals(ids.get("IET"), "America/Indiana/Indianapolis");
        assertEquals(ids.get("IST"), "Asia/Kolkata");
        assertEquals(ids.get("JST"), "Asia/Tokyo");
        assertEquals(ids.get("MIT"), "Pacific/Apia");
        assertEquals(ids.get("NET"), "Asia/Yerevan");
        assertEquals(ids.get("NST"), "Pacific/Auckland");
        assertEquals(ids.get("PLT"), "Asia/Karachi");
        assertEquals(ids.get("PNT"), "America/Phoenix");
        assertEquals(ids.get("PRT"), "America/Puerto_Rico");
        assertEquals(ids.get("PST"), "America/Los_Angeles");
        assertEquals(ids.get("SST"), "Pacific/Guadalcanal");
        assertEquals(ids.get("VST"), "Asia/Ho_Chi_Minh");
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_constant_OLD_IDS_POST_2005_immutable() {
        Map<String, String> ids = ZoneId.OLD_IDS_POST_2005;
        ids.clear();
    }

    //-----------------------------------------------------------------------
    // mapped factory
    //-----------------------------------------------------------------------
    public void test_of_string_Map() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("LONDON", "Europe/London");
        map.put("PARIS", "Europe/Paris");
        ZoneId test = ZoneId.of("LONDON", map);
        assertEquals(test.getID(), "Europe/London");
    }

    public void test_of_string_Map_lookThrough() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("LONDON", "Europe/London");
        map.put("PARIS", "Europe/Paris");
        ZoneId test = ZoneId.of("Europe/Madrid", map);
        assertEquals(test.getID(), "Europe/Madrid");
    }

    public void test_of_string_Map_emptyMap() {
        Map<String, String> map = new HashMap<String, String>();
        ZoneId test = ZoneId.of("Europe/Madrid", map);
        assertEquals(test.getID(), "Europe/Madrid");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_of_string_Map_unknown() {
        Map<String, String> map = new HashMap<String, String>();
        ZoneId.of("Unknown", map);
    }

    //-----------------------------------------------------------------------
    // regular factory
    //-----------------------------------------------------------------------
    @DataProvider(name="String_UTC")
    Object[][] data_of_string_UTC() {
        return new Object[][] {
            {""}, {"Z"},
            {"+00"},{"+0000"},{"+00:00"},{"+000000"},{"+00:00:00"},
            {"-00"},{"-0000"},{"-00:00"},{"-000000"},{"-00:00:00"},
        };
    }

    @Test(dataProvider="String_UTC")
    public void test_of_string_UTC(String id) {
        ZoneId test = ZoneId.of("UTC" + id);
        assertSame(test, ZoneId.UTC);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="String_Fixed")
    Object[][] data_of_string_Fixed() {
        return new Object[][] {
            {"", "UTC"},
            {"+01", "UTC+01:00"},
            {"+0100", "UTC+01:00"},{"+01:00", "UTC+01:00"},
            {"+010000", "UTC+01:00"},{"+01:00:00", "UTC+01:00"},
            {"+12", "UTC+12:00"},
            {"+1234", "UTC+12:34"},{"+12:34", "UTC+12:34"},
            {"+123456", "UTC+12:34:56"},{"+12:34:56", "UTC+12:34:56"},
            {"-02", "UTC-02:00"},
            {"-0200", "UTC-02:00"},{"-02:00", "UTC-02:00"},
            {"-020000", "UTC-02:00"},{"-02:00:00", "UTC-02:00"},
        };
    }

    @Test(dataProvider="String_Fixed")
    public void test_of_string_Fixed(String input, String id) {
        ZoneId test = ZoneId.of("UTC" + input);
        assertEquals(test.getID(), id);
        assertEquals(test.getGroupID(), "");
        assertEquals(test.getRegionID(), id);
        assertEquals(test.getVersionID(), "");
        assertEquals(test.getText(TextStyle.FULL, Locale.UK), id);
        assertEquals(test.isFixed(), true);
        assertEquals(test.getRules().isFixedOffset(), true);
        ZoneOffset offset = id.length() == 3 ? ZoneOffset.UTC : ZoneOffset.of(id.substring(3));
        assertEquals(test.getRules().getOffset(Instant.ofEpochSecond(0L)), offset);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 6, 30));
        assertEquals(info.isTransition(), false);
        assertEquals(info.getTransition(), null);
        assertEquals(info.getOffset(), offset);
        assertEquals(info.getEstimatedOffset(), offset);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="String_UTC_Invalid")
    Object[][] data_of_string_UTC_invalid() {
        return new Object[][] {
                {"A"}, {"B"}, {"C"}, {"D"}, {"E"}, {"F"}, {"G"}, {"H"}, {"I"}, {"J"}, {"K"}, {"L"}, {"M"},
                {"N"}, {"O"}, {"P"}, {"Q"}, {"R"}, {"S"}, {"T"}, {"U"}, {"V"}, {"W"}, {"X"}, {"Y"},
                {"+0"}, {"+0:00"}, {"+00:0"}, {"+0:0"},
                {"+000"}, {"+00000"},
                {"+0:00:00"}, {"+00:0:00"}, {"+00:00:0"}, {"+0:0:0"}, {"+0:0:00"}, {"+00:0:0"}, {"+0:00:0"},
                {"+01_00"}, {"+01;00"}, {"+01@00"}, {"+01:AA"},
                {"+19"}, {"+19:00"}, {"+18:01"}, {"+18:00:01"}, {"+1801"}, {"+180001"},
                {"-0"}, {"-0:00"}, {"-00:0"}, {"-0:0"},
                {"-000"}, {"-00000"},
                {"-0:00:00"}, {"-00:0:00"}, {"-00:00:0"}, {"-0:0:0"}, {"-0:0:00"}, {"-00:0:0"}, {"-0:00:0"},
                {"-19"}, {"-19:00"}, {"-18:01"}, {"-18:00:01"}, {"-1801"}, {"-180001"},
                {"-01_00"}, {"-01;00"}, {"-01@00"}, {"-01:AA"},
                {"@01:00"},
        };
    }

    @Test(dataProvider="String_UTC_Invalid", expectedExceptions=CalendricalException.class)
    public void test_of_string_UTC_invalid(String id) {
        ZoneId.of("UTC" + id);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="String_Invalid")
    Object[][] data_of_string_invalid() {
        return new Object[][] {
                {""}, {":"}, {"#"},
                {"�"}, {"`"}, {"!"}, {"\""}, {"�"}, {"$"}, {"^"}, {"&"}, {"*"}, {"("}, {")"}, {"="},
                {"\\"}, {"|"}, {","}, {"<"}, {">"}, {"?"}, {";"}, {":"}, {"'"}, {"["}, {"]"}, {"{"}, {"}"},
                {"�:A"}, {"`:A"}, {"!:A"}, {"\":A"}, {"�:A"}, {"$:A"}, {"^:A"}, {"&:A"}, {"*:A"}, {"(:A"}, {"):A"}, {"=:A"}, {"+:A"},
                {"\\:A"}, {"|:A"}, {",:A"}, {"<:A"}, {">:A"}, {"?:A"}, {";:A"}, {"::A"}, {"':A"}, {"@:A"}, {"~:A"}, {"[:A"}, {"]:A"}, {"{:A"}, {"}:A"},
                {"A:B#�"}, {"A:B#`"}, {"A:B#!"}, {"A:B#\""}, {"A:B#�"}, {"A:B#$"}, {"A:B#^"}, {"A:B#&"}, {"A:B#*"},
                {"A:B#("}, {"A:B#)"}, {"A:B#="}, {"A:B#+"},
                {"A:B#\\"}, {"A:B#|"}, {"A:B#,"}, {"A:B#<"}, {"A:B#>"}, {"A:B#?"}, {"A:B#;"}, {"A:B#:"},
                {"A:B#'"}, {"A:B#@"}, {"A:B#~"}, {"A:B#["}, {"A:B#]"}, {"A:B#{"}, {"A:B#}"},
        };
    }

    @Test(dataProvider="String_Invalid", expectedExceptions=CalendricalException.class)
    public void test_of_string_invalid(String id) {
        ZoneId.of(id);
    }

    @Test(dataProvider="String_Invalid", expectedExceptions=CalendricalException.class)
    public void test_ofUnchecked_string_invalid(String id) {
        ZoneId.ofUnchecked(id);
    }

    //-----------------------------------------------------------------------
    public void test_of_string_floatingGMT0() {
        ZoneId test = ZoneId.of("GMT0");
        assertEquals(test.getID(), "GMT0");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "GMT0");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_of_string_versionedGMT0() {
        ZoneId test = ZoneId.of("GMT0#2008i");
        assertEquals(test.getID(), "GMT0#2008i");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "GMT0");
        assertEquals(test.getVersionID(), "2008i");
        assertEquals(test.isFixed(), false);
    }

    public void test_of_string_groupGMT0() {
        ZoneId test = ZoneId.of("TZDB:GMT0");
        assertEquals(test.getID(), "GMT0");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "GMT0");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_of_string_groupVersionedGMT0() {
        ZoneId test = ZoneId.of("TZDB:GMT0#2008i");
        assertEquals(test.getID(), "GMT0#2008i");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "GMT0");
        assertEquals(test.getVersionID(), "2008i");
        assertEquals(test.isFixed(), false);
    }

    //-----------------------------------------------------------------------
    public void test_of_string_floatingLondon() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_of_string_versionedLondon() {
        ZoneId test = ZoneId.of("Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London#2008i");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "2008i");
        assertEquals(test.isFixed(), false);
    }

    public void test_of_string_groupLondon() {
        ZoneId test = ZoneId.of("TZDB:Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_of_string_groupVersionedLondon() {
        ZoneId test = ZoneId.of("TZDB:Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London#2008i");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "2008i");
        assertEquals(test.isFixed(), false);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class)
    public void test_of_string_null() {
        ZoneId.of((String) null);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_of_string_unknown_simple() {
        ZoneId.of("Unknown");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_of_string_unknown_group() {
        ZoneId.of("Unknown:Europe/London");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_of_string_unknown_version() {
        ZoneId.of("TZDB:Europe/London#Unknown");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_of_string_unknown_region() {
        ZoneId.of("TZDB:Unknown#2008i");
    }

    //-------------------------------------------------------------------------
    public void test_ofUnchecked_string_invalidNotChecked() {
        ZoneId test = ZoneId.ofUnchecked("UnknownGroup:UnknownRegion#UnknownVersion");
        assertEquals(test.getID(), "UnknownGroup:UnknownRegion#UnknownVersion");
        assertEquals(test.getGroupID(), "UnknownGroup");
        assertEquals(test.getRegionID(), "UnknownRegion");
        assertEquals(test.getVersionID(), "UnknownVersion");
        assertEquals(test.isFixed(), false);
    }

    public void test_ofUnchecked_string_invalidNotChecked_unusualCharacters() {
        ZoneId test = ZoneId.ofUnchecked("QWERTYUIOPASDFGHJKLZXCVBNM%@~/+.-_");
        assertEquals(test.getID(), "QWERTYUIOPASDFGHJKLZXCVBNM%@~/+.-_");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "QWERTYUIOPASDFGHJKLZXCVBNM%@~/+.-_");
        assertEquals(test.getVersionID(), "");
    }

    //-----------------------------------------------------------------------
    // from()
    //-----------------------------------------------------------------------
    public void test_factory_Calendricals() {
        assertEquals(ZoneId.from(ZONE_PARIS, YearMonth.of(2007, 7), DAY_OF_MONTH.field(15), AmPmOfDay.PM, HOUR_OF_AMPM.field(5), MINUTE_OF_HOUR.field(30)), ZONE_PARIS);
        assertEquals(ZoneId.from(ZonedDateTime.of(2007, 7, 15, 17, 30, 0, 0, ZONE_PARIS)), ZONE_PARIS);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_Calendricals_invalid_clash() {
        ZoneId.from(ZONE_PARIS, ZoneId.of("Europe/London"));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_Calendricals_invalid_noDerive() {
        ZoneId.from(LocalTime.of(12, 30));
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_factory_Calendricals_invalid_empty() {
        ZoneId.from();
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_Calendricals_nullArray() {
        ZoneId.from((Calendrical[]) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_Calendricals_null() {
        ZoneId.from((Calendrical) null);
    }

    //-----------------------------------------------------------------------
    // Europe/London
    //-----------------------------------------------------------------------
    public void test_London() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_London_getOffset() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 1, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 2, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 4, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 5, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 6, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 7, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 8, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 9, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 12, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
    }

    public void test_London_getOffset_toDST() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        // cutover at 01:00Z
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 3, 30, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 3, 30, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
    }

    public void test_London_getOffset_fromDST() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
        // cutover at 01:00Z
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 10, 26, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 10, 26, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(0));
    }

    public void test_London_getOffsetInfo() {
        ZoneId test = ZoneId.of("Europe/London");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 1, 1)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 2, 1)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 1)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 4, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 5, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 6, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 7, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 8, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 9, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 1)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 12, 1)), ZoneOffset.ofHours(0));
    }

    public void test_London_getOffsetInfo_toDST() {
        ZoneId test = ZoneId.of("Europe/London");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 24)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 25)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 26)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 27)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 28)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 29)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 30)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 31)), ZoneOffset.ofHours(1));
        // cutover at 01:00Z
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 3, 30, 0, 59, 59, 999999999)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 3, 30, 2, 0, 0, 0)), ZoneOffset.ofHours(1));
    }

    public void test_London_getOffsetInfo_fromDST() {
        ZoneId test = ZoneId.of("Europe/London");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 24)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 25)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 26)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 27)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 28)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 29)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 30)), ZoneOffset.ofHours(0));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 31)), ZoneOffset.ofHours(0));
        // cutover at 01:00Z
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 10, 26, 0, 59, 59, 999999999)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 10, 26, 2, 0, 0, 0)), ZoneOffset.ofHours(0));
    }

    public void test_London_getOffsetInfo_gap() {
        ZoneId test = ZoneId.of("Europe/London");
        final LocalDateTime dateTime = LocalDateTime.of(2008, 3, 30, 1, 0, 0, 0);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(dateTime);
        assertEquals(info.isTransition(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.ofHours(1));
        ZoneOffsetTransition dis = info.getTransition();
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.ofHours(0));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.ofHours(1));
        assertEquals(dis.getInstant(), OffsetDateTime.of(2008, 3, 30, 1, 0, ZoneOffset.UTC).toInstant());
        assertEquals(dis.getDateTimeBefore(), OffsetDateTime.of(2008, 3, 30, 1, 0, ZoneOffset.ofHours(0)));
        assertEquals(dis.getDateTimeAfter(), OffsetDateTime.of(2008, 3, 30, 2, 0, ZoneOffset.ofHours(1)));
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(0)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(1)), false);
        assertEquals(dis.toString(), "Transition[Gap at 2008-03-30T01:00Z to +01:00]");

        assertFalse(dis.equals(null));
        assertFalse(dis.equals(ZoneOffset.ofHours(0)));
        assertTrue(dis.equals(dis));

        final ZoneOffsetTransition otherDis = test.getRules().getOffsetInfo(dateTime).getTransition();
        assertTrue(dis.equals(otherDis));

        assertEquals(dis.hashCode(), otherDis.hashCode());
    }

    public void test_London_getOffsetInfo_overlap() {
        ZoneId test = ZoneId.of("Europe/London");
        final LocalDateTime dateTime = LocalDateTime.of(2008, 10, 26, 1, 0, 0, 0);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(dateTime);
        assertEquals(info.isTransition(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.ofHours(0));
        ZoneOffsetTransition dis = info.getTransition();
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.ofHours(1));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.ofHours(0));
        assertEquals(dis.getInstant(), OffsetDateTime.of(2008, 10, 26, 1, 0, ZoneOffset.UTC).toInstant());
        assertEquals(dis.getDateTimeBefore(), OffsetDateTime.of(2008, 10, 26, 2, 0, ZoneOffset.ofHours(1)));
        assertEquals(dis.getDateTimeAfter(), OffsetDateTime.of(2008, 10, 26, 1, 0, ZoneOffset.ofHours(0)));
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(-1)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(0)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(1)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(2)), false);
        assertEquals(dis.toString(), "Transition[Overlap at 2008-10-26T02:00+01:00 to Z]");

        assertFalse(dis.equals(null));
        assertFalse(dis.equals(ZoneOffset.ofHours(1)));
        assertTrue(dis.equals(dis));

        final ZoneOffsetTransition otherDis = test.getRules().getOffsetInfo(dateTime).getTransition();
        assertTrue(dis.equals(otherDis));

        assertEquals(dis.hashCode(), otherDis.hashCode());
    }

    //-----------------------------------------------------------------------
    // Europe/Paris
    //-----------------------------------------------------------------------
    public void test_Paris() {
        ZoneId test = ZoneId.of("Europe/Paris");
        assertEquals(test.getID(), "Europe/Paris");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/Paris");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_Paris_getOffset() {
        ZoneId test = ZoneId.of("Europe/Paris");
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 1, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 2, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 4, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 5, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 6, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 7, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 8, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 9, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 12, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
    }

    public void test_Paris_getOffset_toDST() {
        ZoneId test = ZoneId.of("Europe/Paris");
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        // cutover at 01:00Z
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 3, 30, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 3, 30, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
    }

    public void test_Paris_getOffset_fromDST() {
        ZoneId test = ZoneId.of("Europe/Paris");
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
        // cutover at 01:00Z
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 10, 26, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(2));
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 10, 26, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.ofHours(1));
    }

    public void test_Paris_getOffsetInfo() {
        ZoneId test = ZoneId.of("Europe/Paris");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 1, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 2, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 4, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 5, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 6, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 7, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 8, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 9, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 1)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 1)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 12, 1)), ZoneOffset.ofHours(1));
    }

    public void test_Paris_getOffsetInfo_toDST() {
        ZoneId test = ZoneId.of("Europe/Paris");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 24)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 25)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 26)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 27)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 28)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 29)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 30)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 31)), ZoneOffset.ofHours(2));
        // cutover at 01:00Z which is 02:00+01:00(local Paris time)
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 3, 30, 1, 59, 59, 999999999)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 3, 30, 3, 0, 0, 0)), ZoneOffset.ofHours(2));
    }

    public void test_Paris_getOffsetInfo_fromDST() {
        ZoneId test = ZoneId.of("Europe/Paris");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 24)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 25)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 26)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 27)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 28)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 29)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 30)), ZoneOffset.ofHours(1));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 31)), ZoneOffset.ofHours(1));
        // cutover at 01:00Z which is 02:00+01:00(local Paris time)
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 10, 26, 1, 59, 59, 999999999)), ZoneOffset.ofHours(2));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 10, 26, 3, 0, 0, 0)), ZoneOffset.ofHours(1));
    }

    public void test_Paris_getOffsetInfo_gap() {
        ZoneId test = ZoneId.of("Europe/Paris");
        final LocalDateTime dateTime = LocalDateTime.of(2008, 3, 30, 2, 0, 0, 0);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(dateTime);
        assertEquals(info.isTransition(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.ofHours(2));
        ZoneOffsetTransition dis = info.getTransition();
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.ofHours(1));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.ofHours(2));
        assertEquals(dis.getInstant(), OffsetDateTime.of(2008, 3, 30, 1, 0, ZoneOffset.UTC).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(3)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(1)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(2)), false);
        assertEquals(dis.toString(), "Transition[Gap at 2008-03-30T02:00+01:00 to +02:00]");

        assertFalse(dis.equals(null));
        assertFalse(dis.equals(ZoneOffset.ofHours(1)));
        assertTrue(dis.equals(dis));

        final ZoneOffsetTransition otherDis = test.getRules().getOffsetInfo(dateTime).getTransition();
        assertTrue(dis.equals(otherDis));

        assertEquals(dis.hashCode(), otherDis.hashCode());
    }

    public void test_Paris_getOffsetInfo_overlap() {
        ZoneId test = ZoneId.of("Europe/Paris");
        final LocalDateTime dateTime = LocalDateTime.of(2008, 10, 26, 2, 0, 0, 0);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(dateTime);
        assertEquals(info.isTransition(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.ofHours(1));
        ZoneOffsetTransition dis = info.getTransition();
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.ofHours(2));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.ofHours(1));
        assertEquals(dis.getInstant(), OffsetDateTime.of(2008, 10, 26, 1, 0, ZoneOffset.UTC).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(3)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(0)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(1)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(2)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(3)), false);
        assertEquals(dis.toString(), "Transition[Overlap at 2008-10-26T03:00+02:00 to +01:00]");

        assertFalse(dis.equals(null));
        assertFalse(dis.equals(ZoneOffset.ofHours(2)));
        assertTrue(dis.equals(dis));

        final ZoneOffsetTransition otherDis = test.getRules().getOffsetInfo(dateTime).getTransition();
        assertTrue(dis.equals(otherDis));

        assertEquals(dis.hashCode(), otherDis.hashCode());
    }

    //-----------------------------------------------------------------------
    // America/New_York
    //-----------------------------------------------------------------------
    public void test_NewYork() {
        ZoneId test = ZoneId.of("America/New_York");
        assertEquals(test.getID(), "America/New_York");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "America/New_York");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), false);
    }

    public void test_NewYork_getOffset() {
        ZoneId test = ZoneId.of("America/New_York");
        ZoneOffset offset = ZoneOffset.ofHours(-5);
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 1, 1, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 2, 1, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 1, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 4, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 5, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 6, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 7, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 8, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 9, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 12, 1, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 1, 28, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 2, 28, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 4, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 5, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 6, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 7, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 8, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 9, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 10, 28, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 28, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 12, 28, offset).toInstant()), ZoneOffset.ofHours(-5));
    }

    public void test_NewYork_getOffset_toDST() {
        ZoneId test = ZoneId.of("America/New_York");
        ZoneOffset offset = ZoneOffset.ofHours(-5);
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 8, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 9, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 10, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 11, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 12, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 13, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 3, 14, offset).toInstant()), ZoneOffset.ofHours(-4));
        // cutover at 02:00 local
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 3, 9, 1, 59, 59, 999999999, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 3, 9, 2, 0, 0, 0, offset).toInstant()), ZoneOffset.ofHours(-4));
    }

    public void test_NewYork_getOffset_fromDST() {
        ZoneId test = ZoneId.of("America/New_York");
        ZoneOffset offset = ZoneOffset.ofHours(-4);
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 1, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 2, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 3, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 4, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 5, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 6, offset).toInstant()), ZoneOffset.ofHours(-5));
        assertEquals(test.getRules().getOffset(OffsetDateTime.ofMidnight(2008, 11, 7, offset).toInstant()), ZoneOffset.ofHours(-5));
        // cutover at 02:00 local
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 11, 2, 1, 59, 59, 999999999, offset).toInstant()), ZoneOffset.ofHours(-4));
        assertEquals(test.getRules().getOffset(OffsetDateTime.of(2008, 11, 2, 2, 0, 0, 0, offset).toInstant()), ZoneOffset.ofHours(-5));
    }

    public void test_NewYork_getOffsetInfo() {
        ZoneId test = ZoneId.of("America/New_York");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 1, 1)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 2, 1)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 1)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 4, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 5, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 6, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 7, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 8, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 9, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 12, 1)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 1, 28)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 2, 28)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 4, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 5, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 6, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 7, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 8, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 9, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 10, 28)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 28)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 12, 28)), ZoneOffset.ofHours(-5));
    }

    public void test_NewYork_getOffsetInfo_toDST() {
        ZoneId test = ZoneId.of("America/New_York");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 8)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 9)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 10)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 11)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 12)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 13)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 3, 14)), ZoneOffset.ofHours(-4));
        // cutover at 02:00 local
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 3, 9, 1, 59, 59, 999999999)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 3, 9, 3, 0, 0, 0)), ZoneOffset.ofHours(-4));
    }

    public void test_NewYork_getOffsetInfo_fromDST() {
        ZoneId test = ZoneId.of("America/New_York");
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 1)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 2)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 3)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 4)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 5)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 6)), ZoneOffset.ofHours(-5));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.ofMidnight(2008, 11, 7)), ZoneOffset.ofHours(-5));
        // cutover at 02:00 local
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 11, 2, 0, 59, 59, 999999999)), ZoneOffset.ofHours(-4));
        checkOffset(test.getRules().getOffsetInfo(LocalDateTime.of(2008, 11, 2, 2, 0, 0, 0)), ZoneOffset.ofHours(-5));
    }

    public void test_NewYork_getOffsetInfo_gap() {
        ZoneId test = ZoneId.of("America/New_York");
        final LocalDateTime dateTime = LocalDateTime.of(2008, 3, 9, 2, 0, 0, 0);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(dateTime);
        assertEquals(info.isTransition(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.ofHours(-4));
        ZoneOffsetTransition dis = info.getTransition();
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.ofHours(-5));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.ofHours(-4));
        assertEquals(dis.getInstant(), OffsetDateTime.of(2008, 3, 9, 2, 0, ZoneOffset.ofHours(-5)).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-5)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-4)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(-5)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(-4)), false);
        assertEquals(dis.toString(), "Transition[Gap at 2008-03-09T02:00-05:00 to -04:00]");

        assertFalse(dis.equals(null));
        assertFalse(dis.equals(ZoneOffset.ofHours(-5)));
        assertTrue(dis.equals(dis));

        final ZoneOffsetTransition otherDis = test.getRules().getOffsetInfo(dateTime).getTransition();
        assertTrue(dis.equals(otherDis));

        assertEquals(dis.hashCode(), otherDis.hashCode());
    }

    public void test_NewYork_getOffsetInfo_overlap() {
        ZoneId test = ZoneId.of("America/New_York");
        final LocalDateTime dateTime = LocalDateTime.of(2008, 11, 2, 1, 0, 0, 0);
        ZoneOffsetInfo info = test.getRules().getOffsetInfo(dateTime);
        assertEquals(info.isTransition(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.ofHours(-5));
        ZoneOffsetTransition dis = info.getTransition();
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.ofHours(-4));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.ofHours(-5));
        assertEquals(dis.getInstant(), OffsetDateTime.of(2008, 11, 2, 2, 0, ZoneOffset.ofHours(-4)).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-5)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-4)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(-1)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(-5)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(-4)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.ofHours(2)), false);
        assertEquals(dis.toString(), "Transition[Overlap at 2008-11-02T02:00-04:00 to -05:00]");

        assertFalse(dis.equals(null));
        assertFalse(dis.equals(ZoneOffset.ofHours(-4)));
        assertTrue(dis.equals(dis));

        final ZoneOffsetTransition otherDis = test.getRules().getOffsetInfo(dateTime).getTransition();
        assertTrue(dis.equals(otherDis));

        assertEquals(dis.hashCode(), otherDis.hashCode());
    }

    //-----------------------------------------------------------------------
    // getXxx() isXxx()
    //-----------------------------------------------------------------------
    public void test_get_TzdbFloating() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.getGroup().getID(), "TZDB");
        assertEquals(test.isFixed(), false);
        assertEquals(test.isFloatingVersion(), true);
        assertEquals(test.isLatestVersion(), true);
    }

    public void test_get_TzdbVersioned() {
        ZoneId test = ZoneId.of("Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London#2008i");
        assertEquals(test.getGroupID(), "TZDB");
        assertEquals(test.getRegionID(), "Europe/London");
        assertEquals(test.getVersionID(), "2008i");
        assertEquals(test.getGroup().getID(), "TZDB");
        assertEquals(test.isFixed(), false);
        assertEquals(test.isFloatingVersion(), false);
        assertEquals(test.isLatestVersion(), LATEST_TZDB.equals("2008i"));
    }

    public void test_get_TzdbFixed() {
        ZoneId test = ZoneId.of("UTC+01:30");
        assertEquals(test.getID(), "UTC+01:30");
        assertEquals(test.getGroupID(), "");
        assertEquals(test.getRegionID(), "UTC+01:30");
        assertEquals(test.getVersionID(), "");
        assertEquals(test.isFixed(), true);
        assertEquals(test.isFloatingVersion(), true);
        assertEquals(test.isLatestVersion(), true);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_get_TzdbFixed_getGroup() {
        ZoneId test = ZoneId.of("UTC+01:30");
        test.getGroup();
    }

    //-----------------------------------------------------------------------
    public void test_withFloatingVersion_TzdbFloating() {
        ZoneId base = ZoneId.of("Europe/London");
        ZoneId test = base.withFloatingVersion();
        assertSame(test, base);
    }

    public void test_withFloatingVersion_TzdbVersioned() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        ZoneId test = base.withFloatingVersion();
        assertEquals(base.getID(), "Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London");
//        assertNotSame(test.getRules(), base.getRules());  // TODO: rewrite test with mocks
    }

    public void test_withFloatingVersion_TzdbFixed() {
        ZoneId base = ZoneId.of("UTC+01:30");
        ZoneId test = base.withFloatingVersion();
        assertSame(test, base);
    }

    //-----------------------------------------------------------------------
    public void test_withLatestVersion_TzdbFloating() {
        ZoneId base = ZoneId.of("Europe/London");
        ZoneId test = base.withLatestVersion();
        assertEquals(base.getID(), "Europe/London");
        assertEquals(test.getID(), "Europe/London#" + LATEST_TZDB);
    }

    public void test_withLatestVersion_TzdbVersioned() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        ZoneId test = base.withLatestVersion();
        assertEquals(base.getID(), "Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London#" + LATEST_TZDB);
//        assertNotSame(test.getRules(), base.getRules());
    }

    public void test_withLatestVersion_TzdbVersioned_alreadyLatest() {
        ZoneId base = ZoneId.of("Europe/London#" + LATEST_TZDB);
        ZoneId test = base.withLatestVersion();
        assertSame(test, base);
        assertSame(test.getRules(), base.getRules());
    }

    public void test_withLatestVersion_TzdbFixed() {
        ZoneId base = ZoneId.of("UTC+01:30");
        ZoneId test = base.withLatestVersion();
        assertSame(test, base);
    }

    //-----------------------------------------------------------------------
    public void test_withVersion_TzdbFloating() {
        ZoneId base = ZoneId.of("Europe/London");
        ZoneId test = base.withVersion("2008i");
        assertEquals(base.getID(), "Europe/London");
        assertEquals(test.getID(), "Europe/London#2008i");
    }

    public void test_withVersion_TzdbFloating_latestVersion() {
        ZoneId base = ZoneId.of("Europe/London");
        ZoneId test = base.withVersion(LATEST_TZDB);
        assertEquals(base.getID(), "Europe/London");
        assertEquals(test.getID(), "Europe/London#" + LATEST_TZDB);
    }

    public void test_withVersion_TzdbFloating_floatingVersion() {
        ZoneId base = ZoneId.of("Europe/London");
        ZoneId test = base.withVersion("");
        assertEquals(test, base);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_withVersion_TzdbFloating_badVersion() {
        ZoneId base = ZoneId.of("Europe/London");
        base.withVersion("20");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withVersion_TzdbFloating_null() {
        ZoneId base = ZoneId.of("Europe/London");
        base.withVersion(null);
    }

    //-----------------------------------------------------------------------
    public void test_withVersion_TzdbVersioned() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        ZoneId test = base.withVersion("2009a");
        assertEquals(base.getID(), "Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London#2009a");
    }

    public void test_withVersion_TzdbVersioned_latestVersion() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        ZoneId test = base.withVersion(LATEST_TZDB);
        assertEquals(base.getID(), "Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London#" + LATEST_TZDB);
    }

    public void test_withVersion_TzdbVersioned_sameVersion() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        ZoneId test = base.withVersion("2008i");
        assertSame(test, base);
    }

    public void test_withVersion_TzdbVersioned_floatingVersion() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        ZoneId test = base.withVersion("");
        assertEquals(base.getID(), "Europe/London#2008i");
        assertEquals(test.getID(), "Europe/London");
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_withVersion_TzdbVersioned_badVersion() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        base.withVersion("20");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withVersion_TzdbVersioned_null() {
        ZoneId base = ZoneId.of("Europe/London#2008i");
        base.withVersion(null);
    }

    //-----------------------------------------------------------------------
    public void test_withVersion_TzdbFixed_floatingVersion() {
        ZoneId base = ZoneId.of("UTC+01:30");
        ZoneId test = base.withVersion("");
        assertSame(test, base);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_withVersion_TzdbFixed_badVersion() {
        ZoneId base = ZoneId.of("UTC+01:30");
        base.withVersion("20");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withVersion_TzdbFixed_null() {
        ZoneId base = ZoneId.of("UTC+01:30");
        base.withVersion(null);
    }

    //-----------------------------------------------------------------------
    // isValid()
    //-----------------------------------------------------------------------
    public void test_isValid() {
        ZoneId testId = ZoneId.of("Europe/London");
        assertEquals(testId.isValid(), true);
        
        ZoneId testFixed = ZoneId.of("UTC+01:30");
        assertEquals(testFixed.isValid(), true);
    }

    //-----------------------------------------------------------------------
    // isValidFor()
    //-----------------------------------------------------------------------
    public void test_isValidFor() {
        OffsetDateTime odt1 = OffsetDateTime.of(2011, 6, 20, 12, 30, ZoneOffset.ofHours(1));
        OffsetDateTime odt2 = OffsetDateTime.of(2011, 6, 20, 12, 30, ZoneOffset.ofHoursMinutes(1, 30));
        
        ZoneId testId = ZoneId.of("Europe/London");
        assertEquals(testId.isValidFor(odt1), true);
        assertEquals(testId.isValidFor(odt2), false);
        
        ZoneId testFixed = ZoneId.of("UTC+01:00");
        assertEquals(testFixed.isValidFor(odt1), true);
        assertEquals(testFixed.isValidFor(odt2), false);
    }

    public void test_isValidFor_null() {
        ZoneId testId = ZoneId.of("Europe/London");
        assertEquals(testId.isValidFor(null), false);
        
        ZoneId testFixed = ZoneId.of("UTC+01:30");
        assertEquals(testFixed.isValidFor(null), false);
    }

//    //-----------------------------------------------------------------------
//    // toTimeZone()
//    //-----------------------------------------------------------------------
//    public void test_toTimeZone() {
//        TimeZone offset = TimeZone.timeZone(1, 2, 3);
//        assertEquals(offset.toTimeZone(), TimeZone.timeZone(offset));
//    }

//    //-----------------------------------------------------------------------
//    // compareTo()
//    //-----------------------------------------------------------------------
//    public void test_compareTo() {
//        TimeZone offset1 = TimeZone.timeZone(1, 2, 3);
//        TimeZone offset2 = TimeZone.timeZone(2, 3, 4);
//        assertTrue(offset1.compareTo(offset2) > 0);
//        assertTrue(offset2.compareTo(offset1) < 0);
//        assertTrue(offset1.compareTo(offset1) == 0);
//        assertTrue(offset2.compareTo(offset2) == 0);
//    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals() {
        ZoneId test1 = ZoneId.of("Europe/London");
        ZoneId test2 = ZoneId.of("Europe/Paris");
        ZoneId test2b = ZoneId.of("Europe/Paris");
        assertEquals(test1.equals(test2), false);
        assertEquals(test2.equals(test1), false);
        
        assertEquals(test1.equals(test1), true);
        assertEquals(test2.equals(test2), true);
        assertEquals(test2.equals(test2b), true);
        
        assertEquals(test1.hashCode() == test1.hashCode(), true);
        assertEquals(test2.hashCode() == test2.hashCode(), true);
        assertEquals(test2.hashCode() == test2b.hashCode(), true);
    }

    public void test_equals_null() {
        assertEquals(ZoneId.of("Europe/London").equals(null), false);
    }

    public void test_equals_notTimeZone() {
        assertEquals(ZoneId.of("Europe/London").equals("Europe/London"), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="ToString")
    Object[][] data_toString() {
        return new Object[][] {
            {"Europe/London", "Europe/London"},
            {"TZDB:Europe/Paris", "Europe/Paris"},
            {"TZDB:Europe/Berlin", "Europe/Berlin"},
            {"Europe/London#2008i", "Europe/London#2008i"},
            {"TZDB:Europe/Paris#2008i", "Europe/Paris#2008i"},
            {"TZDB:Europe/Berlin#2008i", "Europe/Berlin#2008i"},
            {"UTC", "UTC"},
            {"UTC+01:00", "UTC+01:00"},
        };
    }

    @Test(dataProvider="ToString")
    public void test_toString(String id, String expected) {
        ZoneId test = ZoneId.of(id);
        assertEquals(test.toString(), expected);
    }

    //-----------------------------------------------------------------------
    // get(CalendricalRule)
    //-----------------------------------------------------------------------
    public void test_get_CalendricalRule() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.get(Chronology.rule()), null);
        assertEquals(test.get(YEAR), null);
        assertEquals(test.get(HOUR_OF_DAY), null);
        assertEquals(test.get(LocalDate.rule()), null);
        assertEquals(test.get(ZoneOffset.rule()), null);
        assertEquals(test.get(ZoneId.rule()), test);
    }

    public void test_get_CalendricalRule_fixedOffset() {
        ZoneId test = ZoneId.of("UTC+03:00");
        assertEquals(test.get(Chronology.rule()), null);
        assertEquals(test.get(YEAR), null);
        assertEquals(test.get(HOUR_OF_DAY), null);
        assertEquals(test.get(LocalDate.rule()), null);
        assertEquals(test.get(ZoneOffset.rule()), null);  // could get ZoneOffset.ofHours(3), but seems like a bad idea
        assertEquals(test.get(ZoneId.rule()), test);
    }

    @Test(expectedExceptions=NullPointerException.class )
    public void test_get_CalendricalRule_null() {
        ZoneId test = ZoneId.of("Europe/London");
        test.get((CalendricalRule<?>) null);
    }

    public void test_get_unsupported() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.get(MockRuleNoValue.INSTANCE), null);
    }

    //-----------------------------------------------------------------------
    private void checkOffset(ZoneOffsetInfo info, ZoneOffset zoneOffset) {
        assertEquals(info.isTransition(), false);
        assertEquals(info.getTransition(), null);
        assertEquals(info.getOffset(), zoneOffset);
        assertEquals(info.getEstimatedOffset(), zoneOffset);
//        assertEquals(info.containsOffset(zoneOffset), true);
        assertEquals(info.isValidOffset(zoneOffset), true);
    }

}
