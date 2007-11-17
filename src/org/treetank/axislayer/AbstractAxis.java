/*
 * Copyright (c) 2007, Marc Kramis
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
 * $Id$
 */

package org.treetank.axislayer;

import java.util.Iterator;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>AbstractAxis</h1>
 * 
 * <p>
 * Provide standard Java iterator capability compatible with the new
 * enhanced for loop available since Java 5.
 * </p>
 * 
 * <p>
 * All implementations must make sure to call super.hasNext() as the first
 * thing in hasNext().
 * </p>
 * 
 * <p>
 * All users must make sure to call next() after hasNext() evaluated to true.
 * </p>
 */
public abstract class AbstractAxis implements IAxis {

  /** Iterate over transaction exclusive to this step. */
  private final IReadTransaction mRTX;

  /** Key of last found node. */
  private long mKey;

  /** Make sure next() can only be called after hasNext(). */
  private boolean mNext;

  /** Key of node where axis started. */
  private long mStartKey;

  /**
   * Bind axis step to transaction. Make sure to hold IAxis Convention 1 by
   * moving the cursor to the document root node if no node was selected.
   * 
   * @param rtx Transaction to operate with.
   */
  public AbstractAxis(final IReadTransaction rtx) {
    mRTX = rtx;
    if (!mRTX.isSelected()) {
      mRTX.moveToDocumentRoot();
    }
    reset(rtx.getNodeKey());
  }

  /**
   * {@inheritDoc}
   */
  public final Iterator<Long> iterator() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public final Long next() {
    if (!mNext) {
      throw new IllegalStateException(
          "IAxis.next() must be called exactely once after hasNext()"
              + " evaluated to true.");
    }
    mKey = mRTX.getNodeKey();
    mNext = false;
    return mKey;
  }

  /**
   * {@inheritDoc}
   */
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public void reset(final long nodeKey) {
    mStartKey = nodeKey;
    mKey = nodeKey;
    mNext = false;
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction getTransaction() {
    return mRTX;
  }

  /**
   * Make sure the transaction points to the node it started with. This must
   * be called just before hasNext() == false.
   * 
   * @return Key of node where transaction was before the first call of
   *         hasNext().
   */
  public final long resetToStartKey() {
    mRTX.moveTo(mStartKey);
    mNext = false;
    return mStartKey;
  }

  /**
   * Make sure the transaction points to the node after the last hasNext().
   * This must be called first in hasNext().
   * 
   * @return Key of node where transaction was after the last call of
   *         hasNext().
   */
  public final long resetToLastKey() {
    mRTX.moveTo(mKey);
    mNext = true;
    return mKey;
  }

  /**
   * {@inheritDoc}
   */
  public abstract boolean hasNext();

}
