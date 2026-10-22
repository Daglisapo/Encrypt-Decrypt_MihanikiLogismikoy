package gr.ihu.omada12.crypto.model.key;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Base64;

// Υλοποιεί: ENC-08

/**
 * Δημιουργία τυχαίου κλειδιού AES-256, αποθήκευση και φόρτωση από αρχείο .key.
 * Το αρχείο κλειδιού το διαλέγει ρητά ο χρήστης· η εφαρμογή δεν το γράφει πουθενά αλλού (NFR-03).
 */
public class KeyManager {
    /** Νέο τυχαίο κλειδί AES-256, ως Base64. */
    public String generateAesKey() throws CryptoException {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(256);
            return Base64.getEncoder().encodeToString(gen.generateKey().getEncoded());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.keyGeneration", e);
        }
    }

    public void save(String key, Path file) throws CryptoException {
        try {
            Files.writeString(file, key, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CryptoException("error.keySave", file.getFileName());
        }
    }

    public String load(Path file) throws CryptoException {
        try {
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new CryptoException("error.keyLoad", file.getFileName());
        }
    }
}
