package gr.ihu.omada12.crypto.model.cipher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** ENC-01, ENC-02, ENC-03, NFR-01, NFR-06 */
class AesCipherTest {
    private final AesCipher aes = new AesCipher();

    @Test
    void passwordRoundTripWithGreekText() throws CryptoException {
        String secret = "Καλημέρα κόσμε — 2026!";
        assertEquals(secret, aes.decrypt(aes.encrypt(secret, "σωστός-κωδικός"), "σωστός-κωδικός"));
    }

    @Test
    void wrongPasswordFailsInsteadOfReturningGarbage() throws CryptoException {
        String c = aes.encrypt("μήνυμα", "κωδικός-1");
        CryptoException e = assertThrows(CryptoException.class, () -> aes.decrypt(c, "κωδικός-2"));
        assertEquals("error.wrongKeyOrCorrupted", e.getMessageKey());
    }

    @Test // NFR-01: νέο τυχαίο salt/IV σε κάθε κρυπτογράφηση
    void sameInputGivesDifferentCipherTexts() throws CryptoException {
        assertNotEquals(aes.encrypt("ίδιο", "pw"), aes.encrypt("ίδιο", "pw"));
    }

    @Test
    void invalidBase64IsReported() {
        CryptoException e = assertThrows(CryptoException.class, () -> aes.decrypt("@@@ not base64 @@@", "pw"));
        assertEquals("error.notBase64", e.getMessageKey());
    }

    @Test
    void emptyPasswordIsRejected() {
        CryptoException e = assertThrows(CryptoException.class, () -> aes.encrypt("κείμενο", ""));
        assertEquals("error.emptyPassword", e.getMessageKey());
    }

    @Test // TC-07 (οδηγός ρόλου): μη έγκυρο κλειδί AES
    void invalidAesKeyIsRejected() {
        CryptoException e = assertThrows(CryptoException.class, () -> new AesKeyCipher().validateKey("abc"));
        assertTrue(e.getMessageKey().startsWith("error.aesKey"));
    }

    @Test // NFR-06 / TC-08: κείμενο 10.000 χαρακτήρων σε < 1 s
    void tenThousandCharactersUnderOneSecond() throws CryptoException {
        String text = "α".repeat(10_000);
        long start = System.nanoTime();
        String c = aes.encrypt(text, "pw");
        long ms = (System.nanoTime() - start) / 1_000_000;
        assertEquals(text, aes.decrypt(c, "pw"));
        assertTrue(ms < 1000, "Η κρυπτογράφηση πήρε " + ms + " ms");
    }
}
