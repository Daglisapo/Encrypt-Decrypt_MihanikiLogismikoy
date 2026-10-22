package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** SIG-01 … SIG-06 */
class SignatureTest {
    private static final char[] KEY_PW = "Κωδικός-Κλειδιού-7".toCharArray();

    @TempDir
    Path dir;
    private Path document;
    private KeyPair keys;

    @BeforeEach
    void setUp() throws Exception {
        document = Files.writeString(dir.resolve("σύμβαση.txt"), "Ο Α πληρώνει τον Β 100 ευρώ.");
        keys = SignatureAlgorithm.generateKeyPair();
    }

    @Test // SIG-01: το ιδιωτικό κλειδί ανοίγει μόνο με τον κωδικό
    void privateKeyIsProtectedByPassword() throws Exception {
        Path keyFile = dir.resolve("me" + PrivateKeyStore.EXTENSION);
        PrivateKeyStore store = new PrivateKeyStore();
        store.save(keys, keyFile, KEY_PW);
        assertEquals(keys.getPublic(), store.load(keyFile, KEY_PW).getPublic());
        CryptoException e = assertThrows(CryptoException.class, () -> store.load(keyFile, "λάθος".toCharArray()));
        assertEquals("error.wrongKeyPassword", e.getMessageKey());
    }

    @Test // SIG-02 + SIG-03 + SIG-06
    void signedFileVerifiesAndShowsDetails() throws Exception {
        Path sig = SignatureFileCodec.signatureFileFor(document);
        SignatureFileCodec codec = new SignatureFileCodec();
        codec.write(new FileSigner().sign(document, keys), sig);

        VerificationResult result = new SignatureVerifier().verify(document, codec.read(sig), keys.getPublic());
        assertTrue(result.isValid());
        assertEquals("sig.result.valid", result.getMessageKey());
        assertEquals("σύμβαση.txt", result.getRecord().getFileName());
        assertEquals(LocalDate.now(), result.getRecord().getSignedAt().toLocalDate());
        assertEquals(SignatureAlgorithm.fingerprint(keys.getPublic()), result.getRecord().getKeyFingerprint());
    }

    @Test // SIG-03: αλλαγή ενός byte → «Μη έγκυρη»
    void oneChangedByteInvalidatesSignature() throws Exception {
        SignatureRecord record = new FileSigner().sign(document, keys);
        byte[] bytes = Files.readAllBytes(document);
        bytes[0] ^= 1;
        Files.write(document, bytes);
        VerificationResult result = new SignatureVerifier().verify(document, record, keys.getPublic());
        assertFalse(result.isValid());
        assertTrue(result.isKeyMatching());
        assertEquals("sig.result.invalid", result.getMessageKey());
    }

    @Test // SIG-03: υπογραφή που δεν αντιστοιχεί στο κλειδί
    void otherPublicKeyIsRejected() throws Exception {
        SignatureRecord record = new FileSigner().sign(document, keys);
        VerificationResult result = new SignatureVerifier()
                .verify(document, record, SignatureAlgorithm.generateKeyPair().getPublic());
        assertFalse(result.isValid());
        assertFalse(result.isKeyMatching());
    }

    @Test // SIG-04: εξαγωγή/εισαγωγή δημόσιου κλειδιού
    void exportedPublicKeyVerifiesSignatures() throws Exception {
        Path pub = dir.resolve("me" + PublicKeyPem.EXTENSION);
        PublicKeyPem pem = new PublicKeyPem();
        pem.export(keys.getPublic(), pub);
        assertTrue(Files.readString(pub).startsWith("-----BEGIN PUBLIC KEY-----"));
        PublicKey imported = pem.importFrom(pub);
        assertTrue(new SignatureVerifier().verify(document, new FileSigner().sign(document, keys), imported).isValid());
        assertThrows(CryptoException.class, () -> pem.decode("όχι κλειδί"));
    }

    @Test // SIG-05
    void sha256MatchesKnownValueAndComparesFiles() throws Exception {
        Path abc = Files.write(dir.resolve("abc.txt"), "abc".getBytes(StandardCharsets.US_ASCII));
        FileHasher hasher = new FileHasher();
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Hex.encode(hasher.sha256(abc)));
        Path copy = Files.copy(abc, dir.resolve("copy.txt"));
        assertTrue(hasher.sameContent(abc, copy));
        assertFalse(hasher.sameContent(abc, document));
    }
}
