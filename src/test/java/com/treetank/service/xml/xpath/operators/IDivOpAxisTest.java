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
 * $Id: IDivOpAxisTest.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.xml.xpath.operators;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Session;
import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.expr.LiteralExpr;
import com.treetank.service.xml.xpath.expr.SequenceAxis;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.types.Type;

public class IDivOpAxisTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public final void testOperate() throws TreetankException {

        final ISession session = Session.beginSession(ITestConstants.PATH1);
        IReadTransaction rtx = session.beginReadTransaction();
        IItem item1 = new AtomicValue(3.0, Type.DOUBLE);
        IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

        IAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
        IAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
        AbstractOpAxis axis = new IDivOpAxis(rtx, op1, op2);

        assertEquals(true, axis.hasNext());
        // note: although getRawValue() returns [1], parseString returns ""
        // assertEquals(1,
        // Integer.parseInt(TypedValue.parseString(rtx.getRawValue())));
        assertEquals(rtx.keyForName("xs:integer"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        rtx.close();
        session.close();
    }

    @Test
    public final void testGetReturnType() throws TreetankException {

        final ISession session = Session.beginSession(ITestConstants.PATH1);
        IReadTransaction rtx = session.beginReadTransaction();

        IAxis op1 = new SequenceAxis(rtx);
        IAxis op2 = new SequenceAxis(rtx);
        AbstractOpAxis axis = new IDivOpAxis(rtx, op1, op2);

        assertEquals(Type.INTEGER, axis.getReturnType(rtx
                .keyForName("xs:double"), rtx.keyForName("xs:double")));
        assertEquals(Type.INTEGER, axis.getReturnType(rtx
                .keyForName("xs:decimal"), rtx.keyForName("xs:double")));
        assertEquals(Type.INTEGER, axis.getReturnType(rtx
                .keyForName("xs:float"), rtx.keyForName("xs:decimal")));
        assertEquals(Type.INTEGER, axis.getReturnType(rtx
                .keyForName("xs:decimal"), rtx.keyForName("xs:integer")));
        // assertEquals(Type.INTEGER,
        // axis.getReturnType(rtx.keyForName("xs:integer"),
        // rtx.keyForName("xs:integer")));

        try {
            axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
                    .keyForName("xs:yearMonthDuration"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(
                    e.getMessage(),
                    is("err:XPTY0004 The type is not appropriate the expression or the "
                            + "typedoes not match a required type as specified by the matching rules."));
        }

        try {

            axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
                    .keyForName("xs:double"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(
                    e.getMessage(),
                    is("err:XPTY0004 The type is not appropriate the expression or the "
                            + "typedoes not match a required type as specified by the matching rules."));
        }

        try {

            axis.getReturnType(rtx.keyForName("xs:string"), rtx
                    .keyForName("xs:yearMonthDuration"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(
                    e.getMessage(),
                    is("err:XPTY0004 The type is not appropriate the expression or the "
                            + "typedoes not match a required type as specified by the matching rules."));
        }

        try {

            axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
                    .keyForName("xs:IDREF"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(
                    e.getMessage(),
                    is("err:XPTY0004 The type is not appropriate the expression or the "
                            + "typedoes not match a required type as specified by the matching rules."));
        }

        rtx.close();
        session.close();
    }

}
