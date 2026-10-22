package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.Hex;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;

// Υλοποιεί: SIG-01, SIG-06 (αποτύπωμα κλειδιού)

/**
 * Ο αλγόριθμος υπογραφής της εφαρμογής: Ed25519 (υπάρχει στη Java από την έκδοση 15).
 * Μικρά κλειδιά (32 bytes), γρήγορη υπογραφή, χωρίς παραμέτρους που μπορεί να ρυθμιστούν λάθος.
 */
public final class SignatureAlgorithm {
    public static final String NAME = "Ed25519";

    private SignatureAlgorithm() { }

    public static KeyPair generateKeyPair() throws CryptoException {
        try {
            return KeyPairGenerator.getInstance(NAME).generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.keyGeneration", e);
        }
    }

    public static Signature newSignature() throws CryptoException {
        try {
            return Signature.getInstance(NAME);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.signFailed", e);
        }
    }

    public static KeyFactory keyFactory() throws CryptoException {
        try {
            return KeyFactory.getInstance(NAME);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.keyGeneration", e);
        }
    }

    /** Αποτύπωμα δημόσιου κλειδιού: SHA-256 της κωδικοποίησής του, σε ομάδες hex. */
    public static String fingerprint(PublicKey key) throws CryptoException {
        return Hex.grouped(new FileHasher().sha256(key.getEncoded()));
    }
}
