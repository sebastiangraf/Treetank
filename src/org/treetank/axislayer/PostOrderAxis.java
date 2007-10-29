package org.treetank.axislayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;

/**
 * <h1>PostOrder</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderAxis extends AbstractAxis {

  /** For remembering last parent. */
  private final FastStack<Long> mLastParent;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *            Exclusive (immutable) trx to iterate with.
   * @param startAtBeginning
   *            Starting at the beginning of the tree and though just
   *            traversing the whole tree..No, the root is not the start!
   */
  public PostOrderAxis(
      final IReadTransaction rtx,
      final boolean startAtBeginning) {
    super(rtx);
    mLastParent = new FastStack<Long>();
    mLastParent.push(IConstants.NULL_KEY);
    mNextKey = rtx.getNodeKey();
    if (startAtBeginning) {
      startAtBeginning();
    }

  }

  /**
   * Method to start at the beginning of the tree.
   */
  private final void startAtBeginning() {
    getTransaction().moveToDocument();
    while (getTransaction().hasFirstChild()) {
      mLastParent.push(getTransaction().getNodeKey());
      getTransaction().moveToFirstChild();

      mNextKey = getTransaction().getNodeKey();
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {
    INode node = getTransaction().moveTo(mNextKey);
    if (node != null) {
      while (node.hasFirstChild() && node.getNodeKey() != mLastParent.peek()) {
        mLastParent.push(node.getNodeKey());
        node = getTransaction().moveToFirstChild();
      }
      if (node.getNodeKey() == mLastParent.peek()) {
        mLastParent.pop();
      }

      if (node.hasRightSibling()) {
        mNextKey = node.getRightSiblingKey();

      } else {
        mNextKey = mLastParent.peek();
      }

      setCurrentNode(node);
      return true;

    } else {
      setCurrentNode(null);
      return false;
    }
  }

}