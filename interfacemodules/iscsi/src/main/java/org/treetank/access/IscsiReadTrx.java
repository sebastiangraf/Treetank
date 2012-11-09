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

package org.treetank.access;

import org.treetank.api.IIscsiReadTrx;
import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.iscsi.node.ByteNode;


/**
 * @author Andreas Rain
 *
 */
public class IscsiReadTrx implements IIscsiReadTrx {

  /** State of transaction including all cached stuff. */
  protected IPageReadTrx mPageReadTrx;

  /** Strong reference to currently selected node. */
  private INode mCurrentNode;

  /**
   * Constructor.
   * 
   * 
   * @param pPageTrx
   *            Transaction state to work with.
   * @throws TTIOException
   *             if something odd happens within the creation process.
   */
  public IscsiReadTrx(final IPageReadTrx pPageTrx) throws TTException {
      mPageReadTrx = pPageTrx;
      mCurrentNode = (INode)mPageReadTrx.getNode(0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean moveTo(long pKey) {
    try {
      this.mCurrentNode = mPageReadTrx.getNode(pKey);
      return true;
    } catch (TTException e) {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean nextNode() {
    try {
      if(mCurrentNode != null){
        this.mCurrentNode = mPageReadTrx.getNode(((ByteNode) mCurrentNode).getNextNodeKey());
      }
      else{
        return false;
      }
      return true;
    } catch (TTException e) {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getValueOfCurrentNode() {
    if(mCurrentNode == null) return null;
    
    return ((ByteNode) mCurrentNode).getVal();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public INode getCurrentNode() {
    return mCurrentNode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws TTIOException {
    mPageReadTrx.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isClosed() {
    return mPageReadTrx.isClosed();
  }

  /**
   * Make sure that the session is not yet closed when calling this method.
   */
  protected final void assertNotClosed() {
      if (mPageReadTrx.isClosed()) {
          throw new IllegalStateException("Transaction is already closed.");
      }
  }

  /**
   * Replace the state of the transaction.
   * 
   * @param paramTransactionState
   *            State of transaction.
   */
  protected final void setPageTransaction(final IPageReadTrx paramTransactionState) {
      mPageReadTrx = paramTransactionState;
  }

}