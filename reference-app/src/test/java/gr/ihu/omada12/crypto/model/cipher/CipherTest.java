package gr.ihu.omada12.crypto.model.cipher;

import gr.ihu.omada12.crypto.model.key.KeyManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Τα tests του πρωτοτύπου (TC-01, 02, 04, 05 του οδηγού ρόλου Β), προσαρμοσμένα στα κλειδιά μηνυμάτων. */
class CipherTest {

    @Test // TC-01 (οδηγός ρόλου)
    void caesarShiftsByThree() throws CryptoException {
        assertEquals("KHOOR", new CaesarCipher().encrypt("HELLO", "3"));
    }

    @Test
    void caesarDecryptReversesEncrypt() throws CryptoException {
        CaesarCipher caesar = new CaesarCipher();
        assertEquals("Hello, World!", caesar.decrypt(caesar.encrypt("Hello, World!", "7"), "7"));
    }

    @Test // TC-02 (οδηγός ρόλου)
    void vigenereClassicExample() throws CryptoException {
        assertEquals("LXFOPVEFRNHR", new VigenereCipher().encrypt("ATTACKATDAWN", "LEMON"));
    }

    @Test
    void vigenereKeepsNonLettersAndCase() throws CryptoException {
        VigenereCipher v = new VigenereCipher();
        String c = v.encrypt("Attack at dawn!", "lemon");
        assertEquals("Lxfopv ef rnhr!", c);
        assertEquals("Attack at dawn!", v.decrypt(c, "lemon"));
    }

    @Test // TC-04 (οδηγός ρόλου)
    void aesRejectsWrongKey() throws CryptoException {
        KeyManager keys = new KeyManager();
        AesKeyCipher aes = new AesKeyCipher();
        String cipherText = aes.encrypt("μυστικό μήνυμα", keys.generateAesKey());
        CryptoException e = assertThrows(CryptoException.class, () -> aes.decrypt(cipherText, keys.generateAesKey()));
        assertEquals("error.wrongKeyOrCorrupted", e.getMessageKey());
    }

    @Test // TC-05 (οδηγός ρόλου)
    void emptyTextIsRejected() {
        CryptoException e = assertThrows(CryptoException.class, () -> new CaesarCipher().encrypt("", "3"));
        assertEquals("error.emptyText", e.getMessageKey());
    }

    @Test
    void caesarKeyOutOfRangeIsRejected() {
        CryptoException e = assertThrows(CryptoException.class, () -> new CaesarCipher().validateKey("26"));
        assertEquals("error.caesarRange", e.getMessageKey());
        assertThrows(CryptoException.class, () -> new CaesarCipher().validateKey("abc"));
    }

    @Test
    void vigenereRejectsNonLatinKey() {
        assertThrows(CryptoException.class, () -> new VigenereCipher().validateKey("κλειδί"));
    }
}
