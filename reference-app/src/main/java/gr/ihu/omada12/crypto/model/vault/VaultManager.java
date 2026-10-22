package gr.ihu.omada12.crypto.model.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.password.PasswordStrengthMeter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Υλοποιεί: VLT-01, VLT-02, VLT-11, VLT-12

/**
 * Διαχείριση πολλών θησαυροφυλακίων σε έναν φάκελο: λίστα, δημιουργία, ξεκλείδωμα
 * (με καθυστέρηση μετά από λάθη), μετονομασία, διαγραφή.
 */
public class VaultManager {
    /** Ελάχιστη ισχύς κύριου κωδικού: «μέτριος» (2 στα 0-4). */
    public static final int MIN_SCORE = 2;
    private static final String NAME_PATTERN = "[\\p{L}\\p{N} _-]{1,40}";

    private final Path directory;
    private final VaultStore store;
    private final LoginThrottle throttle;

    public VaultManager(Path directory, VaultStore store, LoginThrottle throttle) {
        this.directory = directory;
        this.store = store;
        this.throttle = throttle;
    }

    public List<VaultInfo> list() throws CryptoException {
        List<VaultInfo> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) return result;
        try (Stream<Path> files = Files.list(directory)) {
            for (Path p : files.sorted().collect(Collectors.toList())) {
                String n = p.getFileName().toString();
                if (!n.endsWith(Vault.EXTENSION)) continue;
                LocalDateTime modified = LocalDateTime.ofInstant(Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault());
                result.add(new VaultInfo(n.substring(0, n.length() - Vault.EXTENSION.length()), p, Files.size(p), modified));
            }
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", directory.getFileName());
        }
        return result;
    }

    /** VLT-01: όνομα, κύριος κωδικός και επιβεβαίωση. Ο κωδικός πρέπει να είναι τουλάχιστον «μέτριος». */
    public Vault create(String name, char[] password, char[] confirm) throws CryptoException {
        Path file = pathOf(name);
        checkNewPassword(password, confirm);
        createDirectory();
        return store.create(file, password);
    }

    /** Κοινός έλεγχος νέου κύριου κωδικού (δημιουργία και αλλαγή, VLT-01/VLT-10). */
    public static void checkNewPassword(char[] password, char[] confirm) throws CryptoException {
        if (!Arrays.equals(password, confirm)) throw new CryptoException("error.passwordMismatch");
        if (new PasswordStrengthMeter().evaluate(new String(password)).getScore() < MIN_SCORE) {
            throw new CryptoException("error.passwordTooWeak");
        }
    }

    /** VLT-02 + VLT-12: ξεκλείδωμα με καθυστέρηση μετά από 3 λάθη. */
    public Vault unlock(String name, char[] password) throws CryptoException {
        long wait = throttle.remainingSeconds(name);
        if (wait > 0) throw new CryptoException("error.vaultThrottled", wait);
        try {
            Vault vault = store.open(pathOf(name), password);
            throttle.recordSuccess(name);
            return vault;
        } catch (CryptoException e) {
            if ("error.wrongMasterPassword".equals(e.getMessageKey())) throttle.recordFailure(name);
            throw e;
        }
    }

    public void rename(String oldName, String newName) throws CryptoException {
        Path target = pathOf(newName);
        if (Files.exists(target)) throw new CryptoException("error.vaultExists");
        try {
            Files.move(pathOf(oldName), target);
        } catch (IOException e) {
            throw new CryptoException("error.fileWrite", newName);
        }
    }

    public void delete(String name) throws CryptoException {
        try {
            Files.deleteIfExists(pathOf(name));
        } catch (IOException e) {
            throw new CryptoException("error.fileWrite", name);
        }
    }

    public long remainingWaitSeconds(String name) { return throttle.remainingSeconds(name); }

    public Path pathOf(String name) throws CryptoException {
        if (name == null || !name.trim().matches(NAME_PATTERN)) throw new CryptoException("error.vaultName");
        return directory.resolve(name.trim() + Vault.EXTENSION);
    }

    private void createDirectory() throws CryptoException {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new CryptoException("error.fileWrite", directory.getFileName());
        }
    }
}
