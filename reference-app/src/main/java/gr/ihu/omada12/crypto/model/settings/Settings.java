package gr.ihu.omada12.crypto.model.settings;

// Υλοποιεί: APP-06, APP-07, APP-08, APP-10, APP-12, VLT-08

/** Οι ρυθμίσεις του χρήστη με τις προεπιλεγμένες τιμές τους. */
public class Settings {
    /** Θέμα εμφάνισης. */
    public enum Theme { LIGHT, DARK }

    public static final String DEFAULT_ACCENT = "#2f6fdf";

    private String language = "el";
    private Theme theme = Theme.LIGHT;
    private String accent = DEFAULT_ACCENT;
    private int autoLockMinutes = 5;
    private boolean historyEnabled = true;
    private boolean tutorialDone;

    public String getLanguage() { return language; }

    public void setLanguage(String language) { this.language = "en".equals(language) ? "en" : "el"; }

    public Theme getTheme() { return theme; }

    public void setTheme(Theme theme) { this.theme = theme == null ? Theme.LIGHT : theme; }

    public String getAccent() { return accent; }

    /** Μόνο χρώματα της μορφής #RRGGBB, ώστε να μπαίνουν με ασφάλεια στο CSS. */
    public void setAccent(String accent) {
        this.accent = accent != null && accent.matches("#[0-9a-fA-F]{6}") ? accent : DEFAULT_ACCENT;
    }

    public int getAutoLockMinutes() { return autoLockMinutes; }

    /** 0 = χωρίς αυτόματο κλείδωμα λόγω αδράνειας (κλειδώνει πάντως κατά την έξοδο). */
    public void setAutoLockMinutes(int minutes) { this.autoLockMinutes = Math.max(0, Math.min(120, minutes)); }

    public boolean isHistoryEnabled() { return historyEnabled; }

    public void setHistoryEnabled(boolean historyEnabled) { this.historyEnabled = historyEnabled; }

    public boolean isTutorialDone() { return tutorialDone; }

    public void setTutorialDone(boolean tutorialDone) { this.tutorialDone = tutorialDone; }
}
