package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.AtomicFileWriter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.IOException;
import java.nio.file.Path;

// Υλοποιεί: ENC-07

/**
 * «Κλείδωμα» PDF με το πρότυπο κρυπτογράφησης του ίδιου του PDF (AES-256),
 * ώστε να ανοίγει με τον κωδικό σε ΟΠΟΙΟΝΔΗΠΟΤΕ PDF reader. Χρησιμοποιεί το Apache PDFBox,
 * τη μόνη εξωτερική βιβλιοθήκη της εφαρμογής.
 */
public class PdfLocker {
    private static final int KEY_LENGTH = 256;
    private final AtomicFileWriter writer = new AtomicFileWriter();

    public boolean isLocked(Path pdf) throws CryptoException {
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            return doc.isEncrypted();
        } catch (InvalidPasswordException e) {
            return true;
        } catch (IOException e) {
            throw new CryptoException("error.notPdf", pdf.getFileName());
        }
    }

    public void lock(Path source, Path target, char[] password) throws CryptoException {
        if (password == null || password.length == 0) throw new CryptoException("error.emptyPassword");
        if (isLocked(source)) throw new CryptoException("error.pdfAlreadyLocked");
        String pw = new String(password);
        try (PDDocument doc = open(source, "")) {
            StandardProtectionPolicy policy = new StandardProtectionPolicy(pw, pw, new AccessPermission());
            policy.setEncryptionKeyLength(KEY_LENGTH);
            doc.protect(policy);
            writer.write(target, doc::save);
        } catch (IOException e) {
            throw new CryptoException("error.pdfWrite", target.getFileName());
        }
    }

    public void unlock(Path source, Path target, char[] password) throws CryptoException {
        try (PDDocument doc = open(source, new String(password))) {
            if (!doc.isEncrypted()) throw new CryptoException("error.pdfNotLocked");
            doc.setAllSecurityToBeRemoved(true);
            writer.write(target, doc::save);
        } catch (IOException e) {
            throw new CryptoException("error.pdfWrite", target.getFileName());
        }
    }

    private PDDocument open(Path source, String password) throws CryptoException {
        try {
            return Loader.loadPDF(source.toFile(), password);
        } catch (InvalidPasswordException e) {
            throw new CryptoException("error.pdfWrongPassword");
        } catch (IOException e) {
            throw new CryptoException("error.notPdf", source.getFileName());
        }
    }
}
