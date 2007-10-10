/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.pagelayer;

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.sessionlayer.TransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.StaticTree;

final public class UberPage extends AbstractPage implements IPage {

  private long mRevisionCount;

  private PageReference mIndirectPageReference;

  private RevisionRootPage mCurrentRevisionRootPage;

  private StaticTree mStaticTree;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  private UberPage() {
    mIndirectPageReference = null;
    mStaticTree = null;
  }

  /**
   * Create new uncommitted in-memory uber page.
   * 
   * @param pageCache
   * @return
   * @throws Exception
   */
  public static final UberPage create() throws Exception {

    final UberPage uberPage = new UberPage();

    // Make sure that all references are instantiated.
    uberPage.mRevisionCount = IConstants.UBP_INIT_ROOT_REVISION_KEY;

    // Indirect pages (shallow init).
    uberPage.mIndirectPageReference = createPageReference();
    uberPage.mStaticTree = new StaticTree(uberPage.mIndirectPageReference);

    // Make sure that the first empty revision root page already exists.
    uberPage.mCurrentRevisionRootPage =
        RevisionRootPage.create(IConstants.UBP_ROOT_REVISION_KEY);

    return uberPage;

  }

  /**
   * Read committed uber page from disk.
   * 
   * @param pageCache
   * @param in
   * @throws Exception
   */
  public static final UberPage read(
      final TransactionState state,
      final FastByteArrayReader in) throws Exception {

    final UberPage uberPage = new UberPage();

    // Deserialize uber page.
    uberPage.mRevisionCount = in.readPseudoLong();

    // Indirect pages (shallow load without indirect page instances).
    uberPage.mIndirectPageReference = readPageReference(in);
    uberPage.mStaticTree = new StaticTree(uberPage.mIndirectPageReference);

    // Make sure latest revision root page is active.
    uberPage.mCurrentRevisionRootPage =
        uberPage.getRevisionRootPage(state, uberPage.mRevisionCount);

    return uberPage;
  }

  /**
   * COW committed uber page to modify it.
   * 
   * @param committedUberPage
   * @return
   */
  public static final UberPage clone(final UberPage committedUberPage) {

    final UberPage uberPage = new UberPage();

    // COW uber page.
    uberPage.mRevisionCount = committedUberPage.mRevisionCount;

    // Indirect pages (shallow COW without page instances).
    uberPage.mIndirectPageReference =
        clonePageReference(committedUberPage.mIndirectPageReference);
    uberPage.mStaticTree = new StaticTree(uberPage.mIndirectPageReference);

    uberPage.mCurrentRevisionRootPage =
        committedUberPage.mCurrentRevisionRootPage;

    return uberPage;
  }

  public final long getRevisionCount() {
    return mRevisionCount;
  }

  public final RevisionRootPage getRevisionRootPage(
      final TransactionState state,
      final long revisionKey) throws Exception {

    RevisionRootPage page =
        state.getPageCache().dereferenceRevisionRootPage(
            state,
            mStaticTree.get(state, revisionKey),
            revisionKey);

    return RevisionRootPage.clone(revisionKey, page);

  }

  public final RevisionRootPage prepareRevisionRootPage(
      final TransactionState state) throws Exception {

    // Calculate number of levels and offsets of these levels.
    final int[] offsets =
        StaticTree.calcIndirectPageOffsets(mRevisionCount + 1);

    // Which page reference to COW on immediate level 0?
    mCurrentRevisionRootPage =
        RevisionRootPage.clone(mRevisionCount + 1, mCurrentRevisionRootPage);

    // Indirect reference.
    PageReference reference = mIndirectPageReference;
    IPage page = null;

    //    Remaining levels.
    for (int i = 0; i < offsets.length; i++) {
      page = prepareIndirectPage(state, reference);
      reference = ((IndirectPage) page).getPageReference(offsets[i]);
    }
    reference.setPage(mCurrentRevisionRootPage);

    return mCurrentRevisionRootPage;

  }

  /**
   * {@inheritDoc}
   */
  public final void commit(
      final TransactionState state,
      final PageWriter pageWriter) throws Exception {
    commit(state, pageWriter, mIndirectPageReference);
    mRevisionCount += 1;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) throws Exception {
    out.writePseudoLong(mRevisionCount);
    serialize(out, mIndirectPageReference);
  }

}
