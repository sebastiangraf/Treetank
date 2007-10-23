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
 * $Id: AncestorAxisIteratorTest.java 3186 2007-10-23 06:29:47Z kramis $
 */

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class AncestorAxisTest {

  public static final String PATH =
      "generated" + File.separator + "AncestorAxisTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find ancestors starting from nodeKey 0L (root).
    wtx.moveTo(8L);
    final Iterator<INode> axis1 = new AncestorAxis(wtx);
    assertEquals(true, axis1.hasNext());
    assertEquals(7L, axis1.next().getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(1L, axis1.next().getNodeKey());

    assertEquals(false, axis1.hasNext());

    // Find ancestors starting from nodeKey 1L (first child of root).
    wtx.moveTo(3L);
    final Iterator<INode> axis2 = new AncestorAxis(wtx);
    assertEquals(true, axis2.hasNext());
    assertEquals(1L, axis2.next().getNodeKey());

    assertEquals(false, axis2.hasNext());

    // Find ancestors starting from nodeKey 4L (second child of root).
    wtx.moveTo(2L);
    final Iterator<INode> axis3 = new AncestorAxis(wtx);
    assertEquals(true, axis3.hasNext());
    assertEquals(1L, axis3.next().getNodeKey());

    assertEquals(false, axis3.hasNext());

    // Find ancestors starting from nodeKey 5L (last in document order).
    wtx.moveTo(1L);
    final Iterator<INode> axis4 = new AncestorAxis(wtx);
    assertEquals(false, axis4.hasNext());

    wtx.abort();
    session.close();

  }

}