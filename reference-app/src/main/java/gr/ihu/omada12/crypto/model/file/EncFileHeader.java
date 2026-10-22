package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.KeyDerivation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

// Υλοποιεί: ENC-04, ENC-05 (μορφή αρχείου .enc)

/**
 * Κεφαλίδα αρχείου .enc: "O12F" (4 bytes) + έκδοση (1 byte) + salt (16 bytes).
 * Ολόκληρη η κεφαλίδα μπαίνει ως AAD στο GCM, άρα αν αλλάξει έστω ένα byte
 * η αποκρυπτογράφηση αποτυγχάνει.
 */
public final class EncFileHeader {
    static final byte[] MAGIC = {'O', '1', '2', 'F'};
    static final byte VERSION = 1;
    public static final int LENGTH = MAGIC.length + 1 + KeyDerivation.SALT_BYTES;

    private final byte[] salt;

    public EncFileHeader(byte[] salt) {
        this.salt = salt.clone();
    }

    public byte[] getSalt() { return salt.clone(); }

    public byte[] toBytes() {
        return ByteBuffer.allocate(LENGTH).put(MAGIC).put(VERSION).put(salt).array();
    }

    /** Διαβάζει και ελέγχει την κεφαλίδα από την αρχή του stream. */
    public static EncFileHeader read(InputStream in) throws IOException, CryptoException {
        byte[] raw = in.readNBytes(LENGTH);
        if (raw.length < LENGTH || !Arrays.equals(Arrays.copyOf(raw, MAGIC.length), MAGIC)) {
            throw new CryptoException("error.notEncFile");
        }
        if (raw[MAGIC.length] != VERSION) throw new CryptoException("error.unsupportedVersion");
        return new EncFileHeader(Arrays.copyOfRange(raw, MAGIC.length + 1, LENGTH));
    }
}
