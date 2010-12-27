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
 * $Id: AbstractOpAxisTest.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.xml.xpath.operators;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.TTException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.expr.LiteralExpr;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AbsOpAxisTest {

    @Before
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }

    @Test
    public final void testHasNext() throws TTException {

        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IReadTransaction rtx = session.beginReadTransaction();
        IItem item1 = new AtomicValue(1.0, Type.DOUBLE);
        IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

        AbsAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
        AbsAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
        AbsObAxis axis = new DivOpAxis(rtx, op1, op2);

        assertEquals(true, axis.hasNext());
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        // here both operands are the empty sequence
        axis = new DivOpAxis(rtx, op1, op2);
        assertEquals(true, axis.hasNext());
        assertThat(Double.NaN, is(TypedValue.parseDouble(rtx.getNode().getRawValue())));
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());
        rtx.close();
        session.close();
        database.close();

    }

}
