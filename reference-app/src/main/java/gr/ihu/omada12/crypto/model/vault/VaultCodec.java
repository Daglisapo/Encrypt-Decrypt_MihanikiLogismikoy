package gr.ihu.omada12.crypto.model.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: VLT-03, VLT-04 (μορφή του αποκρυπτογραφημένου περιεχομένου)

/**
 * Μετατρέπει τη λίστα αρχείων σε bytes και αντίστροφα. Αυτά τα bytes κρυπτογραφούνται
 * ΟΛΑ μαζί από το VaultStore, άρα χωρίς κωδικό δεν διαβάζεται ούτε η λίστα με τα ονόματα.
 * Μορφή: έκδοση, πλήθος, και για κάθε αρχείο: id, όνομα, ετικέτα, ημερομηνία, μήκος, δεδομένα.
 */
public class VaultCodec {
    private static final int FORMAT = 1;

    public byte[] encode(List<VaultEntry> entries) throws CryptoException {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(FORMAT);
            out.writeInt(entries.size());
            for (VaultEntry e : entries) {
                out.writeUTF(e.getId());
                out.writeUTF(e.getName());
                out.writeUTF(e.getTag());
                out.writeUTF(e.getAddedAt().toString());
                out.writeInt(e.rawData().length);
                out.write(e.rawData());
            }
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new CryptoException("error.vaultCorrupted", e);
        }
    }

    public List<VaultEntry> decode(byte[] plain) throws CryptoException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(plain))) {
            if (in.readInt() != FORMAT) throw new CryptoException("error.unsupportedVersion");
            int count = in.readInt();
            List<VaultEntry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                String id = in.readUTF();
                String name = in.readUTF();
                String tag = in.readUTF();
                LocalDateTime addedAt = LocalDateTime.parse(in.readUTF());
                byte[] data = new byte[in.readInt()];
                in.readFully(data);
                entries.add(new VaultEntry(id, name, tag, addedAt, data));
            }
            return entries;
        } catch (IOException | RuntimeException e) {
            throw new CryptoException("error.vaultCorrupted", e);
        }
    }
}
