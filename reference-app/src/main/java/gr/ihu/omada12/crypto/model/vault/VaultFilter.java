package gr.ihu.omada12.crypto.model.vault;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;

// Υλοποιεί: VLT-05, VLT-13

/** Φίλτρο αναζήτησης: κείμενο στο όνομα, τύπος, ετικέτα και διάστημα ημερομηνιών. Κενό πεδίο = χωρίς περιορισμό. */
public class VaultFilter {
    private String text = "";
    private String type = "";
    private String tag = "";
    private LocalDate from;
    private LocalDate to;

    public VaultFilter text(String value) { text = value == null ? "" : value.trim().toLowerCase(Locale.ROOT); return this; }

    public VaultFilter type(String value) { type = value == null ? "" : value; return this; }

    public VaultFilter tag(String value) { tag = value == null ? "" : value; return this; }

    public VaultFilter from(LocalDate value) { from = value; return this; }

    public VaultFilter to(LocalDate value) { to = value; return this; }

    public boolean matches(VaultEntry e) {
        LocalDate day = e.getAddedAt().toLocalDate();
        return (text.isEmpty() || e.getName().toLowerCase(Locale.ROOT).contains(text))
                && (type.isEmpty() || type.equalsIgnoreCase(e.getType()))
                && (tag.isEmpty() || tag.equalsIgnoreCase(e.getTag()))
                && (from == null || !day.isBefore(from))
                && (to == null || !day.isAfter(to));
    }

    public List<VaultEntry> apply(List<VaultEntry> entries) {
        return entries.stream().filter(this::matches).collect(Collectors.toList());
    }

    /** Οι διαφορετικοί τύποι που υπάρχουν, για τη λίστα επιλογής του φίλτρου. */
    public static List<String> typesOf(List<VaultEntry> entries) {
        return new ArrayList<>(entries.stream().map(VaultEntry::getType).filter(t -> !t.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new)));
    }

    public static List<String> tagsOf(List<VaultEntry> entries) {
        return new ArrayList<>(entries.stream().map(VaultEntry::getTag).filter(t -> !t.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new)));
    }
}
