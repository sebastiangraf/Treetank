package com.treetank.service.xml.serialize;

import java.io.OutputStream;

import com.treetank.api.IAxis;
import com.treetank.api.ISession;

/**
 * Central class for building up serializers.
 * 
 * Note that not every Implementation (denoted by the internal subclasses) makes
 * use of all parameters.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class SerializerBuilder {

    final IAxis mAxis;

    /**
     * Intermediate boolean for intendtion, not necessary
     */
    boolean mIntent = false;

    /**
     * Intermediate boolean for rest serialization, not necessary
     */
    boolean mREST = false;

    /**
     * Intermediate boolean for rest serialization, not necessary
     */
    boolean mDeclaration = true;

    /**
     * Intermediate boolean for ids, not necessary
     */
    boolean mID = false;

    /**
     * Constructor for the builder;
     * 
     * @param rtx
     * @param mStream
     */
    public SerializerBuilder(final IAxis paramAxis) {
        this.mAxis = paramAxis;
    }

    /**
     * Setting the intention.
     * 
     * @param paramIntent
     *            to set
     */
    public void setIntend(boolean paramIntent) {
        this.mIntent = paramIntent;
    }

    /**
     * Setting the RESTful output
     * 
     * @param paramREST
     *            to set
     */
    public void setREST(boolean paramREST) {
        this.mREST = paramREST;
    }

    /**
     * Setting the declaration
     * 
     * @param paramDeclaration
     *            to set
     */
    public void setDeclaration(boolean paramDeclaration) {
        this.mDeclaration = paramDeclaration;
    }

    /**
     * Setting the ids on nodes
     * 
     * @param paramID
     *            to set
     */
    public void setID(boolean paramID) {
        this.mID = paramID;
    }

    /**
     * Abstract method to build a given serializer
     * 
     * @return the specific implementation of the {@link AbsSerializer}
     */
    public abstract AbsSerializer build();

    public static class XMLSerializerBuilder extends SerializerBuilder {

        private final OutputStream mStream;

        public XMLSerializerBuilder(final IAxis paramAxis,
                final OutputStream paramStream) {
            super(paramAxis);
            this.mStream = paramStream;
        }

        @Override
        public XMLSerializer build() {
            return new XMLSerializer(mAxis, this);
        }

        public OutputStream getStream() {
            return mStream;
        }

    }

    public static class StAXSerializerBuilder extends SerializerBuilder {

        public StAXSerializerBuilder(final IAxis paramAxis) {
            super(paramAxis);
        }

        @Override
        public StAXSerializer build() {
            return new StAXSerializer(mAxis, this);
        }
    }

    public static class SAXSerializerBuilder extends SerializerBuilder {

        public SAXSerializerBuilder(final IAxis paramAxis) {
            super(paramAxis);
        }

        @Override
        public SAXSerializer build() {
            return new SAXSerializer(mAxis, this);
        }
    }

    public static class RevisionedXMLSerializerBuilder extends
            XMLSerializerBuilder {

        private final long[] mVersions;

        private boolean mTimestamp = false;

        private final ISession mSession;

        public RevisionedXMLSerializerBuilder(final ISession paramSession,
                final OutputStream paramStream, final long... paramVersions) {
            super(null, paramStream);
            this.mSession = paramSession;
            this.mVersions = paramVersions;
        }

        public long[] getVersions() {
            return mVersions;
        }

        public void setTimestamp(boolean mTimestamp) {
            this.mTimestamp = mTimestamp;
        }

        public boolean isTimestamp() {
            return mTimestamp;
        }

        @Override
        public RevisionedXMLSerializer build() {
            return new RevisionedXMLSerializer(mSession, this);
        }
    }

}