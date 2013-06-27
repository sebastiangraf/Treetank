/**
 * 
 */
package org.treetank.bench;

import java.io.File;
import java.nio.file.FileSystems;

import org.perfidix.Benchmark;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;
import org.treetank.access.Storage;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.DumbMetaEntryFactory;
import org.treetank.bucket.DumbNodeFactory;
import org.treetank.bucket.DumbNodeFactory.DumbNode;
import org.treetank.exception.TTException;
import org.treetank.io.IOUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class UpdateBench {

    private final String RESOURCENAME = "benchResourcegrave9283";

    private final int ELEMENTS = 262144;

    private final IStorage mStorage;
    private final ResourceConfiguration mConfig;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = BenchUtils.createNodes(new int[] {
        ELEMENTS
    })[0];
    private IBucketWriteTrx mTrx;

    public UpdateBench() throws TTException {
        final File storageFile = FileSystems.getDefault().getPath("tmp", "bench").toFile();
        IOUtils.recursiveDelete(storageFile);
        Injector inj =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(DumbNodeFactory.class).setMetaFacClass(
                DumbMetaEntryFactory.class).createModule());

        mConfig =
            inj.getInstance(IResourceConfigurationFactory.class).create(
                StandardSettings.getProps(storageFile.getAbsolutePath(), RESOURCENAME));

        IOUtils.recursiveDelete(storageFile);
        final StorageConfiguration config = new StorageConfiguration(storageFile);
        Storage.createStorage(config);
        mStorage = Storage.openStorage(storageFile);

    }

    private void insert(int numbersToInsert) throws TTException {
        for (int i = 0; i < numbersToInsert; i++) {
            final long nodeKey = mTrx.incrementNodeKey();
            mNodesToInsert[i].setNodeKey(nodeKey);
            mTrx.setNode(mNodesToInsert[i]);
        }
    }

    @BeforeEachRun
    public void setUp() throws TTException {
        mStorage.createResource(mConfig);
        mSession = mStorage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        mTrx = mSession.beginBucketWtx();
        insert(ELEMENTS);
        mTrx.commitBlocked();
    }

    @Bench(runs = 1)
    public void update() throws TTException {
        final int toModify = 1024;
        for (int i = 0; i < toModify; i++) {
            final long keyToAdapt = Math.abs(BenchUtils.random.nextLong()) % ELEMENTS;

            final DumbNode node = BenchUtils.generateOne();
            node.setNodeKey(keyToAdapt);

            mTrx.setNode(node);

        }
        mTrx.commit();
        mTrx.close();
    }

    public static void main(String[] args) {
        Benchmark bench = new Benchmark();
        bench.add(UpdateBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
    }

}
