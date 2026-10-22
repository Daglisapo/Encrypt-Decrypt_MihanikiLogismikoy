package gr.ihu.omada12.crypto.model.history;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: APP-09, APP-10, NFR-12

/** Ιστορικό σε απλό αρχείο κειμένου (μία γραμμή ανά ενέργεια), όχι σε βάση δεδομένων. */
public class HistoryRepository {
    private final Path file;

    public HistoryRepository(Path file) { this.file = file; }

    public void add(HistoryEntry entry) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, entry.toLine() + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<HistoryEntry> findAll() throws IOException {
        List<HistoryEntry> result = new ArrayList<>();
        if (!Files.exists(file)) return result;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            try {
                result.add(HistoryEntry.fromLine(line));
            } catch (RuntimeException e) {
                // χαλασμένη γραμμή: την παραλείπουμε αντί να χαθεί όλο το ιστορικό
            }
        }
        return result;
    }

    public void clear() throws IOException { Files.deleteIfExists(file); }
}
