package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.ProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Υλοποιεί: ENC-11

/**
 * Μαζική κρυπτογράφηση πολλών αρχείων ή ολόκληρου φακέλου.
 * Η δομή υποφακέλων διατηρείται στον φάκελο εξόδου. Αν ένα αρχείο αποτύχει,
 * καταγράφεται και η διαδικασία συνεχίζει με τα υπόλοιπα.
 */
public class BatchEncryptor {
    private final FileCryptoService service;

    public BatchEncryptor(FileCryptoService service) {
        this.service = service;
    }

    /** Όλα τα κανονικά αρχεία του φακέλου (αναδρομικά), εκτός από όσα είναι ήδη .enc */
    public List<Path> collect(Path folder) throws CryptoException {
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().toLowerCase().endsWith(FileCryptoService.EXTENSION))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CryptoException("error.fileRead", folder.getFileName());
        }
    }

    /**
     * @param baseDir φάκελος αναφοράς για τις σχετικές διαδρομές (null = μόνο το όνομα του αρχείου)
     */
    public BatchReport encryptAll(List<Path> files, Path baseDir, Path outputDir, char[] password,
                                  ProgressListener listener) throws CryptoException {
        BatchReport report = new BatchReport();
        for (int i = 0; i < files.size(); i++) {
            if (listener.isCancelled()) throw new CryptoException("error.cancelled");
            Path source = files.get(i);
            Path target = FileCryptoService.encryptedName(outputDir.resolve(relative(baseDir, source)));
            try {
                Files.createDirectories(target.getParent());
                service.encrypt(source, target, password, ProgressListener.NONE);
                report.addSuccess(target);
            } catch (CryptoException e) {
                report.addFailure(source, e.getMessageKey());
            } catch (IOException e) {
                report.addFailure(source, "error.fileWrite");
            }
            listener.onProgress(i + 1, files.size());
        }
        return report;
    }

    private Path relative(Path baseDir, Path source) {
        if (baseDir != null && source.startsWith(baseDir)) return baseDir.relativize(source);
        return source.getFileName();
    }
}
