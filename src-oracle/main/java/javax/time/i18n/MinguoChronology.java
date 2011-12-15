/*
 * Copyright (c) 2009 Oracle All Rights Reserved.
 */
package javax.time.i18n;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

import javax.time.chronology.Calendrical;
import javax.time.chronology.CalendricalEngine;
import javax.time.chronology.Chronology;
import javax.time.chronology.DateTimeField;
import javax.time.chronology.DateTimeRule;
import javax.time.chronology.DateTimeRuleRange;
import javax.time.chronology.ISOChronology;
import javax.time.ISOPeriodUnit;
import javax.time.MonthOfYear;
import javax.time.PeriodUnit;
import javax.time.calendar.format.TextStyle;

/**
 * The Minguo calendar system.
 * <p>
 * {@code MinguoChronology} defines the rules of the Minguo calendar system.
 * <p>
 * The Minguo calendar system is the same as the ISO calendar system apart from the year.
 * <p>
 * MinguoChronology is immutable and thread-safe.
 *
 * @author Ryoji Suzuki
 * @author Stephen Colebourne
 */
public final class MinguoChronology extends Chronology implements Serializable {

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = 5856454970865881985L;

    /**
     * The singleton instance of {@code MinguoChronology}.
     */
    public static final MinguoChronology INSTANCE = new MinguoChronology();

    /**
     * Containing the offset from the ISO year.
     */
    static final int YEAR_OFFSET = 1911;

    /**
     * Narrow names for eras.
     */
    private static final HashMap<String, String[]> ERA_NARROW_NAMES = new HashMap<String, String[]>();

    /**
     * Short names for eras.
     */
    private static final HashMap<String, String[]> ERA_SHORT_NAMES = new HashMap<String, String[]>();

    /**
     * Full names for eras.
     */
    private static final HashMap<String, String[]> ERA_FULL_NAMES = new HashMap<String, String[]>();

    /**
     * Fallback language for the era names.
     */
    private static final String FALLBACK_LANGUAGE = "en";

    /**
     * Language that has the era names.
     */
    private static final String TARGET_LANGUAGE = "zh";

    /**
     * Name data.
     */
    static {
        ERA_NARROW_NAMES.put(FALLBACK_LANGUAGE, new String[]{"BM", "AM"});
        ERA_NARROW_NAMES.put(TARGET_LANGUAGE, new String[]{"\u6c11\u570b", "\u6c11\u524d"});
        ERA_SHORT_NAMES.put(FALLBACK_LANGUAGE, new String[]{"B.M.", "A.M."});
        ERA_SHORT_NAMES.put(TARGET_LANGUAGE, new String[]{"\u6c11\u570b", "\u6c11\u524d"});
        ERA_FULL_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Before Minguo", "Minguo"});
        ERA_FULL_NAMES.put(TARGET_LANGUAGE, new String[]{"\u4e2d\u83ef\u6c11\u570b", "\u6c11\u570b\u524d"});
    }

    //-----------------------------------------------------------------------
    /**
     * Restrictive constructor.
     */
    private MinguoChronology() {
    }

    /**
     * Resolves singleton.
     *
     * @return the singleton instance
     */
    private Object readResolve() {
        return INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the name of the chronology.
     *
     * @return the name of the chronology, never null
     */
    @Override
    public String getName() {
        return "Minguo";
    }

    //-----------------------------------------------------------------------
//    /**
//     * Gets the rule for the year field in the Minguo chronology.
//     * <p>
//     * The values for the year field match those for year of era in the current era.
//     * For the previous era, the values run backwards and include zero, thus
//     * BEFORE_MINGUO 1 has the value 0, and BEFORE_MINGUO 2 has the value -1.
//     *
//     * @return the rule for the year field, never null
//     */
//    public static DateTimeFieldRule yearRule() {
//        return YearRule.INSTANCE;
//    }

    /**
     * Gets the rule for the era field in the Minguo chronology.
     *
     * @return the rule for the year field, never null
     */
    public static DateTimeRule eraRule() {
        return EraRule.INSTANCE;
    }

    /**
     * Gets the rule for the year of era field in the Minguo chronology.
     *
     * @return the rule for the year of era field, never null
     */
    public static DateTimeRule yearOfEraRule() {
        return YearOfEraRule.INSTANCE;
    }

    /**
     * Gets the rule for the month-of-year field in the Minguo chronology.
     *
     * @return the rule for the month-of-year field, never null
     */
    public static DateTimeRule monthOfYearRule() {
        return MonthOfYearRule.INSTANCE;
    }

    /**
     * Gets the rule for the day-of-month field in the Minguo chronology.
     *
     * @return the rule for the day-of-month field, never null
     */
    public static DateTimeRule dayOfMonthRule() {
        return DayOfMonthRule.INSTANCE;
    }

    /**
     * Gets the rule for the day-of-year field in the Minguo chronology.
     *
     * @return the rule for the day-of-year field, never null
     */
    public static DateTimeRule dayOfYearRule() {
        return DayOfYearRule.INSTANCE;
    }

    /**
     * Gets the rule for the day-of-week field in the Minguo chronology.
     *
     * @return the rule for the day-of-week field, never null
     */
    public static DateTimeRule dayOfWeekRule() {
        return DayOfWeekRule.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the period unit for eras.
     * <p>
     * The period unit defines the concept of a period of an era.
     * This is equivalent to the ISO eras period unit.
     * <p>
     * See {@link #eraRule()} for the main date-time field.
     *
     * @return the period unit for eras, never null
     */
    public static PeriodUnit periodEras() {
        return ISOPeriodUnit.ERAS;
    }

    /**
     * Gets the period unit for years.
     * <p>
     * The period unit defines the concept of a period of a year.
     * This is equivalent to the ISO years period unit.
     * <p>
     * See {@link #yearOfEraRule()} for the main date-time field.
     *
     * @return the period unit for years, never null
     */
    public static PeriodUnit periodYears() {
        return ISOPeriodUnit.YEARS;
    }

    /**
     * Gets the period unit for months.
     * <p>
     * The period unit defines the concept of a period of a month.
     * This is equivalent to the ISO months period unit.
     * <p>
     * See {@link #monthOfYearRule()} for the main date-time field.
     *
     * @return the period unit for months, never null
     */
    public static PeriodUnit periodMonths() {
        return ISOPeriodUnit.MONTHS;
    }

    /**
     * Gets the period unit for weeks.
     * <p>
     * The period unit defines the concept of a period of a week.
     * This is equivalent to the ISO weeks period unit.
     *
     * @return the period unit for weeks, never null
     */
    public static PeriodUnit periodWeeks() {
        return ISOPeriodUnit.WEEKS;
    }

    /**
     * Gets the period unit for days.
     * <p>
     * The period unit defines the concept of a period of a day.
     * This is equivalent to the ISO days period unit.
     * <p>
     * See {@link #dayOfMonthRule()} for the main date-time field.
     *
     * @return the period unit for days, never null
     */
    public static PeriodUnit periodDays() {
        return ISOPeriodUnit.DAYS;
    }

    //-----------------------------------------------------------------------
//    /**
//     * Rule implementation.
//     */
//    private static final class YearRule extends DateTimeFieldRule implements Serializable {
//        /** Singleton instance. */
//        private static final DateTimeFieldRule INSTANCE = new YearRule();
//        /** A serialization identifier for this class. */
//        private static final long serialVersionUID = 1L;
//        /** Constructor. */
//        private YearRule() {
//            super(Integer.class, MinguoChronology.INSTANCE, "Year", YEARS, null,
//                    -MinguoDate.MAX_YEAR_OF_ERA + 1, MinguoDate.MAX_YEAR_OF_ERA);
//        }
//        private Object readResolve() {
//            return INSTANCE;
//        }
//        @Override
//        public Integer getValueQuiet(LocalDate date, LocalTime time) {
//            return (date == null ? null : date.getYear() - YEAR_OFFSET);
//        }
//        @Override
//        protected void mergeDateTime(Calendrical.Merger engine) {
//            Integer moyVal = engine.getValueQuiet(MinguoChronology.INSTANCE.monthOfYear());
//            Integer domVal = engine.getValueQuiet(MinguoChronology.INSTANCE.dayOfMonth());
//            if (moyVal != null && domVal != null) {
//                int year = engine.getParsed(this);
//                int yearOfEra = Math.abs(year);
//                MinguoEra era = (year < 1 ? MinguoEra.BEFORE_MINGUO : MinguoEra.MINGUO);
//                MinguoDate date;
//                if (engine.isStrict()) {
//                    date = MinguoDate.minguoDate(era, yearOfEra, moyVal, domVal);
//                } else {
//                    date = MinguoDate.minguoDate(era, yearOfEra, 1, 1).plusMonths(moyVal)
//                            .plusMonths(-1).plusDays(domVal).plusDays(-1);
//                }
//                engine.storeMergedDate(date.toLocalDate());
//                engine.markFieldAsProcessed(this);
//                engine.markFieldAsProcessed(MinguoChronology.INSTANCE.monthOfYear());
//                engine.markFieldAsProcessed(MinguoChronology.INSTANCE.dayOfMonth());
//            }
//        }
//    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    private static final class EraRule extends DateTimeRule implements Serializable {
        /** Singleton instance. */
        private static final DateTimeRule INSTANCE = new EraRule();
        /** A serialization identifier for this class. */
        private static final long serialVersionUID = 1L;
        /** Constructor. */
        private EraRule() {
            super("MinguoEra", periodEras(), null, 0, 1, null);
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        protected DateTimeField deriveFrom(CalendricalEngine engine) {
            MinguoDate date = engine.derive(MinguoDate.rule());
            return date != null ? field(date.getEra().getValue()) : null;
        }
        // TODO: never worked properly, needs to use proper provider
        @Override
        public String getText(long value, TextStyle textStyle, Locale locale) {
            String[] names = null;
            String language = locale.getLanguage();
            
            if (textStyle == TextStyle.NARROW) {
                names = ERA_NARROW_NAMES.get(language);
                if (names == null) {
                    names = ERA_NARROW_NAMES.get(FALLBACK_LANGUAGE);
                }
            }
            if (textStyle == TextStyle.SHORT) {
                names = ERA_SHORT_NAMES.get(language);
                if (names == null) {
                    names = ERA_SHORT_NAMES.get(FALLBACK_LANGUAGE);
                }
            }
            if (textStyle == TextStyle.FULL) {
                names = ERA_FULL_NAMES.get(language);
                if (names == null) {
                    names = ERA_FULL_NAMES.get(FALLBACK_LANGUAGE);
                }
            }
            return names == null ? Long.toString(value) : names[(int) value];
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    private static final class YearOfEraRule extends DateTimeRule implements Serializable {
        /** Singleton instance. */
        private static final DateTimeRule INSTANCE = new YearOfEraRule();
        /** A serialization identifier for this class. */
        private static final long serialVersionUID = 1L;
        /** Constructor. */
        private YearOfEraRule() {
            super("MinguoYearOfEra", periodYears(), periodEras(),
                    MinguoDate.MIN_YEAR_OF_ERA, MinguoDate.MAX_YEAR_OF_ERA, null);
        }
        private Object readResolve() {
            return INSTANCE;
        }
        // TODO: min/max years based on era
        @Override
        protected void normalize(CalendricalEngine engine) {
            DateTimeField eraVal = engine.getFieldDerived(MinguoChronology.eraRule(), false);
            MinguoEra era = (eraVal != null ? MinguoEra.of(eraVal.getValidIntValue()) : MinguoEra.MINGUO);
            DateTimeField yoeVal = engine.getFieldDerived(this, false);
            // era, year, month, day-of-month
            DateTimeField moy = engine.getFieldDerived(MinguoChronology.monthOfYearRule(), false);
            DateTimeField domVal = engine.getFieldDerived(MinguoChronology.dayOfMonthRule(), false);
            if (moy != null && domVal != null) {
                MinguoDate date = MinguoDate.of(era, yoeVal.getValidIntValue(), MonthOfYear.of(moy.getValidIntValue()), domVal.getValidIntValue());
                engine.setDate(date.toLocalDate(), true);
//                engine.removeProcessed(MinguoChronology.eraRule());
//                engine.removeProcessed(this);
//                engine.removeProcessed(MinguoChronology.monthOfYearRule());
//                engine.removeProcessed(MinguoChronology.dayOfMonthRule());
            }
            // era, year, day-of-year
            DateTimeField doyVal = engine.getFieldDerived(MinguoChronology.dayOfYearRule(), false);
            if (doyVal != null) {
                MinguoDate date = MinguoDate.of(era, yoeVal.getValidIntValue(), MonthOfYear.JANUARY, 1).plusDays(doyVal.getValidIntValue() - 1);
                engine.setDate(date.toLocalDate(), true);
//                engine.removeProcessed(MinguoChronology.eraRule());
//                engine.removeProcessed(this);
//                engine.removeProcessed(MinguoChronology.yearOfEraRule());
//                engine.removeProcessed(MinguoChronology.dayOfYearRule());
            }
        }
        @Override
        protected DateTimeField deriveFrom(CalendricalEngine engine) {
            MinguoDate date = engine.derive(MinguoDate.rule());
            return date != null ? field(date.getYearOfEra()) : null;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    private static final class MonthOfYearRule extends DateTimeRule implements Serializable {
        /** Singleton instance. */
        private static final DateTimeRule INSTANCE = new MonthOfYearRule();
        /** A serialization identifier for this class. */
        private static final long serialVersionUID = 1L;
        /** Constructor. */
        private MonthOfYearRule() {
            super("MinguoMonthOfYear", periodMonths(), periodYears(), 1, 12, null);
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        protected DateTimeField deriveFrom(CalendricalEngine engine) {
            MinguoDate date = engine.derive(MinguoDate.rule());
            return date != null ? field(date.getMonthOfYear().getValue()) : null;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    private static final class DayOfMonthRule extends DateTimeRule implements Serializable {
        /** Singleton instance. */
        private static final DateTimeRule INSTANCE = new DayOfMonthRule();
        /** A serialization identifier for this class. */
        private static final long serialVersionUID = 1L;
        /** Constructor. */
        private DayOfMonthRule() {
            super("MinguoDayOfMonth", periodDays(), periodMonths(), DateTimeRuleRange.of(1, 28, 31), null);
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        public DateTimeRuleRange getValueRange(Calendrical calendrical) {
            DateTimeField moyVal = calendrical.get(MinguoChronology.monthOfYearRule());
            if (moyVal != null) {
                MonthOfYear moy = MonthOfYear.of(moyVal.getValidIntValue());
                if (moy == MonthOfYear.FEBRUARY) {
                    DateTimeField eraVal = calendrical.get(MinguoEra.rule());
                    DateTimeField yoeVal = calendrical.get(MinguoChronology.yearOfEraRule());
                    if (eraVal != null && yoeVal != null) {
                        int yoe = yoeVal.getValidIntValue();
                        int isoYear = (eraVal.getValidIntValue() == MinguoEra.BEFORE_MINGUO.getValue() ? 1 - yoe : yoe) + YEAR_OFFSET;
                        return DateTimeRuleRange.of(1, moy.lengthInDays(ISOChronology.isLeapYear(isoYear)));
                    }
                    return DateTimeRuleRange.of(1, 28, 29);
                } else {
                    return DateTimeRuleRange.of(1, moy.maxLengthInDays());
                }
            }
            return getValueRange();
        }
        @Override
        protected DateTimeField deriveFrom(CalendricalEngine engine) {
            MinguoDate date = engine.derive(MinguoDate.rule());
            return date != null ? field(date.getDayOfMonth()) : null;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    private static final class DayOfYearRule extends DateTimeRule implements Serializable {
        /** Singleton instance. */
        private static final DateTimeRule INSTANCE = new DayOfYearRule();
        /** A serialization identifier for this class. */
        private static final long serialVersionUID = 1L;
        /** Constructor. */
        private DayOfYearRule() {
            super("MinguoDayOfYear", periodDays(), periodYears(), DateTimeRuleRange.of(1, 365, 366), null);
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        public DateTimeRuleRange getValueRange(Calendrical calendrical) {
            DateTimeField era = calendrical.get(MinguoEra.rule());
            DateTimeField yoeVal = calendrical.get(MinguoChronology.yearOfEraRule());
            if (era != null && yoeVal != null) {
                int yoe = yoeVal.getValidIntValue();
                int isoYear = (era.getValidIntValue() == MinguoEra.BEFORE_MINGUO.getValue() ? 1 - yoe : yoe) + YEAR_OFFSET;
                return DateTimeRuleRange.of(1, ISOChronology.isLeapYear(isoYear) ? 366 : 365);
            }
            return getValueRange();
        }
        @Override
        protected DateTimeField deriveFrom(CalendricalEngine engine) {
            MinguoDate date = engine.derive(MinguoDate.rule());
            return date != null ? field(date.getDayOfYear()) : null;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    private static final class DayOfWeekRule extends DateTimeRule implements Serializable {
        /** Singleton instance. */
        private static final DateTimeRule INSTANCE = new DayOfWeekRule();
        /** A serialization identifier for this class. */
        private static final long serialVersionUID = 1L;
        /** Constructor. */
        private DayOfWeekRule() {
            super("MinguoDayOfWeek", periodDays(), periodWeeks(), 1, 7, null);
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        protected DateTimeField deriveFrom(CalendricalEngine engine) {
            MinguoDate date = engine.derive(MinguoDate.rule());
            return date != null ? field(date.getDayOfWeek().getValue()) : null;
        }
    }

}
