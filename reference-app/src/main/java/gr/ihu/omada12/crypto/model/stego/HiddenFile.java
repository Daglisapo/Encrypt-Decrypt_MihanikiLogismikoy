package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// Υλοποιεί: STG-06

/** Μικρό αρχείο μέσα σε εικόνα: μήκος ονόματος (2) + όνομα UTF-8 + περιεχόμενο. */
public class HiddenFile {
    private final String name;
    private final byte[] data;

    public HiddenFile(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() { return name; }

    public byte[] getData() { return data.clone(); }

    public byte[] encode() {
        byte[] n = name.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(2 + n.length + data.length).putShort((short) n.length).put(n).put(data).array();
    }

    public static HiddenFile decode(byte[] bytes) throws CryptoException {
        try {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            byte[] n = new byte[buf.getShort() & 0xFFFF];
            buf.get(n);
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            return new HiddenFile(new String(n, StandardCharsets.UTF_8), data);
        } catch (BufferUnderflowException e) {
            throw new CryptoException("error.noHiddenMessage");
        }
    }
}
