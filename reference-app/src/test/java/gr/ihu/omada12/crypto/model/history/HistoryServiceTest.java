package gr.ihu.omada12.crypto.model.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** APP-09, APP-10, NFR-03 */
class HistoryServiceTest {
    @TempDir
    Path dir;
    private Path file;
    private HistoryService history;

    @BeforeEach
    void setUp() {
        file = dir.resolve("history.log");
        history = new HistoryService(new HistoryRepository(file));
    }

    @Test
    void recordsAreReturnedNewestFirstAndFiltered() throws Exception {
        history.record(AppModule.ENCRYPTION, "action.encryptFile", "report.pdf", true);
        history.record(AppModule.VAULT, "action.vaultUnlock", "Προσωπικά", false);
        history.record(AppModule.STEGO, "action.stegoHide", "cat.png", true);

        List<HistoryEntry> all = history.find(new HistoryFilter());
        assertEquals(3, all.size());
        assertEquals(AppModule.STEGO, all.get(0).getModule());
        assertEquals(1, history.find(new HistoryFilter().module(AppModule.VAULT)).size());
        assertEquals(1, history.find(new HistoryFilter().failuresOnly(true)).size());
        assertEquals("report.pdf", history.find(new HistoryFilter().text("REPORT")).get(0).getFileName());
    }

    @Test // NFR-03: δεν υπάρχει τρόπος να γραφτεί κωδικός· το αρχείο έχει μόνο τα 5 πεδία
    void fileContainsOnlyMetadata() throws Exception {
        history.record(AppModule.ENCRYPTION, "action.encryptText", "", true);
        String line = Files.readAllLines(file).get(0);
        assertEquals(5, line.split("\t", -1).length);
    }

    @Test // APP-10: απενεργοποίηση και καθαρισμός
    void disabledHistoryWritesNothingAndClearRemovesAll() throws Exception {
        history.setEnabled(false);
        history.record(AppModule.LAB, "action.benchmark", "big.iso", true);
        assertFalse(Files.exists(file));

        history.setEnabled(true);
        history.record(AppModule.LAB, "action.benchmark", "big.iso", true);
        assertTrue(Files.exists(file));
        history.clear();
        assertTrue(history.find(new HistoryFilter()).isEmpty());
    }

    @Test
    void tabsAndNewlinesInNamesDoNotBreakTheFile() throws Exception {
        history.record(AppModule.ENCRYPTION, "action.encryptFile", "κακό\tόνομα\n.txt", true);
        assertEquals("κακό όνομα .txt", history.find(new HistoryFilter()).get(0).getFileName());
    }
}
