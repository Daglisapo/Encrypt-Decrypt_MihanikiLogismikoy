package gr.ihu.omada12.crypto.model.crypto;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;

// Υλοποιεί: ENC-03, NFR-01, NFR-02 (κοινό για κείμενο, στεγανογραφία, ιδιωτικά κλειδιά)

/**
 * «Κουτί» που κλειδώνει bytes με κωδικό: salt (16) + IV (12) + κρυπτοκείμενο + ετικέτα.
 * Το salt μπαίνει και ως AAD, ώστε αλλαγή του να ανιχνεύεται.
 */
public class PasswordBox {
    private final KeyDerivation derivation;
    private final AesGcm gcm;

    public PasswordBox() {
        this(new KeyDerivation(), new AesGcm());
    }

    public PasswordBox(KeyDerivation derivation, AesGcm gcm) {
        this.derivation = derivation;
        this.gcm = gcm;
    }

    /** Πόσα bytes προσθέτει η κρυπτογράφηση στο μήκος (χρήσιμο για τη χωρητικότητα, STG-04). */
    public static int overhead() {
        return KeyDerivation.SALT_BYTES + AesGcm.IV_BYTES + AesGcm.TAG_BYTES;
    }

    public byte[] seal(byte[] data, char[] password) throws CryptoException {
        byte[] salt = derivation.newSalt();
        SecretKey key = derivation.derive(password, salt);
        byte[] body = gcm.encrypt(data, key, salt);
        return ByteBuffer.allocate(salt.length + body.length).put(salt).put(body).array();
    }

    public byte[] open(byte[] sealed, char[] password) throws CryptoException {
        if (sealed == null || sealed.length < overhead()) throw new CryptoException("error.corrupted");
        byte[] salt = new byte[KeyDerivation.SALT_BYTES];
        byte[] body = new byte[sealed.length - salt.length];
        System.arraycopy(sealed, 0, salt, 0, salt.length);
        System.arraycopy(sealed, salt.length, body, 0, body.length);
        SecretKey key = derivation.derive(password, salt);
        return gcm.decrypt(body, key, salt);
    }
}
