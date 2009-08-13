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
 * $Id: InstanceOfExprTest.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the InstanceOfExpr.
 * 
 * @author Tina Scherer
 */
public class InstanceOfExprTest {


	@Before
	public void setUp() {
		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void testInstanceOfExpr() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		wtx.commit();
		IReadTransaction rtx = session.beginReadTransaction();

		final IAxis axis1 = new XPathAxis(rtx, "1 instance of xs:integer");
		assertEquals(true, axis1.hasNext());
		assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
		assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis1.hasNext());

		final IAxis axis2 = new XPathAxis(rtx,
				"\"hallo\" instance of xs:integer");
		assertEquals(true, axis2.hasNext());
		assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
		assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis2.hasNext());

		final IAxis axis3 = new XPathAxis(rtx,
				"\"hallo\" instance of xs:string ?");
		assertEquals(true, axis3.hasNext());
		assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
		assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis3.hasNext());

		final IAxis axis4 = new XPathAxis(rtx,
				"\"hallo\" instance of xs:string +");
		assertEquals(true, axis4.hasNext());
		assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
		assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis4.hasNext());

		final IAxis axis5 = new XPathAxis(rtx,
				"\"hallo\" instance of xs:string *");
		assertEquals(true, axis5.hasNext());
		assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
		assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis5.hasNext());

		rtx.close();
		wtx.close();
		session.close();

	}

}
