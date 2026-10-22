package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.ProgressListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** ENC-04, ENC-05, ENC-10, NFR-05 */
class FileCryptoServiceTest {
    private static final char[] PW = "Σωστός-Κωδικός-1".toCharArray();
    private final FileCryptoService service = new FileCryptoService();

    @TempDir
    Path dir;

    private Path randomFile(String name, int size) throws IOException {
        byte[] data = new byte[size];
        new Random(42).nextBytes(data);
        return Files.write(dir.resolve(name), data);
    }

    @Test
    void encryptThenDecryptGivesIdenticalFile() throws Exception {
        Path original = randomFile("report.pdf", 1024 * 1024 + 123);
        Path enc = FileCryptoService.encryptedName(original);
        Path dec = dir.resolve("copy.pdf");
        AtomicLong lastProgress = new AtomicLong();
        ProgressListener progress = new ProgressListener() {
            @Override public void onProgress(long done, long total) { lastProgress.set(done); }
            @Override public boolean isCancelled() { return false; }
        };
        service.encrypt(original, enc, PW, progress);
        service.decrypt(enc, dec, PW, ProgressListener.NONE);
        assertArrayEquals(Files.readAllBytes(original), Files.readAllBytes(dec));
        assertEquals(Files.size(original), lastProgress.get());
        assertEquals(Files.size(original) + EncFileHeader.LENGTH + 12 + 16, Files.size(enc));
    }

    @Test // ENC-05: λάθος κωδικός → μήνυμα και ΚΑΝΕΝΑ αρχείο εξόδου
    void wrongPasswordCreatesNoOutputFile() throws Exception {
        Path enc = dir.resolve("a.enc");
        service.encrypt(randomFile("a.txt", 5000), enc, PW, ProgressListener.NONE);
        Path out = dir.resolve("out.txt");
        CryptoException e = assertThrows(CryptoException.class,
                () -> service.decrypt(enc, out, "λάθος".toCharArray(), ProgressListener.NONE));
        assertEquals("error.wrongKeyOrCorrupted", e.getMessageKey());
        assertFalse(Files.exists(out));
        assertNoTempFiles();
    }

    @Test // ENC-05: αλλοιωμένο αρχείο
    void tamperedFileIsDetected() throws Exception {
        Path enc = dir.resolve("b.enc");
        service.encrypt(randomFile("b.bin", 2000), enc, PW, ProgressListener.NONE);
        byte[] bytes = Files.readAllBytes(enc);
        bytes[bytes.length / 2] ^= 0x01;
        Files.write(enc, bytes);
        assertThrows(CryptoException.class, () -> service.decrypt(enc, dir.resolve("x"), PW, ProgressListener.NONE));
        assertFalse(Files.exists(dir.resolve("x")));
    }

    @Test // ENC-10 + NFR-05: ακύρωση στη μέση δεν αφήνει μισό αρχείο
    void cancelLeavesNoPartialOutput() throws Exception {
        Path source = randomFile("big.bin", 3 * StreamCipherPump.CHUNK);
        ProgressListener cancelAfterFirstChunk = new ProgressListener() {
            private boolean started;
            @Override public void onProgress(long done, long total) { started = true; }
            @Override public boolean isCancelled() { return started; }
        };
        Path target = dir.resolve("big.bin.enc");
        CryptoException e = assertThrows(CryptoException.class,
                () -> service.encrypt(source, target, PW, cancelAfterFirstChunk));
        assertEquals("error.cancelled", e.getMessageKey());
        assertFalse(Files.exists(target));
        assertNoTempFiles();
    }

    @Test
    void plainFileIsNotAcceptedForDecryption() throws Exception {
        Path plain = randomFile("plain.txt", 100);
        CryptoException e = assertThrows(CryptoException.class,
                () -> service.decrypt(plain, dir.resolve("o"), PW, ProgressListener.NONE));
        assertEquals("error.notEncFile", e.getMessageKey());
    }

    @Test // NFR-06: αρχείο 50 MB με AES σε λιγότερο από 5 s
    void fiftyMegabytesUnderFiveSeconds() throws Exception {
        Path big = randomFile("big50.bin", 50 * 1024 * 1024);
        long start = System.nanoTime();
        service.encrypt(big, dir.resolve("big50.bin.enc"), PW, ProgressListener.NONE);
        long ms = (System.nanoTime() - start) / 1_000_000;
        assertTrue(ms < 5000, "Η κρυπτογράφηση 50 MB πήρε " + ms + " ms");
    }

    @Test
    void suggestedNames() {
        assertEquals("doc.pdf.enc", FileCryptoService.encryptedName(Path.of("doc.pdf")).toString());
        assertEquals("doc.pdf", FileCryptoService.decryptedName(Path.of("doc.pdf.enc")).toString());
        assertEquals("doc.bin.dec", FileCryptoService.decryptedName(Path.of("doc.bin")).toString());
    }

    private void assertNoTempFiles() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            assertFalse(files.anyMatch(p -> p.getFileName().toString().endsWith(".part")));
        }
    }
}
