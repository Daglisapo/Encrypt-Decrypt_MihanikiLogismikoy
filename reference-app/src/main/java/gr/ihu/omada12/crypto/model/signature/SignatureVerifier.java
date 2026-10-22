package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;

// Υλοποιεί: SIG-03, SIG-06

/**
 * Επαλήθευση: ξαναϋπολογίζει το SHA-256 του αρχείου και ελέγχει την υπογραφή με το δημόσιο κλειδί.
 * Αν άλλαξε έστω ένα byte του αρχείου, το αποτύπωμα αλλάζει και η υπογραφή δεν ταιριάζει.
 */
public class SignatureVerifier {
    private final FileHasher hasher = new FileHasher();

    public VerificationResult verify(Path file, SignatureRecord record, PublicKey publicKey) throws CryptoException {
        boolean keyMatches = SignatureAlgorithm.fingerprint(publicKey).equals(record.getKeyFingerprint());
        byte[] payload = SignatureRecord.payload(hasher.sha256(file), record.getSignedAt(), record.getKeyFingerprint());
        try {
            Signature verifier = SignatureAlgorithm.newSignature();
            verifier.initVerify(publicKey);
            verifier.update(payload);
            return new VerificationResult(keyMatches && verifier.verify(record.getSignature()), keyMatches, record);
        } catch (GeneralSecurityException e) {
            // κατεστραμμένη υπογραφή = μη έγκυρη, όχι σφάλμα εφαρμογής
            return new VerificationResult(false, keyMatches, record);
        }
    }
}
