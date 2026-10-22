package gr.ihu.omada12.crypto.model.vault;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

// Υλοποιεί: VLT-08

/** Μετρά πόσος χρόνος πέρασε από την τελευταία κίνηση του χρήστη (ρυθμιζόμενο όριο αδράνειας). */
public class InactivityTimer {
    private final Clock clock;
    private Duration timeout;
    private Instant lastActivity;

    public InactivityTimer(Clock clock, Duration timeout) {
        this.clock = clock;
        this.timeout = timeout;
        this.lastActivity = clock.instant();
    }

    /** Κάθε κλικ ή πλήκτρο «μηδενίζει» τον μετρητή. */
    public void touch() { lastActivity = clock.instant(); }

    public void setTimeout(Duration timeout) { this.timeout = timeout; }

    public Duration getTimeout() { return timeout; }

    public boolean isExpired() {
        return !timeout.isZero() && Duration.between(lastActivity, clock.instant()).compareTo(timeout) >= 0;
    }
}
