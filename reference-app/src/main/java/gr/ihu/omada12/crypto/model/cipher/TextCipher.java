package gr.ihu.omada12.crypto.model.cipher;

// Υλοποιεί: ENC-01, ENC-02, NFR-11

/**
 * Κοινό «συμβόλαιο» όλων των αλγορίθμων κειμένου (πρότυπο Strategy).
 * Η διεπαφή χρήστη μιλά ΜΟΝΟ με αυτό το interface, άρα ένας νέος αλγόριθμος
 * είναι μία νέα κλάση + μία γραμμή στο CipherFactory (NFR-11).
 */
public interface TextCipher {
    String getName();

    String encrypt(String text, String key) throws CryptoException;

    String decrypt(String text, String key) throws CryptoException;

    void validateKey(String key) throws CryptoException;

    /** Κλειδί του bundle με οδηγία για το τι κλειδί περιμένει ο αλγόριθμος. */
    default String getKeyHintKey() { return "enc.hint.generic"; }
}
