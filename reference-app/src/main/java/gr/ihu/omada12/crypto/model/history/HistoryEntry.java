package gr.ihu.omada12.crypto.model.history;

import java.time.LocalDateTime;

// Υλοποιεί: APP-09, NFR-03

/**
 * Μία εγγραφή ιστορικού: ημερομηνία, ενότητα, ενέργεια, όνομα αρχείου, αποτέλεσμα.
 * Δεν υπάρχει πεδίο για κωδικό, κλειδί ή περιεχόμενο, άρα δεν μπορούν να γραφτούν ούτε κατά λάθος (NFR-03).
 */
public class HistoryEntry {
    private static final String SEP = "\t";

    private final LocalDateTime timestamp;
    private final AppModule module;
    private final String actionKey;
    private final String fileName;
    private final boolean success;

    public HistoryEntry(LocalDateTime timestamp, AppModule module, String actionKey, String fileName, boolean success) {
        this.timestamp = timestamp;
        this.module = module;
        this.actionKey = actionKey;
        this.fileName = clean(fileName);
        this.success = success;
    }

    public LocalDateTime getTimestamp() { return timestamp; }

    public AppModule getModule() { return module; }

    /** Κλειδί bundle της ενέργειας (π.χ. action.encryptFile), για να εμφανίζεται στη γλώσσα του χρήστη. */
    public String getActionKey() { return actionKey; }

    public String getFileName() { return fileName; }

    public boolean isSuccess() { return success; }

    String toLine() {
        return timestamp + SEP + module + SEP + actionKey + SEP + fileName + SEP + (success ? "OK" : "FAIL");
    }

    static HistoryEntry fromLine(String line) {
        String[] p = line.split(SEP, -1);
        return new HistoryEntry(LocalDateTime.parse(p[0]), AppModule.valueOf(p[1]), p[2], p[3], "OK".equals(p[4]));
    }

    /** Χωρίς tab και αλλαγές γραμμής, ώστε μία εγγραφή = μία γραμμή στο αρχείο. */
    private static String clean(String value) {
        return value == null ? "" : value.replaceAll("[\\t\\r\\n]", " ");
    }
}
