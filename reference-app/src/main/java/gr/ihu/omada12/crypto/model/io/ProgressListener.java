package gr.ihu.omada12.crypto.model.io;

// Υλοποιεί: ENC-10

/**
 * Το μοντέλο αναφέρει πρόοδο και ρωτά αν ο χρήστης πάτησε «Ακύρωση»,
 * χωρίς να ξέρει τίποτα για JavaFX (το συνδέει ο controller με μπάρα προόδου).
 */
public interface ProgressListener {
    /** Ακροατής που δεν κάνει τίποτα, για κλήσεις χωρίς μπάρα προόδου. */
    ProgressListener NONE = new ProgressListener() {
        @Override public void onProgress(long done, long total) { }
        @Override public boolean isCancelled() { return false; }
    };

    void onProgress(long done, long total);

    boolean isCancelled();
}
