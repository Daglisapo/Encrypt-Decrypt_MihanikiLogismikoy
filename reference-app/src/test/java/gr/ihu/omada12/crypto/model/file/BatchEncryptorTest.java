package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.io.ProgressListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** ENC-11: μαζική κρυπτογράφηση φακέλου. */
class BatchEncryptorTest {

    @Test
    void encryptsWholeFolderKeepingStructure(@TempDir Path dir) throws Exception {
        Path in = Files.createDirectories(dir.resolve("in/sub"));
        Files.writeString(dir.resolve("in/a.txt"), "a");
        Files.writeString(in.resolve("b.txt"), "b");
        Files.writeString(dir.resolve("in/old.enc"), "ήδη κρυπτογραφημένο");
        Path out = dir.resolve("out");

        BatchEncryptor batch = new BatchEncryptor(new FileCryptoService());
        List<Path> files = batch.collect(dir.resolve("in"));
        assertEquals(2, files.size());

        BatchReport report = batch.encryptAll(files, dir.resolve("in"), out, "pw".toCharArray(), ProgressListener.NONE);
        assertEquals(2, report.getSucceeded().size());
        assertTrue(report.getFailed().isEmpty());
        assertTrue(Files.exists(out.resolve("a.txt.enc")));
        assertTrue(Files.exists(out.resolve("sub/b.txt.enc")));
    }
}
