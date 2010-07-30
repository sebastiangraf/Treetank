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

package com.treetank.io;

import com.treetank.io.berkeley.BerkeleyKey;
import com.treetank.io.file.FileKey;

/**
 * Factory to build the key out of a fixed source.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class KeyPersistenter {

    /** Constant to define the file-keys. */
    private static final int FILEKIND = 1;
    /** Constant to define the berkeley-keys. */
    private static final int BERKELEYKIND = 2;
    /** Constant to define that no key is stored. */
    private static final int NULLKIND = 3;

    private KeyPersistenter() {
        // method to prohibit instantiation
    }

    /**
     * Simple create-method.
     * 
     * @param mSource
     *            the input from the storage
     * @return the Key.
     */
    public static AbstractKey createKey(final ITTSource mSource) {
        final int kind = mSource.readInt();
        AbstractKey returnVal = null;
        switch (kind) {
        case FILEKIND:
            returnVal = new FileKey(mSource);
            break;
        case BERKELEYKIND:
            returnVal = new BerkeleyKey(mSource);
            break;
        case NULLKIND:
            returnVal = null;
            break;
        default:
            throw new IllegalStateException(new StringBuilder("Kind ").append(kind).append(" is not known")
                .toString());
        }

        return returnVal;
    }

    public static void serializeKey(final ITTSink mSink, final AbstractKey mKey) {

        if (mKey == null) {
            mSink.writeInt(NULLKIND);
        } else {

            if (mKey instanceof FileKey) {
                mSink.writeInt(FILEKIND);
            } else if (mKey instanceof BerkeleyKey) {
                mSink.writeInt(BERKELEYKIND);
            } else {
                throw new IllegalStateException(new StringBuilder("Key ").append(mKey.getClass()).append(
                    " cannot be serialized").toString());
            }

            for (long val : mKey.getKeys()) {
                mSink.writeLong(val);
            }
        }

    }
}
