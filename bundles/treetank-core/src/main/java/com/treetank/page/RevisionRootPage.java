/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: RevisionRootPage.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.page;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * <h1>RevisionRootPage</h1>
 * 
 * <p>
 * Revision root page holds a reference to the name page as well as the static
 * node page tree.
 * </p>
 */
public final class RevisionRootPage extends AbstractPage {

    /** Offset of name page reference. */
    private static final int NAME_REFERENCE_OFFSET = 0;

    /** Offset of indirect page reference. */
    private static final int INDIRECT_REFERENCE_OFFSET = 1;

    /** Number of nodes of this revision. */
    private long mRevisionSize;

    /** Last allocated node key. */
    private long mMaxNodeKey;

    /** Timestamp of revision. */
    private long mRevisionTimestamp;

    /**
     * Create revision root page.
     */
    public RevisionRootPage() {
        super(2, IConstants.UBP_ROOT_REVISION_NUMBER);
        mRevisionSize = 0L;
        final PageReference ref = getReference(NAME_REFERENCE_OFFSET);
        ref.setPage(new NamePage(IConstants.UBP_ROOT_REVISION_NUMBER));
        mMaxNodeKey = -1L;
    }

    /**
     * Read revision root page.
     * 
     * @param in
     *            Input bytes.
     */
    protected RevisionRootPage(final ITTSource in) {
        super(2, in);
        mRevisionSize = in.readLong();
        mMaxNodeKey = in.readLong();
        mRevisionTimestamp = in.readLong();
    }

    /**
     * Clone revision root page.
     * 
     * @param committedRevisionRootPage
     *            Page to clone.
     */
    public RevisionRootPage(final RevisionRootPage committedRevisionRootPage,
            final long revisionToUse) {
        super(2, committedRevisionRootPage, revisionToUse);
        mRevisionSize = committedRevisionRootPage.mRevisionSize;
        mMaxNodeKey = committedRevisionRootPage.mMaxNodeKey;
    }

    /**
     * Get name page reference.
     * 
     * @return Name page reference.
     */
    public PageReference getNamePageReference() {
        return getReference(NAME_REFERENCE_OFFSET);
    }

    /**
     * Get indirect page reference.
     * 
     * @return Indirect page reference.
     */
    public PageReference getIndirectPageReference() {
        return getReference(INDIRECT_REFERENCE_OFFSET);
    }

    /**
     * Get size of revision, i.e., the node count visible in this revision.
     * 
     * @return Revision size.
     */
    public long getRevisionSize() {
        return mRevisionSize;
    }

    /**
     * Get timestamp of revision.
     * 
     * @return Revision timestamp.
     */
    public long getRevisionTimestamp() {
        return mRevisionTimestamp;
    }

    /**
     * Get last allocated node key.
     * 
     * @return Last allocated node key.
     */
    public long getMaxNodeKey() {
        return mMaxNodeKey;
    }

    /**
     * Increment number of nodes by one while allocating another key.
     */
    public void incrementMaxNodeKey() {
        mMaxNodeKey += 1;
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public final void commit(final WriteTransactionState state) {
    // super.commit(state);
    // mRevisionTimestamp = System.currentTimeMillis();
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serialize(final ITTSink out) {
        mRevisionTimestamp = System.currentTimeMillis();
        super.serialize(out);
        out.writeLong(mRevisionSize);
        out.writeLong(mMaxNodeKey);
        out.writeLong(mRevisionTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " revisionSize=" + mRevisionSize
                + ", revisionTimestamp=" + mRevisionTimestamp + ", namePage=("
                + getReference(NAME_REFERENCE_OFFSET) + "), indirectPage=("
                + getReference(INDIRECT_REFERENCE_OFFSET) + ")";
    }

}