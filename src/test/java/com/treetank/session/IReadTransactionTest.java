/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: IReadTransactionTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.utils.DocumentCreater;

public class IReadTransactionTest {

    private static ISession session;

    @BeforeClass
    public static void setUp() {
        try {
            Session.removeSession(ITestConstants.PATH1);
            session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            wtx.close();
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }
    }

    @AfterClass
    public static void tearDown() {
        session.close();
    }

    @Test
    public void testDocumentRoot() {
        final IReadTransaction rtx = session.beginReadTransaction();

        assertEquals(true, rtx.moveToDocumentRoot());
        assertEquals(true, rtx.getNode().isDocumentRoot());
        assertEquals(false, rtx.getNode().hasParent());
        assertEquals(false, rtx.getNode().hasLeftSibling());
        assertEquals(false, rtx.getNode().hasRightSibling());
        assertEquals(true, rtx.getNode().hasFirstChild());

        rtx.close();
    }

    @Test
    public void testConventions() {
        final IReadTransaction rtx = session.beginReadTransaction();

        // IReadTransaction Convention 1.
        assertEquals(true, rtx.moveToDocumentRoot());
        long key = rtx.getNode().getNodeKey();

        // IReadTransaction Convention 2.
        assertEquals(rtx.getNode().hasParent(), rtx.moveToParent());
        assertEquals(key, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasFirstChild(), rtx.moveToFirstChild());
        assertEquals(1L, rtx.getNode().getNodeKey());

        assertEquals(false, rtx.moveTo(Integer.MAX_VALUE));
        assertEquals(false, rtx.moveTo(Integer.MIN_VALUE));
        assertEquals(1L, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasRightSibling(), rtx.moveToRightSibling());
        assertEquals(1L, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasFirstChild(), rtx.moveToFirstChild());
        assertEquals(4L, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasRightSibling(), rtx.moveToRightSibling());
        assertEquals(5L, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasLeftSibling(), rtx.moveToLeftSibling());
        assertEquals(4L, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasParent(), rtx.moveToParent());
        assertEquals(1L, rtx.getNode().getNodeKey());

        rtx.close();
    }

}
