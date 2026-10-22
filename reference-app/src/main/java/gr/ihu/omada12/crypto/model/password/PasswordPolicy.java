package gr.ihu.omada12.crypto.model.password;

// Υλοποιεί: LAB-08

/** Επιλογές της γεννήτριας κωδικών: μήκος και ποιες κατηγορίες χαρακτήρων περιέχει. */
public class PasswordPolicy {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 128;

    private final int length;
    private final boolean lower;
    private final boolean upper;
    private final boolean digits;
    private final boolean symbols;

    public PasswordPolicy(int length, boolean lower, boolean upper, boolean digits, boolean symbols) {
        this.length = length;
        this.lower = lower;
        this.upper = upper;
        this.digits = digits;
        this.symbols = symbols;
    }

    public int getLength() { return length; }

    public boolean useLower() { return lower; }

    public boolean useUpper() { return upper; }

    public boolean useDigits() { return digits; }

    public boolean useSymbols() { return symbols; }
}
