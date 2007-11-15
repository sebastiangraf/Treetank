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

package org.treetank.sessionlayer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.pagelayer.AbstractPage;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.UberPage;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastWeakHashMap;

/**
 * <h1>SessionState</h1>
 * 
 * <p>
 * State of each session.
 * </p>
 */
public final class SessionState {

  /** Session configuration. */
  private SessionConfiguration mSessionConfiguration;

  /** Shared read-only page mPageCache. */
  private Map<Long, AbstractPage> mPageCache;

  /** Write semaphore to assure only one exclusive write transaction exists. */
  private Semaphore mWriteSemaphore;

  /** Read semaphore to control running read transactions. */
  private Semaphore mReadSemaphore;

  /** Strong reference to uber page before the begin of a write transaction. */
  private UberPage mLastCommittedUberPage;

  /**
   * Constructor to bind to a TreeTank file.
   * 
   * <p>
   * The beacon logic works as follows:
   * 
   * <ol>
   * <li><code>Primary beacon == secondary beacon</code>: OK.</li>
   * <li><code>Primary beacon != secondary beacon</code>: try to recover...
   *    <ol type="i">
   *    <li><code>Checksum(uberpage) == primary beacon</code>:
   *        truncate file and write secondary beacon - OK.</li>
   *    <li><code>Checksum(uberpage) == secondary beacon</code>:
   *        write primary beacon - OK.</li>
   *    <li><code>Checksum(uberpage) != secondary beacon 
   *        != primary beacon</code>: NOK.</li>
   *    </ol>
   * </li>
   * </ol>
   * </p>
   * 
   * @param sessionConfiguration Session configuration for the TreeTank.
   */
  protected SessionState(final SessionConfiguration sessionConfiguration) {

    mSessionConfiguration = sessionConfiguration;
    RandomAccessFile file = null;

    try {

      // Make sure that the TreeTank file exists.
      new File(mSessionConfiguration.getAbsolutePath()).createNewFile();

      // Init session members.
      mPageCache = new FastWeakHashMap<Long, AbstractPage>();
      mWriteSemaphore = new Semaphore(IConstants.MAX_WRITE_TRANSACTIONS);
      mReadSemaphore = new Semaphore(IConstants.MAX_READ_TRANSACTIONS);
      final PageReference<UberPage> uberPageReference =
          new PageReference<UberPage>();
      final PageReference<UberPage> secondaryUberPageReference =
          new PageReference<UberPage>();

      file =
          new RandomAccessFile(
              mSessionConfiguration.getAbsolutePath(),
              IConstants.READ_WRITE);

      if (file.length() == 0L) {
        // Bootstrap uber page and make sure there already is a root node.
        mLastCommittedUberPage = new UberPage();
        uberPageReference.setPage(mLastCommittedUberPage);
      } else {

        // Read primary beacon.
        file.seek(IConstants.BEACON_START);
        uberPageReference.setStart(file.readLong());
        uberPageReference.setLength(file.readInt());
        uberPageReference.setChecksum(file.readLong());

        // Read secondary beacon.
        file.seek(file.length() - IConstants.BEACON_LENGTH);
        secondaryUberPageReference.setStart(file.readLong());
        secondaryUberPageReference.setLength(file.readInt());
        secondaryUberPageReference.setChecksum(file.readLong());

        // Beacon logic case 1.
        if (uberPageReference.equals(secondaryUberPageReference)) {

          final FastByteArrayReader in =
              new PageReader(mSessionConfiguration).read(uberPageReference);
          mLastCommittedUberPage = new UberPage(in);

          // Beacon logic case 2.
        } else {
          // TODO implement cases 2i, 2ii, and 2iii to be more robust!
          throw new IllegalStateException(
              "Inconsistent TreeTank file encountered. Primary start="
                  + uberPageReference.getStart()
                  + " size="
                  + uberPageReference.getLength()
                  + " checksum="
                  + uberPageReference.getChecksum()
                  + " secondary start="
                  + secondaryUberPageReference.getStart()
                  + " size="
                  + secondaryUberPageReference.getLength()
                  + " checksum="
                  + secondaryUberPageReference.getChecksum());

        }

      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  protected final IReadTransaction beginReadTransaction() {
    return beginReadTransaction(mLastCommittedUberPage.getRevisionKey());
  }

  protected final IReadTransaction beginReadTransaction(final long revisionKey) {

    try {
      mReadSemaphore.acquire();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ReadTransaction(this, new ReadTransactionState(
        mSessionConfiguration,
        mPageCache,
        mLastCommittedUberPage,
        revisionKey));
  }

  protected final IWriteTransaction beginWriteTransaction(
      final boolean autoCommit) {

    if (mWriteSemaphore.availablePermits() == 0) {
      throw new IllegalStateException(
          "There already is a running exclusive write transaction.");
    }

    try {
      mWriteSemaphore.acquire();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new WriteTransaction(this, getWriteTransactionState(), autoCommit);
  }

  protected final WriteTransactionState getWriteTransactionState() {
    return new WriteTransactionState(
        mSessionConfiguration,
        mPageCache,
        new UberPage(mLastCommittedUberPage));
  }

  protected final UberPage getLastCommittedUberPage() {
    return mLastCommittedUberPage;
  }

  protected final void setLastCommittedUberPage(
      final UberPage lastCommittedUberPage) {
    mLastCommittedUberPage = lastCommittedUberPage;
  }

  protected final void closeWriteTransaction() {
    mWriteSemaphore.release();
  }

  protected final void closeReadTransaction() {
    mReadSemaphore.release();
  }

  protected final void close() {
    if (mWriteSemaphore.drainPermits() != IConstants.MAX_WRITE_TRANSACTIONS) {
      throw new IllegalStateException("Session can not be closed due to a"
          + " running exclusive write transaction.");
    }
    if (mReadSemaphore.drainPermits() != IConstants.MAX_READ_TRANSACTIONS) {
      throw new IllegalStateException("Session can not be closed due to one"
          + " or more running share read transactions.");
    }

    // Immediately release all ressources.
    mSessionConfiguration = null;
    mPageCache = null;
    mWriteSemaphore = null;
    mReadSemaphore = null;
    mLastCommittedUberPage = null;
  }

  protected final SessionConfiguration getSessionConfiguration() {
    return mSessionConfiguration;
  }

  /**
   * Required to close file handle.
   * 
   * @throws Throwable if the finalization of the superclass does not work.
   */
  protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }

}
