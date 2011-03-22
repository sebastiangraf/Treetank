/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.service.xml.xpath.operators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.treetank.access.ReadTransaction;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.concurrent.ConcurrentAxis;
import org.treetank.service.xml.xpath.functions.Function;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.settings.EFixed;

import static org.treetank.service.xml.xpath.XPathAxis.XPATH_10_COMP;


/**
 * <h1>AbstractOpAxis</h1>
 * <p>
 * Abstract axis for all operators performing an arithmetic operation.
 * </p>
 */
public abstract class ConcurrentAbsOpAxis extends AbsAxis {

    /** First arithmetic operand. */
    private final AbsAxis mOperand1;

    /** Second arithmetic operand. */
    private final AbsAxis mOperand2;

    /** True, if axis has not been evaluated yet. */
    private boolean mIsFirst;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param op1
     *            First value of the operation
     * @param op2
     *            Second value of the operation
     */

    public ConcurrentAbsOpAxis(final IReadTransaction rtx, final AbsAxis op1, final AbsAxis op2) {
        super(rtx);
        mOperand1 = op1;
        mOperand2 = op2;
        mIsFirst = true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long nodeKey) {

        super.reset(nodeKey);
        mIsFirst = true;
        if (mOperand1 != null) {
            mOperand1.reset(nodeKey);
        }

        if (mOperand2 != null) {
            mOperand2.reset(nodeKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (mIsFirst) {
            mIsFirst = false;

            final Future<Integer> itemF1 = ConcurrentAxis.EXECUTOR.submit(new Atomize(getTransaction(), mOperand1));

            final Future<Integer> itemF2 = ConcurrentAxis.EXECUTOR.submit(new Atomize(getTransaction(), mOperand2));

            try {
                Integer item1 = itemF1.get();
                Integer item2 = itemF2.get();

                if (item1 != EFixed.NULL_NODE_KEY.getStandardProperty()) {
                    if (item2 != EFixed.NULL_NODE_KEY.getStandardProperty()) {
                        final IItem result =
                            operate((AtomicValue)getTransaction().getItemList().getItem(item1),
                                (AtomicValue)getTransaction().getItemList().getItem(item2));
                        // add retrieved AtomicValue to item list
                        int itemKey = getTransaction().getItemList().addItem(result);
                        getTransaction().moveTo(itemKey);

                        return true;
                    }
                }

            } catch (final InterruptedException mExp) {
                mExp.printStackTrace();
            } catch (final ExecutionException mExp) {
                mExp.printStackTrace();
            }

            if (XPATH_10_COMP) { // and empty sequence, return NaN
                final IItem result = new AtomicValue(Double.NaN, Type.DOUBLE);
                int itemKey = getTransaction().getItemList().addItem(result);
                getTransaction().moveTo(itemKey);
                return true;
            }

        }
        // either not the first call, or empty sequence
        resetToStartKey();
        return false;

    }

    /**
     * Performs the operation on the two input operands. First checks if the types
     * of the operands are a valid combination for the operation and if so
     * computed the result. Otherwise an XPathError is thrown.
     * 
     * @param operand1
     *            first input operand
     * @param operand2
     *            second input operand
     * @return result of the operation
     */
    protected abstract IItem operate(final AtomicValue operand1, final AtomicValue operand2);

    /**
     * Checks if the types of the operands are a valid combination for the
     * operation and if so returns the corresponding result type. Otherwise an
     * XPathError is thrown.
     * This typed check is done according to the
     * <a href="http://www.w3.org/TR/xpath20/#mapping">Operator Mapping</a>.
     * 
     * @param op1
     *            first operand's type key
     * @param op2
     *            second operand's type key
     * @return return type of the arithmetic function according to the operand
     *         type combination.
     */
    protected abstract Type getReturnType(final int op1, final int op2);

    @Override
    public void setTransaction(final IReadTransaction rtx) {
        super.setTransaction(rtx);
        mOperand1.setTransaction(rtx);
        mOperand2.setTransaction(rtx);
    }

}

class Atomize implements Callable<Integer> {

    final AbsAxis mAxis;

    final IReadTransaction mRtx;

    Atomize(final IReadTransaction rtx, final AbsAxis axis) {
        mRtx = new ReadTransaction((ReadTransaction)rtx);
        mAxis = axis;
        mAxis.setTransaction(mRtx);

    }

    public Integer call() {

        if (mAxis.hasNext()) {

            int type = mAxis.getTransaction().getNode().getTypeKey();
            AtomicValue atom;

            if (XPATH_10_COMP) {
                if (type == mAxis.getTransaction().keyForName("xs:double")
                    || type == mAxis.getTransaction().keyForName("xs:untypedAtomic")
                    || type == mAxis.getTransaction().keyForName("xs:boolean")
                    || type == mAxis.getTransaction().keyForName("xs:string")
                    || type == mAxis.getTransaction().keyForName("xs:integer")
                    || type == mAxis.getTransaction().keyForName("xs:float")
                    || type == mAxis.getTransaction().keyForName("xs:decimal")) {
                    Function.fnnumber(mAxis.getTransaction());
                }

                atom =
                    new AtomicValue(mAxis.getTransaction().getNode().getRawValue(), mAxis.getTransaction()
                        .getNode().getTypeKey());
            } else {
                // unatomicType is cast to double
                if (type == mAxis.getTransaction().keyForName("xs:untypedAtomic")) {
                    type = mAxis.getTransaction().keyForName("xs:double");
                    // TODO: throw error, of cast fails
                }

                atom = new AtomicValue(mAxis.getTransaction().getNode().getRawValue(), type);
            }

            return mAxis.getTransaction().getItemList().addItem(atom);

        }

        return (Integer)EFixed.NULL_NODE_KEY.getStandardProperty();

    }
}