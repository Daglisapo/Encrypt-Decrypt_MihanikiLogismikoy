package gr.ihu.omada12.crypto.model.cipher;

import java.util.List;

// Υλοποιεί: ENC-01, NFR-11

/**
 * Πρότυπο Factory: η διεπαφή ζητά αλγόριθμο με το όνομα, χωρίς να ξέρει την κλάση.
 * Νέος αλγόριθμος = νέα κλάση TextCipher + μία γραμμή στη λίστα και στο switch.
 */
public final class CipherFactory {
    public static final String CAESAR = "Caesar";
    public static final String VIGENERE = "Vigenère";
    public static final String AES = "AES-256";
    public static final String AES_KEY = "AES-256 (key)";

    private CipherFactory() { }

    public static List<String> availableAlgorithms() {
        return List.of(AES, AES_KEY, CAESAR, VIGENERE);
    }

    public static TextCipher create(String name) {
        switch (name) {
            case CAESAR:   return new CaesarCipher();
            case VIGENERE: return new VigenereCipher();
            case AES:      return new AesCipher();
            case AES_KEY:  return new AesKeyCipher();
            default: throw new IllegalArgumentException("Unknown algorithm: " + name);
        }
    }
}
