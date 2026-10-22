package gr.ihu.omada12.crypto.model.password;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: LAB-08

/**
 * Γεννήτρια ισχυρών κωδικών με κρυπτογραφικά ασφαλή τυχαιότητα (SecureRandom).
 * Από κάθε επιλεγμένη κατηγορία μπαίνει τουλάχιστον ένας χαρακτήρας και στο τέλος ανακατεύονται.
 */
public class PasswordGenerator {
    static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String DIGITS = "0123456789";
    static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?";

    private final SecureRandom random = new SecureRandom();

    public String generate(PasswordPolicy policy) throws CryptoException {
        if (policy.getLength() < PasswordPolicy.MIN_LENGTH || policy.getLength() > PasswordPolicy.MAX_LENGTH) {
            throw new CryptoException("error.generatorLength", PasswordPolicy.MIN_LENGTH, PasswordPolicy.MAX_LENGTH);
        }
        List<String> groups = groupsOf(policy);
        if (groups.isEmpty()) throw new CryptoException("error.generatorNoClasses");

        StringBuilder all = new StringBuilder();
        List<Character> chars = new ArrayList<>();
        for (String g : groups) {
            all.append(g);
            chars.add(pick(g));
        }
        while (chars.size() < policy.getLength()) chars.add(pick(all));
        shuffle(chars);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    private List<String> groupsOf(PasswordPolicy p) {
        List<String> groups = new ArrayList<>();
        if (p.useLower()) groups.add(LOWER);
        if (p.useUpper()) groups.add(UPPER);
        if (p.useDigits()) groups.add(DIGITS);
        if (p.useSymbols()) groups.add(SYMBOLS);
        return groups;
    }

    private char pick(CharSequence from) { return from.charAt(random.nextInt(from.length())); }

    /** Fisher-Yates: κάθε διάταξη είναι εξίσου πιθανή. */
    private void shuffle(List<Character> chars) {
        for (int i = chars.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Character tmp = chars.get(i);
            chars.set(i, chars.get(j));
            chars.set(j, tmp);
        }
    }
}
