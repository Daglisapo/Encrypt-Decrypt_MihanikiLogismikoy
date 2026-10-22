package gr.ihu.omada12.crypto.model.io;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Υλοποιεί: NFR-05, ENC-05 (σε αποτυχία δεν μένει αρχείο εξόδου)

/**
 * Γράφει πρώτα σε προσωρινό αρχείο στον ίδιο φάκελο και μόνο στο τέλος το μετονομάζει.
 * Αν κάτι αποτύχει (λάθος κωδικός, ακύρωση, διακοπή), το προσωρινό σβήνεται
 * και ούτε το αρχικό ούτε το αρχείο-στόχος αλλοιώνονται.
 */
public class AtomicFileWriter {

    /** Ό,τι γράφει το περιεχόμενο στο stream. */
    public interface Body {
        void writeTo(OutputStream out) throws IOException, CryptoException;
    }

    public void write(Path target, Body body) throws CryptoException {
        Path dir = target.toAbsolutePath().getParent();
        Path temp = null;
        boolean done = false;
        try {
            temp = Files.createTempFile(dir, ".o12-", ".part");
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(temp))) {
                body.writeTo(out);
            }
            move(temp, target);
            done = true;
        } catch (IOException e) {
            throw new CryptoException("error.fileWrite", target.getFileName());
        } finally {
            if (!done && temp != null) deleteQuietly(temp);
        }
    }

    private void move(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteQuietly(Path temp) {
        try {
            Files.deleteIfExists(temp);
        } catch (IOException ignored) {
            // Αν δεν σβηστεί, μένει ένα κρυφό .part χωρίς χρήσιμο περιεχόμενο.
        }
    }
}
