package gr.ihu.omada12.crypto.model.analysis;

// Υλοποιεί: LAB-05 (βαθμολόγηση), LAB-06

/**
 * Ανάλυση συχνοτήτων λατινικών γραμμάτων και σύγκριση με τα αγγλικά (τεστ χ²).
 * Μικρό χ² = το κείμενο μοιάζει με αγγλικό.
 */
public class FrequencyAnalyzer {
    /** Συχνότητες γραμμάτων στα αγγλικά (%), A..Z */
    public static final double[] ENGLISH = {8.2, 1.5, 2.8, 4.3, 12.7, 2.2, 2.0, 6.1, 7.0, 0.15, 0.77, 4.0, 2.4,
            6.7, 7.5, 1.9, 0.095, 6.0, 6.3, 9.1, 2.8, 0.98, 2.4, 0.15, 2.0, 0.074};

    public int[] counts(String text) {
        int[] counts = new int[26];
        for (char c : text.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') counts[c - 'A']++;
        }
        return counts;
    }

    /** Ποσοστό (%) κάθε γράμματος A..Z στο κείμενο, για το ραβδόγραμμα (LAB-06). */
    public double[] percentages(String text) {
        int[] counts = counts(text);
        int total = 0;
        for (int c : counts) total += c;
        double[] result = new double[26];
        for (int i = 0; i < 26 && total > 0; i++) result[i] = 100.0 * counts[i] / total;
        return result;
    }

    /** Στατιστικό χ²: πόσο απέχουν οι συχνότητες του κειμένου από τις αναμενόμενες των αγγλικών. */
    public double chiSquared(String text) {
        int[] counts = counts(text);
        int n = 0;
        for (int c : counts) n += c;
        if (n == 0) return Double.MAX_VALUE;
        double chi = 0;
        for (int i = 0; i < 26; i++) {
            double expected = n * ENGLISH[i] / 100.0;
            chi += Math.pow(counts[i] - expected, 2) / expected;
        }
        return chi;
    }
}
