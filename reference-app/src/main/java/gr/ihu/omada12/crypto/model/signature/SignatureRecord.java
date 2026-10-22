package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.crypto.Hex;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

// Υλοποιεί: SIG-02, SIG-06

/**
 * Το περιεχόμενο ενός αρχείου .sig: ποιο αρχείο, πότε, με ποιο κλειδί, και η ίδια η υπογραφή.
 * Η υπογραφή καλύπτει το SHA-256 του αρχείου ΚΑΙ την ημερομηνία ΚΑΙ το αποτύπωμα κλειδιού,
 * άρα κανένα από αυτά δεν αλλάζει χωρίς να «χαλάσει» η υπογραφή.
 */
public class SignatureRecord {
    private final String fileName;
    private final LocalDateTime signedAt;
    private final String keyFingerprint;
    private final byte[] signature;

    public SignatureRecord(String fileName, LocalDateTime signedAt, String keyFingerprint, byte[] signature) {
        this.fileName = fileName;
        this.signedAt = signedAt;
        this.keyFingerprint = keyFingerprint;
        this.signature = signature.clone();
    }

    public String getFileName() { return fileName; }

    public LocalDateTime getSignedAt() { return signedAt; }

    public String getKeyFingerprint() { return keyFingerprint; }

    public byte[] getSignature() { return signature.clone(); }

    /** Τα bytes που υπογράφονται πραγματικά. */
    public static byte[] payload(byte[] fileDigest, LocalDateTime signedAt, String fingerprint) {
        String text = "omada12-signature-v1\n" + Hex.encode(fileDigest) + "\n" + signedAt + "\n" + fingerprint;
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
