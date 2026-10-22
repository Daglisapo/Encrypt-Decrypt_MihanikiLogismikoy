package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.nio.charset.StandardCharsets;

// Υλοποιεί: STG-02, STG-06

/** Ό,τι βρέθηκε κρυμμένο στην εικόνα: κείμενο ή αρχείο. */
public class RevealedPayload {
    private final PayloadKind kind;
    private final byte[] body;

    public RevealedPayload(PayloadKind kind, byte[] body) {
        this.kind = kind;
        this.body = body;
    }

    public PayloadKind getKind() { return kind; }

    public boolean isFile() { return kind.isFile(); }

    public String getText() { return new String(body, StandardCharsets.UTF_8); }

    public HiddenFile getFile() throws CryptoException { return HiddenFile.decode(body); }
}
