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
package com.treetank.exception;

import java.util.concurrent.ExecutionException;

/**
 * This class holds all exceptions which can occure with the usage of
 * multithreaded exceptions.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TreetankThreadedException extends TreetankException {

    /** Generated ID. */
    private static final long serialVersionUID = -2891221683798924769L;

    /**
     * Constructor for threaded exceptions.
     * 
     * @param mExc
     *            tp be stored
     */
    public TreetankThreadedException(final InterruptedException mExc) {
        super(mExc);
    }

    /**
     * Exception for weird thread behaviour.
     * 
     * @param message
     *            to be stored
     */
    public TreetankThreadedException(final String... message) {
        super(message);
    }

    /**
     * Constructor for execution exception exceptions.
     * 
     * @param mExc
     *            to be stored
     */
    public TreetankThreadedException(final ExecutionException mExc) {
        super(mExc);
    }

}
