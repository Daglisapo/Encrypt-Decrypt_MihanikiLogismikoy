package gr.ihu.omada12.crypto.model.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** VLT-01…VLT-10, VLT-13, VLT-14 */
class VaultTest {
    static final char[] MASTER = "Θησαυρός-2026-Ασφαλής!".toCharArray();

    @TempDir
    Path dir;
    private VaultManager manager;
    private Path secretFile;

    @BeforeEach
    void setUp() throws Exception {
        manager = new VaultManager(dir.resolve("vaults"), new VaultStore(), new LoginThrottle(new MutableClock()));
        secretFile = Files.writeString(dir.resolve("diary-secret.txt"), "Πολύ προσωπικό περιεχόμενο");
    }

    @Test // VLT-03 + η λίστα και τα περιεχόμενα δεν φαίνονται στο δοχείο
    void containerRevealsNeitherNamesNorContent() throws Exception {
        Vault vault = manager.create("Προσωπικά", MASTER, MASTER);
        vault.addFile(secretFile, "ημερολόγιο");
        String raw = new String(Files.readAllBytes(vault.getFile()), StandardCharsets.ISO_8859_1);
        assertFalse(raw.contains("diary-secret"));
        assertFalse(new String(Files.readAllBytes(vault.getFile()), StandardCharsets.UTF_8).contains("προσωπικό"));
    }

    @Test // VLT-02, VLT-04, VLT-07
    void lockThenUnlockShowsSameEntries() throws Exception {
        Vault vault = manager.create("v1", MASTER, MASTER);
        vault.addFile(secretFile, "");
        vault.lock();
        assertTrue(vault.isLocked());
        CryptoException e = assertThrows(CryptoException.class, vault::getEntries);
        assertEquals("error.vaultLocked", e.getMessageKey());

        Vault reopened = manager.unlock("v1", MASTER);
        List<VaultEntry> entries = reopened.getEntries();
        assertEquals(1, entries.size());
        assertEquals("diary-secret.txt", entries.get(0).getName());
        assertEquals("TXT", entries.get(0).getType());
        assertEquals(Files.size(secretFile), entries.get(0).getSize());
        assertEquals(LocalDate.now(), entries.get(0).getAddedAt().toLocalDate());
    }

    @Test
    void wrongMasterPasswordIsRejected() throws Exception {
        manager.create("v2", MASTER, MASTER).lock();
        CryptoException e = assertThrows(CryptoException.class, () -> manager.unlock("v2", "λάθος".toCharArray()));
        assertEquals("error.wrongMasterPassword", e.getMessageKey());
    }

    @Test // VLT-06 + VLT-14
    void exportAndPreviewGiveOriginalBytes() throws Exception {
        Vault vault = manager.create("v3", MASTER, MASTER);
        VaultEntry entry = vault.addFile(secretFile, "");
        assertArrayEquals(Files.readAllBytes(secretFile), vault.read(entry.getId()));
        Path out = Files.createDirectories(dir.resolve("export"));
        Path exported = vault.export(entry.getId(), out);
        assertArrayEquals(Files.readAllBytes(secretFile), Files.readAllBytes(exported));
    }

    @Test // VLT-09 + VLT-13: οι αλλαγές αποθηκεύονται αμέσως
    void deleteAndTagArePersisted() throws Exception {
        Vault vault = manager.create("v4", MASTER, MASTER);
        VaultEntry keep = vault.add("a.png", new byte[]{1, 2, 3}, "");
        VaultEntry gone = vault.add("b.pdf", new byte[]{4}, "");
        vault.setTag(keep.getId(), "φωτογραφίες");
        vault.delete(gone.getId());
        vault.lock();

        List<VaultEntry> entries = manager.unlock("v4", MASTER).getEntries();
        assertEquals(1, entries.size());
        assertEquals("φωτογραφίες", entries.get(0).getTag());
    }

    @Test // VLT-10
    void changePasswordReplacesOldOne() throws Exception {
        char[] next = "Νέος-Κύριος-Κωδικός-99".toCharArray();
        Vault vault = manager.create("v5", MASTER, MASTER);
        assertThrows(CryptoException.class, () -> vault.changePassword("λάθος".toCharArray(), next));
        vault.changePassword(MASTER, next);
        vault.lock();
        assertThrows(CryptoException.class, () -> manager.unlock("v5", MASTER));
        assertTrue(manager.unlock("v5", next).getEntries().isEmpty());
    }
}
