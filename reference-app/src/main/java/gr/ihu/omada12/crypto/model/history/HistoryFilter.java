package gr.ihu.omada12.crypto.model.history;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// Υλοποιεί: APP-10

/** Φίλτρο ιστορικού: ενότητα, μόνο αποτυχίες, κείμενο στο όνομα αρχείου. null/κενό = όλα. */
public class HistoryFilter {
    private AppModule module;
    private boolean failuresOnly;
    private String text = "";

    public HistoryFilter module(AppModule value) { module = value; return this; }

    public HistoryFilter failuresOnly(boolean value) { failuresOnly = value; return this; }

    public HistoryFilter text(String value) { text = value == null ? "" : value.toLowerCase(Locale.ROOT); return this; }

    public boolean matches(HistoryEntry e) {
        return (module == null || module == e.getModule())
                && (!failuresOnly || !e.isSuccess())
                && (text.isEmpty() || e.getFileName().toLowerCase(Locale.ROOT).contains(text));
    }

    public List<HistoryEntry> apply(List<HistoryEntry> entries) {
        return entries.stream().filter(this::matches).collect(Collectors.toList());
    }
}
