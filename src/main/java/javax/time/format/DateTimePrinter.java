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
package javax.time.format;

import javax.time.CalendricalException;

/**
 * Strategy for printing a calendrical to an appendable.
 * <p>
 * The printer may print any part, or the whole, of the input Calendrical.
 * Typically, a complete print is constructed from a number of smaller
 * units, each outputting a single field.
 * <p>
 * This interface must be implemented with care to ensure other classes in
 * the framework operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * <p>
 * The context is not a thread-safe object and a new instance will be created
 * for each print that occurs. The context must not be stored in an instance
 * variable or shared with any other threads.
 *
 * @author Stephen Colebourne
 */
public interface DateTimePrinter {

    /**
     * Prints the calendrical object to the buffer.
     * <p>
     * The context holds information to use during the print.
     * It also contains the calendrical information to be printed.
     * <p>
     * The buffer must not be mutated beyond the content controlled by the implementation.
     *
     * @param context  the context to print using, not null
     * @param buf  the buffer to append to, not null
     * @return false if unable to query the value from the calendrical, true otherwise
     * @throws CalendricalException if the calendrical cannot be printed successfully
     */
    boolean print(DateTimePrintContext context, StringBuilder buf);

}
