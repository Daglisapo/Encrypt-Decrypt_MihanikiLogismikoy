package gr.ihu.omada12.crypto.model.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.FileProcessor;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// Υλοποιεί: VLT-03, VLT-04, VLT-06, VLT-07, VLT-09, VLT-10, VLT-13, VLT-14

/**
 * Ένα ΞΕΚΛΕΙΔΩΜΕΝΟ θησαυροφυλάκιο. Κάθε αλλαγή (προσθήκη, διαγραφή, ετικέτα)
 * αποθηκεύεται αμέσως κρυπτογραφημένη στο δοχείο. Μετά το lock() τα δεδομένα
 * και το κλειδί σβήνονται από τη μνήμη και κάθε κλήση αποτυγχάνει.
 */
public class Vault {
    public static final String EXTENSION = ".o12vault";

    private final Path file;
    private final List<VaultEntry> entries;
    private final VaultStore store;
    private SecretKey key;
    private byte[] salt;

    Vault(Path file, SecretKey key, byte[] salt, List<VaultEntry> entries, VaultStore store) {
        this.file = file;
        this.key = key;
        this.salt = salt;
        this.entries = entries;
        this.store = store;
    }

    public Path getFile() { return file; }

    public String getName() {
        String n = file.getFileName().toString();
        return n.endsWith(EXTENSION) ? n.substring(0, n.length() - EXTENSION.length()) : n;
    }

    public boolean isLocked() { return key == null; }

    public List<VaultEntry> getEntries() throws CryptoException {
        requireUnlocked();
        return Collections.unmodifiableList(entries);
    }

    public VaultEntry addFile(Path source, String tag) throws CryptoException {
        return add(source.getFileName().toString(), new FileProcessor().read(source), tag);
    }

    public VaultEntry add(String name, byte[] data, String tag) throws CryptoException {
        requireUnlocked();
        VaultEntry entry = new VaultEntry(UUID.randomUUID().toString(), name, tag, LocalDateTime.now(), data);
        entries.add(entry);
        try {
            store.save(this);
        } catch (CryptoException e) {
            entries.remove(entry); // η μνήμη μένει ίδια με τον δίσκο
            throw e;
        }
        return entry;
    }

    public void delete(String id) throws CryptoException {
        VaultEntry entry = find(id);
        int index = entries.indexOf(entry);
        entries.remove(entry);
        try {
            store.save(this);
        } catch (CryptoException e) {
            entries.add(index, entry);
            throw e;
        }
    }

    public void setTag(String id, String tag) throws CryptoException {
        find(id).setTag(tag);
        store.save(this);
    }

    /** Περιεχόμενο για προεπισκόπηση στη μνήμη, χωρίς εγγραφή στον δίσκο (VLT-14). */
    public byte[] read(String id) throws CryptoException {
        return find(id).copyData();
    }

    /** Πού θα γραφτεί το αρχείο κατά την εξαγωγή (ο controller ελέγχει αν υπάρχει ήδη, APP-05). */
    public Path exportTarget(String id, Path directory) throws CryptoException {
        return directory.resolve(find(id).getName());
    }

    public Path export(String id, Path directory) throws CryptoException {
        Path target = exportTarget(id, directory);
        new FileProcessor().write(target, find(id).rawData());
        return target;
    }

    public void changePassword(char[] current, char[] next) throws CryptoException {
        requireUnlocked();
        if (!store.matches(current, salt, key)) throw new CryptoException("error.wrongMasterPassword");
        byte[] oldSalt = salt;
        SecretKey oldKey = key;
        salt = store.newSalt();
        key = store.derive(next, salt);
        try {
            store.save(this);
        } catch (CryptoException e) {
            salt = oldSalt;
            key = oldKey;
            throw e;
        }
    }

    /** Κλείδωμα: σβήνει περιεχόμενα και κλειδί από τη μνήμη (VLT-07). */
    public void lock() {
        for (VaultEntry e : entries) e.wipe();
        entries.clear();
        key = null;
        salt = null;
    }

    SecretKey key() { return key; }

    byte[] salt() { return salt; }

    List<VaultEntry> entriesForSave() { return entries; }

    private VaultEntry find(String id) throws CryptoException {
        requireUnlocked();
        for (VaultEntry e : entries) {
            if (e.getId().equals(id)) return e;
        }
        throw new CryptoException("error.vaultEntryMissing");
    }

    private void requireUnlocked() throws CryptoException {
        if (isLocked()) throw new CryptoException("error.vaultLocked");
    }
}
