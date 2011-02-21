/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: CompAxisTest.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.xml.xpath.expr;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit-test class to test the functionality of the CompAxis.
 * 
 * @author Tina Scherer
 */
public class CompAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testComp() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveToDocumentRoot();

        final AbsAxis axis1 = new XPathAxis(rtx, "1.0 = 1.0");
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new XPathAxis(rtx, "(1, 2, 3) < (2, 3)");
        assertEquals(true, axis2.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis2.hasNext());

        final AbsAxis axis3 = new XPathAxis(rtx, "(1, 2, 3) > (3, 4)");
        assertEquals(true, axis3.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis3.hasNext());

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}
