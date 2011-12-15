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

import javax.time.CalendricalException;

/**
 * An exception used when an exception is connected to a specified rule.
 * <p>
 * Many aspects of calendrical processing are rule based.
 * When a rule is the trigger for an exception, the rule field should be populated.
 *
 * @author Stephen Colebourne
 */
public class CalendricalRuleException extends CalendricalException {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The rule that caused the exception.
     */
    private final CalendricalRule<?> rule;

    /**
     * Constructs a new exception with a message and optional rule.
     *
     * @param message  the message describing the problem, should not be null
     * @param rule  the rule that caused the exception, null if not caused by a specific rule
     */
    public CalendricalRuleException(String message, CalendricalRule<?> rule) {
        super(message);
        this.rule = rule;
    }

    /**
     * Constructs a new exception with a message and optional rule.
     *
     * @param message  the message describing the problem, should not be null
     * @param rule  the rule that caused the exception, null if not caused by a specific rule
     * @param cause  the cause of the exception, may be null
     */
    public CalendricalRuleException(String message, CalendricalRule<?> rule, Throwable cause) {
        super(message, cause);
        this.rule = rule;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the rule that is connected to the exception.
     *
     * @return the rule, null if unknown
     */
    public CalendricalRule<?> getRule() {
        return rule;
    }

}
