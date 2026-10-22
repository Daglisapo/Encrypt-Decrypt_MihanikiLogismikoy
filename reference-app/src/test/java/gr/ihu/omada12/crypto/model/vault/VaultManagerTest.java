package gr.ihu.omada12.crypto.model.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;

import static gr.ihu.omada12.crypto.model.vault.VaultTest.MASTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** VLT-01, VLT-11, VLT-12 */
class VaultManagerTest {
    @TempDir
    Path dir;
    private MutableClock clock;
    private VaultManager manager;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        manager = new VaultManager(dir, new VaultStore(), new LoginThrottle(clock));
    }

    @Test // VLT-01: επιβεβαίωση κωδικού και ελάχιστη ισχύς
    void createValidatesConfirmationAndStrength() {
        CryptoException mismatch = assertThrows(CryptoException.class,
                () -> manager.create("a", MASTER, "άλλος".toCharArray()));
        assertEquals("error.passwordMismatch", mismatch.getMessageKey());
        CryptoException weak = assertThrows(CryptoException.class,
                () -> manager.create("a", "123456".toCharArray(), "123456".toCharArray()));
        assertEquals("error.passwordTooWeak", weak.getMessageKey());
        assertThrows(CryptoException.class, () -> manager.create("../κακό", MASTER, MASTER));
    }

    @Test // VLT-11: λίστα, μετονομασία, διαγραφή
    void listRenameDelete() throws Exception {
        manager.create("Εργασία", MASTER, MASTER).lock();
        manager.create("Σπίτι", MASTER, MASTER).lock();
        assertEquals(2, manager.list().size());

        manager.rename("Σπίτι", "Οικογένεια");
        assertTrue(manager.list().stream().anyMatch(v -> v.getName().equals("Οικογένεια")));
        assertThrows(CryptoException.class, () -> manager.rename("Εργασία", "Οικογένεια"));

        manager.delete("Εργασία");
        assertEquals(1, manager.list().size());
    }

    @Test // VLT-12: μετά από 3 λάθη, αναμονή 30 s
    void threeWrongPasswordsTriggerDelay() throws Exception {
        manager.create("v", MASTER, MASTER).lock();
        for (int i = 0; i < 3; i++) {
            assertThrows(CryptoException.class, () -> manager.unlock("v", "λάθος".toCharArray()));
        }
        CryptoException blocked = assertThrows(CryptoException.class, () -> manager.unlock("v", MASTER));
        assertEquals("error.vaultThrottled", blocked.getMessageKey());
        assertEquals(30L, blocked.getArgs()[0]);

        clock.advance(Duration.ofSeconds(31));
        assertTrue(manager.unlock("v", MASTER).getEntries().isEmpty());
    }
}
