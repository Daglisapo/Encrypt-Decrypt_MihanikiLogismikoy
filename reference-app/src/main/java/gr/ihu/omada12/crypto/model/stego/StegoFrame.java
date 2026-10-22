package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.nio.ByteBuffer;
import java.util.Arrays;

// Υλοποιεί: STG-01, STG-02

/**
 * «Πλαίσιο» των κρυμμένων δεδομένων: "O12S" (4) + είδος (1) + μήκος (4) + σώμα.
 * Η υπογραφή "O12S" επιτρέπει να πούμε «δεν υπάρχει μήνυμα» αντί να διαβάσουμε θόρυβο.
 */
public final class StegoFrame {
    static final byte[] MAGIC = {'O', '1', '2', 'S'};
    public static final int HEADER_LENGTH = MAGIC.length + 1 + 4;

    private final PayloadKind kind;
    private final int bodyLength;

    private StegoFrame(PayloadKind kind, int bodyLength) {
        this.kind = kind;
        this.bodyLength = bodyLength;
    }

    public PayloadKind getKind() { return kind; }

    public int getBodyLength() { return bodyLength; }

    public static byte[] build(PayloadKind kind, byte[] body) {
        return ByteBuffer.allocate(HEADER_LENGTH + body.length)
                .put(MAGIC).put((byte) kind.getCode()).putInt(body.length).put(body).array();
    }

    public static StegoFrame parseHeader(byte[] header, long capacity) throws CryptoException {
        if (!Arrays.equals(Arrays.copyOf(header, MAGIC.length), MAGIC)) throw new CryptoException("error.noHiddenMessage");
        ByteBuffer buf = ByteBuffer.wrap(header, MAGIC.length, HEADER_LENGTH - MAGIC.length);
        PayloadKind kind = PayloadKind.fromCode(buf.get());
        int length = buf.getInt();
        if (length < 0 || length > capacity - HEADER_LENGTH) throw new CryptoException("error.noHiddenMessage");
        return new StegoFrame(kind, length);
    }
}
