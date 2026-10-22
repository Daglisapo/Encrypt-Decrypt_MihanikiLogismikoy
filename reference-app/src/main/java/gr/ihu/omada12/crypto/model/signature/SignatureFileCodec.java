package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Υλοποιεί: SIG-02 (ξεχωριστό αρχείο .sig), SIG-06

/**
 * Το .sig είναι απλό κείμενο «κλειδί=τιμή», ώστε να ανοίγει και με Notepad:
 * <pre>
 * format=omada12-signature-v1
 * algorithm=Ed25519
 * file=report.pdf
 * signedAt=2026-10-22T14:03:11
 * keyFingerprint=1a2b 3c4d …
 * signature=Base64…
 * </pre>
 */
public class SignatureFileCodec {
    public static final String EXTENSION = ".sig";
    private static final String FORMAT = "omada12-signature-v1";

    public String encode(SignatureRecord r) {
        return "format=" + FORMAT + "\n"
                + "algorithm=" + SignatureAlgorithm.NAME + "\n"
                + "file=" + r.getFileName() + "\n"
                + "signedAt=" + r.getSignedAt() + "\n"
                + "keyFingerprint=" + r.getKeyFingerprint() + "\n"
                + "signature=" + Base64.getEncoder().encodeToString(r.getSignature()) + "\n";
    }

    public SignatureRecord decode(String text) throws CryptoException {
        Map<String, String> values = new HashMap<>();
        for (String line : text.split("\\R")) {
            int eq = line.indexOf('=');
            if (eq > 0) values.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
        }
        if (!FORMAT.equals(values.get("format"))) throw new CryptoException("error.notSignature");
        try {
            return new SignatureRecord(values.get("file"), LocalDateTime.parse(values.get("signedAt")),
                    values.get("keyFingerprint"), Base64.getDecoder().decode(values.get("signature")));
        } catch (RuntimeException e) {
            throw new CryptoException("error.notSignature");
        }
    }

    public void write(SignatureRecord record, Path file) throws CryptoException {
        try {
            Files.writeString(file, encode(record), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CryptoException("error.fileWrite", file.getFileName());
        }
    }

    public SignatureRecord read(Path file) throws CryptoException {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return decode(String.join("\n", lines));
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", file.getFileName());
        }
    }

    /** report.pdf → report.pdf.sig */
    public static Path signatureFileFor(Path file) {
        return file.resolveSibling(file.getFileName() + EXTENSION);
    }
}
