package gr.ihu.omada12.crypto.model.lab;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.AtomicFileWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

// Υλοποιεί: LAB-04

/** Εξαγωγή αποτελεσμάτων σε CSV (τελεία για δεκαδικά, ώστε να διαβάζεται παντού). */
public class BenchmarkCsv {
    static final String HEADER = "algorithm,input_bytes,output_bytes,repetitions,encrypt_ms,decrypt_ms,encrypt_MBps,decrypt_MBps";

    public String toCsv(List<BenchmarkResult> results) {
        StringBuilder sb = new StringBuilder(HEADER).append('\n');
        for (BenchmarkResult r : results) {
            sb.append(String.format(Locale.ROOT, "%s,%d,%d,%d,%.3f,%.3f,%.2f,%.2f%n", r.getAlgorithm(),
                    r.getInputBytes(), r.getOutputBytes(), r.getRepetitions(), r.getEncryptMillis(),
                    r.getDecryptMillis(), r.getEncryptMBps(), r.getDecryptMBps()));
        }
        return sb.toString();
    }

    public void write(List<BenchmarkResult> results, Path file) throws CryptoException {
        byte[] bytes = toCsv(results).getBytes(StandardCharsets.UTF_8);
        new AtomicFileWriter().write(file, out -> out.write(bytes));
    }
}
