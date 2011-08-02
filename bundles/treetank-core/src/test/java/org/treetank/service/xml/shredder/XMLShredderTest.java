/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.shredder;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.custommonkey.xmlunit.XMLTestCase;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.IConstants;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XMLShredderTest extends XMLTestCase {

    public static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test.xml";

    public static final String XML2 = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test2.xml";

    public static final String XML3 = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test3.xml";

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        holder = Holder.generate();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testSTAXShredder() throws Exception {

        // Setup parsed session.
        XMLShredder.main(XML, PATHS.PATH2.getFile().getAbsolutePath());
        final IReadTransaction expectedTrx = holder.rtx;

        // Verify.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session = database2.getSession(new SessionConfiguration.Builder());
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToDocumentRoot();
        final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);
        final Iterator<Long> descendants = new DescendantAxis(rtx);

        while (expectedDescendants.hasNext() && descendants.hasNext()) {
            assertEquals(expectedTrx.getNode().getNodeKey(), rtx.getNode().getNodeKey());
            assertEquals(expectedTrx.getNode().getParentKey(), rtx.getNode().getParentKey());
            assertEquals(((AbsStructNode)expectedTrx.getNode()).getFirstChildKey(), ((AbsStructNode)rtx
                .getNode()).getFirstChildKey());
            assertEquals(((AbsStructNode)expectedTrx.getNode()).getLeftSiblingKey(), ((AbsStructNode)rtx
                .getNode()).getLeftSiblingKey());
            assertEquals(((AbsStructNode)expectedTrx.getNode()).getRightSiblingKey(), ((AbsStructNode)rtx
                .getNode()).getRightSiblingKey());

            if (expectedTrx.getNode().getKind() == ENodes.ELEMENT_KIND
                || rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                assertEquals(((ElementNode)expectedTrx.getNode()).getChildCount(), ((ElementNode)rtx
                    .getNode()).getChildCount());
                assertEquals(((ElementNode)expectedTrx.getNode()).getAttributeCount(), ((ElementNode)rtx
                    .getNode()).getAttributeCount());
                assertEquals(((ElementNode)expectedTrx.getNode()).getNamespaceCount(), ((ElementNode)rtx
                    .getNode()).getNamespaceCount());
            }
            assertEquals(expectedTrx.getNode().getKind(), rtx.getNode().getKind());
            assertEquals(expectedTrx.nameForKey(expectedTrx.getNode().getNameKey()), rtx.nameForKey(rtx
                .getNode().getNameKey()));
            assertEquals(expectedTrx.nameForKey(expectedTrx.getNode().getURIKey()), rtx.nameForKey(rtx
                .getNode().getURIKey()));
            if (expectedTrx.getNode().getKind() == ENodes.TEXT_KIND
                || rtx.getNode().getKind() == ENodes.TEXT_KIND) {
                assertEquals(new String(expectedTrx.getNode().getRawValue(), IConstants.DEFAULT_ENCODING),
                    new String(rtx.getNode().getRawValue(), IConstants.DEFAULT_ENCODING));
            }
        }

        rtx.close();
        session.close();
    }

    @Test
    public void testShredIntoExisting() throws Exception {

        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        assertEquals(1, wtx.getRevisionNumber());
        wtx.moveToDocumentRoot();
        wtx.moveToFirstChild();
        final XMLShredder shredder2 =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML)), EShredderInsert.ADDASRIGHTSIBLING);
        shredder2.call();
        assertEquals(2, wtx.getRevisionNumber());
        wtx.close();

        // Setup expected session.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession expectedSession = database2.getSession(new SessionConfiguration.Builder());

        final IWriteTransaction expectedTrx = expectedSession.beginWriteTransaction();
        DocumentCreater.create(expectedTrx);
        expectedTrx.commit();
        expectedTrx.moveToDocumentRoot();

        // Verify.
        final IReadTransaction rtx = holder.session.beginReadTransaction();

        final Iterator<Long> descendants = new DescendantAxis(rtx);
        final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);

        while (expectedDescendants.hasNext()) {
            expectedDescendants.next();
            descendants.hasNext();
            descendants.next();
            assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
        }

        expectedTrx.moveToDocumentRoot();
        final Iterator<Long> expectedDescendants2 = new DescendantAxis(expectedTrx);
        while (expectedDescendants2.hasNext()) {
            expectedDescendants2.next();
            descendants.hasNext();
            descendants.next();
            assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
        }

        expectedTrx.close();
        expectedSession.close();
        rtx.close();
    }

    @Test
    public void testAttributesNSPrefix() throws Exception {
        // Setup expected session.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession expectedSession2 = database.getSession(new SessionConfiguration.Builder());
        final IWriteTransaction expectedTrx2 = expectedSession2.beginWriteTransaction();
        DocumentCreater.createWithoutNamespace(expectedTrx2);
        expectedTrx2.commit();

        // Setup parsed session.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session2 = database2.getSession(new SessionConfiguration.Builder());
        final IWriteTransaction wtx = session2.beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML2)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.commit();
        wtx.close();

        // Verify.
        final IReadTransaction rtx = session2.beginReadTransaction();
        rtx.moveToDocumentRoot();
        final Iterator<Long> expectedAttributes = new DescendantAxis(expectedTrx2);
        final Iterator<Long> attributes = new DescendantAxis(rtx);

        while (expectedAttributes.hasNext() && attributes.hasNext()) {
            if (expectedTrx2.getNode().getKind() == ENodes.ELEMENT_KIND
                || rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                assertEquals(((ElementNode)expectedTrx2.getNode()).getNamespaceCount(), ((ElementNode)rtx
                    .getNode()).getNamespaceCount());
                assertEquals(((ElementNode)expectedTrx2.getNode()).getAttributeCount(), ((ElementNode)rtx
                    .getNode()).getAttributeCount());
                for (int i = 0; i < ((ElementNode)expectedTrx2.getNode()).getAttributeCount(); i++) {
                    assertEquals(expectedTrx2.nameForKey(expectedTrx2.getNode().getNameKey()), rtx
                        .nameForKey(rtx.getNode().getNameKey()));
                    assertEquals(expectedTrx2.getNode().getNameKey(), rtx.getNode().getNameKey());
                    assertEquals(expectedTrx2.nameForKey(expectedTrx2.getNode().getURIKey()), rtx
                        .nameForKey(rtx.getNode().getURIKey()));

                }
            }
        }

        assertEquals(expectedAttributes.hasNext(), attributes.hasNext());

        expectedTrx2.close();
        expectedSession2.close();
        rtx.close();
        session2.close();
    }

    @Test
    public void testShreddingLargeText() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder());
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML3)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();

        final IReadTransaction rtx = session.beginReadTransaction();
        assertTrue(rtx.moveToFirstChild());
        assertTrue(rtx.moveToFirstChild());

        final StringBuilder tnkBuilder = new StringBuilder();
        do {
            tnkBuilder.append(TypedValue.parseString(rtx.getNode().getRawValue()));
        } while (rtx.moveToRightSibling());

        final String tnkString = tnkBuilder.toString();

        rtx.close();
        session.close();

        final XMLEventReader validater = XMLShredder.createReader(new File(XML3));
        final StringBuilder xmlBuilder = new StringBuilder();
        while (validater.hasNext()) {
            final XMLEvent event = validater.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.CHARACTERS:
                final String text = ((Characters)event).getData().trim();
                if (text.length() > 0) {
                    xmlBuilder.append(text);
                }
                break;
            }
        }

        assertEquals(xmlBuilder.toString(), tnkString);
    }
}
