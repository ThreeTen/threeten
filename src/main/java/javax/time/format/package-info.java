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

/**
 * Provides classes to print and parse dates and times.
 * <p>
 * Printing and parsing is based around the {@link javax.time.calendar.format.DateTimeFormatter DateTimeFormatter} class.
 * Instances are generally obtained from {@link javax.time.calendar.format.DateTimeFormatters DateTimeFormatters},
 * however {@link javax.time.calendar.format.DateTimeFormatterBuilder DateTimeFormatterBuilder} can be used
 * if more power is needed.
 * <p>
 * Localization occurs by calling {@link javax.time.calendar.format.DateTimeFormatter#withLocale(java.util.Locale) withLocale(Locale)}
 * on the formatter. Further customization is possible using
 * {@link javax.time.calendar.format.DateTimeFormatSymbols DateTimeFormatSymbols}.
 * <p>
 * Access is also provided to the low-level {@link javax.time.calendar.format.DateTimePrinter DateTimePrinter}
 * and {@link javax.time.calendar.format.DateTimeParser DateTimeParser} interfaces.
 * If necessary, these can be implemented and plugged into a formatter using the builder.
 * The context classes are used by the low-level printer/parser interfaces.
 */
package javax.time.format;
