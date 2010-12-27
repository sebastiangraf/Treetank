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

package com.treetank.service.xml.xpath.functions;

import java.util.List;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.TTXPathException;
import com.treetank.utils.TypedValue;

/**
 * <h1>FNPosition</h1>
 * <p>
 * IAxis that represents the function fn:position specified in <a
 * href="http://www.w3.org/TR/xquery-operators/"> XQuery 1.0 and XPath 2.0 Functions and Operators</a>.
 * </p>
 * <p>
 * The function returns position of the item in the expression result set.
 * </p>
 */
public class FNPosition extends AbsFunction {

    /**
     * Constructor.
     * 
     * Initializes internal state and do a statical analysis concerning the
     * function's arguments.
     * 
     * @param rtx
     *            Transaction to operate on
     * @param args
     *            List of function arguments
     * @param min
     *            min number of allowed function arguments
     * @param max
     *            max number of allowed function arguments
     * @param returnType
     *            the type that the function's result will have
     * @throws TTXPathException
     *             if function check fails
     */
    public FNPosition(final IReadTransaction rtx, final List<AbsAxis> args, final int min, final int max,
        final int returnType) throws TTXPathException {

        super(rtx, args, min, max, returnType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] computeResult() {
        Integer position = 0;
        final long currentNode = getTransaction().getNode().getNodeKey();
        getTransaction().moveToParent();
        getTransaction().moveToFirstChild();
        do {
            position++;
            getTransaction().moveToRightSibling();
        } while (getTransaction().getNode().getNodeKey() != currentNode);

        return TypedValue.getBytes(position.toString());
    }

}
