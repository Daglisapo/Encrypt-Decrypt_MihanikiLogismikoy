package gr.ihu.omada12.crypto.model.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** APP-06, APP-07, APP-08 */
class SettingsRepositoryTest {

    @Test
    void settingsSurviveRestart(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("settings.properties");
        Settings s = new Settings();
        s.setLanguage("en");
        s.setTheme(Settings.Theme.DARK);
        s.setAccent("#aa3355");
        s.setAutoLockMinutes(10);
        s.setHistoryEnabled(false);
        s.setTutorialDone(true);
        new SettingsRepository(file).save(s);

        Settings loaded = new SettingsRepository(file).load(); // «νέα εκκίνηση»
        assertEquals("en", loaded.getLanguage());
        assertEquals(Settings.Theme.DARK, loaded.getTheme());
        assertEquals("#aa3355", loaded.getAccent());
        assertEquals(10, loaded.getAutoLockMinutes());
        assertFalse(loaded.isHistoryEnabled());
        assertTrue(loaded.isTutorialDone());
    }

    @Test
    void missingOrBrokenFileGivesDefaults(@TempDir Path dir) throws Exception {
        assertEquals("el", new SettingsRepository(dir.resolve("none")).load().getLanguage());
        Path broken = Files.writeString(dir.resolve("b.properties"), "theme=PURPLE\nautoLockMinutes=abc");
        Settings s = new SettingsRepository(broken).load();
        assertEquals(Settings.Theme.LIGHT, s.getTheme());
        assertEquals(5, s.getAutoLockMinutes());
    }

    @Test
    void invalidValuesAreSanitised() {
        Settings s = new Settings();
        s.setAccent("red; -fx-background-image: url(x)");
        assertEquals(Settings.DEFAULT_ACCENT, s.getAccent());
        s.setLanguage("fr");
        assertEquals("el", s.getLanguage());
        s.setAutoLockMinutes(-3);
        assertEquals(0, s.getAutoLockMinutes());
    }
}
