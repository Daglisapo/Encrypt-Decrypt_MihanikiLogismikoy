package gr.ihu.omada12.crypto.model.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.AesGcm;
import gr.ihu.omada12.crypto.model.crypto.KeyDerivation;
import gr.ihu.omada12.crypto.model.io.AtomicFileWriter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

// Υλοποιεί: VLT-01, VLT-02, VLT-03, NFR-01, NFR-02, NFR-04, NFR-05

/**
 * Το θησαυροφυλάκιο είναι ΕΝΑ κρυπτογραφημένο αρχείο-δοχείο (.o12vault):
 * "O12V" + έκδοση + salt (16) | IV (12) | AES-GCM( όλη η λίστα αρχείων + περιεχόμενα ) | ετικέτα.
 * Το κλειδί παράγεται από τον κύριο κωδικό με PBKDF2. Δεν υπάρχει ανάκτηση κωδικού (NFR-04).
 */
public class VaultStore {
    static final byte[] MAGIC = {'O', '1', '2', 'V'};
    static final byte VERSION = 1;
    static final int HEADER_LENGTH = MAGIC.length + 1 + KeyDerivation.SALT_BYTES;

    private final KeyDerivation derivation = new KeyDerivation();
    private final AesGcm gcm = new AesGcm();
    private final VaultCodec codec = new VaultCodec();
    private final AtomicFileWriter writer = new AtomicFileWriter();

    public Vault create(Path file, char[] password) throws CryptoException {
        if (Files.exists(file)) throw new CryptoException("error.vaultExists");
        byte[] salt = derivation.newSalt();
        Vault vault = new Vault(file, derivation.derive(password, salt), salt, new ArrayList<>(), this);
        save(vault);
        return vault;
    }

    public Vault open(Path file, char[] password) throws CryptoException {
        byte[] all = readAll(file);
        if (all.length < HEADER_LENGTH || !Arrays.equals(Arrays.copyOf(all, MAGIC.length), MAGIC)) {
            throw new CryptoException("error.notVault");
        }
        if (all[MAGIC.length] != VERSION) throw new CryptoException("error.unsupportedVersion");
        byte[] header = Arrays.copyOf(all, HEADER_LENGTH);
        byte[] salt = Arrays.copyOfRange(header, MAGIC.length + 1, HEADER_LENGTH);
        SecretKey key = derivation.derive(password, salt);
        byte[] plain;
        try {
            plain = gcm.decrypt(Arrays.copyOfRange(all, HEADER_LENGTH, all.length), key, header);
        } catch (CryptoException e) {
            throw new CryptoException("error.wrongMasterPassword");
        }
        return new Vault(file, key, salt, codec.decode(plain), this);
    }

    /** Ελέγχει αν ο κωδικός δίνει το ίδιο κλειδί με το τρέχον (για την αλλαγή κωδικού, VLT-10). */
    boolean matches(char[] password, byte[] salt, SecretKey key) throws CryptoException {
        byte[] candidate = derivation.derive(password, salt).getEncoded();
        return MessageDigest.isEqual(candidate, key.getEncoded());
    }

    byte[] newSalt() { return derivation.newSalt(); }

    SecretKey derive(char[] password, byte[] salt) throws CryptoException { return derivation.derive(password, salt); }

    /** Ξαναγράφει ολόκληρο το δοχείο, ατομικά (προσωρινό αρχείο + μετονομασία). */
    void save(Vault vault) throws CryptoException {
        byte[] header = ByteBuffer.allocate(HEADER_LENGTH).put(MAGIC).put(VERSION).put(vault.salt()).array();
        byte[] body = gcm.encrypt(codec.encode(vault.entriesForSave()), vault.key(), header);
        writer.write(vault.getFile(), out -> {
            out.write(header);
            out.write(body);
        });
    }

    private byte[] readAll(Path file) throws CryptoException {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", file.getFileName());
        }
    }
}
