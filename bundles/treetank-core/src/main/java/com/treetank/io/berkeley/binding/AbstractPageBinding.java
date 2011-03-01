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

package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.AbsPage;
import com.treetank.page.PagePersistenter;

/**
 * Binding for storing {@link AbsPage} objects within the Berkeley DB.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class AbstractPageBinding extends TupleBinding<AbsPage> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AbsPage entryToObject(final TupleInput arg0) {
        return PagePersistenter.createPage(new TupleInputSource(arg0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectToEntry(final AbsPage arg0, final TupleOutput arg1) {
        PagePersistenter.serializePage(new TupleOutputSink(arg1), arg0);
    }

}
