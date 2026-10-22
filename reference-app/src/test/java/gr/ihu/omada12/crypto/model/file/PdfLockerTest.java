package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** ENC-07: κλείδωμα PDF που ανοίγει με κωδικό σε κάθε reader, και αφαίρεση κωδικού. */
class PdfLockerTest {
    private final PdfLocker locker = new PdfLocker();

    private Path samplePdf(Path dir) throws Exception {
        Path file = dir.resolve("sample.pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file.toFile());
        }
        return file;
    }

    @Test
    void lockedPdfNeedsPasswordAndUnlockRemovesIt(@TempDir Path dir) throws Exception {
        Path pdf = samplePdf(dir);
        Path locked = dir.resolve("locked.pdf");
        locker.lock(pdf, locked, "pdf-κωδικός".toCharArray());

        assertTrue(locker.isLocked(locked));
        assertThrows(InvalidPasswordException.class, () -> Loader.loadPDF(locked.toFile()).close());
        try (PDDocument doc = Loader.loadPDF(locked.toFile(), "pdf-κωδικός")) {
            assertEquals(1, doc.getNumberOfPages());
        }

        Path unlocked = dir.resolve("unlocked.pdf");
        locker.unlock(locked, unlocked, "pdf-κωδικός".toCharArray());
        assertFalse(locker.isLocked(unlocked));
    }

    @Test
    void wrongPasswordOnUnlockIsReported(@TempDir Path dir) throws Exception {
        Path locked = dir.resolve("locked.pdf");
        locker.lock(samplePdf(dir), locked, "σωστός".toCharArray());
        CryptoException e = assertThrows(CryptoException.class,
                () -> locker.unlock(locked, dir.resolve("x.pdf"), "λάθος".toCharArray()));
        assertEquals("error.pdfWrongPassword", e.getMessageKey());
    }

    @Test
    void nonPdfIsRejected(@TempDir Path dir) throws Exception {
        Path txt = java.nio.file.Files.writeString(dir.resolve("a.pdf"), "δεν είμαι PDF");
        CryptoException e = assertThrows(CryptoException.class,
                () -> locker.lock(txt, dir.resolve("b.pdf"), "pw".toCharArray()));
        assertEquals("error.notPdf", e.getMessageKey());
    }
}
