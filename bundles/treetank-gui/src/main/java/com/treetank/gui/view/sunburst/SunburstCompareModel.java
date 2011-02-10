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
package com.treetank.gui.view.sunburst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.diff.DiffFactory;
import com.treetank.diff.EDiff;
import com.treetank.diff.EDiffKind;
import com.treetank.diff.IDiffObserver;
import com.treetank.exception.TTException;
import com.treetank.exception.TTIOException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstItem.EStructType;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

import processing.core.PApplet;

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

    /** Modification of current node. */
    private transient EDiff mDiff;

    /** Maximum descendant count in tree. */
    private transient long mMaxDescendantCount;

    /** {@link List} of {@link EDiff} constants. */
    private transient List<EDiff> mDiffs;

    /** Node relations used for simplyfing the SunburstItem constructor. */
    private transient NodeRelations mRelations;

    /** Weighting of modifications. */
    private transient float mModWeight;

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
        long nodeKey = 0;
        try {
            mLock.acquire();
            mLastItems.add(new ArrayList<SunburstItem>(mItems));
            nodeKey = mItems.get(mGUI.mHitTestIndex).mNode.getNodeKey();
        } catch (final InterruptedException e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
        traverseTree(paramContainer.setKey(nodeKey));
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
        final long nodeKey = paramRtx.getNode().getNodeKey();
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            final AbsStructNode node = (AbsStructNode)paramRtx.getNode();
            if (node.hasFirstChild()) {
                depth++;
                // Set depth max.
                depthMax = Math.max(depth, depthMax);
            } else if (!node.hasRightSibling()) {
                // Next node will be a right sibling of an anchestor node or the traversal ends.
                final long currNodeKey = mRtx.getNode().getNodeKey();
                do {
                    if (((AbsStructNode)mRtx.getNode()).hasParent()) {
                        mRtx.moveToParent();
                        depth--;
                    } else {
                        break;
                    }
                } while (!((AbsStructNode)mRtx.getNode()).hasRightSibling());
                mRtx.moveTo(currNodeKey);
            }
        }
        paramRtx.moveTo(nodeKey);
        return depthMax;
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final SunburstContainer paramContainer) {
        assert paramContainer.mRevision >= 0;
        assert paramContainer.mKey >= 0;
        assert paramContainer.mDepth >= 0;
        assert paramContainer.mModWeight >= 0;

        try {
            mLock.acquire();
            new Thread(new TraverseCompareTree(paramContainer.mRevision, paramContainer.mKey,
                paramContainer.mDepth, paramContainer.mModWeight, this)).start();
        } catch (final Exception e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
    }

    /** Traverse and compare trees. */
    private final class TraverseCompareTree implements Runnable, IDiffObserver {

        /** Timeout for {@link CountDownLatch}. */
        private static final long TIMEOUT_S = 200000;

        /** {@link CountDownLatch} to wait until {@link List} of {@link EDiff}s has been created. */
        private final CountDownLatch mStart;

        /** Revision to compare. */
        private final long mRevision;

        /** Key from which to start traversal. */
        private final long mKey;

        /** {@link SunburstModel}. */
        private final SunburstCompareModel mModel;

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
        private TraverseCompareTree(final long paramRevision, final long paramKey, final long paramDepth,
            final float paramModificationWeight, final AbsModel paramModel) {
            assert paramRevision >= 0;
            assert paramKey > -1 && mRtx != null && !mRtx.isClosed();
            assert paramDepth >= 0;
            assert paramModificationWeight >= 0;
            assert paramModel != null;

            try {
                if (paramRevision < mRtx.getRevisionNumber()) {
                    throw new IllegalArgumentException(
                        "paramRevision must be greater than the currently opened revision!");
                }
            } catch (final TTIOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            mRevision = paramRevision;
            mKey = paramKey;
            mModWeight = paramModificationWeight;
            mModel = (SunburstCompareModel)paramModel;
            mRelations = new NodeRelations();
            mDiff = EDiff.SAME;
            mStart = new CountDownLatch(1);

            mRtx.moveTo(mKey);
            if (mRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                mRtx.moveToFirstChild();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            LOGWRAPPER.debug("Build sunburst items.");

            // Remove all elements from item list.
            mItems.clear();

            try {
                // Get min and max textLength of the new revision.
                final IReadTransaction rtx = mDb.getSession().beginReadTransaction(mRevision);
                getMinMaxTextLength(rtx);
                rtx.close();

                // Invoke diff.
                final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
                observer.add(this);
                DiffFactory.invokeStructuralDiff(mDb.getDatabase(), mRtx.getNode().getNodeKey(), mRevision,
                    mRtx.getRevisionNumber(), EDiffKind.NORMAL, observer);

                // Wait for diff list to complete.
                mStart.await(TIMEOUT_S, TimeUnit.SECONDS);

                // Maximum depth in old revision.
                mDepthMax = getDepthMax(mRtx);

                for (final AbsAxis axis = new SunburstCompareDescendantAxis(mRtx, true, mModel, mDiffs); axis
                    .hasNext(); axis.next()) {
                }
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            mModel.firePropertyChange("done", null, true);
            mRtx.moveTo(mKey);
            LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
        }

        /** {@inheritDoc} */
        @Override
        public void diffListener(final EDiff paramDiff) {
            if (paramDiff == EDiff.DONE) {
                mStart.countDown();
            } else {
                mDiffs.add(paramDiff);
            }
        }
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
        final long parentModificationCount = paramItem.mParentModificationCount;
        final int depth = paramDepth;

        // Add a sunburst item.
        final AbsStructNode node = (AbsStructNode)mRtx.getNode();
        final EStructType structKind =
            node.hasFirstChild() ? EStructType.ISINNERNODE : EStructType.ISLEAFNODE;

        // Calculate extension.
        float extension = 0f;
        if (descendantCount == 0) {
            extension = parExtension;
        } else if (modificationCount == 0f || parentModificationCount == 0f) {
            extension =
                mModWeight * parExtension * (float)descendantCount / (float)parentDescCount
                    + (1 - mModWeight);
        } else {
            extension =
                mModWeight * (parExtension * (float)descendantCount / (float)parentDescCount)
                    + (1 - mModWeight)
                    * (parExtension * (float)modificationCount / (float)parentModificationCount);
        }

        LOGWRAPPER.debug("indexToParent: " + indexToParent);

        // Set node relations.
        int actualDepth = depth;
        if (mDiff != EDiff.SAME && mDiff != EDiff.DONE && depth < mDepthMax) {
            actualDepth = mDepthMax + 1;
        }
        String text = null;
        if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
            mRelations.setAll(actualDepth, structKind, mRtx.getValueOfCurrentNode().length(), mMinTextLength,
                mMaxTextLength, indexToParent);
            text = mRtx.getValueOfCurrentNode();
        } else {
            mRelations
                .setAll(actualDepth, structKind, descendantCount, 0, mMaxDescendantCount, indexToParent);
        }

        // Build item.
        if (text != null) {
            mItems.add(new SunburstItem.Builder(mParent, this, angle, extension, mRelations, mDb)
                .setNode(node).setText(text).build());
        } else {
            mItems.add(new SunburstItem.Builder(mParent, this, angle, extension, mRelations, mDb)
                .setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
        }

        // Set depth max.
        mDepthMax = Math.max(depth, mDepthMax);

        return extension;
    }
}