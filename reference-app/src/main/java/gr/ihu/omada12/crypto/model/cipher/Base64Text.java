package gr.ihu.omada12.crypto.model.cipher;

import java.util.Base64;

// Υλοποιεί: ENC-02 (έλεγχος εισόδου κρυπτογραφήματος)

/** Αποκωδικοποίηση Base64 με κατανοητό μήνυμα αντί για IllegalArgumentException. */
public final class Base64Text {
    private Base64Text() { }

    public static byte[] decode(String text) throws CryptoException {
        if (text == null || text.isBlank()) throw new CryptoException("error.emptyText");
        try {
            return Base64.getDecoder().decode(text.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new CryptoException("error.notBase64");
        }
    }
}
