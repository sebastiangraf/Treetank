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
 * $Id: IntersectAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */
package com.treetank.service.xml.xpath.expr;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.IAxisTest;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 * 
 */
public class IntersectAxisTest {

	public static final String PATH = "target" + File.separator + "tnk"
			+ File.separator + "IntersectAxisTest.tnk";

	@Before
	public void setUp() {

		Session.removeSession(PATH);
	}

	@Test
	public void testIntersect() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(PATH);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		IReadTransaction rtx = session.beginReadTransaction();

		rtx.moveTo(1L);

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"child::node() intersect b"), new long[] { 5L, 9L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"child::node() intersect b intersect child::node()[@p:x]"),
				new long[] { 9L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"child::node() intersect child::node()[attribute::p:x]"),
				new long[] { 9L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"child::node()/parent::node() intersect self::node()"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//node() intersect //text()"), new long[] { 4L, 8L, 13L, 6L,
				12L });

		rtx.moveTo(1L);
		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"b/preceding::node() intersect text()"), new long[] { 4L, 8L });

		rtx.close();
		wtx.abort();
		wtx.close();
		session.close();

	}

}
