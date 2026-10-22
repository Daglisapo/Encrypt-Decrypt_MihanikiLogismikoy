package gr.ihu.omada12.crypto.model.vault;

import java.time.LocalDateTime;
import java.util.Arrays;

// Υλοποιεί: VLT-03, VLT-04, VLT-13

/** Ένα αρχείο μέσα στο θησαυροφυλάκιο: στοιχεία (όνομα, τύπος, μέγεθος, ημερομηνία, ετικέτα) και περιεχόμενο. */
public class VaultEntry {
    private final String id;
    private final String name;
    private final LocalDateTime addedAt;
    private String tag;
    private byte[] data;

    public VaultEntry(String id, String name, String tag, LocalDateTime addedAt, byte[] data) {
        this.id = id;
        this.name = name;
        this.tag = tag == null ? "" : tag;
        this.addedAt = addedAt;
        this.data = data;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getTag() { return tag; }

    void setTag(String tag) { this.tag = tag == null ? "" : tag.trim(); }

    public LocalDateTime getAddedAt() { return addedAt; }

    public long getSize() { return data.length; }

    /** Ο τύπος είναι η κατάληξη του ονόματος με κεφαλαία (PDF, PNG…), ή κενό. */
    public String getType() {
        int dot = name.lastIndexOf('.');
        return dot < 0 || dot == name.length() - 1 ? "" : name.substring(dot + 1).toUpperCase();
    }

    /** Αντίγραφο του περιεχομένου, για να μην αλλάζει κανείς απ' έξω τα δεδομένα του θησαυροφυλακίου. */
    public byte[] copyData() { return data.clone(); }

    byte[] rawData() { return data; }

    /** Μηδενίζει το περιεχόμενο στη μνήμη κατά το κλείδωμα (VLT-07). */
    void wipe() {
        Arrays.fill(data, (byte) 0);
        data = new byte[0];
    }
}
