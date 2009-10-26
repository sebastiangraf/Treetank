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
 * $Id: XMLSerializerTest.java 4376 2008-08-25 07:27:39Z kramis $
 */

package com.treetank.service.xml;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class XMLSerializerTest {

    @Before
    public void setUp() {
        Session.removeSession(ITestConstants.PATH1);
    }

    @Test
    public void testXMLSerializer() {
        try { // Setup session.
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            wtx.close();

            // Generate from this session.
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final IReadTransaction rtx = session.beginReadTransaction();
            final XMLSerializer serializer = new XMLSerializer(rtx, out);
            serializer.run();
            TestCase.assertEquals(DocumentCreater.XML_TANK, out.toString());
            rtx.close();
            session.close();
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }
    }

}
