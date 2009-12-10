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
 * $Id: ClassInvocation.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.xml.xpath;

import static org.junit.Assert.fail;

import java.io.File;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Session;
import com.treetank.access.SessionConfiguration;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.utils.TypedValue;

public class ClassInvocation {

    public static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "test.xml";

    public static void main(String[] args) {
        try {
            TestHelper.deleteEverything();
            XMLShredder.shred(XML, new SessionConfiguration(
                    ITestConstants.PATH1));

            // Build simple test tree.
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IReadTransaction rtx = session.beginReadTransaction();
            // rtx.moveTo(17L);

            String query = "fn:count(//b)";

            System.out.println("Query: " + query);
            for (long key : new XPathAxis(rtx, query)) {
                System.out.println(key);
                System.out.println(rtx.nameForKey(rtx.getNode().getNameKey()));
                System.out.println(TypedValue.parseString(rtx.getNode()
                        .getRawValue()));
                System.out.println(rtx.nameForKey(rtx.getNode().getTypeKey())); // will
                // return
                // null
                // for
                // atomic
                // values
                // TODO:
                // adapt
                // ReadTransaction

            }

            rtx.close();
            session.close();
            TestHelper.closeEverything();
        } catch (final TreetankException exc) {
            fail(exc.toString());
        }
    }

}
