/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.service.xml.serialize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.settings.ECharsForSerializing;
import com.treetank.utils.IConstants;
import com.treetank.utils.LogWrapper;

import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_ID;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT_SPACES;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_REST;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_XMLDECL;

/**
 * <h1>XMLSerializer</h1>
 * 
 * <p>
 * Most efficient way to serialize a subtree into an OutputStream. The encoding always is UTF-8. Note that the
 * OutputStream internally is wrapped by a BufferedOutputStream. There is no need to buffer it again outside
 * of this class.
 * </p>
 */
public final class XMLSerializer extends AbsSerializer {

    /** 
     * Logger for determining the log level. 
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLSerializer.class);

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** Offset that must be added to digit to make it ASCII. */
    private static final int ASCII_OFFSET = 48;

    /** Precalculated powers of each available long digit. */
    private static final long[] LONG_POWERS =
        {
            1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L,
            10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
            1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L
        };

    /** OutputStream to write to. */
    private final OutputStream mOut;

    /** Indent output. */
    private final boolean mIndent;

    /** Serialize XML declaration. */
    private final boolean mSerializeXMLDeclaration;

    /** Serialize rest header and closer and rest:id. */
    private final boolean mSerializeRest;

    /** Serialize id.*/
    private final boolean mSerializeId;

    /** Number of spaces to indent. */
    private final int mIndentSpaces;

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read.
     */
    private XMLSerializer(final ISession session, final long nodeKey, final XMLSerializerBuilder builder,
        final long... versions) {
        super(session, nodeKey, versions);
        mOut = new BufferedOutputStream(builder.mStream, 4096);
        mIndent = builder.mIndent;
        mSerializeXMLDeclaration = builder.mDeclaration;
        mSerializeRest = builder.mREST;
        mSerializeId = builder.mID;
        mIndentSpaces = builder.mIndentSpaces;
    }

    /**
     * Emit node (start element or characters).
     * 
     * @throws IOException
     */
    @Override
    protected void emitEndElement(final IReadTransaction rtx) {
        try {
            switch (rtx.getNode().getKind()) {
            case ROOT_KIND:
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            case ELEMENT_KIND:
                // Emit start element.
                indent();
                mOut.write(ECharsForSerializing.OPEN.getBytes());
                mOut.write(rtx.rawNameForKey(rtx.getNode().getNameKey()));
                final long key = rtx.getNode().getNodeKey();
                // Emit namespace declarations.
                for (int index = 0, length = ((ElementNode)rtx.getNode()).getNamespaceCount(); index < length; index++) {
                    rtx.moveToNamespace(index);
                    if (rtx.nameForKey(rtx.getNode().getNameKey()).length() == 0) {
                        mOut.write(ECharsForSerializing.XMLNS.getBytes());
                        write(rtx.nameForKey(rtx.getNode().getURIKey()));
                        mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    } else {
                        mOut.write(ECharsForSerializing.XMLNS_COLON.getBytes());
                        write(rtx.nameForKey(rtx.getNode().getNameKey()));
                        mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                        write(rtx.nameForKey(rtx.getNode().getURIKey()));
                        mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    }
                    rtx.moveTo(key);
                }
                // Emit attributes.
                // Add virtual rest:id attribute.
                if (mSerializeId) {
                    if (mSerializeRest) {
                        mOut.write(ECharsForSerializing.REST_PREFIX.getBytes());
                    } else {
                        mOut.write(ECharsForSerializing.SPACE.getBytes());
                    }
                    mOut.write(ECharsForSerializing.ID.getBytes());
                    mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                    write(rtx.getNode().getNodeKey());
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                }

                // Iterate over all persistent attributes.
                for (int index = 0; index < ((ElementNode)rtx.getNode()).getAttributeCount(); index++) {
                    long nodeKey = rtx.getNode().getNodeKey();
                    rtx.moveToAttribute(index);
                    if (rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                        System.out.println(nodeKey);
                        System.out.println(rtx.getNode().getNodeKey());
                    }
                    mOut.write(ECharsForSerializing.SPACE.getBytes());
                    mOut.write(rtx.rawNameForKey(rtx.getNode().getNameKey()));
                    mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                    mOut.write(rtx.getNode().getRawValue());
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    rtx.moveTo(key);
                }
                if (((AbsStructNode)rtx.getNode()).hasFirstChild()) {
                    mOut.write(ECharsForSerializing.CLOSE.getBytes());
                } else {
                    mOut.write(ECharsForSerializing.SLASH_CLOSE.getBytes());
                }
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            case TEXT_KIND:
                indent();
                mOut.write(rtx.getNode().getRawValue());
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    /**
     * Emit end element.
     * 
     * @throws IOException
     */
    @Override
    protected void emitStartElement(final IReadTransaction rtx) {
        try {
            indent();
            mOut.write(ECharsForSerializing.OPEN_SLASH.getBytes());
            mOut.write(rtx.rawNameForKey(rtx.getNode().getNameKey()));
            mOut.write(ECharsForSerializing.CLOSE.getBytes());
            if (mIndent) {
                mOut.write(ECharsForSerializing.NEWLINE.getBytes());
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    @Override
    protected void emitStartDocument() {
        try {
            if (mSerializeXMLDeclaration) {
                write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            }
            if (mSerializeRest) {
                write("<rest:sequence xmlns:rest=\"REST\"><rest:item>");
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    @Override
    protected void emitEndDocument() {
        try {
            if (mSerializeRest) {
                write("</rest:item></rest:sequence>");
            }
            mOut.flush();
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }

    }

    @Override
    protected void emitStartManualElement(final long version) {
        try {
            write("<tt revision=\"");
            write(Long.toString(version));
            write("\">");
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }

    }

    @Override
    protected void emitEndManualElement(final long version) {
        try {
            write("</tt>");
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    /**
     * Indentation of output.
     * 
     * @throws IOException
     */
    private void indent() throws IOException {
        if (mIndent) {
            for (int i = 0; i < mStack.size() * mIndentSpaces; i++) {
                mOut.write(" ".getBytes());
            }
        }
    }

    /**
     * Write characters of string.
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    protected void write(final String string) throws UnsupportedEncodingException, IOException {
        mOut.write(string.getBytes(IConstants.DEFAULT_ENCODING));
    }

    /**
     * Write non-negative non-zero long as UTF-8 bytes.
     * 
     * @throws IOException
     */
    private void write(final long value) throws IOException {
        final int length = (int)Math.log10((double)value);
        int digit = 0;
        long remainder = value;
        for (int i = length; i >= 0; i--) {
            digit = (byte)(remainder / LONG_POWERS[i]);
            mOut.write((byte)(digit + ASCII_OFFSET));
            remainder -= digit * LONG_POWERS[i];
        }
    }

    /**
     * Main method.
     * 
     * @param args
     *            args[0] specifies the input-TT file/folder; args[1] specifies
     *            the output XML file.
     * @throws Exception
     *             Any exception.
     */
    public static void main(String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: XMLSerializer input-TT output.xml");
            System.exit(1);
        }

        System.out.print("Serializing '" + args[0] + "' to '" + args[1] + "' ... ");
        long time = System.currentTimeMillis();
        final File target = new File(args[1]);
        target.delete();
        final FileOutputStream outputStream = new FileOutputStream(target);

        final IDatabase db = Database.openDatabase(new File(args[0]));
        final ISession session = db.getSession();

        final XMLSerializer serializer = new XMLSerializerBuilder(session, outputStream).build();
        serializer.call();

        session.close();
        db.close();
        outputStream.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }

    public static class XMLSerializerBuilder {
        /**
         * Intermediate boolean for indendation, not necessary.
         */
        private transient boolean mIndent;

        /**
         * Intermediate boolean for rest serialization, not necessary.
         */
        private transient boolean mREST;

        /**
         * Intermediate boolean for XML-Decl serialization, not necessary.
         */
        private transient boolean mDeclaration = true;

        /**
         * Intermediate boolean for ids, not necessary.
         */
        private transient boolean mID;

        /**
         * Intermediate number of spaces to indent, not necessary.
         */
        private transient int mIndentSpaces = 2;

        /** Stream to pipe to. */
        private final transient OutputStream mStream;

        /** Session to use. */
        private final transient ISession mSession;

        /** Versions to use. */
        private final transient long[] mVersions;

        /** Node key of subtree to shredder. */
        private final transient long mNodeKey;

        /**
         * Constructor, setting the necessary stuff.
         * 
         * @param paramStream
         */
        public XMLSerializerBuilder(final ISession session, final OutputStream paramStream,
            final long... versions) {
            mNodeKey = 0;
            mStream = paramStream;
            mSession = session;
            mVersions = versions;
        }

        /**
         * Constructor.
         * 
         * @param session
         *            {@link ISession}.
         * @param nodeKey
         *            Root node key of subtree to shredder.
         * @param paramStream
         *            {@link OutputStream}.
         * @param properties
         *            {@link XMLSerializerProperties}.
         * @param versions
         *            Versions to serialize.
         */
        public XMLSerializerBuilder(final ISession session, final long nodeKey,
            final OutputStream paramStream, final XMLSerializerProperties properties, final long... versions) {
            mSession = session;
            mNodeKey = nodeKey;
            mStream = paramStream;
            mVersions = versions;
            final ConcurrentMap<?, ?> map = properties.getmProps();
            mIndent = (Boolean)map.get(S_INDENT[0]);
            mREST = (Boolean)map.get(S_REST[0]);
            mID = (Boolean)map.get(S_ID[0]);
            mIndentSpaces = (Integer)map.get(S_INDENT_SPACES[0]);
            mDeclaration = (Boolean)map.get(S_XMLDECL[0]);
        }

        /**
         * Setting the indention.
         * 
         * @param paramIndent
         *            to set
         */
        public void setIndend(final boolean paramIndent) {
            this.mIndent = paramIndent;
        }

        /**
         * Setting the RESTful output.
         * 
         * @param paramREST
         *            to set
         */
        public void setREST(boolean paramREST) {
            this.mREST = paramREST;
        }

        /**
         * Setting the declaration.
         * 
         * @param paramDeclaration
         *            to set
         */
        public void setDeclaration(boolean paramDeclaration) {
            this.mDeclaration = paramDeclaration;
        }

        /**
         * Setting the ids on nodes.
         * 
         * @param paramID
         *            to set
         */
        public void setID(boolean paramID) {
            this.mID = paramID;
        }

        /**
         * Building new Serializer.
         * 
         * @return a new instance
         */
        public XMLSerializer build() {
            return new XMLSerializer(mSession, mNodeKey, this, mVersions);
        }
    }

}
