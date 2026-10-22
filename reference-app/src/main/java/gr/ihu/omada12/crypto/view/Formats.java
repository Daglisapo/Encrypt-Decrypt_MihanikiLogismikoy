package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.password.CrackTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Υλοποιεί: VLT-04, LAB-07, APP-09 (μορφοποίηση για εμφάνιση)

/** Μορφοποίηση αριθμών, μεγεθών, ημερομηνιών και χρόνων για εμφάνιση στη διεπαφή. */
public final class Formats {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] UNITS = {"B", "KB", "MB", "GB"};

    private Formats() { }

    public static String size(long bytes) {
        double value = bytes;
        int unit = 0;
        while (value >= 1024 && unit < UNITS.length - 1) {
            value /= 1024;
            unit++;
        }
        return unit == 0 ? bytes + " B" : String.format(Locale.ROOT, "%.1f %s", value, UNITS[unit]);
    }

    public static String dateTime(LocalDateTime t) { return t == null ? "" : DATE_TIME.format(t); }

    public static String number(double value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }

    /** π.χ. «3,2 ημέρες» / «μεγαλύτερο από 1.000.000 αιώνες» */
    public static String crackTime(I18n i18n, CrackTime time) {
        String unitKey = "pw.crack." + time.getUnit().name().toLowerCase(Locale.ROOT);
        double amount = time.getAmount();
        if (amount > 1_000_000) return i18n.text("pw.crack.huge", i18n.text(unitKey, "1.000.000"));
        return i18n.text(unitKey, number(amount, amount < 10 ? 1 : 0));
    }
}
