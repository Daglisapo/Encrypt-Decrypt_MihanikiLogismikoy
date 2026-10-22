package gr.ihu.omada12.crypto.model.vault;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// Υλοποιεί: VLT-12

/**
 * Μετά από 3 λάθος κωδικούς, η επόμενη προσπάθεια επιτρέπεται μετά από 30 δευτερόλεπτα.
 * Κάθε επόμενο λάθος διπλασιάζει την αναμονή (έως 15 λεπτά). Το Clock δίνεται απ' έξω
 * ώστε τα tests να «προχωρούν» τον χρόνο χωρίς αναμονή.
 */
public class LoginThrottle {
    public static final int FREE_ATTEMPTS = 3;
    public static final long BASE_DELAY_SECONDS = 30;
    public static final long MAX_DELAY_SECONDS = 15 * 60;

    private final Clock clock;
    private final Map<String, Integer> failures = new HashMap<>();
    private final Map<String, Instant> blockedUntil = new HashMap<>();

    public LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    /** Δευτερόλεπτα αναμονής πριν από την επόμενη προσπάθεια (0 = επιτρέπεται τώρα). */
    public long remainingSeconds(String id) {
        Instant until = blockedUntil.get(id);
        if (until == null) return 0;
        long left = Duration.between(clock.instant(), until).toMillis();
        return left <= 0 ? 0 : (left + 999) / 1000;
    }

    public void recordFailure(String id) {
        int count = failures.getOrDefault(id, 0) + 1;
        failures.put(id, count);
        if (count >= FREE_ATTEMPTS) {
            long delay = Math.min(MAX_DELAY_SECONDS, BASE_DELAY_SECONDS << Math.min(count - FREE_ATTEMPTS, 10));
            blockedUntil.put(id, clock.instant().plusSeconds(delay));
        }
    }

    public void recordSuccess(String id) {
        failures.remove(id);
        blockedUntil.remove(id);
    }

    public int failures(String id) { return failures.getOrDefault(id, 0); }
}
