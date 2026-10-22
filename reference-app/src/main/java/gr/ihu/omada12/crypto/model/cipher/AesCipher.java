package gr.ihu.omada12.crypto.model.cipher;

import gr.ihu.omada12.crypto.model.crypto.PasswordBox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Υλοποιεί: ENC-01, ENC-02, ENC-03, NFR-01, NFR-02

/**
 * AES-256-GCM με ΚΩΔΙΚΟ πρόσβασης: ο χρήστης δεν χειρίζεται κλειδιά (ENC-03).
 * Το κλειδί παράγεται με PBKDF2 και η έξοδος είναι Base64(salt + IV + κρυπτοκείμενο).
 * Με λάθος κωδικό η αποκρυπτογράφηση ΑΠΟΤΥΓΧΑΝΕΙ (δεν βγάζει σκουπίδια).
 */
public class AesCipher implements TextCipher {
    private final PasswordBox box;

    public AesCipher() {
        this(new PasswordBox());
    }

    public AesCipher(PasswordBox box) {
        this.box = box;
    }

    @Override public String getName() { return "AES-256"; }

    @Override public String getKeyHintKey() { return "enc.hint.aesPassword"; }

    @Override public void validateKey(String key) throws CryptoException {
        if (key == null || key.isEmpty()) throw new CryptoException("error.emptyPassword");
    }

    @Override public String encrypt(String text, String key) throws CryptoException {
        if (text == null || text.isEmpty()) throw new CryptoException("error.emptyText");
        validateKey(key);
        byte[] sealed = box.seal(text.getBytes(StandardCharsets.UTF_8), key.toCharArray());
        return Base64.getEncoder().encodeToString(sealed);
    }

    @Override public String decrypt(String text, String key) throws CryptoException {
        validateKey(key);
        byte[] sealed = Base64Text.decode(text);
        return new String(box.open(sealed, key.toCharArray()), StandardCharsets.UTF_8);
    }
}
