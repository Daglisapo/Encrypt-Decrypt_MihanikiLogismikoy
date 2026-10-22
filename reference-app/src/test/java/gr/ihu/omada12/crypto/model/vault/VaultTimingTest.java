package gr.ihu.omada12.crypto.model.vault;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** VLT-08 (αδράνεια) και VLT-12 (καθυστέρηση), με «ψεύτικο» ρολόι. */
class VaultTimingTest {

    @Test
    void throttleDoublesDelayAfterEachExtraFailure() {
        MutableClock clock = new MutableClock();
        LoginThrottle throttle = new LoginThrottle(clock);
        throttle.recordFailure("v");
        throttle.recordFailure("v");
        assertEquals(0, throttle.remainingSeconds("v"));
        throttle.recordFailure("v");
        assertEquals(30, throttle.remainingSeconds("v"));
        throttle.recordFailure("v");
        assertEquals(60, throttle.remainingSeconds("v"));
        throttle.recordSuccess("v");
        assertEquals(0, throttle.remainingSeconds("v"));
    }

    @Test
    void sessionLocksAfterIdleTimeout() throws Exception {
        MutableClock clock = new MutableClock();
        VaultSession session = new VaultSession(new InactivityTimer(clock, Duration.ofMinutes(5)));
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("o12");
        Vault vault = new VaultStore().create(dir.resolve("t" + Vault.EXTENSION), VaultTest.MASTER);
        session.open(vault);

        clock.advance(Duration.ofMinutes(4));
        session.touch();
        clock.advance(Duration.ofMinutes(4));
        assertFalse(session.lockIfIdle());
        clock.advance(Duration.ofMinutes(1));
        assertTrue(session.lockIfIdle());
        assertTrue(vault.isLocked());
        assertFalse(session.isOpen());
    }
}
