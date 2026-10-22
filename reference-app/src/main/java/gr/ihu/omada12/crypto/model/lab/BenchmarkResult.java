package gr.ihu.omada12.crypto.model.lab;

// Υλοποιεί: LAB-01, LAB-02

/** Μέσοι όροι μίας σύγκρισης: χρόνοι, ταχύτητα (MB/s) και μέγεθος εξόδου. */
public class BenchmarkResult {
    private final String algorithm;
    private final long inputBytes;
    private final long outputBytes;
    private final double encryptMillis;
    private final double decryptMillis;
    private final int repetitions;

    public BenchmarkResult(String algorithm, long inputBytes, long outputBytes,
                           double encryptMillis, double decryptMillis, int repetitions) {
        this.algorithm = algorithm;
        this.inputBytes = inputBytes;
        this.outputBytes = outputBytes;
        this.encryptMillis = encryptMillis;
        this.decryptMillis = decryptMillis;
        this.repetitions = repetitions;
    }

    public String getAlgorithm() { return algorithm; }

    public long getInputBytes() { return inputBytes; }

    public long getOutputBytes() { return outputBytes; }

    public double getEncryptMillis() { return encryptMillis; }

    public double getDecryptMillis() { return decryptMillis; }

    public int getRepetitions() { return repetitions; }

    public double getEncryptMBps() { return speed(encryptMillis); }

    public double getDecryptMBps() { return speed(decryptMillis); }

    private double speed(double millis) {
        return millis <= 0 ? 0 : (inputBytes / 1_000_000.0) / (millis / 1000.0);
    }
}
