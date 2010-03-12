package com.treetank;

import java.io.File;
import java.util.Random;

import javax.xml.stream.XMLStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.ENodes;

public final class OverallTest {

    private static int NUM_CHARS = 3;
    private static int ELEMENTS = 1000;
    private static int COMMITPERCENTAGE = 20;
    private static int REMOVEPERCENTAGE = 20;
    private static final Random ran = new Random(0l);
    public static String chars = "abcdefghijklm";

    private static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "auction.xml";

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    public void testXML() throws Exception {

        for (int i = 0; i < Integer
                .parseInt(EDatabaseSetting.REVISION_TO_RESTORE
                        .getStandardProperty()) * 2; i++) {
            final IDatabase database = Database
                    .openDatabase(ITestConstants.PATH1);
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            if (wtx.moveToFirstChild()) {
                wtx.remove();
                wtx.commit();
            } else {
                wtx.abort();
            }

            final XMLStreamReader reader = XMLShredder.createReader(new File(
                    XML));
            final XMLShredder shredder = new XMLShredder(wtx, reader, true);
            shredder.call();

            wtx.close();
            session.close();
            database.close();

        }
    }

    @Test
    public void testBullshitInsert() throws TreetankException {
        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.insertElementAsFirstChild(getString(), "");
        for (int i = 0; i < ELEMENTS; i++) {
            if (ran.nextBoolean()) {
                wtx.insertElementAsFirstChild(getString(), "");

            } else {
                wtx.insertElementAsRightSibling(getString(), "");
            }
            // while (ran.nextBoolean()) {
            // wtx.insertAttribute(getString(), getString(), getString());
            // wtx.moveToParent();
            // }
            // while (ran.nextBoolean()) {
            // wtx.insertNamespace(getString(), getString());
            // wtx.moveToParent();
            // }
            if (ran.nextInt(100) < COMMITPERCENTAGE) {
                wtx.commit();
            }
            wtx.moveTo(ran.nextInt(i + 1) + 1);
            // TODO Check if reference check can occur on "=="
            if (wtx.getNode().getKind() != ENodes.ELEMENT_KIND) {
                wtx.moveToParent();
            }
        }
        final long key = wtx.getNode().getNodeKey();
        wtx.remove();
        wtx.insertElementAsFirstChild(getString(), "");
        wtx.moveTo(key);
        wtx.commit();
        wtx.close();
        session.close();
    }

    @Test
    public void testBullshitWithRemove() throws TreetankException {
        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.insertElementAsFirstChild(getString(), "");
        for (int i = 0; i < ELEMENTS; i++) {
            if (ran.nextBoolean()) {
                wtx.insertElementAsFirstChild(getString(), "");
            } else {
                wtx.insertElementAsRightSibling(getString(), "");
            }
            while (ran.nextBoolean()) {
                wtx.insertAttribute(getString(), getString(), getString());
                wtx.moveToParent();
            }
            while (ran.nextBoolean()) {
                wtx.insertNamespace(getString(), getString());
                wtx.moveToParent();
            }

            if (ran.nextInt(100) < REMOVEPERCENTAGE) {
                wtx.remove();
            }

            if (ran.nextInt(100) < COMMITPERCENTAGE) {
                wtx.commit();
            }
            do {
                wtx.moveTo(ran.nextInt(i + 1) + 1);
            } while (wtx.getNode() == null);
            // TODO Check if reference check can occur on "=="
            if (wtx.getNode().getKind() != ENodes.ELEMENT_KIND) {
                wtx.moveToParent();
            }
        }
        final long key = wtx.getNode().getNodeKey();
        wtx.remove();
        wtx.insertElementAsFirstChild(getString(), "");
        wtx.moveTo(key);
        wtx.commit();
        wtx.close();
        session.close();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    private static String getString() {
        char[] buf = new char[NUM_CHARS];

        for (int i = 0; i < buf.length; i++) {
            buf[i] = chars.charAt(ran.nextInt(chars.length()));
        }

        return new String(buf);
    }

}
