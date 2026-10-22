package gr.ihu.omada12.crypto.model.cipher;

// Υλοποιεί: ENC-01, ENC-02 (Caesar), βάση για LAB-05

/** Κρυπτογράφηση Caesar: κάθε γράμμα μετατοπίζεται κατά σταθερό αριθμό θέσεων (1-25). */
public class CaesarCipher extends ClassicalCipher {
    @Override public String getName() { return "Caesar"; }

    @Override public String getKeyHintKey() { return "enc.hint.caesar"; }

    @Override public void validateKey(String key) throws CryptoException {
        try {
            int k = Integer.parseInt(key.trim());
            if (k < 1 || k > 25) throw new CryptoException("error.caesarRange");
        } catch (NumberFormatException | NullPointerException e) {
            throw new CryptoException("error.caesarNotNumber");
        }
    }

    @Override public String encrypt(String text, String key) throws CryptoException {
        requireText(text);
        validateKey(key);
        return apply(text, Integer.parseInt(key.trim()));
    }

    @Override public String decrypt(String text, String key) throws CryptoException {
        requireText(text);
        validateKey(key);
        return apply(text, -Integer.parseInt(key.trim()));
    }

    /** Δημόσια ώστε να τη χρησιμοποιεί και η κρυπτανάλυση (brute force). */
    public String apply(String text, int shift) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) sb.append(shift(c, shift));
        return sb.toString();
    }
}
