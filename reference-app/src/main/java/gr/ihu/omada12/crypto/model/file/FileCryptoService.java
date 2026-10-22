package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.AesGcm;
import gr.ihu.omada12.crypto.model.crypto.KeyDerivation;
import gr.ihu.omada12.crypto.model.io.AtomicFileWriter;
import gr.ihu.omada12.crypto.model.io.ProgressListener;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

// Υλοποιεί: ENC-04, ENC-05, ENC-10, NFR-01, NFR-02, NFR-05

/**
 * Κρυπτογράφηση/αποκρυπτογράφηση οποιουδήποτε αρχείου σε .enc με κωδικό.
 * Μορφή .enc: κεφαλίδα (EncFileHeader) + IV (12) + κρυπτοκείμενο + ετικέτα GCM.
 * Η έξοδος γράφεται ατομικά: με λάθος κωδικό ή αλλοιωμένο αρχείο ΔΕΝ δημιουργείται αρχείο.
 */
public class FileCryptoService {
    public static final String EXTENSION = ".enc";
    /** Η αποκρυπτογράφηση GCM κρατά τα δεδομένα στη μνήμη μέχρι τον έλεγχο της ετικέτας. */
    public static final long MAX_BYTES = 512L * 1024 * 1024;

    private final KeyDerivation derivation = new KeyDerivation();
    private final AesGcm gcm = new AesGcm();
    private final StreamCipherPump pump = new StreamCipherPump();
    private final AtomicFileWriter writer = new AtomicFileWriter();

    public void encrypt(Path source, Path target, char[] password, ProgressListener listener) throws CryptoException {
        long size = checkSource(source, target);
        byte[] salt = derivation.newSalt();
        SecretKey key = derivation.derive(password, salt);
        byte[] header = new EncFileHeader(salt).toBytes();
        byte[] iv = gcm.newIv();
        Cipher cipher = gcm.newCipher(Cipher.ENCRYPT_MODE, key, iv, header);
        writer.write(target, out -> {
            out.write(header);
            out.write(iv);
            try (InputStream in = new BufferedInputStream(Files.newInputStream(source))) {
                pump.pump(in, out, cipher, size, listener);
            }
        });
    }

    public void decrypt(Path source, Path target, char[] password, ProgressListener listener) throws CryptoException {
        long size = checkSource(source, target);
        writer.write(target, out -> {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(source))) {
                EncFileHeader header = EncFileHeader.read(in);
                byte[] iv = in.readNBytes(AesGcm.IV_BYTES);
                if (iv.length < AesGcm.IV_BYTES) throw new CryptoException("error.corrupted");
                SecretKey key = derivation.derive(password, header.getSalt());
                Cipher cipher = gcm.newCipher(Cipher.DECRYPT_MODE, key, iv, header.toBytes());
                pump.pump(in, out, cipher, size - EncFileHeader.LENGTH - AesGcm.IV_BYTES, listener);
            }
        });
    }

    /** Προτεινόμενο όνομα εξόδου: έγγραφο.pdf → έγγραφο.pdf.enc */
    public static Path encryptedName(Path source) {
        return source.resolveSibling(source.getFileName() + EXTENSION);
    }

    /** Προτεινόμενο όνομα εξόδου: έγγραφο.pdf.enc → έγγραφο.pdf (αλλιώς προσθήκη .dec) */
    public static Path decryptedName(Path source) {
        String name = source.getFileName().toString();
        if (name.toLowerCase().endsWith(EXTENSION) && name.length() > EXTENSION.length()) {
            return source.resolveSibling(name.substring(0, name.length() - EXTENSION.length()));
        }
        return source.resolveSibling(name + ".dec");
    }

    private long checkSource(Path source, Path target) throws CryptoException {
        try {
            if (!Files.isRegularFile(source)) throw new CryptoException("error.fileNotFound", source.getFileName());
            if (source.toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) {
                throw new CryptoException("error.sameFile");
            }
            long size = Files.size(source);
            if (size > MAX_BYTES) throw new CryptoException("error.fileTooLarge", MAX_BYTES / (1024 * 1024));
            return size;
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", source.getFileName());
        }
    }
}
