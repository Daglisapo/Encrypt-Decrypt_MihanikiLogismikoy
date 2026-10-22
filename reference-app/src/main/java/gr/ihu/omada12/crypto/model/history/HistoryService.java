package gr.ihu.omada12.crypto.model.history;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

// Υλοποιεί: APP-09, APP-10, NFR-03

/**
 * Καταγραφή ενεργειών από όλες τις ενότητες. Αν ο χρήστης το απενεργοποιήσει, δεν γράφεται τίποτα.
 * Αποτυχία εγγραφής ιστορικού δεν σταματά ποτέ την κύρια ενέργεια του χρήστη.
 */
public class HistoryService {
    private final HistoryRepository repository;
    private boolean enabled = true;

    public HistoryService(HistoryRepository repository) {
        this.repository = repository;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEnabled() { return enabled; }

    /** @param fileName μόνο το ΟΝΟΜΑ του αρχείου (ή κενό), ποτέ περιεχόμενο ή κωδικός */
    public void record(AppModule module, String actionKey, String fileName, boolean success) {
        if (!enabled) return;
        try {
            repository.add(new HistoryEntry(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                    module, actionKey, fileName, success));
        } catch (IOException ignored) {
            // το ιστορικό είναι βοηθητικό: δεν «ρίχνουμε» την κύρια ενέργεια
        }
    }

    /** Νεότερες εγγραφές πρώτα. */
    public List<HistoryEntry> find(HistoryFilter filter) throws IOException {
        List<HistoryEntry> all = filter.apply(repository.findAll());
        Collections.reverse(all);
        return all;
    }

    public void clear() throws IOException { repository.clear(); }
}
