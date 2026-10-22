package gr.ihu.omada12.crypto.model.cipher;

import gr.ihu.omada12.crypto.model.crypto.AesGcm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Υλοποιεί: ENC-01, ENC-02, ENC-08 (χρήση τυχαίου κλειδιού από αρχείο)

/**
 * AES-256-GCM με ΕΤΟΙΜΟ τυχαίο κλειδί (Base64, 32 bytes) αντί για κωδικό.
 * Είναι η εκδοχή του πρωτοτύπου και συνδυάζεται με το KeyManager (ENC-08).
 */
public class AesKeyCipher implements TextCipher {
    private final AesGcm gcm = new AesGcm();

    @Override public String getName() { return "AES-256 (key)"; }

    @Override public String getKeyHintKey() { return "enc.hint.aesKey"; }

    @Override public void validateKey(String key) throws CryptoException { toSecretKey(key); }

    @Override public String encrypt(String text, String key) throws CryptoException {
        if (text == null || text.isEmpty()) throw new CryptoException("error.emptyText");
        byte[] out = gcm.encrypt(text.getBytes(StandardCharsets.UTF_8), toSecretKey(key), null);
        return Base64.getEncoder().encodeToString(out);
    }

    @Override public String decrypt(String text, String key) throws CryptoException {
        SecretKey secret = toSecretKey(key);
        return new String(gcm.decrypt(Base64Text.decode(text), secret, null), StandardCharsets.UTF_8);
    }

    /** Το κλειδί πληκτρολογείται/αποθηκεύεται ως Base64 (32 bytes = AES-256). */
    public SecretKey toSecretKey(String base64Key) throws CryptoException {
        if (base64Key == null || base64Key.isBlank()) throw new CryptoException("error.aesKeyFormat");
        try {
            byte[] raw = Base64.getDecoder().decode(base64Key.trim());
            if (raw.length != 32) throw new CryptoException("error.aesKeyLength");
            return new SecretKeySpec(raw, "AES");
        } catch (IllegalArgumentException e) {
            throw new CryptoException("error.aesKeyFormat");
        }
    }
}
