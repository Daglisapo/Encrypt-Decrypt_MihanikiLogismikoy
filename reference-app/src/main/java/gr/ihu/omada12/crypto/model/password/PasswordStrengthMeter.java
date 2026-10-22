package gr.ihu.omada12.crypto.model.password;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Υλοποιεί: VLT-01 (ένδειξη ισχύος), LAB-07

/**
 * Εκτίμηση ισχύος κωδικού σε bits εντροπίας.
 * bits ≈ «ενεργό μήκος» × log2(μέγεθος αλφαβήτου). Επαναλήψεις (aaaa) και ακολουθίες (1234, abcd)
 * μετράνε λιγότερο, και οι πολύ γνωστοί κωδικοί πέφτουν σχεδόν στο μηδέν.
 */
public class PasswordStrengthMeter {
    private static final Set<String> COMMON = Set.of("password", "123456", "12345678", "123456789", "qwerty",
            "111111", "000000", "letmein", "admin", "welcome", "iloveyou", "monkey", "dragon", "abc123",
            "football", "password1", "qwerty123", "1q2w3e4r", "sunshine", "princess", "passw0rd", "123123");
    private static final double COMMON_BITS = 8;

    public PasswordStrength evaluate(String password) {
        String pw = password == null ? "" : password;
        int pool = poolSize(pw);
        boolean common = COMMON.contains(pw.toLowerCase(Locale.ROOT));
        double bits = pool == 0 ? 0 : effectiveLength(pw) * (Math.log(pool) / Math.log(2));
        if (common) bits = Math.min(bits, COMMON_BITS);
        return new PasswordStrength(scoreOf(bits), bits, CrackTime.forBits(bits), hints(pw, common));
    }

    /** 0 = πολύ αδύναμος … 4 = πολύ ισχυρός */
    static int scoreOf(double bits) {
        if (bits < 28) return 0;
        if (bits < 36) return 1;
        if (bits < 60) return 2;
        if (bits < 80) return 3;
        return 4;
    }

    static int poolSize(String pw) {
        boolean lower = false, upper = false, digit = false, symbol = false, other = false;
        for (char c : pw.toCharArray()) {
            if (c >= 'a' && c <= 'z') lower = true;
            else if (c >= 'A' && c <= 'Z') upper = true;
            else if (c >= '0' && c <= '9') digit = true;
            else if (c < 128) symbol = true;
            else other = true; // π.χ. ελληνικά γράμματα
        }
        return (lower ? 26 : 0) + (upper ? 26 : 0) + (digit ? 10 : 0) + (symbol ? 33 : 0) + (other ? 48 : 0);
    }

    /** Επανάληψη του προηγούμενου χαρακτήρα μετρά 1/4, διαδοχικός χαρακτήρας (a→b, 1→2) μετρά 1/2. */
    static double effectiveLength(String pw) {
        double length = 0;
        for (int i = 0; i < pw.length(); i++) {
            int diff = i == 0 ? Integer.MAX_VALUE : Math.abs(pw.charAt(i) - pw.charAt(i - 1));
            if (diff == 0) length += 0.25;
            else if (diff == 1) length += 0.5;
            else length += 1;
        }
        return length;
    }

    private List<String> hints(String pw, boolean common) {
        List<String> hints = new ArrayList<>();
        if (common) hints.add("pw.hint.common");
        if (pw.length() < 12) hints.add("pw.hint.length");
        if (classes(pw) < 3) hints.add("pw.hint.mix");
        return hints;
    }

    private int classes(String pw) {
        int count = 0;
        if (pw.chars().anyMatch(Character::isLowerCase)) count++;
        if (pw.chars().anyMatch(Character::isUpperCase)) count++;
        if (pw.chars().anyMatch(Character::isDigit)) count++;
        if (pw.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) count++;
        return count;
    }
}
