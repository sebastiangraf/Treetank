/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: VariableAxis.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractAxis;

/**
 * <h1>VariableAxis</h1>
 * <p>
 * Evaluated the given binding sequence, the variable is bound to and stores in
 * a list that can be accessed by other sequences and notifies its observers, as
 * soon as a new value of the binding sequence has been evaluated.
 * </p>
 */
public class VariableAxis extends AbstractAxis implements IAxis {

    /** Sequence that defines the values, the variable is bound to. */
    private final IAxis bindingSeq;

    private final List<VarRefExpr> mVarRefs;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param inSeq
     *            sequence, the variable is bound to.
     */
    public VariableAxis(final IReadTransaction rtx, final IAxis inSeq) {

        super(rtx);
        bindingSeq = inSeq;
        mVarRefs = new ArrayList<VarRefExpr>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long nodeKey) {
        super.reset(nodeKey);
        if (bindingSeq != null) {
            bindingSeq.reset(nodeKey);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (bindingSeq.hasNext()) {
            notifyObs();
            return true;
        }

        resetToStartKey();
        return false;

    }

    /**
     * Tell all observers that the a new item of the binding sequence has been
     * evaluated.
     */
    private void notifyObs() {

        for (VarRefExpr varRef : mVarRefs) {
            varRef.update(getTransaction().getNode().getNodeKey());
        }
    }

    /**
     * Add an observer to the list.
     * 
     * @param observer
     *            axis that wants to be notified of any change of this axis
     */
    public void addObserver(final VarRefExpr observer) {

        mVarRefs.add(observer);
    }

}