/*
 * Copyright (c) 2011, Stephen Colebourne & Michael Nascimento Santos
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

import java.util.Locale;

import javax.time.CalendricalException;
import javax.time.chronology.Calendrical;
import javax.time.chronology.CalendricalRule;

/**
 * Context object used during date and time printing.
 * <p>
 * This class provides a single wrapper to items used in the print.
 * <p>
 * This class is a mutable context intended for use from a single thread.
 * Usage of the class is thread-safe within standard printing as the framework creates
 * a new instance of the class for each print and printing is single-threaded.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
public final class DateTimePrintContext {

    /**
     * The calendrical being output.
     */
    private Calendrical calendrical;
    /**
     * The locale, not null.
     */
    private Locale locale;
    /**
     * The date time format symbols, not null.
     */
    private DateTimeFormatSymbols symbols;
    /**
     * Whether the current formatter is optional.
     */
    private int optional;

    /**
     * Creates a new instance of the context.
     * <p>
     * This should normally only be created by the printer.
     *
     * @param calendrical  the calendrical being output, not null
     * @param locale  the locale to use, not null
     * @param symbols  the symbols to use during parsing, not null
     */
    DateTimePrintContext(Calendrical calendrical, Locale locale, DateTimeFormatSymbols symbols) {
        super();
        setCalendrical(calendrical);
        setLocale(locale);
        setSymbols(symbols);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the calendrical being output.
     *
     * @return the calendrical, not null
     */
    public Calendrical getCalendrical() {
        return calendrical;
    }

    /**
     * Sets the calendrical being output.
     *
     * @param calendrical  the calendrical, not null
     */
    public void setCalendrical(Calendrical calendrical) {
        DateTimeFormatter.checkNotNull(calendrical, "Calendrical must not be null");
        this.calendrical = calendrical;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the locale.
     * <p>
     * This locale is used to control localization in the print output except
     * where localization is controlled by the symbols.
     *
     * @return the locale, not null
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     * <p>
     * This locale is used to control localization in the print output except
     * where localization is controlled by the symbols.
     *
     * @param symbols  the locale, not null
     */
    public void setLocale(Locale locale) {
        DateTimeFormatter.checkNotNull(locale, "Locale must not be null");
        this.locale = locale;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the formatting symbols.
     * <p>
     * The symbols control the localization of numeric output.
     *
     * @return the formatting symbols, not null
     */
    public DateTimeFormatSymbols getSymbols() {
        return symbols;
    }

    /**
     * Sets the formatting symbols.
     * <p>
     * The symbols control the localization of numeric output.
     *
     * @param symbols  the formatting symbols, not null
     */
    public void setSymbols(DateTimeFormatSymbols symbols) {
        DateTimeFormatter.checkNotNull(symbols, "DateTimeFormatSymbols must not be null");
        this.symbols = symbols;
    }

    //-----------------------------------------------------------------------
    /**
     * Starts the printing of an optional segment of the input.
     */
    void startOptional() {
        this.optional++;
    }

    /**
     * Ends the printing of an optional segment of the input.
     */
    void endOptional() {
        this.optional--;
    }

    /**
     * Gets the value of the specified rule.
     * <p>
     * This will return the value for the specified rule.
     *
     * @param rule  the rue to find, not null
     * @return the value, null if not found and optional is true
     * @throws CalendricalException if the rule is not available and optional is false
     */
    public <T> T getValue(CalendricalRule<T> rule) {
        if (optional > 0) {
            return rule.getValue(calendrical);
        } else {
            return rule.getValueChecked(calendrical);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string version of the context for debugging.
     *
     * @return a string representation of the context, not null
     */
    @Override
    public String toString() {
        return calendrical.toString();
    }

}
