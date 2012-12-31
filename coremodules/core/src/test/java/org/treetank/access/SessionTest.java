/**
 * 
 */
package org.treetank.access;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.ModuleFactory;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;

import com.google.inject.Inject;

/**
 * Testcase for Session.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class SessionTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;
    
    private ResourceConfiguration mResource;
    
    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        Properties props =
            StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }
    
    /**
     * Test method for
     * {@link org.treetank.access.Session#Session(org.treetank.access.Database, org.treetank.access.conf.ResourceConfiguration, org.treetank.access.conf.SessionConfiguration)}
     * .
     */
    @Test
    public void testSession() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageReadTransaction(long)}.
     */
    @Test
    public void testBeginPageReadTransaction() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageWriteTransaction()}.
     */
    @Test
    public void testBeginPageWriteTransaction() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageWriteTransaction(long, long)}.
     */
    @Test
    public void testBeginPageWriteTransactionLongLong() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#close()}.
     */
    @Test
    public void testClose() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#assertAccess(long)}.
     */
    @Test
    public void testAssertAccess() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#truncate()}.
     */
    @Test
    public void testTruncate() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.treetank.access.Session#setLastCommittedUberPage(org.treetank.page.UberPage)}.
     */
    @Test
    public void testSetLastCommittedUberPage() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#getMostRecentVersion()}.
     */
    @Test
    public void testGetMostRecentVersion() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#getConfig()}.
     */
    @Test
    public void testGetConfig() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#deregisterPageTrx(org.treetank.api.IPageReadTrx)}.
     */
    @Test
    public void testDeregisterPageTrx() {
//        fail("Not yet implemented");
    }

}
