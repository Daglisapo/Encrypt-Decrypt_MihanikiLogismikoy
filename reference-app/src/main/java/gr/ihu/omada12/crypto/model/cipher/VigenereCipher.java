package gr.ihu.omada12.crypto.model.cipher;

// Υλοποιεί: ENC-01, ENC-02 (Vigenère)

/** Vigenère: κάθε γράμμα μετατοπίζεται κατά το αντίστοιχο γράμμα της λέξης-κλειδιού. */
public class VigenereCipher extends ClassicalCipher {
    @Override public String getName() { return "Vigenère"; }

    @Override public String getKeyHintKey() { return "enc.hint.vigenere"; }

    @Override public void validateKey(String key) throws CryptoException {
        if (key == null || !key.matches("[A-Za-z]+")) throw new CryptoException("error.vigenereKey");
    }

    @Override public String encrypt(String text, String key) throws CryptoException { return run(text, key, 1); }

    @Override public String decrypt(String text, String key) throws CryptoException { return run(text, key, -1); }

    /** Η μετατόπιση που δίνει το γράμμα του κλειδιού στη θέση j (A=0, B=1, …). */
    public static int keyShift(String key, int j) {
        return Character.toUpperCase(key.charAt(j % key.length())) - 'A';
    }

    private String run(String text, String key, int direction) throws CryptoException {
        requireText(text);
        validateKey(key);
        StringBuilder sb = new StringBuilder(text.length());
        int j = 0; // προχωρά μόνο όταν συναντάμε γράμμα
        for (char c : text.toCharArray()) {
            if (isLatinLetter(c)) {
                sb.append(shift(c, keyShift(key, j) * direction));
                j++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
