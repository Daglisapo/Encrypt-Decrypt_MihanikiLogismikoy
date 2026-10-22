package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// Υλοποιεί: SIG-02

/** Υπογράφει ένα αρχείο με το ιδιωτικό κλειδί και επιστρέφει τα στοιχεία για το .sig */
public class FileSigner {
    private final FileHasher hasher = new FileHasher();

    public SignatureRecord sign(Path file, KeyPair keys) throws CryptoException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String fingerprint = SignatureAlgorithm.fingerprint(keys.getPublic());
        byte[] payload = SignatureRecord.payload(hasher.sha256(file), now, fingerprint);
        try {
            Signature signer = SignatureAlgorithm.newSignature();
            signer.initSign(keys.getPrivate());
            signer.update(payload);
            return new SignatureRecord(file.getFileName().toString(), now, fingerprint, signer.sign());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.signFailed", e);
        }
    }
}
