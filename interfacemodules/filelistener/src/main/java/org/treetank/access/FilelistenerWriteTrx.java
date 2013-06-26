package org.treetank.access;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.IFilelistenerWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.file.node.FileNode;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory.MetaKey;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory.MetaValue;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Andreas Rain
 */
public class FilelistenerWriteTrx implements IFilelistenerWriteTrx {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilelistenerWriteTrx.class);

    /** Session for abort/commit. */
    private final ISession mSession;

    /** Delegator for the read access */
    private final FilelistenerReadTrx mDelegate;

    /**
     * {@inheritDoc}
     */
    public FilelistenerWriteTrx(IBucketWriteTrx pPageTrx, ISession pSession) throws TTException {
        mSession = pSession;
        mDelegate = new FilelistenerReadTrx(pPageTrx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFilePaths() {
        return mDelegate.getFilePaths();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fileExists(String pRelativePath) {
        return mDelegate.fileExists(pRelativePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFullFile(String pRelativePath) throws TTIOException, IOException {
        return mDelegate.getFullFile(pRelativePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mDelegate.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mDelegate.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addEmptyFile(String pRelativePath) throws TTException, IOException {
        MetaKey key = new MetaKey(pRelativePath);
        MetaValue value = new MetaValue(FilelistenerReadTrx.emptyFileKey);
        getBucketTransaction().getMetaBucket().put(key, value);

        return;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTException
     * @throws IOException
     */
    @Override
    public synchronized void addFile(File pFile, String pRelativePath) throws TTException, IOException {
        LOGGER.info("Adding file " + pFile.getName());

        int readingAmount = 0;

        @SuppressWarnings("resource")
        FileChannel ch = new RandomAccessFile(pFile, "rw").getChannel();
        FileLock lock = null;

        while (lock == null) {
            try {
                lock = ch.tryLock();
            } catch (OverlappingFileLockException e) {
                // File is already locked in this thread or virtual machine
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(FileNode.FILENODESIZE);

        LOGGER.info("Successfully initialized byte source.");
        readingAmount += ch.read(buffer);

        if (readingAmount <= 0) {
            MetaKey key = new MetaKey(pRelativePath);
            MetaValue value = new MetaValue(FilelistenerReadTrx.emptyFileKey);
            getBucketTransaction().getMetaBucket().put(key, value);

            return;
        }

        long newKey = getBucketTransaction().incrementNodeKey();

        if (fileExists(pRelativePath)) {
            removeFile(pRelativePath);
        }

        // Setting a new header file node
        MetaKey key = new MetaKey(pRelativePath);
        MetaValue value = new MetaValue(newKey);

        // And adding it to the meta map
        LOGGER.info("Metakeypair setup");
        getBucketTransaction().getMetaBucket().put(key, value);

        // Creating and setting the headernode.
        FileNode headerNode = new FileNode(newKey, new byte[FileNode.FILENODESIZE]);
        headerNode.setHeader(true);
        headerNode.setEof(false);

        headerNode.setVal(buffer.array());

        getBucketTransaction().setNode(headerNode);

        // Creating and setting following nodes based on the file size.
        FileNode node;
        FileNode lastNode;

        int currentReadingAmount = 0;
        while ((currentReadingAmount = ch.read(buffer = ByteBuffer.allocate(FileNode.FILENODESIZE))) > 0) {
            LOGGER.info("" + currentReadingAmount);
            byte[] slice = Arrays.copyOf(buffer.array(), currentReadingAmount);

            node = new FileNode(getBucketTransaction().incrementNodeKey(), slice);
            node.setHeader(false);
            node.setEof(false);

            lastNode = (FileNode)getBucketTransaction().getNode(node.getNodeKey() - 1);
            lastNode.setNextNodeKey(node.getNodeKey());
            getBucketTransaction().setNode(lastNode);
            getBucketTransaction().setNode(node);

            readingAmount += currentReadingAmount;
        }

        ByteArrayDataOutput size = ByteStreams.newDataOutput();
        size.writeInt(readingAmount);

        node = new FileNode(getBucketTransaction().incrementNodeKey(), size.toByteArray());

        node.setHeader(false);
        node.setEof(true);

        lastNode = (FileNode)getBucketTransaction().getNode(node.getNodeKey() - 1);

        lastNode.setNextNodeKey(node.getNodeKey());

        getBucketTransaction().setNode(lastNode);

        getBucketTransaction().setNode(node);

        Preconditions.checkArgument(getBucketTransaction().getNode(newKey) != null);
        lock.release();
        ch.close();

        System.out.println("Done writing.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeFile(String pRelativePath) throws TTException {
        // If the file already exists we just override it
        // and remove the last meta entry since the key won't be correct anymore.
        getBucketTransaction().getMetaBucket().remove(new MetaKey(pRelativePath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkAccessAndCommit();

        // ICommitStrategy uber page.
        getBucketTransaction().commit();
    }

    /**
     * Checking write access and intermediate commit.
     * 
     * @throws TTException
     *             if anything weird happens
     */
    private void checkAccessAndCommit() throws TTException {

        mDelegate.assertNotClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() throws TTException {
        mDelegate.assertNotClosed();

        long revisionToSet = 0;
        revisionToSet = mDelegate.mPageReadTrx.getRevision() - 1;

        getBucketTransaction().close();

        // Reset internal transaction state to last committed uber page.
        mDelegate.setPageTransaction(mSession.beginBucketWtx(revisionToSet));
    }

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    private BucketWriteTrx getBucketTransaction() {

        return (BucketWriteTrx)mDelegate.mPageReadTrx;
    }
}