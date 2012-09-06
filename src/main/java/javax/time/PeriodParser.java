/*
 * Copyright (c) 2009-2011, Stephen Colebourne & Michael Nascimento Santos
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

import javax.time.format.CalendricalParseException;

/**
 * A period parser that creates an instance of {@code Period} from a string.
 * This parses the ISO-8601 period format {@code PnYnMnDTnHnMn.nS}.
 * <p>
 * This class is mutable and intended for use by a single thread.
 *
 * @author Darryl West
 * @author Stephen Colebourne
 */
final class PeriodParser {

    /**
     * Used to validate the correct sequence of tokens.
     */
    private static final String TOKEN_SEQUENCE = "PYMDTHMS";
    /**
     * The standard string representing a zero period.
     */
    private static final String ZERO = "PT0S";

    /**
     * The number of years.
     */
    private int years;
    /**
     * The number of months.
     */
    private int months;
    /**
     * The number of days.
     */
    private int days;
    /**
     * The number of hours.
     */
    private int hours;
    /**
     * The number of minutes.
     */
    private int minutes;
    /**
     * The number of seconds.
     */
    private int seconds;
    /**
     * The number of nanoseconds.
     */
    private long nanos;
    /**
     * Whether the seconds were negative.
     */
    private boolean negativeSecs;
    /**
     * Parser position index.
     */
    private int index;
    /**
     * Original text.
     */
    private CharSequence text;

    /**
     * Constructor.
     * 
     * @param text  the text to parse, not null
     */
    PeriodParser(CharSequence text) {
        this.text = text;
    }

    //-----------------------------------------------------------------------
    /**
     * Performs the parse.
     * <p>
     * This parses the text set in the constructor in the format PnYnMnDTnHnMn.nS.
     *
     * @return the created Period, not null
     * @throws CalendricalParseException if the text cannot be parsed to a Period
     */
    LocalPeriod parse() {
        // force to upper case and coerce the comma to dot
        
        String s = text.toString().toUpperCase().replace(',', '.');
        // check for zero and skip parse
        if (ZERO.equals(s)) {
            return LocalPeriod.ZERO;
        }
        if (s.length() < 3 || s.charAt(0) != 'P') {
            throw new CalendricalParseException("Period could not be parsed: " + text, text, 0);
        }
        validateCharactersAndOrdering(s, text);
        
        // strip off the leading P
        String[] datetime = s.substring(1).split("T");
        switch (datetime.length) {
            case 2:
                parseDate(datetime[0], 1);
                parseTime(datetime[1], datetime[0].length() + 2);
                break;
            case 1:
                parseDate(datetime[0], 1);
                break;
        }
        return toPeriod();
    }

    private void parseDate(String s, int baseIndex) {
        index = 0;
        while (index < s.length()) {
            String value = parseNumber(s);
            if (index < s.length()) {
                char c = s.charAt(index);
                switch(c) {
                    case 'Y': years = parseInt(value, baseIndex) ; break;
                    case 'M': months = parseInt(value, baseIndex) ; break;
                    case 'D': days = parseInt(value, baseIndex) ; break;
                    default:
                        throw new CalendricalParseException("Period could not be parsed, unrecognized letter '" +
                                c + ": " + text, text, baseIndex + index);
                }
                index++;
            }
        }
    }

    private void parseTime(String s, int baseIndex) {
        index = 0;
        s = prepareTime(s, baseIndex);
        while (index < s.length()) {
            String value = parseNumber(s);
            if (index < s.length()) {
                char c = s.charAt(index);
                switch(c) {
                    case 'H': hours = parseInt(value, baseIndex) ; break;
                    case 'M': minutes = parseInt(value, baseIndex) ; break;
                    case 'S': seconds = parseInt(value, baseIndex) ; break;
                    case 'N': nanos = parseNanos(value, baseIndex); break;
                    default:
                        throw new CalendricalParseException("Period could not be parsed, unrecognized letter '" +
                                c + "': " + text, text, baseIndex + index);
                }
                index++;
            }
        }
    }

    private long parseNanos(String s, int baseIndex) {
        if (s.length() > 9) {
            throw new CalendricalParseException("Period could not be parsed, nanosecond range exceeded: " +
                    text, text, baseIndex + index - s.length());
        }
        // pad to the right to create 10**9, then trim
        return Long.parseLong((s + "000000000").substring(0, 9));
    }

    private String prepareTime(String s, int baseIndex) {
        if (s.contains(".")) {
            int i = s.indexOf(".") + 1;
            
            // verify that the first character after the dot is a digit
            if (Character.isDigit(s.charAt(i))) {
                i++;
            } else {
                throw new CalendricalParseException("Period could not be parsed, invalid decimal number: " +
                        text, text, baseIndex + index);
            }
            
            // verify that only digits follow the decimal point followed by an S
            while (i < s.length()) {
                // || !Character.isDigit(s.charAt(i))
                char c = s.charAt(i);
                if (Character.isDigit(c) || c == 'S') {
                    i++;
                } else {
                    throw new CalendricalParseException("Period could not be parsed, invalid decimal number: " +
                            text, text, baseIndex + index);
                }
            }
            s = s.replace('S', 'N').replace('.', 'S');
            if (s.contains("-0S")) {
                negativeSecs = true;
                s = s.replace("-0S", "0S");
            }
        }
        return s;
    }

    private int parseInt(String s, int baseIndex) {
        try {
            int value = Integer.parseInt(s);
            if (s.charAt(0) == '-' && value == 0) {
                throw new CalendricalParseException("Period could not be parsed, invalid number '" +
                        s + "': " + text, text, baseIndex + index - s.length());
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new CalendricalParseException("Period could not be parsed, invalid number '" +
                    s + "': " + text, text, baseIndex + index - s.length());
        }
    }

    private String parseNumber(String s) {
        int start = index;
        while (index < s.length()) {
            char c = s.charAt(index);
            if ((c < '0' || c > '9') && c != '-') {
                break;
            }
            index++;
        }
        return s.substring(start, index);
    }

    private void validateCharactersAndOrdering(String s, CharSequence text) {
        char[] chars = s.toCharArray();
        int tokenPos = 0;
        boolean lastLetter = false;
        for (int i = 0; i < chars.length; i++) {
            if (tokenPos >= TOKEN_SEQUENCE.length()) {
                throw new CalendricalParseException("Period could not be parsed, characters after last 'S': " + text, text, i);
            }
            char c = chars[i];
            if ((c < '0' || c > '9') && c != '-' && c != '.') {
                tokenPos = TOKEN_SEQUENCE.indexOf(c, tokenPos);
                if (tokenPos < 0) {
                    throw new CalendricalParseException("Period could not be parsed, invalid character '" + c + "': " + text, text, i);
                }
                tokenPos++;
                lastLetter = true;
            } else {
                lastLetter = false;
            }
        }
        if (lastLetter == false) {
            throw new CalendricalParseException("Period could not be parsed, invalid last character: " + text, text, s.length() - 1);
        }
    }

    private LocalPeriod toPeriod() {
        return LocalPeriod.of(years, months, days, hours, minutes, seconds, negativeSecs || seconds < 0 ? -nanos : nanos);
    }

}
