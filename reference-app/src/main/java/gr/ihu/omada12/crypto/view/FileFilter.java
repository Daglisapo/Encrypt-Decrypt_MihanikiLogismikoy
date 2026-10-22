package gr.ihu.omada12.crypto.view;

import java.util.List;

// Υλοποιεί: ENC-06 (φίλτρα στα παράθυρα επιλογής αρχείων)

/**
 * Φίλτρο αρχείων για τα παράθυρα επιλογής: κλειδί bundle για την περιγραφή + μοτίβα (π.χ. *.pdf).
 * Δική μας κλάση αντί για FileChooser.ExtensionFilter, ώστε οι μέθοδοι να μην έχουν παραμέτρους
 * τύπου JavaFX (το MetricsCalculator δεν τους αναγνωρίζει και μηδενίζει τις μετρικές της κλάσης).
 */
public class FileFilter {
    private final String descriptionKey;
    private final List<String> patterns;

    public FileFilter(String descriptionKey, String... patterns) {
        this.descriptionKey = descriptionKey;
        this.patterns = List.of(patterns);
    }

    public String getDescriptionKey() { return descriptionKey; }

    public List<String> getPatterns() { return patterns; }
}
