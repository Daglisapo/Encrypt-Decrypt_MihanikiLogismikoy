package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

// Υλοποιεί: SIG-04

/**
 * Εξαγωγή/εισαγωγή δημόσιου κλειδιού στην πρότυπη μορφή PEM (κείμενο),
 * ώστε να σταλεί σε άλλον χρήστη που θα επαληθεύει τις υπογραφές μας.
 */
public class PublicKeyPem {
    public static final String EXTENSION = ".pub";
    private static final String BEGIN = "-----BEGIN PUBLIC KEY-----";
    private static final String END = "-----END PUBLIC KEY-----";

    public String encode(PublicKey key) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(key.getEncoded());
        return BEGIN + "\n" + b64 + "\n" + END + "\n";
    }

    public PublicKey decode(String pem) throws CryptoException {
        if (pem == null || !pem.contains(BEGIN) || !pem.contains(END)) throw new CryptoException("error.notPublicKey");
        String body = pem.substring(pem.indexOf(BEGIN) + BEGIN.length(), pem.indexOf(END)).replaceAll("\\s", "");
        try {
            byte[] der = Base64.getDecoder().decode(body);
            return SignatureAlgorithm.keyFactory().generatePublic(new X509EncodedKeySpec(der));
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new CryptoException("error.notPublicKey");
        }
    }

    public void export(PublicKey key, Path file) throws CryptoException {
        try {
            Files.writeString(file, encode(key), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new CryptoException("error.fileWrite", file.getFileName());
        }
    }

    public PublicKey importFrom(Path file) throws CryptoException {
        try {
            return decode(Files.readString(file, StandardCharsets.US_ASCII));
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", file.getFileName());
        }
    }
}
