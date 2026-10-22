package gr.ihu.omada12.crypto.model.vault;

import java.nio.file.Path;
import java.time.LocalDateTime;

// Υλοποιεί: VLT-11

/** Ό,τι φαίνεται για ένα θησαυροφυλάκιο ΧΩΡΙΣ ξεκλείδωμα: όνομα, μέγεθος δοχείου, τελευταία αλλαγή. */
public class VaultInfo {
    private final String name;
    private final Path file;
    private final long sizeBytes;
    private final LocalDateTime modified;

    public VaultInfo(String name, Path file, long sizeBytes, LocalDateTime modified) {
        this.name = name;
        this.file = file;
        this.sizeBytes = sizeBytes;
        this.modified = modified;
    }

    public String getName() { return name; }

    public Path getFile() { return file; }

    public long getSizeBytes() { return sizeBytes; }

    public LocalDateTime getModified() { return modified; }
}
