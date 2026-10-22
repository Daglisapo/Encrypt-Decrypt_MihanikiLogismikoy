package gr.ihu.omada12.crypto.model.crypto;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

// Υλοποιεί: NFR-01 (AES-256-GCM, νέο τυχαίο IV σε κάθε κρυπτογράφηση)

/**
 * Ο «κινητήρας» AES-GCM που χρησιμοποιούν όλες οι ενότητες.
 * Μορφή εξόδου: IV (12 bytes) + κρυπτοκείμενο + ετικέτα ελέγχου (16 bytes).
 * Το GCM ανιχνεύει λάθος κλειδί και αλλοιωμένα δεδομένα: η αποκρυπτογράφηση
 * αποτυγχάνει αντί να βγάλει σκουπίδια.
 */
public class AesGcm {
    public static final String TRANSFORMATION = "AES/GCM/NoPadding";
    public static final int IV_BYTES = 12;
    public static final int TAG_BITS = 128;
    public static final int TAG_BYTES = TAG_BITS / 8;

    private final SecureRandom random = new SecureRandom();

    public byte[] newIv() {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        return iv;
    }

    /** Κρυπτογραφεί όλο τον πίνακα. Το aad (μπορεί να είναι null) προστατεύεται χωρίς να κρυπτογραφείται. */
    public byte[] encrypt(byte[] data, SecretKey key, byte[] aad) throws CryptoException {
        byte[] iv = newIv();
        try {
            byte[] ct = newCipher(Cipher.ENCRYPT_MODE, key, iv, aad).doFinal(data);
            return ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.encryptFailed", e);
        }
    }

    public byte[] decrypt(byte[] blob, SecretKey key, byte[] aad) throws CryptoException {
        if (blob == null || blob.length < IV_BYTES + TAG_BYTES) throw new CryptoException("error.corrupted");
        byte[] iv = new byte[IV_BYTES];
        System.arraycopy(blob, 0, iv, 0, IV_BYTES);
        try {
            return newCipher(Cipher.DECRYPT_MODE, key, iv, aad).doFinal(blob, IV_BYTES, blob.length - IV_BYTES);
        } catch (AEADBadTagException e) {
            throw new CryptoException("error.wrongKeyOrCorrupted", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.decryptFailed", e);
        }
    }

    /** Έτοιμο Cipher για κρυπτογράφηση «κατά κομμάτια» (μεγάλα αρχεία, ENC-10). */
    public Cipher newCipher(int mode, SecretKey key, byte[] iv, byte[] aad) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance(TRANSFORMATION);
            c.init(mode, key, new GCMParameterSpec(TAG_BITS, iv));
            if (aad != null) c.updateAAD(aad);
            return c;
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.cipherInit", e);
        }
    }
}
