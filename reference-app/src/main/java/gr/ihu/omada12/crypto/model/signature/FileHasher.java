package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Υλοποιεί: SIG-05, SIG-02, SIG-03

/** Αποτύπωμα SHA-256 αρχείου (διαβάζεται σε κομμάτια, άρα δουλεύει και για μεγάλα αρχεία). */
public class FileHasher {
    private static final int CHUNK = 64 * 1024;

    public byte[] sha256(Path file) throws CryptoException {
        MessageDigest digest = newDigest();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[CHUNK];
            int n;
            while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n);
            return digest.digest();
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", file.getFileName());
        }
    }

    public byte[] sha256(byte[] data) throws CryptoException {
        return newDigest().digest(data);
    }

    /** Δύο αρχεία είναι ίδια όταν έχουν το ίδιο αποτύπωμα. */
    public boolean sameContent(Path a, Path b) throws CryptoException {
        return MessageDigest.isEqual(sha256(a), sha256(b));
    }

    private MessageDigest newDigest() throws CryptoException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("error.hashFailed", e);
        }
    }
}
