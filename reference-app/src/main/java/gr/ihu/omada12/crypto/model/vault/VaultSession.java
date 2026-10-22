package gr.ihu.omada12.crypto.model.vault;

// Υλοποιεί: VLT-07, VLT-08, APP-03

/**
 * Κρατά το θησαυροφυλάκιο που είναι ανοιχτό τώρα. Κλειδώνει με ένα κουμπί,
 * μετά από αδράνεια ή κατά την έξοδο από την εφαρμογή.
 */
public class VaultSession {
    private final InactivityTimer timer;
    private Vault current;

    public VaultSession(InactivityTimer timer) {
        this.timer = timer;
    }

    public void open(Vault vault) {
        lock();
        current = vault;
        timer.touch();
    }

    public Vault current() { return current; }

    public boolean isOpen() { return current != null && !current.isLocked(); }

    public void touch() { timer.touch(); }

    public InactivityTimer getTimer() { return timer; }

    public void lock() {
        if (current != null) current.lock();
        current = null;
    }

    /** Καλείται περιοδικά από τη διεπαφή· επιστρέφει true αν μόλις κλείδωσε λόγω αδράνειας. */
    public boolean lockIfIdle() {
        if (isOpen() && timer.isExpired()) {
            lock();
            return true;
        }
        return false;
    }
}
