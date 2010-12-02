/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.service.xml.xpath;

/**
 * <h1>XPathConstants</h1>
 * <p>
 * This interface contains constants that are used by the XPathScanner, the XPathToken and the XPathParser as
 * well.
 * </p>
 */
public interface XPathConstants {

    /** Token types. */
    enum Token {
        /** Invalid type. */
        INVALID,
        /** Text type. */
        TEXT,
        /** Name type. */
        NAME,
        /** Value type. */
        VALUE,
        /** Token type that represents a '/' . */
        SLASH,
        /** Token type that represents a descendant step. */
        DESC_STEP,
        /** Token type that represents a left parenthesis. */
        OPEN_BR,
        /** Token type that represents a right parenthesis. */
        CLOSE_BR,
        /** Token type that represents a comparison. */
        COMP,
        /** Token type that represents an equality comparison. */
        EQ,
        /** Token type that represents a diversity comparison. */
        N_EQ,
        /** Token type that represents an opening squared bracket. */
        OPEN_SQP,
        /** Token type that represents a closing squared bracket. */
        CLOSE_SQP,
        /** Token type that represents the @ symbol. */
        AT,
        /** Token type that represents the point. */
        POINT,
        /** Token type that represents a colon : . */
        COLON,
        /** Token type that represents a normal quote : " . */
        DBL_QUOTE,
        /** Token type that represents a single quote : ' . */
        SINGLE_QUOTE,
        /** Token type that represents the dollar sign : $ . */
        DOLLAR,
        /** Token type that represents a plus. */
        PLUS,
        /** Token type that represents a minus. */
        MINUS,
        /** Token type that represents a interrogation mark: ? */
        INTERROGATION,
        /** Token type that represents a star. */
        STAR,
        /** Token type that represents a left shift: << . */
        L_SHIFT,
        /** Token type that represents a right shift: >> . */
        R_SHIFT,
        /** Token type that represents a shortcut for the parent: .. . */
        PARENT,
        /** Token type that represents a comma. . */
        COMMA,
        /** Token type that represents the or sign: | . */
        OR,
        /** Token type that represents a comment (: ...... :) */
        COMMENT,
        /** Token type that represents a whitespace. */
        SPACE,
        /**
         * Token type that represents an 'E' or an 'e' that is part of a double
         * value.
         */
        E_NUMBER,
        /** Token type for the end of the string to parse. */
        END
    }

    /** Declares if the evaluation is compatible to XPath 1.0 or not. */
    public static boolean XPATH_10_COMP = true;

    /** Enum representing the wildcards '*', '+' and '?'. */
    public enum Wildcard {
        /** Represents the wildcard '*'. */
        ASTERISK,
        /** Represents the wildcard '+'. */
        PLUS,
        /** Represents the wildcard '?'. */
        INTEROGATION
    }

}