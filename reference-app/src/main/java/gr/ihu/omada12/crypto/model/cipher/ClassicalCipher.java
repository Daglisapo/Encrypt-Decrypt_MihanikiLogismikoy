package gr.ihu.omada12.crypto.model.cipher;

// Υλοποιεί: ENC-01, ENC-02

/** Κοινή λογική για τους κλασικούς αλγορίθμους: μετατόπιση γράμματος στο λατινικό αλφάβητο. */
public abstract class ClassicalCipher implements TextCipher {
    protected static final int ALPHABET = 26;

    /** Μετατοπίζει ένα γράμμα κατά shift θέσεις, κρατά κεφαλαία/πεζά, αφήνει ίδια τα μη-γράμματα. */
    public static char shift(char c, int shift) {
        if (c >= 'A' && c <= 'Z') return (char) ('A' + Math.floorMod(c - 'A' + shift, ALPHABET));
        if (c >= 'a' && c <= 'z') return (char) ('a' + Math.floorMod(c - 'a' + shift, ALPHABET));
        return c;
    }

    /** true μόνο για λατινικά γράμματα, τα μόνα που αλλάζουν οι κλασικοί αλγόριθμοι. */
    public static boolean isLatinLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    protected void requireText(String text) throws CryptoException {
        if (text == null || text.isBlank()) throw new CryptoException("error.emptyText");
    }
}
