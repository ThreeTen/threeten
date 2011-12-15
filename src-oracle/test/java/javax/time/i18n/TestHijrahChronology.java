package javax.time.i18n;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.time.chronology.DateTimeField;
import javax.time.chronology.DateTimeRule;
import javax.time.chronology.DateTimeRuleRange;

import org.testng.annotations.Test;

public class TestHijrahChronology {

    @Test
    public void testGetName() {
        assertEquals(HijrahChronology.INSTANCE.getName(), "Hijrah");
    }
    
    @Test
    public void testConstructor() throws Exception {
        for (Constructor<?> constructor : HijrahChronology.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }
    
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Object chronology = HijrahChronology.INSTANCE;
        assertTrue(chronology instanceof Serializable);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(chronology);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertSame(ois.readObject(), chronology);
    }

    @Test
    public void testImmutable() throws Exception {
        Class<HijrahChronology> cls = HijrahChronology.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()));
            }
        }
    }

    @Test
    public void testEra() throws Exception {
        DateTimeRule rule = HijrahChronology.eraRule();
        assertEquals(rule.getType(), DateTimeField.class);
        assertEquals(rule.getName(), "HijrahEra");
        assertEquals(rule.getValueRange(), DateTimeRuleRange.of(0, 1));
        assertEquals(rule.getPeriodUnit(), HijrahChronology.periodEras());
        assertEquals(rule.getPeriodRange(), null);
        serialize(rule);
    }

    @Test
    public void testYearOfEra() throws Exception {
        DateTimeRule rule = HijrahChronology.yearOfEraRule();
        assertEquals(rule.getType(), DateTimeField.class);
        assertEquals(rule.getName(), "HijrahYearOfEra");
        assertEquals(rule.getValueRange(), DateTimeRuleRange.of(HijrahDate.MIN_YEAR_OF_ERA, HijrahDate.MAX_YEAR_OF_ERA));
        assertEquals(rule.getPeriodUnit(), HijrahChronology.periodYears());
        assertEquals(rule.getPeriodRange(), HijrahChronology.periodEras());
        serialize(rule);
    }

    @Test
    public void testMonthOfYear() throws Exception {
        DateTimeRule rule = HijrahChronology.monthOfYearRule();
        assertEquals(rule.getType(), DateTimeField.class);
        assertEquals(rule.getName(), "HijrahMonthOfYear");
        assertEquals(rule.getValueRange(), DateTimeRuleRange.of(1, 12));
        assertEquals(rule.getPeriodUnit(), HijrahChronology.periodMonths());
        assertEquals(rule.getPeriodRange(), HijrahChronology.periodYears());
    }

    @Test
    public void testDayOfMonth() throws Exception {
        DateTimeRule rule = HijrahChronology.dayOfMonthRule();
        assertEquals(rule.getType(), DateTimeField.class);
        assertEquals(rule.getName(), "HijrahDayOfMonth");
        assertEquals(rule.getValueRange(), DateTimeRuleRange.of(1, 29, 30));
        assertEquals(rule.getPeriodUnit(), HijrahChronology.periodDays());
        assertEquals(rule.getPeriodRange(), HijrahChronology.periodMonths());
        serialize(rule);
    }

    @Test
    public void testDayOfYear() throws Exception {
        DateTimeRule rule = HijrahChronology.dayOfYearRule();
        assertEquals(rule.getType(), DateTimeField.class);
        assertEquals(rule.getName(), "HijrahDayOfYear");
        assertEquals(rule.getValueRange(), DateTimeRuleRange.of(1, 354, 355));
        assertEquals(rule.getPeriodUnit(), HijrahChronology.periodDays());
        assertEquals(rule.getPeriodRange(), HijrahChronology.periodYears());
        serialize(rule);

    }

    @Test
    public void testDayOfWeek() throws Exception {
        DateTimeRule rule = HijrahChronology.dayOfWeekRule();
        assertEquals(rule.getType(), DateTimeField.class);
        assertEquals(rule.getName(), "HijrahDayOfWeek");
        assertEquals(rule.getValueRange(), DateTimeRuleRange.of(1, 7));
        assertEquals(rule.getPeriodUnit(), HijrahChronology.periodDays());
        assertEquals(rule.getPeriodRange(), HijrahChronology.periodWeeks());
        serialize(rule);
    }

    public void test_toString() throws Exception {
        assertEquals(HijrahChronology.INSTANCE.toString(), "Hijrah");
    }

    private void serialize(DateTimeRule rule) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(rule);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertSame(ois.readObject(), rule);
    }

}
