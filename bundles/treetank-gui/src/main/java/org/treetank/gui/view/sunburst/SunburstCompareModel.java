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

package org.treetank.gui.view.sunburst;

import java.util.*;
import java.util.concurrent.*;

import javax.xml.namespace.QName;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IStructuralItem;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.diff.DiffDepth;
import org.treetank.diff.DiffFactory;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffKind;
import org.treetank.diff.IDiffObserver;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.AbsNode;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.sunburst.SunburstItem.Builder;
import org.treetank.gui.view.sunburst.SunburstItem.EStructType;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Model to compare revisions.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstCompareModel extends AbsModel implements IModel, Iterator<SunburstItem> {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(SunburstCompareModel.class));

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    SunburstCompareModel(final PApplet paramApplet, final ReadDB paramDb) {
        super(paramApplet, paramDb);
    }

    /** {@inheritDoc} */
    @Override
    public void update(final SunburstContainer paramContainer) {
        mLastItems.push(new ArrayList<SunburstItem>(mItems));
        mLastDepths.push(mLastMaxDepth);
        mLastOldDepths.push(mLastOldMaxDepth);
        traverseTree(paramContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final SunburstContainer paramContainer) {
        assert paramContainer.mRevision >= 0;
        assert paramContainer.mKey >= 0;
        assert paramContainer.mDepth >= 0;
        assert paramContainer.mModWeight >= 0;

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<SunburstFireContainer> future =
            executor.submit(new TraverseCompareTree(paramContainer.mRevision, mDb.getRevisionNumber(),
                paramContainer.mKey, paramContainer.mDepth, paramContainer.mModWeight, this));
        try {
            mItems = future.get().mItems;
            mLastMaxDepth = future.get().mDepthMax;
            mLastOldMaxDepth = future.get().mOldDepthMax;
            firePropertyChange("oldMaxDepth", null, mLastOldMaxDepth);
            firePropertyChange("maxDepth", null, mLastMaxDepth);
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        shutdownAndAwaitTermination(executor);

        firePropertyChange("done", null, true);
    }

    /** Traverse and compare trees. */
    private static final class TraverseCompareTree extends AbsComponent implements
        Callable<SunburstFireContainer>, IDiffObserver, ITraverseModel {

        /** Timeout for {@link CountDownLatch}. */
        private static final long TIMEOUT_S = 200000;

        /** {@link CountDownLatch} to wait until {@link List} of {@link EDiff}s has been created. */
        private final CountDownLatch mStart;

        /** Revision to compare. */
        private final long mRevision;

        /** Key from which to start traversal. */
        private final long mKey;

        /** {@link SunburstCompareModel}. */
        private final SunburstCompareModel mModel;

        /** {@link List} of {@link SunburstItem}s. */
        private final List<SunburstItem> mItems;

        /** {@link NodeRelations} instance. */
        private final NodeRelations mRelations;

        /** Maximum depth in the tree. */
        private transient int mDepthMax;

        /** Minimum text length. */
        private transient int mMinTextLength;

        /** Maximum text length. */
        private transient int mMaxTextLength;

        /** {@link ReadDb} instance. */
        private final ReadDB mDb;

        /** Maximum descendant count in tree. */
        private transient long mMaxDescendantCount;

        /** Parent processing frame. */
        private final PApplet mParent;

        /** {@link IReadTransaction} instance. */
        private transient IReadTransaction mRtx;

        /** Weighting of modifications. */
        private transient float mModWeight;

        /** Maximum depth in new revision. */
        private transient int mNewDepthMax;

        /** {@link IReadTransaction} on the revision to compare. */
        private transient IReadTransaction mNewRtx;

        /** {@link List} of {@link EDiff} constants. */
        private transient List<Diff> mDiffs;

        /** Start depth in the tree. */
        private transient int mDepth;

        /**
         * Constructor.
         * 
         * @param paramRevision
         *            the revision to compare
         * @param paramKey
         *            key from which to start traversal
         * @param paramDepth
         *            depth to prune
         * @param paramModificationWeight
         *            determines how much modifications are weighted to compute the extension angle of each
         *            {@link SunburstItem}
         * @param paramModel
         *            the {@link SunburstModel}
         */
        private TraverseCompareTree(final long paramNewRevision, final long paramCurrRevision,
            final long paramKey, final int paramDepth, final float paramModificationWeight,
            final AbsModel paramModel) {
            assert paramNewRevision >= 0;
            assert paramKey >= 0;
            assert paramDepth >= 0;
            assert paramModificationWeight >= 0;
            assert paramModel != null;

            if (paramNewRevision < paramCurrRevision) {
                throw new IllegalArgumentException(
                    "paramNewRevision must be greater than the currently opened revision!");
            }

            mModel = (SunburstCompareModel)paramModel;
            mDb = mModel.mDb;

            try {
                mRtx = mModel.mDb.getSession().beginReadTransaction(paramCurrRevision);
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            mRevision = paramNewRevision;
            mKey = paramKey == 0 ? paramKey + 1 : paramKey;
            mModWeight = paramModificationWeight;
            mRelations = new NodeRelations();
            mDiffs = new LinkedList<Diff>();
            mStart = new CountDownLatch(1);
            mItems = new LinkedList<SunburstItem>();
            mParent = mModel.mParent;
            mDepth = paramDepth;
            mRtx.moveTo(mKey);
        }

        @Override
        public SunburstFireContainer call() {
            LOGWRAPPER.debug("Build sunburst items.");

            // Remove all elements from item list.
            mItems.clear();

            try {
                // Get min and max textLength of both revisions.
                final IReadTransaction rtx = mDb.getSession().beginReadTransaction(mRevision);
                getMinMaxTextLength(rtx);
                rtx.close();

                // Invoke diff.
                final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
                observer.add(this);
                DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mDb.getDatabase(), mKey, mRevision,
                    mRtx.getRevisionNumber(), EDiffKind.NORMAL, observer).setNewDepth(mDepth).setOldDepth(
                    mDepth));

                // Wait for diff list to complete.
                mStart.await(TIMEOUT_S, TimeUnit.SECONDS);

                for (final Diff diff : mDiffs) {
                    final EDiff diffEnum = diff.getDiff();

                    System.out.println(diffEnum);
                }

                // Maximum depth in old revision.
                mDepthMax = getDepthMax(mRtx);

                mNewRtx = mDb.getSession().beginReadTransaction(mRevision);
                mNewRtx.moveTo(mKey);
                for (final AbsAxis axis =
                    new SunburstCompareDescendantAxis(true, this, mNewRtx, mRtx, mDiffs, mDepthMax, mDepth); axis
                    .hasNext(); axis.next()) {
                }
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            try {
                mRtx.close();
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
            LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
            LOGWRAPPER.debug("oldMaxDepth: " + mDepthMax);

            return new SunburstFireContainer(mItems, mNewDepthMax).setOldDepthMax(mDepthMax);
        }

        /** {@inheritDoc} */
        @Override
        public void diffListener(final EDiff paramDiff, final IItem paramNewNode, final IItem paramOldNode,
            final DiffDepth paramDepth) {
            mDiffs.add(new Diff(paramDiff, paramNewNode, paramOldNode, paramDepth));
        }

        /** {@inheritDoc} */
        @Override
        public void diffDone() {
            mStart.countDown();
        }

        /**
         * Get the maximum depth in the tree.
         * 
         * @param paramRtx
         *            {@link IReadTransaction} on the tree
         * @return maximum depth
         */
        private int getDepthMax(final IReadTransaction paramRtx) {
            assert paramRtx != null;
            assert !paramRtx.isClosed();

            int depthMax = 0;
            int depth = 0;
            int index = -1;
            final long nodeKey = paramRtx.getNode().getNodeKey();
            for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
                index++;
                while (index < mDiffs.size() && mDiffs.get(index).getDiff() == EDiff.INSERTED) {
                    index++;
                }
                final IStructuralItem node = paramRtx.getStructuralNode();
                if (node.hasFirstChild()) {
                    depth++;
                    
                    int tmpIndex = index + 1;
                    while (tmpIndex < mDiffs.size() && mDiffs.get(tmpIndex).getDiff() == EDiff.INSERTED) {
                        tmpIndex++;
                    }
                    index = tmpIndex - 1;

                    if (//index < mDiffs.size() && mDiffs.get(index).getDiff() == EDiff.SAME
                        index + 1 < mDiffs.size() && mDiffs.get(index + 1).getDiff() == EDiff.SAME) {
                        // Set depth max.
                        depthMax = Math.max(depth, depthMax);
                    }
                } else if (!node.hasRightSibling()) {
                    // Next node will be a right sibling of an anchestor node or the traversal ends.
                    final long currNodeKey = mRtx.getNode().getNodeKey();
                    do {
                        if (mRtx.getStructuralNode().hasParent()) {
                            mRtx.moveToParent();
                            depth--;
                        } else {
                            break;
                        }
                    } while (!mRtx.getStructuralNode().hasRightSibling());
                    mRtx.moveTo(currNodeKey);
                }
            }
            paramRtx.moveTo(nodeKey);
            return depthMax;
        }

        /** {@inheritDoc} */
        @Override
        public void getMinMaxTextLength(final IReadTransaction paramRtx) {
            assert paramRtx != null;
            assert !paramRtx.isClosed();

            mMinTextLength = Integer.MAX_VALUE;
            mMaxTextLength = Integer.MIN_VALUE;
            for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
                if (paramRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                    final int length = paramRtx.getValueOfCurrentNode().length();
                    if (length < mMinTextLength) {
                        mMinTextLength = length;
                    }

                    if (length > mMaxTextLength) {
                        mMaxTextLength = length;
                    }
                }
            }

            for (final AbsAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
                if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                    final int length = mRtx.getValueOfCurrentNode().length();
                    if (length < mMinTextLength) {
                        mMinTextLength = length;
                    }

                    if (length > mMaxTextLength) {
                        mMaxTextLength = length;
                    }
                }
            }

            if (mMinTextLength == Integer.MAX_VALUE) {
                mMinTextLength = 0;
            }
            if (mMaxTextLength == Integer.MIN_VALUE) {
                mMaxTextLength = 0;
            }

            LOGWRAPPER.debug("MINIMUM text length: " + mMinTextLength);
            LOGWRAPPER.debug("MAXIMUM text length: " + mMaxTextLength);
        }

        /** {@inheritDoc} */
        @Override
        public float createSunburstItem(final Item paramItem, final int paramDepth, final int paramIndex) {
            // Initialize variables.
            final float angle = paramItem.mAngle;
            final float parExtension = paramItem.mExtension;
            final int indexToParent = paramItem.mIndexToParent;
            final long descendantCount = paramItem.mDescendantCount;
            final long parentDescCount = paramItem.mParentDescendantCount;
            final long modificationCount = paramItem.mModificationCount;
            long parentModificationCount = paramItem.mParentModificationCount;
            final boolean subtract = paramItem.mSubtract;
            final EDiff diff = paramItem.mDiff;
            final int depth = paramDepth;

            // Add a sunburst item.
            IStructuralItem node = null;
            if (diff == EDiff.DELETED) {
                node = mRtx.getStructuralNode();
            } else {
                node = mNewRtx.getStructuralNode();
            }
            final EStructType structKind =
                node.hasFirstChild() ? EStructType.ISINNERNODE : EStructType.ISLEAFNODE;

            // Calculate extension.
            float extension = 2 * PConstants.PI;
            if (indexToParent > -1) {
                if (mItems.get(indexToParent).getSubtract()) {
                    parentModificationCount -= 1;
                }
                extension =
                    (1 - mModWeight)
                        * (parExtension * (float)descendantCount / ((float)parentDescCount - 1f))
                        + mModWeight
                        * (parExtension * (float)modificationCount / ((float)parentModificationCount - 1f));
            }

            LOGWRAPPER.debug("ITEM: " + paramIndex);
            LOGWRAPPER.debug("modificationCount: " + modificationCount);
            LOGWRAPPER.debug("parentModificationCount: " + parentModificationCount);
            LOGWRAPPER.debug("descendantCount: " + descendantCount);
            LOGWRAPPER.debug("parentDescCount: " + parentDescCount);
            LOGWRAPPER.debug("indexToParent: " + indexToParent);
            LOGWRAPPER.debug("extension: " + extension);
            LOGWRAPPER.debug("depth: " + depth);
            LOGWRAPPER.debug("angle: " + angle);

            // Set node relations.
            String text = null;
            if (node.getKind() == ENodes.TEXT_KIND) {
                if (diff == EDiff.DELETED) {
                    text = mRtx.getValueOfCurrentNode();
                } else {
                    text = mNewRtx.getValueOfCurrentNode();
                }
                mRelations.setSubtract(subtract).setAll(depth, structKind, text.length(), mMinTextLength,
                    mMaxTextLength, indexToParent);
            } else {
                mRelations.setSubtract(subtract).setAll(depth, structKind, descendantCount, 0,
                    mMaxDescendantCount, indexToParent);
            }

            // Build item.
            if (text != null) {
                LOGWRAPPER.debug("text: " + text);
                SunburstItem.Builder builder =
                    new SunburstItem.Builder(mParent, mModel, angle, extension, mRelations, mDb)
                        .setNode(node).setText(text).setDiff(diff);
                updated(diff, builder);
                mItems.add(builder.build());
            } else {
                QName name = null;
                if (diff == EDiff.DELETED) {
                    name = mRtx.getQNameOfCurrentNode();
                } else {
                    name = mNewRtx.getQNameOfCurrentNode();
                }
                LOGWRAPPER.debug("name: " + name.getLocalPart());
                SunburstItem.Builder builder =
                    new SunburstItem.Builder(mParent, mModel, angle, extension, mRelations, mDb)
                        .setNode(node).setQName(name).setDiff(diff);
                updated(diff, builder);
                mItems.add(builder.build());
            }

            // Set depth max.
            mNewDepthMax = Math.max(depth, mNewDepthMax);

            return extension;
        }

        /**
         * Add old node text or {@link QName} to the {@link SunburstItem.Builder}.
         * 
         * @param paramDiff
         *            determines if it's value is EDiff.UPDATED
         * @param builder
         *            {@link SunburstItem.Builder} instance
         */
        private void updated(final EDiff paramDiff, final SunburstItem.Builder builder) {
            if (paramDiff == EDiff.UPDATED) {
                final IStructuralItem oldNode = mRtx.getStructuralNode();
                if (oldNode.getKind() == ENodes.TEXT_KIND) {
                    builder.setOldText(mRtx.getValueOfCurrentNode());
                } else {
                    builder.setOldQName(mRtx.getQNameOfCurrentNode());
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Integer> getDescendants(final IReadTransaction paramRtx) throws InterruptedException,
            ExecutionException {
            assert paramRtx != null;

            // Get descendants for every node and save it to a list.
            final List<Integer> descendants = new LinkedList<Integer>();
            // final ExecutorService executor = Executors.newSingleThreadExecutor();
            // Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            final List<Diff> diffs = new LinkedList<Diff>(mDiffs);
            boolean firstNode = true;
            int index = 0;
            final int depth = diffs.get(0).getDepth().getNewDepth();

            for (final AbsAxis axis = new DescendantAxis(paramRtx, true); 0 < diffs.size()
                && diffs.get(0).getDiff() == EDiff.DELETED && depth < diffs.get(0).getDepth().getOldDepth()
                || axis.hasNext();) {
                if (axis.getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                    if (diffs.get(0).getDiff() != EDiff.DELETED) {
                        axis.next();
                    }

                    try {
                        final int descs =
                            new Descendants(axis.getTransaction().getRevisionNumber(),
                                mRtx.getRevisionNumber(), axis.getTransaction().getNode().getNodeKey(),
                                mDb.getSession(), index, diffs).call();
                        if (firstNode && diffs.get(0).getDiff() != EDiff.DELETED) {
                            firstNode = false;
                            mMaxDescendantCount = descs;
                        }
                        // System.out.println(submit.get());
                        // if (axis.getTransaction().getNode().getKind() == ENodes.ELEMENT_KIND) {
                        // System.out.println(axis.getTransaction().getQNameOfCurrentNode());
                        // }

                        descendants.add(descs);
                        index++;
                        diffs.remove(0);
                    } catch (final TTIOException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    } catch (final AbsTTException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }
                }
            }
            // shutdownAndAwaitTermination(executor);

            return descendants;
        }

        /** Counts descendants. */
        private final class Descendants implements Callable<Integer> {
            /** Treetank {@link IReadTransaction} on new revision. */
            private transient IReadTransaction mNewRtx;

            /** Treetank {@link IReadTransaction} on old revision. */
            private transient IReadTransaction mOldRtx;

            /** Index of current diff. */
            private int mIndex;

            /** {@List} of {@Diff}s. */
            private final List<Diff> mDiffs;

            /**
             * Constructor.
             * 
             * @param paramNewRevision
             *            new revision number
             * @param paramOldRevision
             *            old revision number
             * @param paramKey
             *            key of node
             * @param paramSession
             *            {@link ISession} instance
             * @param paramIndex
             *            current index in diff list
             * @param paramDiffs
             *            {@link List} of {@link Diff}s
             */
            Descendants(final long paramNewRevision, final long paramOldRevision, final long paramKey,
                final ISession paramSession, final int paramIndex, final List<Diff> paramDiffs) {
                assert paramNewRevision > 0;
                assert paramOldRevision >= 0;
                assert paramSession != null;
                assert paramIndex > -1;
                assert paramDiffs != null;
                try {
                    synchronized (paramSession) {
                        mNewRtx = paramSession.beginReadTransaction(paramNewRevision);
                        mOldRtx = paramSession.beginReadTransaction(paramOldRevision);
                    }
                } catch (final TTIOException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                } catch (final AbsTTException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
                mNewRtx.moveTo(paramKey);
                mIndex = 0;
                mDiffs = new LinkedList<Diff>(paramDiffs);
            }

            @Override
            public Integer call() throws AbsTTException {
                int retVal = 0;

                final Diff diff = mDiffs.get(mIndex);
                if (diff.getDiff() == EDiff.DELETED) {
                    mOldRtx.moveTo(diff.getOldNode().getNodeKey());

                    for (final AbsAxis axis = new DescendantAxis(mOldRtx, true); axis.hasNext(); axis.next()) {
                        retVal++;
                    }
                } else {
                    final int depth = diff.getDepth().getNewDepth();
                    final long nodeKey = mNewRtx.getNode().getNodeKey();

                    // Be sure we are on the root node.
                    if (mNewRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                        mNewRtx.moveToFirstChild();
                    }

                    // Root node has been deleted.
                    if (retVal < mDiffs.size() && mDiffs.get(retVal).getDiff() == EDiff.DELETED) {
                        retVal++;
                    }

                    // Root node.
                    retVal++;

                    if (mNewRtx.getStructuralNode().getChildCount() > 0) {
                        do {
                            if (((AbsStructNode)mNewRtx.getNode()).hasFirstChild()) {
                                retVal = countDeletes(retVal, depth);
                                mNewRtx.moveToFirstChild();
                                retVal++;
                            } else {
                                while (!((AbsStructNode)mNewRtx.getNode()).hasRightSibling()) {
                                    if (((AbsStructNode)mNewRtx.getNode()).hasParent()
                                        && mNewRtx.getNode().getNodeKey() != nodeKey) {
                                        retVal = countDeletes(retVal, depth);
                                        mNewRtx.moveToParent();
                                    } else {
                                        break;
                                    }
                                }
                                if (mNewRtx.getNode().getNodeKey() != nodeKey) {
                                    retVal = countDeletes(retVal, depth);
                                    mNewRtx.moveToRightSibling();
                                    retVal++;
                                }
                            }
                        } while (mNewRtx.getNode().getNodeKey() != nodeKey);
                        mNewRtx.moveTo(nodeKey);
                    } else
                    // Remember deleted subtrees of the current node.
                    if (mIndex + 1 < mDiffs.size()
                        && retVal == 1
                        && mDiffs.get(mIndex + 1).getDiff() == EDiff.DELETED
                        && mDiffs.get(mIndex + 1).getDepth().getOldDepth() > mDiffs.get(mIndex).getDepth()
                            .getNewDepth()) {
                        mOldRtx.moveTo(mDiffs.get(mIndex + 1).getOldNode().getNodeKey());

                        int diffIndex = mIndex;
                        do {
                            final long key = mOldRtx.getStructuralNode().getNodeKey();

                            if (diffIndex + 1 < mDiffs.size()
                                && mDiffs.get(mIndex).getDepth().getNewDepth() == (mDiffs.get(diffIndex + 1)
                                    .getDepth().getOldDepth() - 1)) {
                                for (final AbsAxis axis = new DescendantAxis(mOldRtx, true); axis.hasNext(); axis
                                    .next()) {
                                    diffIndex++;
                                    retVal++;
                                }
                            } else {
                                break;
                            }

                            mOldRtx.moveTo(key);
                        } while (mOldRtx.getStructuralNode().hasRightSibling()
                            && mOldRtx.moveToRightSibling());

                        mIndex++;
                    }
                }

                mNewRtx.close();
                return retVal;
            }

            /**
             * Count subsequent deletes.
             * 
             * @param paramCount
             *            current count of descendants
             * @param paramDepth
             *            depth of node in new revision
             * @return counter
             */
            private int countDeletes(final int paramCount, final int paramDepth) {
                int retVal = paramCount;
                while (retVal < mDiffs.size() && mDiffs.get(retVal).getDiff() == EDiff.DELETED
                    && paramDepth < mDiffs.get(retVal).getDepth().getOldDepth()) {
                    retVal++;
                }
                return retVal;
            }
        }
    }
}
