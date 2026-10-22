package gr.ihu.omada12.crypto.model.io;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Υλοποιεί: VLT-03, STG-06 (ανάγνωση μικρών αρχείων ολόκληρων στη μνήμη)

/** Ανάγνωση/εγγραφή ολόκληρων αρχείων, με όριο μεγέθους για να μη «γεμίσει» η μνήμη. */
public class FileProcessor {
    public static final long MAX_BYTES = 100L * 1024 * 1024; // 100 MB

    public byte[] read(Path file) throws CryptoException {
        try {
            if (!Files.isRegularFile(file)) throw new CryptoException("error.fileNotFound", file.getFileName());
            if (Files.size(file) > MAX_BYTES) throw new CryptoException("error.fileTooLarge", MAX_BYTES / (1024 * 1024));
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", file.getFileName());
        }
    }

    public void write(Path file, byte[] data) throws CryptoException {
        new AtomicFileWriter().write(file, out -> out.write(data));
    }
}
