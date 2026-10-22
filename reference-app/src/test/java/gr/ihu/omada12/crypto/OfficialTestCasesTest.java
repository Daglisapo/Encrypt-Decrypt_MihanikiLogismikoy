package gr.ihu.omada12.crypto;

import gr.ihu.omada12.crypto.model.cipher.AesCipher;
import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.file.FileCryptoService;
import gr.ihu.omada12.crypto.model.io.ProgressListener;
import gr.ihu.omada12.crypto.model.signature.FileSigner;
import gr.ihu.omada12.crypto.model.signature.SignatureAlgorithm;
import gr.ihu.omada12.crypto.model.signature.SignatureRecord;
import gr.ihu.omada12.crypto.model.signature.SignatureVerifier;
import gr.ihu.omada12.crypto.model.signature.VerificationResult;
import gr.ihu.omada12.crypto.model.stego.StegoService;
import gr.ihu.omada12.crypto.model.vault.LoginThrottle;
import gr.ihu.omada12.crypto.model.vault.VaultManager;
import gr.ihu.omada12.crypto.model.vault.VaultStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Τα 5 προτεινόμενα «επίσημα» test cases του Παραδοτέου 2 (βλ. SXEDIASH.md, ενότητα δ).
 * Κάθε μέθοδος = μία γραμμή του πίνακα Test Case # · Description · Test Data · Expected Result.
 */
class OfficialTestCasesTest {

    @TempDir
    Path dir;

    @Test
    @DisplayName("TC-01 · UC-01/UC-02 · Κρυπτογράφηση και αποκρυπτογράφηση κειμένου με κωδικό (AES-256)")
    void tc01TextRoundTrip() throws CryptoException {
        AesCipher aes = new AesCipher();
        String cipherText = aes.encrypt("Καλημέρα κόσμε", "Κωδικός-2026");
        assertNotEquals("Καλημέρα κόσμε", cipherText);
        assertEquals("Καλημέρα κόσμε", aes.decrypt(cipherText, "Κωδικός-2026"));
    }

    @Test
    @DisplayName("TC-02 · UC-04 εναλλακτική ροή · Λάθος κωδικός σε αρχείο .enc: μήνυμα και κανένα αρχείο εξόδου")
    void tc02WrongPasswordCreatesNoFile() throws Exception {
        FileCryptoService service = new FileCryptoService();
        Path pdf = Files.write(dir.resolve("report.pdf"), new byte[50_000]);
        Path enc = dir.resolve("report.pdf.enc");
        service.encrypt(pdf, enc, "Σωστός-1".toCharArray(), ProgressListener.NONE);
        Path out = dir.resolve("out.pdf");
        CryptoException e = assertThrows(CryptoException.class,
                () -> service.decrypt(enc, out, "Λάθος-2".toCharArray(), ProgressListener.NONE));
        assertEquals("error.wrongKeyOrCorrupted", e.getMessageKey());
        assertFalse(Files.exists(out));
    }

    @Test
    @DisplayName("TC-03 · UC-08 · 3 λάθος κύριοι κωδικοί → αναμονή 30 s, μετά ξεκλείδωμα με τον σωστό")
    void tc03VaultThrottling() throws Exception {
        FakeClock clock = new FakeClock();
        VaultManager vaults = new VaultManager(dir, new VaultStore(), new LoginThrottle(clock));
        char[] master = "Θησαυρός-2026-Ασφαλής!".toCharArray();
        vaults.create("Προσωπικά", master, master).lock();
        for (int i = 0; i < 3; i++) {
            assertThrows(CryptoException.class, () -> vaults.unlock("Προσωπικά", "λάθος".toCharArray()));
        }
        CryptoException e = assertThrows(CryptoException.class, () -> vaults.unlock("Προσωπικά", master));
        assertEquals("error.vaultThrottled", e.getMessageKey());
        clock.now = clock.now.plus(Duration.ofSeconds(31));
        assertTrue(vaults.unlock("Προσωπικά", master).getEntries().isEmpty());
    }

    @Test
    @DisplayName("TC-04 · UC-18 · Αλλαγή ενός byte μετά την υπογραφή → «Μη έγκυρη»")
    void tc04TamperedFileFailsVerification() throws Exception {
        Path doc = Files.writeString(dir.resolve("symvasi.txt"), "Ο Α πληρώνει στον Β 100 ευρώ.");
        KeyPair keys = SignatureAlgorithm.generateKeyPair();
        SignatureRecord record = new FileSigner().sign(doc, keys);
        assertTrue(new SignatureVerifier().verify(doc, record, keys.getPublic()).isValid());

        Files.writeString(doc, "Ο Α πληρώνει στον Β 900 ευρώ.");
        VerificationResult result = new SignatureVerifier().verify(doc, record, keys.getPublic());
        assertFalse(result.isValid());
        assertEquals("sig.result.invalid", result.getMessageKey());
    }

    @Test
    @DisplayName("TC-05 · UC-20/UC-21 · Απόκρυψη και ανάκτηση μηνύματος σε PNG· μήνυμα που δεν χωράει απορρίπτεται")
    void tc05StegoHideRevealAndCapacity() throws Exception {
        Path cover = dir.resolve("cover.png");
        ImageIO.write(new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB), "png", cover.toFile());
        StegoService stego = new StegoService();
        Path out = stego.hideText(cover, "Συνάντηση στις 8", null, dir.resolve("out.jpg"));
        assertTrue(out.toString().endsWith(".png"));
        assertEquals("Συνάντηση στις 8", stego.reveal(out, null).getText());

        String tooLong = "x".repeat(64 * 64 * 3 / 8);
        CryptoException e = assertThrows(CryptoException.class, () -> stego.hideText(cover, tooLong, null, out));
        assertEquals("error.stegoTooLarge", e.getMessageKey());
    }

    /** Ρολόι που προχωράμε με το χέρι, για να μη περιμένει το test 30 δευτερόλεπτα. */
    private static final class FakeClock extends Clock {
        private Instant now = Instant.parse("2026-12-01T10:00:00Z");

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }

        @Override public Clock withZone(ZoneId zone) { return this; }

        @Override public Instant instant() { return now; }
    }
}
