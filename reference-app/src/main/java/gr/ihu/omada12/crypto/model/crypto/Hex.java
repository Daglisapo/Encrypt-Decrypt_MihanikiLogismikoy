package gr.ihu.omada12.crypto.model.crypto;

// Υλοποιεί: SIG-05, SIG-06 (εμφάνιση αποτυπωμάτων)

/** Μετατροπή bytes σε δεκαεξαδικό κείμενο (π.χ. για αποτυπώματα SHA-256). */
public final class Hex {
    private static final char[] DIGITS = "0123456789abcdef".toCharArray();

    private Hex() { }

    public static String encode(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(DIGITS[(b >> 4) & 0xF]).append(DIGITS[b & 0xF]);
        }
        return sb.toString();
    }

    /** Ίδιο με encode, σε ομάδες των 4 χαρακτήρων για να διαβάζεται/συγκρίνεται με το μάτι. */
    public static String grouped(byte[] bytes) {
        String hex = encode(bytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 4) {
            if (i > 0) sb.append(' ');
            sb.append(hex, i, Math.min(i + 4, hex.length()));
        }
        return sb.toString();
    }
}
