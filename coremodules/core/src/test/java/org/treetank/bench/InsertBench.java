package org.treetank.bench;

import java.io.File;
import java.nio.file.FileSystems;

import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;
import org.treetank.CoreTestHelper;
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

public class InsertBench {

    private final String RESOURCENAME = "benchResourcegrave9283";

    private final IStorage mStorage;
    private final ResourceConfiguration mConfig;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = CoreTestHelper.createNodes(new int[] {
        262144
    })[0];
    private IBucketWriteTrx mTrx;

    public InsertBench() throws TTException {
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

    @BeforeEachRun
    public void setUp() throws TTException {
        mStorage.createResource(mConfig);
        mSession = mStorage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        mTrx = mSession.beginBucketWtx();
    }

    @Bench
    public void bench16384() throws TTException {
        insert(16384);
        mTrx.commit();
        insert(16384);
        mTrx.commit();
    }

    @Bench
    public void bench16384Direct() throws TTException {
        insert(16384);
        insert(16384);
        mTrx.commit();
    }

    @Bench
    public void bench32768() throws TTException {
        insert(32768);
        mTrx.commit();
        insert(32768);
        mTrx.commit();
    }

    @Bench
    public void bench32768Direct() throws TTException {
        insert(32768);
        insert(32768);
        mTrx.commit();
    }

    @Bench
    public void bench65536() throws TTException {
        insert(65536);
        mTrx.commit();
        insert(65536);
        mTrx.commit();
    }

    @Bench
    public void bench65536Direct() throws TTException {
        insert(65536);
        insert(65536);
        mTrx.commit();
    }

    private void insert(int numbersToInsert) throws TTException {
        for (int i = 0; i < numbersToInsert; i++) {
            final long nodeKey = mTrx.incrementNodeKey();
            mNodesToInsert[i].setNodeKey(nodeKey);
            mTrx.setNode(mNodesToInsert[i]);
        }
    }

    @AfterEachRun
    public void tearDown() throws TTException {
        mTrx.close();
        mSession.close();
        mStorage.truncateResource(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
    }

    public static void main(String[] args) {
        Benchmark bench = new Benchmark();
        bench.add(InsertBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
    }

}
