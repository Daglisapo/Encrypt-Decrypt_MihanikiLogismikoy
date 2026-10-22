package gr.ihu.omada12.crypto.model.vault;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** VLT-05, VLT-13, VLT-14 */
class VaultFilterTest {
    private final List<VaultEntry> entries = List.of(
            new VaultEntry("1", "Διαβατήριο.pdf", "έγγραφα", LocalDateTime.of(2026, 1, 10, 9, 0), new byte[1]),
            new VaultEntry("2", "beach.PNG", "διακοπές", LocalDateTime.of(2026, 7, 2, 18, 0), new byte[1]),
            new VaultEntry("3", "σημειώσεις.txt", "", LocalDateTime.of(2026, 10, 1, 12, 0), new byte[1]));

    @Test
    void filtersByNameTypeTagAndDate() {
        assertEquals(1, new VaultFilter().text("διαβατ").apply(entries).size());
        assertEquals(1, new VaultFilter().type("png").apply(entries).size());
        assertEquals(1, new VaultFilter().tag("διακοπές").apply(entries).size());
        assertEquals(2, new VaultFilter().from(LocalDate.of(2026, 7, 1)).apply(entries).size());
        assertEquals(1, new VaultFilter().from(LocalDate.of(2026, 1, 1)).to(LocalDate.of(2026, 1, 31)).apply(entries).size());
        assertEquals(3, new VaultFilter().apply(entries).size());
    }

    @Test
    void listsDistinctTypesAndTags() {
        assertEquals(List.of("PDF", "PNG", "TXT"), VaultFilter.typesOf(entries));
        // Φυσική σειρά Unicode: το «έ» (U+03AD) είναι πριν από το «δ» (U+03B4)
        assertEquals(List.of("έγγραφα", "διακοπές"), VaultFilter.tagsOf(entries));
    }

    @Test
    void previewKindFromType() {
        assertEquals(PreviewKind.IMAGE, PreviewKind.of("PNG"));
        assertEquals(PreviewKind.TEXT, PreviewKind.of("txt"));
        assertEquals(PreviewKind.NONE, PreviewKind.of("PDF"));
    }
}
