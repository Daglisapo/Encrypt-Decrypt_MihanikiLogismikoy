package gr.ihu.omada12.crypto.model.password;

// Υλοποιεί: LAB-07

/**
 * Εκτίμηση χρόνου «σπασίματος» με εξαντλητική αναζήτηση: κατά μέσο όρο ψάχνουμε τον μισό χώρο,
 * 2^(bits-1) δοκιμές, με 10^10 δοκιμές/δευτερόλεπτο (κάρτα γραφικών σε γρήγορο hash, χειρότερο σενάριο).
 * Η μονάδα (δευτερόλεπτα, λεπτά… αιώνες) επιλέγεται ώστε ο αριθμός να διαβάζεται εύκολα.
 */
public class CrackTime {
    public static final double GUESSES_PER_SECOND = 1e10;

    /** Μονάδες χρόνου με τη διάρκειά τους σε δευτερόλεπτα. */
    public enum Unit {
        INSTANT(0), SECONDS(1), MINUTES(60), HOURS(3600), DAYS(86_400), YEARS(31_557_600), CENTURIES(3_155_760_000L);

        private final long seconds;

        Unit(long seconds) { this.seconds = seconds; }

        public long getSeconds() { return seconds; }
    }

    private final double seconds;
    private final Unit unit;

    public CrackTime(double seconds) {
        this.seconds = seconds;
        this.unit = unitFor(seconds);
    }

    public static CrackTime forBits(double bits) {
        return new CrackTime(Math.pow(2, Math.max(0, bits - 1)) / GUESSES_PER_SECOND);
    }

    public double getSeconds() { return seconds; }

    public Unit getUnit() { return unit; }

    /** Ο αριθμός στη μονάδα που επιλέχθηκε (π.χ. 3.2 ημέρες). */
    public double getAmount() { return unit == Unit.INSTANT ? 0 : seconds / unit.getSeconds(); }

    static Unit unitFor(double seconds) {
        Unit chosen = Unit.INSTANT;
        for (Unit u : Unit.values()) {
            if (u != Unit.INSTANT && seconds >= u.getSeconds()) chosen = u;
        }
        return chosen;
    }
}
