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
 * $Id: AddOpAxisTest.java 4433 2008-08-28 14:26:02Z scherer $
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
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.expr.LiteralExpr;
import com.treetank.service.xml.xpath.expr.SequenceAxis;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AddOpAxisTest {

    @Before
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }

    @Test
    public final void testOperate() throws TTException {

        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IReadTransaction rtx = session.beginReadTransaction();
        IItem item1 = new AtomicValue(1.0, Type.DOUBLE);
        IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

        AbsAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
        AbsAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
        AbsObAxis axis = new AddOpAxis(rtx, op1, op2);

        assertEquals(true, axis.hasNext());
        assertThat(3.0, is(TypedValue.parseDouble(rtx.getNode().getRawValue())));
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        rtx.close();
        session.close();
        database.close();
    }

    @Test
    public final void testGetReturnType() throws TTException {

        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IReadTransaction rtx = session.beginReadTransaction();

        AbsAxis op1 = new SequenceAxis(rtx);
        AbsAxis op2 = new SequenceAxis(rtx);
        AbsObAxis axis = new AddOpAxis(rtx, op1, op2);

        assertEquals(Type.DOUBLE, axis
            .getReturnType(rtx.keyForName("xs:double"), rtx.keyForName("xs:double")));
        assertEquals(Type.DOUBLE, axis.getReturnType(rtx.keyForName("xs:decimal"), rtx
            .keyForName("xs:double")));
        assertEquals(Type.FLOAT, axis.getReturnType(rtx.keyForName("xs:float"), rtx.keyForName("xs:decimal")));
        assertEquals(Type.DECIMAL, axis.getReturnType(rtx.keyForName("xs:decimal"), rtx
            .keyForName("xs:integer")));
        // assertEquals(Type.INTEGER,
        // axis.getReturnType(rtx.keyForName("xs:integer"),
        // rtx.keyForName("xs:integer")));

        assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(rtx.keyForName("xs:yearMonthDuration"), rtx
            .keyForName("xs:yearMonthDuration")));
        assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(rtx.keyForName("xs:dayTimeDuration"), rtx
            .keyForName("xs:dayTimeDuration")));

        assertEquals(Type.DATE, axis.getReturnType(rtx.keyForName("xs:date"), rtx
            .keyForName("xs:yearMonthDuration")));
        assertEquals(Type.DATE, axis.getReturnType(rtx.keyForName("xs:date"), rtx
            .keyForName("xs:dayTimeDuration")));
        assertEquals(Type.TIME, axis.getReturnType(rtx.keyForName("xs:time"), rtx
            .keyForName("xs:dayTimeDuration")));
        assertEquals(Type.DATE_TIME, axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
            .keyForName("xs:yearMonthDuration")));
        assertEquals(Type.DATE_TIME, axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
            .keyForName("xs:dayTimeDuration")));

        try {
            axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx.keyForName("xs:dateTime"));
            fail("Expected an XPathError-Exception.");
        } catch (final TTXPathException e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules. "));
        }

        try {
            axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx.keyForName("xs:double"));
            fail("Expected an XPathError-Exception.");
        } catch (final TTXPathException e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules. "));
        }

        try {
            axis.getReturnType(rtx.keyForName("xs:string"), rtx.keyForName("xs:yearMonthDuration"));
            fail("Expected an XPathError-Exception.");
        } catch (final TTXPathException e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules. "));
        }

        try {

            axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx.keyForName("xs:IDREF"));
            fail("Expected an XPathError-Exception.");
        } catch (final TTXPathException e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules. "));
        }

        rtx.close();
        session.close();
        database.close();

    }
}
