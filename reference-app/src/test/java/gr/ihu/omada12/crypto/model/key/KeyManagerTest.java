package gr.ihu.omada12.crypto.model.key;

import gr.ihu.omada12.crypto.model.cipher.AesKeyCipher;
import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** ENC-08: δημιουργία, αποθήκευση και φόρτωση κλειδιού. */
class KeyManagerTest {
    private final KeyManager keys = new KeyManager();

    @Test
    void generatedKeyIs256Bits() throws CryptoException {
        assertEquals(32, Base64.getDecoder().decode(keys.generateAesKey()).length);
    }

    @Test
    void savedKeyCanBeLoadedAndUsed(@TempDir Path dir) throws CryptoException {
        String key = keys.generateAesKey();
        Path file = dir.resolve("my.key");
        keys.save(key, file);
        String loaded = keys.load(file);
        assertEquals(key, loaded);
        AesKeyCipher aes = new AesKeyCipher();
        assertEquals("γεια", aes.decrypt(aes.encrypt("γεια", key), loaded));
    }

    @Test
    void missingKeyFileIsReported(@TempDir Path dir) {
        CryptoException e = assertThrows(CryptoException.class, () -> keys.load(dir.resolve("none.key")));
        assertEquals("error.keyLoad", e.getMessageKey());
    }
}
