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
 * $Id: ItemFilterTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.service.xml.xpath.filter;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.IFilterTest;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class ItemFilterTest {


	@Before
	public void setUp() {

		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void testIFilterConvetions() {

		// Build simple test tree.
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveTo(9L);
		IFilterTest.testIFilterConventions(new ItemFilter(wtx), true);

		wtx.moveTo(3L);
		IFilterTest.testIFilterConventions(new ItemFilter(wtx), true);

		wtx.moveTo(2L);
		wtx.moveToAttribute(0);
		IFilterTest.testIFilterConventions(new ItemFilter(wtx), true);

		wtx.abort();
		wtx.close();
		session.close();

	}
}
