/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.axis.filter;

import org.treetank.api.INodeReadTransaction;
import org.treetank.node.ENode;
import org.treetank.node.ElementNode;
import org.treetank.node.interfaces.INameNode;
import org.treetank.utils.NamePageHash;

/**
 * <h1>WildcardFilter</h1>
 * <p>
 * Filters ELEMENTS and ATTRIBUTES and supports wildcards either instead of the namespace prefix, or the local
 * name.
 * </p>
 */
public class WildcardFilter extends AbsFilter {

    /** Defines, if the defined part of the qualified name is the local name. */
    private final boolean mIsName;

    /** Name key of the defined name part. */
    private final int mKnownPartKey;

    /** NodeTrans for getting the localname. */
    private final INodeReadTransaction mRtx;

    /**
     * Default constructor.
     * 
     * @param rtx
     *            Transaction to operate on
     * @param mKnownPart
     *            part of the qualified name that is specified. This can be
     *            either the namespace prefix, or the local name
     * @param mIsName
     *            defines, if the specified part is the prefix, or the local
     *            name (true, if it is the local name)
     */
    public WildcardFilter(final INodeReadTransaction rtx, final String mKnownPart, final boolean mIsName) {
        super(rtx);
        this.mIsName = mIsName;
        mKnownPartKey = NamePageHash.generateHashForString(mKnownPart);
        mRtx = rtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean filter() {
        if (getNode().getKind() == ENode.ELEMENT_KIND) {

            if (mIsName) { // local name is given
                final String localname =

                mRtx.nameForKey(((INameNode)getNode()).getNameKey()).replaceFirst(".*:", "");
                final int localnameKey = NamePageHash.generateHashForString(localname);

                return localnameKey == mKnownPartKey;
            } else { // namespace prefix is given
                final int nsCount = ((ElementNode)getNode()).getNamespaceCount();
                for (int i = 0; i < nsCount; i++) {
                    moveTo(((ElementNode)getNode()).getNamespaceKey(i));
                    final int prefixKey = mKnownPartKey;
                    if (((INameNode)getNode()).getNameKey() == prefixKey) {
                        moveTo(getNode().getParentKey());
                        return true;
                    }
                    moveTo(getNode().getParentKey());
                }
            }

        } else if (getNode().getKind() == ENode.ATTRIBUTE_KIND) {
            // supporting attributes here is difficult, because treetank
            // does not provide a way to acces the name and namespace of
            // the current attribute (attribute index is not known here)
            throw new IllegalStateException("Wildcards are not supported in attribute names yet.");
        }

        return false;

    }
}
