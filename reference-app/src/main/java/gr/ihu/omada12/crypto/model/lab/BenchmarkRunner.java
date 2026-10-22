package gr.ihu.omada12.crypto.model.lab;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.ProgressListener;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Υλοποιεί: LAB-01, LAB-02

/**
 * Μετρά κάθε αλγόριθμο N φορές στο ίδιο αρχείο και βγάζει μέσους όρους (δίκαιη σύγκριση, LAB-02).
 * Πριν από τις μετρήσεις γίνονται μερικές εκτελέσεις «ζέσταμα», ώστε ο JIT της Java να μεταγλωττίσει
 * τον κώδικα και ο πρώτος αλγόριθμος να μην αδικείται.
 * Σε κάθε επανάληψη ελέγχεται ότι η αποκρυπτογράφηση δίνει ξανά τα αρχικά δεδομένα.
 */
public class BenchmarkRunner {
    public static final int MAX_REPETITIONS = 50;
    static final int WARMUP_ROUNDS = 3;

    private long done;
    private long total;

    public List<BenchmarkResult> run(List<BenchmarkAlgorithm> algorithms, byte[] data, int repetitions,
                                     ProgressListener listener) throws CryptoException {
        if (repetitions < 1 || repetitions > MAX_REPETITIONS) {
            throw new CryptoException("error.repetitions", 1, MAX_REPETITIONS);
        }
        done = 0;
        total = (long) algorithms.size() * repetitions;
        List<BenchmarkResult> results = new ArrayList<>();
        for (BenchmarkAlgorithm algo : algorithms) {
            try {
                results.add(measure(algo, data, repetitions, listener));
            } catch (GeneralSecurityException e) {
                throw new CryptoException("error.benchmarkFailed", algo.getName());
            }
        }
        return results;
    }

    private BenchmarkResult measure(BenchmarkAlgorithm algo, byte[] data, int repetitions, ProgressListener listener)
            throws GeneralSecurityException, CryptoException {
        for (int i = 0; i < WARMUP_ROUNDS; i++) algo.decrypt(algo.encrypt(data)); // ζέσταμα
        long encNanos = 0;
        long decNanos = 0;
        int outSize = 0;
        for (int i = 0; i < repetitions; i++) {
            if (listener.isCancelled()) throw new CryptoException("error.cancelled");
            long t0 = System.nanoTime();
            byte[] enc = algo.encrypt(data);
            long t1 = System.nanoTime();
            byte[] dec = algo.decrypt(enc);
            long t2 = System.nanoTime();
            if (!Arrays.equals(data, dec)) throw new CryptoException("error.benchmarkMismatch", algo.getName());
            encNanos += t1 - t0;
            decNanos += t2 - t1;
            outSize = enc.length;
            listener.onProgress(++done, total);
        }
        return new BenchmarkResult(algo.getName(), data.length, outSize,
                encNanos / 1e6 / repetitions, decNanos / 1e6 / repetitions, repetitions);
    }
}
