package gr.ihu.omada12.crypto.model.lab;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.security.GeneralSecurityException;
import java.util.List;

// Υλοποιεί: LAB-01, NFR-11

/** Factory για τους αλγορίθμους σύγκρισης. Νέος αλγόριθμος = νέα κλάση + μία γραμμή εδώ. */
public final class BenchmarkCatalog {
    private BenchmarkCatalog() { }

    public static List<BenchmarkAlgorithm> all() throws CryptoException {
        try {
            return List.of(new AesGcmBenchmark(), new AesCbcBenchmark(), new ChaCha20Benchmark());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.cipherInit", e);
        }
    }
}
