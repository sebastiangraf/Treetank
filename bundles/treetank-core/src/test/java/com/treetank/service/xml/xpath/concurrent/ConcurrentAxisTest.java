package com.treetank.service.xml.xpath.concurrent;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.ChildAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.NestedAxis;
import com.treetank.axis.filter.NameFilter;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;

import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.SkipBench;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrentAxisTest {

    /** XML file name to test. */
    private static final String XMLFILE = "10mb.xml";
    /** Path to XML file. */
    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + XMLFILE;
    /** IDatabase instance. */
    private IDatabase mDatabase;
    /** ISession instance. */
    private ISession mSession;
    /** IReadTranscation instance. */
    private IReadTransaction mRtx;

    /**
     * Constructor, just to meet checkstyle requirements.
     */
    public ConcurrentAxisTest() {

    }

    /**
     * Method is called once before each test. It deletes all states, shreds XML file to database and
     * initializes the required variables.
     */
    @BeforeEachRun
    @Before
    public final void setUp() {
        try {
            TestHelper.deleteEverything();
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
            mDatabase = TestHelper.getDatabase(PATHS.PATH1.getFile());
            mSession = mDatabase.getSession();
            mRtx = mSession.beginReadTransaction();
        } catch (final Exception mExe) {
            mExe.printStackTrace();
        }
    }

    /**
     * Test seriell.
     */
    @Ignore
    @SkipBench
    @Bench
    @Test
    public final void testSeriellOld() {
        // final String query = "//people/person[@id=\"person3\"]/name";
        // final String query = "count(//location[text() = \"United States\"])";
        final String query = "//regions/africa//location";
        // final String result = "<name>Limor Simone</name>";
        final int resultNumber = 55;
        AbsAxis axis = null;
        try {
            axis = new XPathAxis(mRtx, query);
        } catch (final TTXPathException ttExp) {
            ttExp.printStackTrace();
        }
        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test seriell.
     */
    @Bench
    @Test
    public final void testSeriellNew() {

        /* query: //regions/africa//location */
        final int resultNumber = 55;
        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                "regions")), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "africa"))),
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "location")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);
    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Test
    @Ignore
    public final void testConcurrent() {
        /* query: //regions/africa//location */
        final int resultNumber = 55;
        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx,
                true), new NameFilter(mRtx, "regions"))), new ConcurrentAxis(mRtx, new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "africa")))), new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "name"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Close all connections.
     */
    @AfterEachRun
    @After
    public final void tearDown() {
        try {
            mRtx.close();
            mSession.close();
            mDatabase.close();
            TestHelper.closeEverything();
        } catch (final Exception mExe) {
            mExe.printStackTrace();
        }

    }

}