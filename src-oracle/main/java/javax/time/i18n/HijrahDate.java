/*
 * Copyright (c) 2009 Oracle All Rights Reserved.
 */
package javax.time.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.time.CalendricalException;
import javax.time.calendar.MathUtils;
import javax.time.calendar.Calendrical;
import javax.time.calendar.CalendricalEngine;
import javax.time.calendar.CalendricalRule;
import javax.time.DayOfWeek;
import javax.time.calendar.IllegalCalendarFieldValueException;
import javax.time.calendar.InvalidCalendarFieldException;
import javax.time.LocalDate;

/**
 * A date in the Hijrah calendar system.
 * <p>
 * {@code HijrahDate} is an immutable class that represents a date in the Hijrah calendar system.
 * The rules of the calendar system are described in {@link HijrahChronology}.
 * <p>
 * The Hijrah calendar has a different total of days in a year than
 * Gregorian calendar, and a month is based on the period of a complete
 * revolution of the moon around the earth (as between successive new moons).
 * The calendar cycles becomes longer and unstable, and sometimes a manual
 * adjustment (for entering deviation) is necessary for correctness
 * because of the complex algorithm.
 * <p>
 * HijrahDate supports the manual adjustment feature by providing a configuration
 * file. The configuration file contains the adjustment (deviation) data with following format.
 * <pre>
 *   StartYear/StartMonth(0-based)-EndYear/EndMonth(0-based):Deviation day (1, 2, -1, or -2)
 *   Line separator or ";" is used for the separator of each deviation data.</pre>
 *   Here is the example.
 * <pre>
 *     1429/0-1429/1:1
 *     1429/2-1429/7:1;1429/6-1429/11:1
 *     1429/11-9999/11:1</pre>
 * The default location of the configuration file is:
 * <pre>
 *   $CLASSPATH/javax/time/i18n</pre>
 * And the default file name is:
 * <pre>
 *   hijrah_deviation.cfg</pre>
 * The default location and file name can be overriden by setting
 * following two Java's system property.
 * <pre>
 *   Location: javax.time.i18n.HijrahDate.deviationConfigDir
 *   File name: javax.time.i18n.HijrahDate.deviationConfigFile</pre>
 * <p>
 * Instances of this class may be created from other date objects that implement {@code Calendrical}.
 * Notably this includes {@link LocalDate} and all other date classes from other calendar systems.
 * <p>
 * HijrahDate is immutable and thread-safe.
 *
 * @author Ryoji Suzuki
 * @author Stephen Colebourne
 */
public final class HijrahDate
        implements Calendrical, Comparable<HijrahDate>, Serializable {

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = 4427481252775308020L;
    /**
     * The minimum valid year-of-era.
     */
    public static final int MIN_YEAR_OF_ERA = 1;
    /**
     * The maximum valid year-of-era.
     * This is currently set to 9999 but may be changed to increase the valid range
     * in a future version of the specification.
     */
    public static final int MAX_YEAR_OF_ERA = 9999;
    /**
     * 0-based, for number of day-of-year in the beginning of month in normal
     * year.
     */
    private static final int NUM_DAYS[] =
        {0, 30, 59, 89, 118, 148, 177, 207, 236, 266, 295, 325};
    /**
     * 0-based, for number of day-of-year in the beginning of month in leap year.
     */
    private static final int LEAP_NUM_DAYS[] =
        {0, 30, 59, 89, 118, 148, 177, 207, 236, 266, 295, 325};
    /**
     * 0-based, for day-of-month in normal year.
     */
    private static final int MONTH_LENGTH[] =
        {30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29};
    /**
     * 0-based, for day-of-month in leap year.
     */
    private static final int LEAP_MONTH_LENGTH[] =
        {30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30};

    /**
     * <pre>
     *                            Greatest       Least
     * Field name        Minimum   Minimum     Maximum     Maximum
     * ----------        -------   -------     -------     -------
     * ERA                     0         0           1           1
     * YEAR_OF_ERA             1         1        9999        9999
     * MONTH_OF_YEAR           1         1          12          12
     * DAY_OF_MONTH            1         1          29          30
     * DAY_OF_YEAR             1         1         354         355
     * </pre>
     *
     * Minimum values.
     */
    private static final int MIN_VALUES[] =
        {
        0,
        MIN_YEAR_OF_ERA,
        0,
        1,
        0,
        1,
        1
        };

    /**
     * Least maximum values.
     */
    private static final int LEAST_MAX_VALUES[] =
        {
        1,
        MAX_YEAR_OF_ERA,
        11,
        51,
        5,
        29,
        354
        };

    /**
     * Maximum values.
     */
    private static final int MAX_VALUES[] =
        {
        1,
        MAX_YEAR_OF_ERA,
        11,
        52,
        6,
        30,
        355
        };

   /**
     * Position of day-of-month. This value is used to get the min/max value
     * from an array.
     */
    private static final int POSITION_DAY_OF_MONTH = 5;
    /**
     * Position of day-of-year. This value is used to get the min/max value from
     * an array.
     */
    private static final int POSITION_DAY_OF_YEAR = 6;
    /**
     * Zero-based start date of cycle year.
     */
    private static final int CYCLEYEAR_START_DATE[] =
        {
        0,
        354,
        709,
        1063,
        1417,
        1772,
        2126,
        2481,
        2835,
        3189,
        3544,
        3898,
        4252,
        4607,
        4961,
        5315,
        5670,
        6024,
        6379,
        6733,
        7087,
        7442,
        7796,
        8150,
        8505,
        8859,
        9214,
        9568,
        9922,
        10277
        };

    /**
     * File separator.
     */
    private static final char FILE_SEP = File.separatorChar;
    /**
     * Path separator.
     */
    private static final String PATH_SEP = File.pathSeparator;
    /**
     * Default config file name.
     */
    private static final String DEFAULT_CONFIG_FILENAME = "hijrah_deviation.cfg";
    /**
     * Default path to the config file.
     */
    private static final String DEFAULT_CONFIG_PATH = "javax" + FILE_SEP + "time" + FILE_SEP + "i18n";
    /**
     * Holding the adjusted month days in year. The key is a year (Integer) and
     * the value is the all the month days in year (Integer[]).
     */
    private static final Hashtable<Integer, Integer[]> ADJUSTED_MONTH_DAYS = new Hashtable<Integer, Integer[]>();
    /**
     * Holding the adjusted month length in year. The key is a year (Integer)
     * and the value is the all the month length in year (Integer[]).
     */
    private static final Hashtable<Integer, Integer[]> ADJUSTED_MONTH_LENGTHS = new Hashtable<Integer, Integer[]>();
    /**
     * Holding the adjusted days in the 30 year cycle. The key is a cycle number
     * (Integer) and the value is the all the starting days of the year in the
     * cycle (Integer[]).
     */
    private static final Hashtable<Integer, Integer[]> ADJUSTED_CYCLE_YEARS = new Hashtable<Integer, Integer[]>();
    /**
     * Holding the adjusted cycle in the 1 - 30000 year. The key is the cycle
     * number (Integer) and the value is the starting days in the cycle in the
     * term.
     */
    private static final Long[] ADJUSTED_CYCLES;
    /**
     * Holding the adjusted min values.
     */
    private static final Integer[] ADJUSTED_MIN_VALUES;
    /**
     * Holding the adjusted max least max values.
     */
    private static final Integer[] ADJUSTED_LEAST_MAX_VALUES;
    /**
     * Holding adjusted max values.
     */
    private static final Integer[] ADJUSTED_MAX_VALUES;
    /**
     * Holding the non-adjusted month days in year for non leap year.
     */
    private static final Integer[] DEFAULT_MONTH_DAYS;
    /**
     * Holding the non-adjusted month days in year for leap year.
     */
    private static final Integer[] DEFAULT_LEAP_MONTH_DAYS;
    /**
     * Holding the non-adjusted month length for non leap year.
     */
    private static final Integer[] DEFAULT_MONTH_LENGTHS;
    /**
     * Holding the non-adjusted month length for leap year.
     */
    private static final Integer[] DEFAULT_LEAP_MONTH_LENGTHS;
    /**
     * Holding the non-adjusted 30 year cycle starting day.
     */
    private static final Integer[] DEFAULT_CYCLE_YEARS;
    /**
     * number of 30-year cycles to hold the deviation data.
     */
    private static final int MAX_ADJUSTED_CYCLE = 334; // to support year 9999

    static { // Initialize the static integer array;

        DEFAULT_MONTH_DAYS = new Integer[NUM_DAYS.length];
        for (int i = 0; i < NUM_DAYS.length; i++) {
            DEFAULT_MONTH_DAYS[i] = new Integer(NUM_DAYS[i]);
        }

        DEFAULT_LEAP_MONTH_DAYS = new Integer[LEAP_NUM_DAYS.length];
        for (int i = 0; i < LEAP_NUM_DAYS.length; i++) {
            DEFAULT_LEAP_MONTH_DAYS[i] = new Integer(LEAP_NUM_DAYS[i]);
        }

        DEFAULT_MONTH_LENGTHS = new Integer[MONTH_LENGTH.length];
        for (int i = 0; i < MONTH_LENGTH.length; i++) {
            DEFAULT_MONTH_LENGTHS[i] = new Integer(MONTH_LENGTH[i]);
        }

        DEFAULT_LEAP_MONTH_LENGTHS = new Integer[LEAP_MONTH_LENGTH.length];
        for (int i = 0; i < LEAP_MONTH_LENGTH.length; i++) {
            DEFAULT_LEAP_MONTH_LENGTHS[i] = new Integer(LEAP_MONTH_LENGTH[i]);
        }

        DEFAULT_CYCLE_YEARS = new Integer[CYCLEYEAR_START_DATE.length];
        for (int i = 0; i < CYCLEYEAR_START_DATE.length; i++) {
            DEFAULT_CYCLE_YEARS[i] = new Integer(CYCLEYEAR_START_DATE[i]);
        }

        ADJUSTED_CYCLES = new Long[MAX_ADJUSTED_CYCLE];
        for (int i = 0; i < ADJUSTED_CYCLES.length; i++) {
            ADJUSTED_CYCLES[i] = new Long(10631 * i);
        }
        // Initialize min values, least max values and max values.
        ADJUSTED_MIN_VALUES = new Integer[MIN_VALUES.length];
        for (int i = 0; i < MIN_VALUES.length; i++) {
            ADJUSTED_MIN_VALUES[i] = new Integer(MIN_VALUES[i]);
        }
        ADJUSTED_LEAST_MAX_VALUES = new Integer[LEAST_MAX_VALUES.length];
        for (int i = 0; i < LEAST_MAX_VALUES.length; i++) {
            ADJUSTED_LEAST_MAX_VALUES[i] = new Integer(LEAST_MAX_VALUES[i]);
        }
        ADJUSTED_MAX_VALUES = new Integer[MAX_VALUES.length];
        for (int i = 0; i < MAX_VALUES.length; i++) {
            ADJUSTED_MAX_VALUES[i] = new Integer(MAX_VALUES[i]);
        }
        try {
            readDeviationConfig();
        } catch (IOException e) {
            // do nothing. Ignore deviation config.
            // e.printStackTrace();
        } catch (ParseException e) {
            // do nothing. Ignore deviation config.
            // e.printStackTrace();
        }
    }
    /**
     * Number of Gregorian day of July 19, year 622 (Gregorian), which is epoch day
     * of Hijrah calendar.
     */
    private static final int HIJRAH_JAN_1_1_GREGORIAN_DAY = -492148;

    /**
     * The era.
     */
    private final transient HijrahEra era;
    /**
     * The year.
     */
    private final transient int yearOfEra;
    /**
     * The month-of-year.
     */
    private final transient int monthOfYear;
    /**
     * The day-of-month.
     */
    private final transient int dayOfMonth;
    /**
     * The day-of-year.
     */
    private final transient int dayOfYear;
    /**
     * The day-of-week.
     */
    private final transient DayOfWeek dayOfWeek;
    /**
     * Gregorian days for this object. Holding number of days since 1970/01/01.
     * The number of days are calculated with pure Gregorian calendar
     * based.
     */
    private final long gregorianEpochDay;
    /**
     * True if year is leap year.
     */
    private final transient boolean isLeapYear;

    //-----------------------------------------------------------------------
    /**
     * Gets the rule for {@code HijrahDate}.
     *
     * @return the rule for the date, never null
     */
    public static CalendricalRule<HijrahDate> rule() {
        return Rule.INSTANCE;
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains an instance of {@code HijrahDate} from the Hijrah era year,
     * month-of-year and day-of-month. This uses the Hijrah era.
     *
     * @param yearOfHijrahEra  the year to represent in the Hijrah era, from 1 to 9999
     * @param monthOfYear  the month-of-year to represent, from 1 to 12
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the Hijrah date, never null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static HijrahDate of(int yearOfHijrahEra, int monthOfYear, int dayOfMonth) {
        return HijrahDate.of(HijrahEra.HIJRAH, yearOfHijrahEra, monthOfYear, dayOfMonth);
    }

    /**
     * Obtains an instance of {@code HijrahDate} from the era, year-of-era
     * month-of-year and day-of-month.
     *
     * @param era  the era to represent, not null
     * @param yearOfEra  the year-of-era to represent, from 1 to 9999
     * @param monthOfYear  the month-of-year to represent, from 1 to 12
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the Hijrah date, never null
     * @throws IllegalCalendarFieldValueException if the value of any field is out of range
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public static HijrahDate of(HijrahEra era, int yearOfEra, int monthOfYear, int dayOfMonth) {
        I18NUtil.checkNotNull(era, "HijrahEra must not be null");
        HijrahChronology.yearOfEraRule().checkValidValue(yearOfEra);
        HijrahChronology.monthOfYearRule().checkValidValue(monthOfYear);
        HijrahChronology.dayOfMonthRule().checkValidValue(dayOfMonth);
        long gregorianDays = getGregorianEpochDay(era.getValue(), yearOfEra, monthOfYear, dayOfMonth);
        return new HijrahDate(gregorianDays);
    }

    /**
     * Obtains an instance of {@code HijrahDate} from a calendrical.
     * <p>
     * This can be used extract the date directly from any implementation
     * of {@code Calendrical}, including those in other calendar systems.
     *
     * @param calendrical  the calendrical to extract from, not null
     * @return the Hijrah date, never null
     * @throws CalendricalException if the date cannot be obtained
     */
    public static HijrahDate of(Calendrical calendrical) {
        return rule().getValueChecked(calendrical);
    }

    /**
     * Obtains an instance of {@code HijrahDate} from a date.
     *
     * @param date  the date to use, not null
     * @return the Hijrah date, never null
     * @throws IllegalCalendarFieldValueException if the year is invalid
     */
    static HijrahDate of(LocalDate date) {
        long gregorianDays = date.toEpochDay();
        return new HijrahDate(gregorianDays);
    }

    /**
     * Constructs an instance with the specified date.
     *
     * @param gregorianDay  the number of days from 0001/01/01 (Gregorian), caller calculated
     */
    private HijrahDate(long gregorianDay) {
        int[] dateInfo = getHijrahDateInfo(gregorianDay);
        
        HijrahChronology.eraRule().checkValidValue(dateInfo[0]);
        HijrahChronology.yearOfEraRule().checkValidValue(dateInfo[1]);
        HijrahChronology.monthOfYearRule().checkValidValue(dateInfo[2]);
        HijrahChronology.dayOfMonthRule().checkValidValue(dateInfo[3]);
        HijrahChronology.dayOfYearRule().checkValidValue(dateInfo[4]);
        HijrahChronology.dayOfWeekRule().checkValidValue(dateInfo[5]);
        
        this.era = HijrahEra.of(dateInfo[0]);
        this.yearOfEra = dateInfo[1];
        this.monthOfYear = dateInfo[2];
        this.dayOfMonth = dateInfo[3];
        this.dayOfYear = dateInfo[4];
        this.dayOfWeek = DayOfWeek.of(dateInfo[5]);
        this.gregorianEpochDay = gregorianDay;
        this.isLeapYear = isLeapYear(this.yearOfEra);
    }

    /**
     * Replaces the date instance from the stream with a valid one.
     *
     * @return the resolved date, never null
     */
    private Object readResolve() {
        return new HijrahDate(this.gregorianEpochDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the chronology that this date uses, which is the Hijrah calendar system.
     *
     * @return the Hijrah chronology, never null
     */
    public HijrahChronology getChronology() {
        return HijrahChronology.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the specified calendrical rule.
     * <p>
     * This method queries the value of the specified calendrical rule.
     * If the value cannot be returned for the rule from this date then
     * {@code null} will be returned.
     *
     * @param ruleToDerive  the rule to derive, not null
     * @return the value for the rule, null if the value cannot be returned
     */
    public <T> T get(CalendricalRule<T> ruleToDerive) {
        return CalendricalEngine.derive(ruleToDerive, rule(), toLocalDate(), null, null, null, getChronology(), null);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the Hijrah era field.
     *
     * @return the era, never null
     */
    public HijrahEra getEra() {
        return this.era;
    }

    /**
     * Gets the Hijrah year-of-era field.
     *
     * @return the year, from 1 to 9999
     */
    public int getYearOfEra() {
        return this.yearOfEra;
    }

    /**
     * Gets the Hijrah month-of-year field.
     *
     * @return the month-of-year, from 1 to 12
     */
    public int getMonthOfYear() {
        return this.monthOfYear;
    }

    /**
     * Gets the Hijrah day-of-month field.
     *
     * @return the day-of-month, from 1 to 29-30 (it changes depending on deviation configuration)
     */
    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    /**
     * Gets the Hijrah day-of-year field.
     *
     * @return the day-of-year, from 1 to 354-355 (it changes depending on deviation configuration)
     */
    public int getDayOfYear() {
        return this.dayOfYear;
    }

    /**
     * Gets the day-of-week field, which is an enum {@code DayOfWeek}.
     * <p>
     * This method returns the enum {@link DayOfWeek} for the day-of-week.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link DayOfWeek#getValue() int value}.
     * <p>
     * Additional information can be obtained from the {@code DayOfWeek}.
     * This includes textual names of the values.
     *
     * @return the day-of-week, never null
     */
    public DayOfWeek getDayOfWeek() {
        return this.dayOfWeek;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the year is a leap year, according to the Hijrah calendar system rules.
     *
     * @return true if this date is in a leap year
     */
    public boolean isLeapYear() {
        return this.isLeapYear;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date with the year altered.
     * <p>
     * This method changes the year of the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param era  the era to set in the returned date, not null
     * @param yearOfEra  the year-of-era to set in the returned date, from 1 to 9999
     * @return a {@code HijrahDate} based on this date with the requested year, never null
     * @throws IllegalCalendarFieldValueException if the year-of-era value is invalid
     */
    public HijrahDate withYear(HijrahEra era, int yearOfEra) {
        return HijrahDate.of(era, yearOfEra, this.monthOfYear, this.dayOfMonth);
    }

    /**
     * Returns a copy of this date with the year-of-era altered.
     * <p>
     * This method changes the year-of-era of the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearOfEra  the year-of-era to set in the returned date, from 1 to 9999
     * @return a {@code HijrahDate} based on this date with the requested year-of-era, never null
     * @throws IllegalCalendarFieldValueException if the year-of-era value is invalid
     */
    public HijrahDate withYearOfEra(int yearOfEra) {
        return withYear(getEra(), yearOfEra);
    }

    /**
     * Returns a copy of this date with the month-of-year altered.
     * <p>
     * This method changes the month-of-year of the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, from 1 to 12
     * @return a {@code HijrahDate} based on this date with the requested month, never null
     * @throws IllegalCalendarFieldValueException if the month-of-year is invalid
     */
    public HijrahDate withMonthOfYear(int monthOfYear) {
        return HijrahDate.of(this.era, this.yearOfEra, monthOfYear, this.dayOfMonth);
    }

    /**
     * Returns a copy of this date with the day-of-month altered.
     * <p>
     * This method changes the day-of-month of the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 29-30 (it changes depending on deviation configuration)
     * @return a {@code HijrahDate} based on this date with the requested day, never null
     * @throws IllegalCalendarFieldValueException if the day-of-month value is invalid
     * @throws InvalidCalendarFieldException if the day-of-month is invalid for the month-year
     */
    public HijrahDate withDayOfMonth(int dayOfMonth) {
        return HijrahDate.of(this.era, this.yearOfEra, this.monthOfYear, dayOfMonth);
    }

    /**
     * Returns a copy of this date with the day-of-year altered.
     * <p>
     * This method changes the day-of-year of the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 354-355 (it changes depending on deviation configuration)
     * @return a {@code HijrahDate} based on this date with the requested day, never null
     * @throws IllegalCalendarFieldValueException if the day-of-year value is invalid
     * @throws InvalidCalendarFieldException if the day-of-year is invalid for the year
     */
    public HijrahDate withDayOfYear(int dayOfYear) {
        HijrahChronology.dayOfYearRule().checkValidValue(dayOfYear);
        return HijrahDate.of(this.era, this.yearOfEra, 1, 1).plusDays(dayOfYear).plusDays(-1);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date with the specified number of years added.
     * <p>
     * This method adds the specified amount in years to the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return a {@code HijrahDate} based on this date with the years added, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate plusYears(int years) {
        if (years == 0) {
            return this;
        }
        int newYear = 0;
        newYear = MathUtils.safeAdd(this.yearOfEra, years);
        return HijrahDate.of(this.era, newYear, this.monthOfYear, this.dayOfMonth);
    }

    /**
     * Returns a copy of this date with the specified number of months added.
     * <p>
     * This method adds the specified amount in months to the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return a {@code HijrahDate} based on this date with the months added, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate plusMonths(int months) {
        if (months == 0) {
            return this;
        }
        int newMonth = this.monthOfYear - 1;
        newMonth = newMonth + months;
        int years = newMonth / 12;
        newMonth = newMonth % 12;
        if (newMonth < 0) {
            newMonth += 12;
            years = MathUtils.safeDecrement(years);
        }
        int newYear = MathUtils.safeAdd(this.yearOfEra, years);
        return HijrahDate.of(this.era, newYear, newMonth + 1, this.dayOfMonth);
    }

    /**
     * Returns a copy of this date with the specified number of weeks added.
     * <p>
     * This method adds the specified amount in weeks to the date.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return a {@code HijrahDate} based on this date with the weeks added, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate plusWeeks(int weeks) {
        return plusDays(7L * weeks);
    }

    /**
     * Returns a copy of this date with the specified number of days added.
     * <p>
     * This method adds the specified amount in days to the date.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return a {@code HijrahDate} based on this date with the days added, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate plusDays(long days) {
        return new HijrahDate(this.gregorianEpochDay + days);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date with the specified number of years subtracted.
     * <p>
     * This method subtracts the specified amount in years from the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return a {@code HijrahDate} based on this date with the years subtracted, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate minusYears(int years) {
        if (years == 0) {
            return this;
        }
        int newYear = 0;
        newYear = MathUtils.safeSubtract(this.yearOfEra, years);
        return HijrahDate.of(this.era, newYear, this.monthOfYear, this.dayOfMonth);
    }

    /**
     * Returns a copy of this date with the specified number of months subtracted.
     * <p>
     * This method subtracts the specified amount in months from the date.
     * If the resulting date is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return a {@code HijrahDate} based on this date with the months subtracted, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate minusMonths(int months) {
        if (months == 0) {
            return this;
        }
        int years = months / 12;
        int newMonth = this.monthOfYear - 1;
        newMonth = newMonth - (months % 12);
        if (newMonth >= 12) {
            newMonth = newMonth % 12;
            years = MathUtils.safeDecrement(years);
        } else if (newMonth < 0) {
            newMonth += 12;
            years = MathUtils.safeIncrement(years);
        }
        int newYear = MathUtils.safeAdd(this.yearOfEra, years);
        return HijrahDate.of(this.era, newYear, (newMonth + 1), this.dayOfMonth);
    }

    /**
     * Returns a copy of this date with the specified number of weeks subtracted.
     * <p>
     * This method subtracts the specified amount in weeks from the date.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return a {@code HijrahDate} based on this date with the weeks subtracted, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate minusWeeks(int weeks) {
        return minusDays(7L * weeks);
    }

    /**
     * Returns a copy of this date with the specified number of days subtracted.
     * <p>
     * This method subtracts the specified amount in days from the date.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return a {@code HijrahDate} based on this date with the days subtracted, never null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public HijrahDate minusDays(long days) {
        return new HijrahDate(this.gregorianEpochDay - days);
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date to a {@code LocalDate}, which is the default representation
     * of a date, and provides values in the ISO-8601 calendar system.
     *
     * @return the equivalent date in the ISO-8601 calendar system, never null
     */
    public LocalDate toLocalDate() {
        return LocalDate.ofEpochDay(this.gregorianEpochDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this date to another date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    public int compareTo(HijrahDate other) {
        return MathUtils.safeCompare(this.gregorianEpochDay, other.gregorianEpochDay);
    }

    /**
     * Checks if this date is after the specified date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is after the specified date
     */
    public boolean isAfter(HijrahDate other) {
        return this.gregorianEpochDay > other.gregorianEpochDay;
    }

    /**
     * Checks if this date is before the specified date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is before the specified date
     */
    public boolean isBefore(HijrahDate other) {
        return this.gregorianEpochDay < other.gregorianEpochDay;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this date is equal to the specified date.
     * <p>
     * The comparison is based on the time-line position of the dates.
     *
     * @param other  the other date to compare to, null returns false
     * @return true if this is equal to the specified date
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof HijrahDate) {
            HijrahDate otherDate = (HijrahDate) other;
            return this.gregorianEpochDay == otherDate.gregorianEpochDay;
        }
        return false;
    }

    /**
     * A hash code for this date.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return (yearOfEra & 0xFFFFF800) ^ ((yearOfEra << 11) + (monthOfYear << 6) + (dayOfMonth));
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this date as a {@code String}, such as {@code 1430-03-05 (Hijrah)}.
     * <p>
     * The output will be in the format {@code yyyy-MM-dd (Hijrah)}.
     *
     * @return the formatted date, never null
     */
    @Override
    public String toString() {
        int yearValue = getYearOfEra();
        int monthValue = getMonthOfYear();
        int dayValue = getDayOfMonth();
        StringBuilder buf = new StringBuilder();
        buf.append(yearValue);
        return buf.append(monthValue < 10 ? "-0" : "-").append(monthValue)
                .append(dayValue < 10 ? "-0" : "-").append(dayValue).append(
                        " (" + getChronology().getName() + ")").toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the int array containing the following field from the julian day.
     *
     * int[0] = ERA
     * int[1] = YEAR
     * int[2] = MONTH
     * int[3] = DATE
     * int[4] = DAY_OF_YEAR
     * int[5] = DAY_OF_WEEK
     *
     * @param julianDay  a julian day.
     */
    private static int[] getHijrahDateInfo(long gregorianDays) {
        int era, year, month, date, dayOfWeek, dayOfYear;

        int cycleNumber, yearInCycle, dayOfCycle;

        long epochDay = gregorianDays - HIJRAH_JAN_1_1_GREGORIAN_DAY;

        if (epochDay >= 0) {
            cycleNumber = getCycleNumber(epochDay); // 0 - 99.
            dayOfCycle = getDayOfCycle(epochDay, cycleNumber); // 0 - 10631.
            yearInCycle = getYearInCycle(cycleNumber, dayOfCycle); // 0 - 29.
            dayOfYear = getDayOfYear(cycleNumber, dayOfCycle, yearInCycle);
            // 0 - 354/355
            year = cycleNumber * 30 + yearInCycle + 1; // 1-based year.
            month = getMonthOfYear(dayOfYear, year); // 0-based month-of-year
            date = getDayOfMonth(dayOfYear, month, year); // 0-based date
            ++date; // Convert from 0-based to 1-based
            era = HijrahEra.HIJRAH.getValue();
        } else {
            cycleNumber = (int) epochDay / 10631; // 0 or negative number.
            dayOfCycle = (int) epochDay % 10631; // -10630 - 0.
            if (dayOfCycle == 0) {
                dayOfCycle = -10631;
                cycleNumber++;
            }
            yearInCycle = getYearInCycle(cycleNumber, dayOfCycle); // 0 - 29.
            dayOfYear = getDayOfYear(cycleNumber, dayOfCycle, yearInCycle);
            year = cycleNumber * 30 - yearInCycle; // negative number.
            year = 1 - year;
            dayOfYear = (isLeapYear(year) ? (dayOfYear + 355)
                    : (dayOfYear + 354));
            month = getMonthOfYear(dayOfYear, year);
            date = getDayOfMonth(dayOfYear, month, year);
            ++date; // Convert from 0-based to 1-based
            era = HijrahEra.BEFORE_HIJRAH.getValue();
        }
        // Hijrah day zero is a Friday
        dayOfWeek = (int) ((epochDay + 5) % 7);
        dayOfWeek += (dayOfWeek <= 0) ? 7 : 0;

        int dateInfo[] = new int[6];
        dateInfo[0] = era;
        dateInfo[1] = year;
        dateInfo[2] = month + 1; // change to 1-based.
        dateInfo[3] = date;
        dateInfo[4] = dayOfYear + 1; // change to 1-based.
        dateInfo[5] = dayOfWeek;
        return dateInfo;
    }

    /**
     * Return Gregorian epoch day from Hijrah year, month, and day.
     *
     * @param era  the era to represent, caller calculated
     * @param year  the year to represent, caller calculated
     * @param monthOfYear  the month-of-year to represent, caller calculated
     * @param dayOfMonth  the day-of-month to represent, caller calculated
     * @return a julian day
     */
    private static long getGregorianEpochDay(int era, int year, int monthOfYear, int dayOfMonth) {
        long day = yearToGregorianEpochDay(era, year);
        day += getMonthDays(monthOfYear - 1, year);
        day += dayOfMonth;
        return day;
    }

    /**
     * Returns the Gregorian epoch day from the year.
     *
     * @param era  the era to represent
     * @param year  the year to represent
     * @return a julian day
     */
    private static long yearToGregorianEpochDay(int era, int year) {

        if (era == HijrahEra.BEFORE_HIJRAH.getValue()) {
            year = 1 - year;
        }

        int cycleNumber = (year - 1) / 30; // 0-based.
        int yearInCycle = (year - 1) % 30; // 0-based.

        int dayInCycle = getAdjustedCycle(cycleNumber)[Math.abs(yearInCycle)]
                .intValue();

        if (yearInCycle < 0) {
            dayInCycle = -dayInCycle;
        }

        Long cycleDays;

        try {
            cycleDays = ADJUSTED_CYCLES[cycleNumber];
        } catch (ArrayIndexOutOfBoundsException e) {
            cycleDays = null;
        }

        if (cycleDays == null) {
            cycleDays = new Long(cycleNumber * 10631);
        }

        return (cycleDays.longValue() + dayInCycle + HIJRAH_JAN_1_1_GREGORIAN_DAY - 1);
    }

    /**
     * Returns the 30 year cycle number from the epoch day.
     *
     * @param epochDay  an epoch day
     * @return a cycle number
     */
    private static int getCycleNumber(long epochDay) {
        Long[] days = ADJUSTED_CYCLES;
        int cycleNumber;
        try {
            for (int i = 0; i < days.length; i++) {
                if (epochDay < days[i].longValue()) {
                    return i - 1;
                }
            }
            cycleNumber = (int) epochDay / 10631;
        } catch (ArrayIndexOutOfBoundsException e) {
            cycleNumber = (int) epochDay / 10631;
        }
        return cycleNumber;
    }

    /**
     * Returns day of cycle from the epoch dan and cycle number.
     *
     * @param epochDay  an epoch day
     * @param cycleNumber  a cycle number
     * @return a day of cycle
     */
    private static int getDayOfCycle(long epochDay, int cycleNumber) {
        Long day;

        try {
            day = ADJUSTED_CYCLES[cycleNumber];
        } catch (ArrayIndexOutOfBoundsException e) {
            day = null;
        }
        if (day == null) {
            day = new Long(cycleNumber * 10631);
        }
        return (int) (epochDay - day.longValue());
    }

    /**
     * Returns the year in cycle from the cycle number and day of cycle.
     *
     * @param cycleNumber  a cycle number
     * @param dayOfCycle  day of cycle
     * @return a year in cycle
     */
    private static int getYearInCycle(int cycleNumber, long dayOfCycle) {
        Integer[] cycles = getAdjustedCycle(cycleNumber);
        if (dayOfCycle == 0) {
            return 0;
        }

        if (dayOfCycle > 0) {
            for (int i = 0; i < cycles.length; i++) {
                if (dayOfCycle < cycles[i].intValue()) {
                    return i - 1;
                }
            }
            return 29;
        } else {
            dayOfCycle = -dayOfCycle;
            for (int i = 0; i < cycles.length; i++) {
                if (dayOfCycle <= cycles[i].intValue()) {
                    return i - 1;
                }
            }
            return 29;
        }
    }

    /**
     * Returns adjusted 30 year cycle startind day as Integer array from the
     * cycle number specified.
     *
     * @param cycleNumber  a cycle number
     * @return an Integer array
     */
    private static Integer[] getAdjustedCycle(int cycleNumber) {
        Integer[] cycles;
        try {
            cycles = ADJUSTED_CYCLE_YEARS.get(new Integer(cycleNumber));
        } catch (ArrayIndexOutOfBoundsException e) {
            cycles = null;
        }
        if (cycles == null) {
            cycles = DEFAULT_CYCLE_YEARS;
        }
        return cycles;
    }

    /**
     * Returns adjusted month days as Integer array form the year specified.
     *
     * @param year  a year
     * @return an Integer array
     */
    private static Integer[] getAdjustedMonthDays(int year) {
        Integer[] newMonths;
        try {
            newMonths = ADJUSTED_MONTH_DAYS.get(new Integer(year));
        } catch (ArrayIndexOutOfBoundsException e) {
            newMonths = null;
        }
        if (newMonths == null) {
            if (isLeapYear(year)) {
                newMonths = DEFAULT_LEAP_MONTH_DAYS;
            } else {
                newMonths = DEFAULT_MONTH_DAYS;
            }
        }
        return newMonths;
    }

    /**
     * Returns adjusted month length as Integer array form the year specified.
     *
     * @param year  a year
     * @return an Integer array
     */
    private static Integer[] getAdjustedMonthLength(int year) {
        Integer[] newMonths;
        try {
            newMonths = ADJUSTED_MONTH_LENGTHS.get(new Integer(year));
        } catch (ArrayIndexOutOfBoundsException e) {
            newMonths = null;
        }
        if (newMonths == null) {
            if (isLeapYear(year)) {
                newMonths = DEFAULT_LEAP_MONTH_LENGTHS;
            } else {
                newMonths = DEFAULT_MONTH_LENGTHS;
            }
        }
        return newMonths;
    }

    /**
     * Returns day-of-year.
     *
     * @param cycleNumber  a cycle number
     * @param dayOfCycle  day of cycle
     * @param yearInCycle  year in cycle
     * @return day-of-year
     */
    private static int getDayOfYear(int cycleNumber, int dayOfCycle, int yearInCycle) {
        Integer[] cycles = getAdjustedCycle(cycleNumber);

        if (dayOfCycle > 0) {
            return dayOfCycle - cycles[yearInCycle].intValue();
        } else {
            return cycles[yearInCycle].intValue() + dayOfCycle;
        }
    }

    /**
     * Returns month-of-year. 0-based.
     *
     * @param dayOfYear  day-of-year
     * @param year  a year
     * @return month-of-year
     */
    private static int getMonthOfYear(int dayOfYear, int year) {

        Integer[] newMonths = getAdjustedMonthDays(year);

        if (dayOfYear >= 0) {
            for (int i = 0; i < newMonths.length; i++) {
                if (dayOfYear < newMonths[i].intValue()) {
                    return i - 1;
                }
            }
            return 11;
        } else {
            dayOfYear = (isLeapYear(year) ? (dayOfYear + 355)
                    : (dayOfYear + 354));
            for (int i = 0; i < newMonths.length; i++) {
                if (dayOfYear < newMonths[i].intValue()) {
                    return i - 1;
                }
            }
            return 11;
        }
    }

    /**
     * Returns day-of-month.
     *
     * @param dayOfYear  day of  year
     * @param month  month
     * @param year  year
     * @return day-of-month
     */
    private static int getDayOfMonth(int dayOfYear, int month, int year) {

        Integer[] newMonths = getAdjustedMonthDays(year);

        if (dayOfYear >= 0) {
            if (month > 0) {
                return dayOfYear - newMonths[month].intValue();
            } else {
                return dayOfYear;
            }
        } else {
            dayOfYear = (isLeapYear(year) ? (dayOfYear + 355)
                    : (dayOfYear + 354));
            if (month > 0) {
                return dayOfYear - newMonths[month].intValue();
            } else {
                return dayOfYear;
            }
        }
    }

    /**
     * Determines if the given year is a leap year.
     *
     * @param year  year
     * @return true if leap year
     */
    private static boolean isLeapYear(int year) {
        return (14 + 11 * (year > 0 ? year : -year)) % 30 < 11;
    }

    /**
     * Returns month days from the beginning of year.
     *
     * @param month  month (0-based)
     * @parma year  year
     * @return month days from the beginning of year
     */
    private static int getMonthDays(int month, int year) {
        Integer[] newMonths = getAdjustedMonthDays(year);
        return newMonths[month].intValue();
    }
    
    /**
     * Returns month length.
     *
     * @param month  month (0-based)
     * @param year  year
     * @return month length
     */
    static int getMonthLength(int month, int year) {
      Integer[] newMonths = getAdjustedMonthLength(year);
      return newMonths[month].intValue();
    }
    
    /**
     * Returns year length.
     *
     * @param year  year
     * @return year length
     */
    static int getYearLength(int year) {

        int cycleNumber = (year - 1) / 30;
        Integer[] cycleYears;
        try {
            cycleYears = ADJUSTED_CYCLE_YEARS.get(cycleNumber);
        } catch (ArrayIndexOutOfBoundsException e) {
            cycleYears = null;
        }
        if (cycleYears != null) {
            int yearInCycle = (year - 1) % 30;
            if (yearInCycle == 29) {
                return ADJUSTED_CYCLES[cycleNumber + 1].intValue()
                        - ADJUSTED_CYCLES[cycleNumber].intValue()
                        - cycleYears[yearInCycle].intValue();
            }
            return cycleYears[yearInCycle + 1].intValue()
                    - cycleYears[yearInCycle].intValue();
        } else {
            return isLeapYear(year) ? 355 : 354;
        }
    }

    /**
     * Returns maximum day-of-month.
     *
     * @return maximum day-of-month
     */
    static int getMaximumDayOfMonth() {
        return ADJUSTED_MAX_VALUES[POSITION_DAY_OF_MONTH];
    }

    /**
     * Returns smallest maximum day-of-month.
     *
     * @return smallest maximum day-of-month
     */
    static int getSmallestMaximumDayOfMonth() {
        return ADJUSTED_LEAST_MAX_VALUES[POSITION_DAY_OF_MONTH];
    }

    /**
     * Returns maximum day-of-year.
     *
     * @return maximum day-of-year
     */
    static int getMaximumDayOfYear() {
        return ADJUSTED_MAX_VALUES[POSITION_DAY_OF_YEAR];
    }

    /**
     * Returns smallest maximum day-of-year.
     *
     * @return smallest maximum day-of-year
     */
    static int getSmallestMaximumDayOfYear() {
        return ADJUSTED_LEAST_MAX_VALUES[POSITION_DAY_OF_YEAR];
    }

    // ----- Deviation handling -----//

    /**
     * Adds deviation definition. The year and month sepcifed should be the
     * caluculated Hijrah year and month. The month is 0 based. e.g. 8 for
     * Ramadan (9th month) Addition of anything minus deviation days is
     * calculated negatively in the case the user wants to subtract days from
     * the calendar. For example, adding -1 days will subtract one day from the
     * current date. Please note that this behavior is different from the
     * addDeviaiton method.
     *
     * @param startYear  start year
     * @param startMonth  start month
     * @param endYear  end year
     * @param endMonth  end month
     * @param offset  offset
     */
    private static void addDeviationAsHijrah(int startYear,
            int startMonth, int endYear, int endMonth, int offset) {

        if (startYear < 1) {
            throw new IllegalArgumentException("startYear < 1");
        }
        if (endYear < 1) {
            throw new IllegalArgumentException("endYear < 1");
        }
        if (startMonth < 0 || startMonth > 11) {
            throw new IllegalArgumentException(
                    "startMonth < 0 || startMonth > 11");
        }
        if (endMonth < 0 || endMonth > 11) {
            throw new IllegalArgumentException("endMonth < 0 || endMonth > 11");
        }
        if (endYear > 9999) {
            throw new IllegalArgumentException("endYear > 9999");
        }
        if (endYear < startYear) {
            throw new IllegalArgumentException("startYear > endYear");
        }
        if (endYear == startYear && endMonth < startMonth) {
            throw new IllegalArgumentException(
                    "startYear == endYear && endMonth < startMonth");
        }

        // Adjusting start year.
        boolean isStartYLeap = isLeapYear(startYear);

        // Adjusting the number of month.
        Integer[] orgStartMonthNums = ADJUSTED_MONTH_DAYS.get(new Integer(
                startYear));
        if (orgStartMonthNums == null) {
            if (isStartYLeap) {
                orgStartMonthNums = new Integer[LEAP_NUM_DAYS.length];
                for (int l = 0; l < LEAP_NUM_DAYS.length; l++) {
                    orgStartMonthNums[l] = new Integer(LEAP_NUM_DAYS[l]);
                }
            } else {
                orgStartMonthNums = new Integer[NUM_DAYS.length];
                for (int l = 0; l < NUM_DAYS.length; l++) {
                    orgStartMonthNums[l] = new Integer(NUM_DAYS[l]);
                }
            }
        }

        Integer[] newStartMonthNums = new Integer[orgStartMonthNums.length];

        for (int month = 0; month < 12; month++) {
            if (month > startMonth) {
                newStartMonthNums[month] = new Integer(orgStartMonthNums[month]
                        .intValue()
                        - offset);
            } else {
                newStartMonthNums[month] = new Integer(orgStartMonthNums[month]
                        .intValue());
            }
        }

        ADJUSTED_MONTH_DAYS.put(new Integer(startYear), newStartMonthNums);

        // Adjusting the days of month.

        Integer[] orgStartMonthLengths = ADJUSTED_MONTH_LENGTHS.get(new Integer(
                startYear));
        if (orgStartMonthLengths == null) {
            if (isStartYLeap) {
                orgStartMonthLengths = new Integer[LEAP_MONTH_LENGTH.length];
                for (int l = 0; l < LEAP_MONTH_LENGTH.length; l++) {
                    orgStartMonthLengths[l] = new Integer(LEAP_MONTH_LENGTH[l]);
                }
            } else {
                orgStartMonthLengths = new Integer[MONTH_LENGTH.length];
                for (int l = 0; l < MONTH_LENGTH.length; l++) {
                    orgStartMonthLengths[l] = new Integer(MONTH_LENGTH[l]);
                }
            }
        }

        Integer[] newStartMonthLengths = new Integer[orgStartMonthLengths.length];

        for (int month = 0; month < 12; month++) {
            if (month == startMonth) {
                newStartMonthLengths[month] = new Integer(
                        orgStartMonthLengths[month].intValue() - offset);
            } else {
                newStartMonthLengths[month] = new Integer(
                        orgStartMonthLengths[month].intValue());
            }
        }

        ADJUSTED_MONTH_LENGTHS.put(new Integer(startYear), newStartMonthLengths);

        if (startYear != endYear) {
            // System.out.println("over year");
            // Adjusting starting 30 year cycle.
            int sCycleNumber = (startYear - 1) / 30;
            int sYearInCycle = (startYear - 1) % 30; // 0-based.
            Integer[] startCycles = ADJUSTED_CYCLE_YEARS.get(new Integer(
                    sCycleNumber));
            if (startCycles == null) {
                startCycles = new Integer[CYCLEYEAR_START_DATE.length];
                for (int j = 0; j < startCycles.length; j++) {
                    startCycles[j] = new Integer(CYCLEYEAR_START_DATE[j]);
                }
            }

            for (int j = sYearInCycle + 1; j < CYCLEYEAR_START_DATE.length; j++) {
                startCycles[j] = new Integer(startCycles[j].intValue() - offset);
            }

            // System.out.println(sCycleNumber + ":" + sYearInCycle);
            ADJUSTED_CYCLE_YEARS.put(new Integer(sCycleNumber), startCycles);

            int sYearInMaxY = (startYear - 1) / 30;
            int sEndInMaxY = (endYear - 1) / 30;

            if (sYearInMaxY != sEndInMaxY) {
                // System.out.println("over 30");
                // Adjusting starting 30 * MAX_ADJUSTED_CYCLE year cycle.
                // System.out.println(sYearInMaxY);

                for (int j = sYearInMaxY + 1; j < ADJUSTED_CYCLES.length; j++) {
                    ADJUSTED_CYCLES[j] = new Long(ADJUSTED_CYCLES[j].longValue()
                            - offset);
                }

                // Adjusting ending 30 * MAX_ADJUSTED_CYCLE year cycles.
                for (int j = sEndInMaxY + 1; j < ADJUSTED_CYCLES.length; j++) {
                    ADJUSTED_CYCLES[j] = new Long(ADJUSTED_CYCLES[j].longValue()
                            + offset);
                }
            }

            // Adjusting ending 30 year cycle.
            int eCycleNumber = (endYear - 1) / 30;
            int sEndInCycle = (endYear - 1) % 30; // 0-based.
            Integer[] endCycles = ADJUSTED_CYCLE_YEARS.get(new Integer(
                    eCycleNumber));
            if (endCycles == null) {
                endCycles = new Integer[CYCLEYEAR_START_DATE.length];
                for (int j = 0; j < endCycles.length; j++) {
                    endCycles[j] = new Integer(CYCLEYEAR_START_DATE[j]);
                }
            }
            for (int j = sEndInCycle + 1; j < CYCLEYEAR_START_DATE.length; j++) {
                endCycles[j] = new Integer(endCycles[j].intValue() + offset);
            }
            ADJUSTED_CYCLE_YEARS.put(new Integer(eCycleNumber), endCycles);
        }

        // Adjusting ending year.
        boolean isEndYLeap = isLeapYear(endYear);

        Integer[] orgEndMonthDays = ADJUSTED_MONTH_DAYS.get(new Integer(endYear));

        if (orgEndMonthDays == null) {
            if (isEndYLeap) {
                orgEndMonthDays = new Integer[LEAP_NUM_DAYS.length];
                for (int l = 0; l < LEAP_NUM_DAYS.length; l++) {
                    orgEndMonthDays[l] = new Integer(LEAP_NUM_DAYS[l]);
                }
            } else {
                orgEndMonthDays = new Integer[NUM_DAYS.length];
                for (int l = 0; l < NUM_DAYS.length; l++) {
                    orgEndMonthDays[l] = new Integer(NUM_DAYS[l]);
                }
            }
        }

        Integer[] newEndMonthDays = new Integer[orgEndMonthDays.length];

        for (int month = 0; month < 12; month++) {
            if (month > endMonth) {
                newEndMonthDays[month] = new Integer(orgEndMonthDays[month]
                        .intValue()
                        + offset);
            } else {
                newEndMonthDays[month] = new Integer(orgEndMonthDays[month]
                        .intValue());
            }
        }

        ADJUSTED_MONTH_DAYS.put(new Integer(endYear), newEndMonthDays);

        // Adjusting the days of month.
        Integer[] orgEndMonthLengths = ADJUSTED_MONTH_LENGTHS.get(new Integer(
                endYear));

        if (orgEndMonthLengths == null) {
            if (isEndYLeap) {
                orgEndMonthLengths = new Integer[LEAP_MONTH_LENGTH.length];
                for (int l = 0; l < LEAP_MONTH_LENGTH.length; l++) {
                    orgEndMonthLengths[l] = new Integer(LEAP_MONTH_LENGTH[l]);
                }
            } else {
                orgEndMonthLengths = new Integer[MONTH_LENGTH.length];
                for (int l = 0; l < MONTH_LENGTH.length; l++) {
                    orgEndMonthLengths[l] = new Integer(MONTH_LENGTH[l]);
                }
            }
        }

        Integer[] newEndMonthLengths = new Integer[orgEndMonthLengths.length];

        for (int month = 0; month < 12; month++) {
            if (month == endMonth) {
                newEndMonthLengths[month] = new Integer(
                        orgEndMonthLengths[month].intValue() + offset);
            } else {
                newEndMonthLengths[month] = new Integer(
                        orgEndMonthLengths[month].intValue());
            }
        }

        ADJUSTED_MONTH_LENGTHS.put(new Integer(endYear), newEndMonthLengths);

        Integer[] startMonthLengths = ADJUSTED_MONTH_LENGTHS.get(new Integer(
                startYear));
        Integer[] endMonthLengths = ADJUSTED_MONTH_LENGTHS.get(new Integer(
                endYear));
        Integer[] startMonthDays = ADJUSTED_MONTH_DAYS
                .get(new Integer(startYear));
        Integer[] endMonthDays = ADJUSTED_MONTH_DAYS.get(new Integer(endYear));

        int startMonthLength = startMonthLengths[startMonth].intValue();
        int endMonthLength = endMonthLengths[endMonth].intValue();
        int startMonthDay = startMonthDays[11].intValue()
                + startMonthLengths[11].intValue();
        int endMonthDay = endMonthDays[11].intValue()
                + endMonthLengths[11].intValue();

        int maxMonthLength = ADJUSTED_MAX_VALUES[POSITION_DAY_OF_MONTH]
                .intValue();
        int leastMaxMonthLength = ADJUSTED_LEAST_MAX_VALUES[POSITION_DAY_OF_MONTH]
                .intValue();

        if (maxMonthLength < startMonthLength) {
            maxMonthLength = startMonthLength;
        }
        if (maxMonthLength < endMonthLength) {
            maxMonthLength = endMonthLength;
        }
        ADJUSTED_MAX_VALUES[POSITION_DAY_OF_MONTH] = new Integer(maxMonthLength);

        if (leastMaxMonthLength > startMonthLength) {
            leastMaxMonthLength = startMonthLength;
        }
        if (leastMaxMonthLength > endMonthLength) {
            leastMaxMonthLength = endMonthLength;
        }
        ADJUSTED_LEAST_MAX_VALUES[POSITION_DAY_OF_MONTH] = new Integer(
                leastMaxMonthLength);

        int maxMonthDay = ADJUSTED_MAX_VALUES[POSITION_DAY_OF_YEAR].intValue();
        int leastMaxMonthDay = ADJUSTED_LEAST_MAX_VALUES[POSITION_DAY_OF_YEAR]
                .intValue();

        if (maxMonthDay < startMonthDay) {
            maxMonthDay = startMonthDay;
        }
        if (maxMonthDay < endMonthDay) {
            maxMonthDay = endMonthDay;
        }

        ADJUSTED_MAX_VALUES[POSITION_DAY_OF_YEAR] = new Integer(maxMonthDay);

        if (leastMaxMonthDay > startMonthDay) {
            leastMaxMonthDay = startMonthDay;
        }
        if (leastMaxMonthDay > endMonthDay) {
            leastMaxMonthDay = endMonthDay;
        }
        ADJUSTED_LEAST_MAX_VALUES[POSITION_DAY_OF_YEAR] = new Integer(
                leastMaxMonthDay);
    }

    /**
     * Read hijrah_deviation.cfg file. The config file contains the deviation data with
     * following format.
     *
     * StartYear/StartMonth(0-based)-EndYear/EndMonth(0-based):Deviation day (1,
     * 2, -1, or -2)
     *
     * Line separator or ";" is used for the separator of each deviation data.
     *
     * Here is the example.
     *
     * 1429/0-1429/1:1
     * 1429/2-1429/7:1;1429/6-1429/11:1
     * 1429/11-9999/11:1
     *
     * @throws IOException for zip/jar file handling exception.
     * @throws ParseException if the format of the configuration file is wrong.
     */
    private static void readDeviationConfig() throws IOException,
            ParseException {
        
        InputStream is = getConfigFileInputStream();

        if (is != null) {
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                String line = "";
                int num = 0;
                while ((line = br.readLine()) != null) {
                    num++;
                    line = line.trim();
                    parseLine(line, num);
                }
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
     * Parse each deviation element.
     *
     * @param line  a line to parse
     * @param num  line number
     * @throws ParseException if line has incorrect format.
     */
    private static void parseLine(String line, int num) throws ParseException {
        StringTokenizer st = new StringTokenizer(line, ";");
        while (st.hasMoreTokens()) {
            String deviationElement = st.nextToken();
            int offsetIndex = deviationElement.indexOf(':');
            if (offsetIndex != -1) {
                String offsetString = deviationElement.substring(
                        offsetIndex + 1, deviationElement.length());
                int offset;
                try {
                    offset = Integer.parseInt(offsetString);
                } catch (NumberFormatException ex) {
                    throw new ParseException(
                            "Offset is not properly set at line " + num + ".",
                            num);
                }
                int separatorIndex = deviationElement.indexOf('-');
                if (separatorIndex != -1) {
                    String startDateStg = deviationElement.substring(0,
                            separatorIndex);
                    String endDateStg = deviationElement.substring(
                            separatorIndex + 1, offsetIndex);
                    int startDateYearSepIndex = startDateStg.indexOf('/');
                    int endDateYearSepIndex = endDateStg.indexOf('/');
                    int startYear = -1;
                    int endYear = -1;
                    int startMonth = -1;
                    int endMonth = -1;
                    if (startDateYearSepIndex != -1) {
                        String startYearStg = startDateStg.substring(0,
                                startDateYearSepIndex);
                        String startMonthStg = startDateStg.substring(
                                startDateYearSepIndex + 1, startDateStg
                                        .length());
                        try {
                            startYear = Integer.parseInt(startYearStg);
                        } catch (NumberFormatException ex) {
                            throw new ParseException(
                                    "Start year is not properly set at line "
                                            + num + ".", num);
                        }
                        try {
                            startMonth = Integer.parseInt(startMonthStg);
                        } catch (NumberFormatException ex) {
                            throw new ParseException(
                                    "Start month is not properly set at line "
                                            + num + ".", num);
                        }
                    } else {
                        throw new ParseException(
                                "Start year/month has incorrect format at line "
                                        + num + ".", num);
                    }
                    if (endDateYearSepIndex != -1) {
                        String endYearStg = endDateStg.substring(0,
                                endDateYearSepIndex);
                        String endMonthStg = endDateStg.substring(
                                endDateYearSepIndex + 1, endDateStg.length());
                        try {
                            endYear = Integer.parseInt(endYearStg);
                        } catch (NumberFormatException ex) {
                            throw new ParseException(
                                    "End year is not properly set at line "
                                            + num + ".", num);
                        }
                        try {
                            endMonth = Integer.parseInt(endMonthStg);
                        } catch (NumberFormatException ex) {
                            throw new ParseException(
                                    "End month is not properly set at line "
                                            + num + ".", num);
                        }
                    } else {
                        throw new ParseException(
                                "End year/month has incorrect format at line "
                                        + num + ".", num);
                    }
                    if (startYear != -1 && startMonth != -1 && endYear != -1
                            && endMonth != -1) {
                        addDeviationAsHijrah(startYear, startMonth, endYear,
                                endMonth, offset);
                    } else {
                        throw new ParseException("Unknown error at line " + num
                                + ".", num);
                    }
                } else {
                    throw new ParseException(
                            "Start and end year/month has incorrect format at line "
                                    + num + ".", num);
                }
            } else {
                throw new ParseException("Offset has incorrect format at line "
                        + num + ".", num);
            }
        }
    }

    /**
     * Return InputStream for deviaiton configuration file.
     * The default location of the deviation file is:
     * <pre>
     *   $CLASSPATH/javax/time/i18n
     * </pre>
     * And the default file name is:
     * <pre>
     *   hijrah_deviation.cfg
     * </pre>
     * The default location and file name can be overriden by setting
     * following two Java's system property.
     * <pre>
     *   Location: javax.time.i18n.HijrahDate.deviationConfigDir
     *   File name: javax.time.i18n.HijrahDate.deviationConfigFile
     * </pre>
     * Regarding the file format, see readDeviationConfig() method for details.
     *
     * @return InputStream for file reading exception.
     * @throws IOException for zip/jar file handling exception.
     */
    private static InputStream getConfigFileInputStream() throws IOException {

        String fileName = System
                .getProperty("javax.time.i18n.HijrahDate.deviationConfigFile");

        if (fileName == null) {
            fileName = DEFAULT_CONFIG_FILENAME;
        }

        String dir = System
                .getProperty("javax.time.i18n.HijrahDate.deviationConfigDir");

        if (dir != null) {
            if (!(dir.length() == 0 && dir.endsWith(System
                    .getProperty("file.separator")))) {
                dir = dir + System.getProperty("file.separator");
            }
            File file = new File(dir + FILE_SEP + fileName);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (IOException ioe) {
                    throw ioe;
                }
            } else {
                return null;
            }
        } else {
            String classPath = System.getProperty("java.class.path");
            StringTokenizer st = new StringTokenizer(classPath, PATH_SEP);
            while (st.hasMoreTokens()) {
                String path = st.nextToken();
                File file = new File(path);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        File f = new File(
                                path + FILE_SEP + DEFAULT_CONFIG_PATH, fileName);
                        if (f.exists()) {
                            try {
                                return new FileInputStream(path + FILE_SEP
                                        + DEFAULT_CONFIG_PATH + FILE_SEP
                                        + fileName);
                            } catch (IOException ioe) {
                                throw ioe;
                            }
                        }
                    } else {
                        ZipFile zip;
                        try {
                            zip = new ZipFile(file);
                        } catch (IOException ioe) {
                            zip = null;
                        }

                        if (zip != null) {
                            String targetFile = DEFAULT_CONFIG_PATH + FILE_SEP
                                    + fileName;
                            ZipEntry entry = zip.getEntry(targetFile);

                            if (entry == null) {
                                if (FILE_SEP == '/') {
                                    targetFile = targetFile.replace('/', '\\');
                                } else if (FILE_SEP == '\\') {
                                    targetFile = targetFile.replace('\\', '/');
                                }
                                entry = zip.getEntry(targetFile);
                            }

                            if (entry != null) {
                                try {
                                    return zip.getInputStream(entry);
                                } catch (IOException ioe) {
                                    throw ioe;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule implementation.
     */
    static final class Rule extends CalendricalRule<HijrahDate> implements Serializable {
        private static final CalendricalRule<HijrahDate> INSTANCE = new Rule();
        private static final long serialVersionUID = 1L;
        private Rule() {
            super(HijrahDate.class, "HijrahDate");
        }
        private Object readResolve() {
            return INSTANCE;
        }
        @Override
        protected HijrahDate deriveFrom(CalendricalEngine engine) {
            LocalDate ld = engine.getDate(true);
            return ld != null ? HijrahDate.of(ld) : null;
        }
    }

}
