/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.TestDocument;

public class XMLShredderTest {

  public static final String XML = "xml" + File.separator + "test.xml";

  public static final String PATH =
      "generated" + File.separator + "XMLShredderTest.tnk";

  public static final String EXPECTED_PATH =
      "generated" + File.separator + "ExpectedXMLShredderTest.tnk";

  @BeforeClass
  public static void setUp() {
    Session.removeSession(PATH);
    Session.removeSession(EXPECTED_PATH);
  }

  @Test
  public void testSTAXShredder() throws IOException, XMLStreamException {

    // Setup expected session.
    final ISession expectedSession = Session.beginSession(EXPECTED_PATH);
    final IWriteTransaction expectedTrx =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedTrx);
    expectedTrx.commit();

    // Setup parsed session.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();
    final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);
    final Iterator<Long> descendants = new DescendantAxis(rtx);

    assertEquals(expectedTrx.getRevisionSize(), rtx.getRevisionSize());
    while (expectedDescendants.hasNext() && descendants.hasNext()) {
      assertEquals(expectedTrx.getNodeKey(), rtx.getNodeKey());
      assertEquals(expectedTrx.getParentKey(), rtx.getParentKey());
      assertEquals(expectedTrx.getFirstChildKey(), rtx.getFirstChildKey());
      assertEquals(expectedTrx.getLeftSiblingKey(), rtx.getLeftSiblingKey());
      assertEquals(expectedTrx.getRightSiblingKey(), rtx.getRightSiblingKey());
      assertEquals(expectedTrx.getChildCount(), rtx.getChildCount());
      assertEquals(expectedTrx.getKind(), rtx.getKind());
      assertEquals(expectedTrx.nameForKey(expectedTrx.getLocalPartKey()), rtx
          .nameForKey(rtx.getLocalPartKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getURIKey()), rtx
          .nameForKey(rtx.getURIKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getPrefixKey()), rtx
          .nameForKey(rtx.getPrefixKey()));
      if (expectedTrx.isText()) {
        assertEquals(new String(
            expectedTrx.getValue(),
            IConstants.DEFAULT_ENCODING), new String(
            rtx.getValue(),
            IConstants.DEFAULT_ENCODING));
      }
    }

    expectedTrx.close();
    expectedSession.close();
    rtx.close();
    session.close();

  }

  @Test
  public void testShredIntoExisting() throws IOException, XMLStreamException {
    try {
      XMLShredder.shred(XML, new SessionConfiguration(PATH));
      TestCase.fail();
    } catch (Exception e) {
      // Must fail.
    }
  }

}
