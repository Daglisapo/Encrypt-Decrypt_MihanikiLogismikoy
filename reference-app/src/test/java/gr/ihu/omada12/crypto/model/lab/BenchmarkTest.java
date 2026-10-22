package gr.ihu.omada12.crypto.model.lab;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.ProgressListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** LAB-01, LAB-02, LAB-04 */
class BenchmarkTest {
    private final byte[] data = new byte[256 * 1024];

    @Test
    void comparesThreeAlgorithmsWithSensibleNumbers() throws CryptoException {
        List<BenchmarkResult> results = new BenchmarkRunner().run(BenchmarkCatalog.all(), data, 3, ProgressListener.NONE);
        assertEquals(3, results.size());
        for (BenchmarkResult r : results) {
            assertEquals(data.length, r.getInputBytes());
            assertTrue(r.getOutputBytes() > data.length, r.getAlgorithm());
            assertTrue(r.getEncryptMillis() > 0 && r.getEncryptMBps() > 0, r.getAlgorithm());
            assertEquals(3, r.getRepetitions());
        }
        // GCM και ChaCha20-Poly1305 προσθέτουν IV 12 + ετικέτα 16 bytes
        assertEquals(data.length + 28, results.get(0).getOutputBytes());
        assertEquals(data.length + 28, results.get(2).getOutputBytes());
    }

    @Test
    void invalidRepetitionsAreRejected() {
        assertThrows(CryptoException.class,
                () -> new BenchmarkRunner().run(BenchmarkCatalog.all(), data, 0, ProgressListener.NONE));
    }

    @Test // LAB-04
    void csvHasHeaderAndOneLinePerAlgorithm(@TempDir Path dir) throws Exception {
        List<BenchmarkResult> results = List.of(new BenchmarkResult("AES-256-GCM", 1_000_000, 1_000_028, 2.5, 2.0, 5));
        Path csv = dir.resolve("r.csv");
        new BenchmarkCsv().write(results, csv);
        List<String> lines = Files.readAllLines(csv);
        assertEquals(BenchmarkCsv.HEADER, lines.get(0));
        assertEquals("AES-256-GCM,1000000,1000028,5,2.500,2.000,400.00,500.00", lines.get(1));
    }
}
