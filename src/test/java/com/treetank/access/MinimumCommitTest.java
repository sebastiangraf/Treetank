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
 * $Id: MinimumCommitTest.java 4376 2008-08-25 07:27:39Z kramis $
 */

package com.treetank.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.DocumentCreater;

public class MinimumCommitTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void test() throws TreetankException {
        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        ISession session = database.getSession();
        IWriteTransaction wtx = session.beginWriteTransaction();
        assertEquals(0L, wtx.getRevisionNumber());
        wtx.commit();

        wtx.close();
        session.close();

        session = database.getSession();
        wtx = session.beginWriteTransaction();
        assertEquals(1L, wtx.getRevisionNumber());
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        wtx = session.beginWriteTransaction();
        assertEquals(2L, wtx.getRevisionNumber());
        wtx.commit();
        wtx.close();

        IReadTransaction rtx = session.beginReadTransaction();
        assertEquals(2L, rtx.getRevisionNumber());
        rtx.close();
        session.close();
        database.close();

    }

    @Test
    public void testTimestamp() throws TreetankException {
        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        assertEquals(0L, wtx.getRevisionTimestamp());
        wtx.commit();
        wtx.close();

        final IReadTransaction rtx = session.beginReadTransaction();
        assertTrue(rtx.getRevisionTimestamp() < (System.currentTimeMillis() + 1));
        rtx.close();

        session.close();
        database.close();

    }

}
