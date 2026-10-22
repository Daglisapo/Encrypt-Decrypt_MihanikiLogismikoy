package gr.ihu.omada12.crypto.model.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// Υλοποιεί: APP-08

/** Αποθήκευση ρυθμίσεων σε αρχείο .properties, ώστε να διατηρούνται μετά την επανεκκίνηση. */
public class SettingsRepository {
    private final Path file;

    public SettingsRepository(Path file) { this.file = file; }

    /** Αν το αρχείο λείπει ή είναι χαλασμένο, επιστρέφει τις προεπιλογές. */
    public Settings load() {
        Settings s = new Settings();
        if (!Files.exists(file)) return s;
        Properties p = new Properties();
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(in);
            s.setLanguage(p.getProperty("language", s.getLanguage()));
            s.setTheme(Settings.Theme.valueOf(p.getProperty("theme", s.getTheme().name())));
            s.setAccent(p.getProperty("accent", s.getAccent()));
            s.setAutoLockMinutes(Integer.parseInt(p.getProperty("autoLockMinutes", "5")));
            s.setHistoryEnabled(Boolean.parseBoolean(p.getProperty("historyEnabled", "true")));
            s.setTutorialDone(Boolean.parseBoolean(p.getProperty("tutorialDone", "false")));
        } catch (IOException | RuntimeException e) {
            return new Settings();
        }
        return s;
    }

    public void save(Settings s) throws IOException {
        Properties p = new Properties();
        p.setProperty("language", s.getLanguage());
        p.setProperty("theme", s.getTheme().name());
        p.setProperty("accent", s.getAccent());
        p.setProperty("autoLockMinutes", String.valueOf(s.getAutoLockMinutes()));
        p.setProperty("historyEnabled", String.valueOf(s.isHistoryEnabled()));
        p.setProperty("tutorialDone", String.valueOf(s.isTutorialDone()));
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            p.store(out, "Omada 12 - settings");
        }
    }
}
