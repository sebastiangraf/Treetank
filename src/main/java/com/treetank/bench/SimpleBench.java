/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * Permission to use, copy, modify, and/or distribute this software for non-
 * commercial use with or without fee is hereby granted, provided that the 
 * above copyright notice, the patent notice, and this permission notice
 * appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: Benchmark.java 4378 2008-08-25 07:40:39Z kramis $
 */

package com.treetank.bench;

import java.io.File;

import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.ChildAxis;
import com.treetank.service.xml.XMLShredder;

public class SimpleBench {

    public final static int TASKS = 3;

    public final static String XML_PATH = "src/test/resources/shakespeare.xml";

    public final static String TNK_PATH = "/tmp/tnk/data/shakespeare.tnk";

    public final static byte[] TNK_KEY = null; // "1234567812345678".getBytes();

    public final static boolean TNK_CHECKSUM = false;

    public static void main(final String[] args) {

        try {
            long start = System.currentTimeMillis();
            Database.truncateDatabase(new File(TNK_PATH));
            XMLShredder.main(XML_PATH, TNK_PATH);
            long stop = System.currentTimeMillis();
            System.out.println("Time to shred shakespeare.xml: "
                    + (stop - start) + "[ms]");

            final IDatabase db = Database.openDatabase(new File(TNK_PATH));

            final ISession session = db.getSession();
            final IReadTransaction rtx = session.beginReadTransaction();
            final IAxis axis = new ChildAxis(rtx);
            while (axis.hasNext()) {
                axis.next();
            }
            rtx.close();
            session.close();
            start = System.currentTimeMillis();
            System.out.println("Time to traverse shakespeare.xml: "
                    + (start - stop) + "[ms]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
