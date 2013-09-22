/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.xpath.expr;

import org.treetank.api.INodeReadTrx;
import org.treetank.axis.AbsAxis;
import org.treetank.data.AtomicValue;
import org.treetank.service.xml.xpath.SequenceType;
import org.treetank.utils.NamePageHash;
import org.treetank.utils.TypedValue;

/**
 * <h1>InstanceOfExpr</h1>
 * <p>
 * The boolean instance of expression returns true if the value of its first operand matches the SequenceType
 * in its second operand, according to the rules for SequenceType matching; otherwise it returns false.
 * </p>
 */
public class InstanceOfExpr extends AbsExpression {

    /** The sequence to test. */
    private final AbsAxis mInputExpr;

    /** The sequence type that the sequence needs to have to be an instance of. */
    private final SequenceType mSequenceType;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param mRtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mInputExpr
     *            input expression, to test
     * @param mSequenceType
     *            sequence type to test whether the input sequence matches to.
     */
    public InstanceOfExpr(final INodeReadTrx mRtx, final AbsAxis mInputExpr, final SequenceType mSequenceType) {

        super(mRtx);
        this.mInputExpr = mInputExpr;
        this.mSequenceType = mSequenceType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {

        super.reset(mNodeKey);

        if (mInputExpr != null) {
            mInputExpr.reset(mNodeKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AtomicValue evaluate() {

        boolean isInstanceOf;

        if (mInputExpr.hasNext()) {
            if (mSequenceType.isEmptySequence()) {
                isInstanceOf = false;
            } else {

                isInstanceOf = mSequenceType.getFilter().filter();
                switch (mSequenceType.getWildcard()) {

                case '*':
                case '+':
                    // This seams to break the pipeline, but because the
                    // intermediate
                    // result are no longer used, it might be not that bad
                    while (mInputExpr.hasNext() && isInstanceOf) {
                        isInstanceOf = isInstanceOf && mSequenceType.getFilter().filter();
                    }
                    break;
                default: // no wildcard, or '?'
                    // only one result item is allowed
                    isInstanceOf = isInstanceOf && !mInputExpr.hasNext();
                }
            }

        } else { // empty sequence
            isInstanceOf =
                mSequenceType.isEmptySequence()
                    || (mSequenceType.hasWildcard() && (mSequenceType.getWildcard() == '?' || mSequenceType
                        .getWildcard() == '*'));
        }

        // create result item and move transaction to it.
        AtomicValue val =
            new AtomicValue(TypedValue.getBytes(Boolean.toString(isInstanceOf)), NamePageHash
                .generateHashForString("xs:boolean"));
        final int mItemKey = getItemList().addItem(val);
        moveTo(mItemKey);
        return val;

    }
}
