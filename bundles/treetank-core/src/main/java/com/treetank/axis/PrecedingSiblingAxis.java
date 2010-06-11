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
 * $Id: PrecedingSiblingAxis.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.axis;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.node.IStructuralNode;
import com.treetank.settings.ENodes;

/**
 * <h1>PrecedingSiblingAxis</h1>
 * 
 * <p>
 * Iterate over all preceding siblings of kind ELEMENT or TEXT starting at a
 * given node. Self is not included.
 * </p>
 */
public class PrecedingSiblingAxis extends AbstractAxis implements IAxis {

	private boolean mIsFirst;

	/**
	 * Constructor initializing internal state.
	 * 
	 * @param rtx
	 *            Exclusive (immutable) trx to iterate with.
	 */
	public PrecedingSiblingAxis(final IReadTransaction rtx) {

		super(rtx);
		mIsFirst = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void reset(final long nodeKey) {

		super.reset(nodeKey);
		mIsFirst = true;

	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean hasNext() {

		if (mIsFirst) {
			mIsFirst = false;
			// if the context node is an attribute or namespace node,
			// the following-sibling axis is empty
			if (getTransaction().getNode().getKind() == ENodes.ATTRIBUTE_KIND
			// || getTransaction().isNamespaceKind()
			) {
				resetToStartKey();
				return false;
			}
		}

		resetToLastKey();

		if (((IStructuralNode) getTransaction().getNode()).hasLeftSibling()) {
			getTransaction().moveToLeftSibling();
			return true;
		}
		resetToStartKey();
		return false;
	}

}
