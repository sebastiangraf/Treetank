package com.treetank.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.exception.TreetankIOException;
import com.treetank.page.AbstractPage;
import com.treetank.page.NodePage;
import com.treetank.session.SessionConfiguration;

public class TransactionLogCacheTest {

    private final AbstractPage[] pages = new AbstractPage[LRUCache.CACHE_CAPACITY + 1];

    private ICache cache;

    @Before
    public void setUp() {
        try {
            TestHelper.deleteEverything();
            cache = new TransactionLogCache(new SessionConfiguration(
                    ITestConstants.PATH1));
            for (int i = 0; i < pages.length; i++) {
                final NodePage page = new NodePage(i);
                pages[i] = page;
                cache.put(i, page);
            }
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void test() {
        for (int i = 0; i < pages.length; i++) {
            final NodePage page1 = (NodePage) pages[i];
            final NodePage page2 = (NodePage) cache.get(i);
            assertEquals(page1.getNodePageKey(), page2.getNodePageKey());
        }
        cache.clear();
    }

    @Test
    public void tearDown() {
        TestHelper.closeEverything();
    }
}