/**
 * 
 */
package org.treetank.io.decorators;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.treetank.exception.TTByteHandleException;

/**
 * Decorator for encrypting any content.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class EncryptionDecorator extends ByteRepresentationDecorator {

    private static final String ALGORITHM = "AES";
    private static final int ITERATIONS = 2;
    //128bit key
    private static final byte[] keyValue = new byte[] {
        'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k','k', 'k', 'k', 'k', 'k', 'k', 'k', 'k'
    };
    private final Cipher mCipher;

    private final Key key;

    /**
     * Constructor.
     * 
     * @param pComponent
     * @throws TTByteHandleException
     */
    public EncryptionDecorator(final IByteRepresentation pComponent) throws TTByteHandleException {
        super(pComponent);
        try {
            mCipher = Cipher.getInstance(ALGORITHM);
            key = new SecretKeySpec(keyValue, ALGORITHM);
        } catch (final NoSuchAlgorithmException exc) {
            throw new TTByteHandleException(exc);
        } catch (final NoSuchPaddingException exc) {
            throw new TTByteHandleException(exc);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTByteHandleException
     */
    public byte[] serialize(final byte[] pToSerialize) throws TTByteHandleException {
        final byte[] superSerialized = super.serialize(pToSerialize);
        try {
            mCipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] toEncrypt = superSerialized;
            for (int i = 0; i < ITERATIONS; i++) {
                byte[] encValue = mCipher.doFinal(toEncrypt);
                toEncrypt = encValue;
            }
            return toEncrypt;
        } catch (final GeneralSecurityException exc) {
            throw new TTByteHandleException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[] deserialize(byte[] pToDeserialize) throws TTByteHandleException {
        final byte[] superDeserialized = super.serialize(pToDeserialize);
        try {
            mCipher.init(Cipher.DECRYPT_MODE, key);

            byte[] toDecrypt = superDeserialized;
            for (int i = 0; i < ITERATIONS; i++) {
                byte[] decValue = mCipher.doFinal(toDecrypt);
                toDecrypt = decValue;
            }
            return toDecrypt;

        } catch (final InvalidKeyException exc) {
            throw new TTByteHandleException(exc);
        } catch (final GeneralSecurityException exc) {
            throw new TTByteHandleException(exc);
        }

    }
}
