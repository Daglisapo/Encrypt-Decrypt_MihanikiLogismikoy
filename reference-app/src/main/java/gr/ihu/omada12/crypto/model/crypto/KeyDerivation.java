package gr.ihu.omada12.crypto.model.crypto;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

// Υλοποιεί: ENC-03, NFR-02

/**
 * Παραγωγή κλειδιού AES-256 από κωδικό πρόσβασης με PBKDF2-HMAC-SHA256.
 * 600.000 επαναλήψεις (σύσταση OWASP) και τυχαίο salt 16 bytes ανά κρυπτογράφηση,
 * ώστε ο ίδιος κωδικός να δίνει διαφορετικό κλειδί σε κάθε αρχείο.
 */
public class KeyDerivation {
    public static final int ITERATIONS = 600_000;
    public static final int SALT_BYTES = 16;
    public static final int KEY_BITS = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private final SecureRandom random = new SecureRandom();

    public byte[] newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    public SecretKey derive(char[] password, byte[] salt) throws CryptoException {
        if (password == null || password.length == 0) throw new CryptoException("error.emptyPassword");
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            byte[] raw = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return new SecretKeySpec(raw, "AES");
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.keyDerivation", e);
        } finally {
            spec.clearPassword(); // ο κωδικός δεν μένει στη μνήμη του spec (NFR-03)
        }
    }
}
