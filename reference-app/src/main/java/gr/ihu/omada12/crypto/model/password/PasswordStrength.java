package gr.ihu.omada12.crypto.model.password;

import java.util.Collections;
import java.util.List;

// Υλοποιεί: VLT-01, LAB-07

/** Αποτέλεσμα ελέγχου ισχύος: βαθμός 0-4, bits εντροπίας, χρόνος «σπασίματος» και συμβουλές (κλειδιά bundle). */
public class PasswordStrength {
    private final int score;
    private final double entropyBits;
    private final CrackTime crackTime;
    private final List<String> hintKeys;

    public PasswordStrength(int score, double entropyBits, CrackTime crackTime, List<String> hintKeys) {
        this.score = score;
        this.entropyBits = entropyBits;
        this.crackTime = crackTime;
        this.hintKeys = hintKeys;
    }

    public int getScore() { return score; }

    public double getEntropyBits() { return entropyBits; }

    public CrackTime getCrackTime() { return crackTime; }

    public List<String> getHintKeys() { return Collections.unmodifiableList(hintKeys); }

    /** Κλειδί bundle για το επίπεδο: pw.level.0 … pw.level.4 */
    public String getLevelKey() { return "pw.level." + score; }

    /** 0.0 - 1.0 για τη μπάρα ισχύος. */
    public double getRatio() { return (score + 1) / 5.0; }
}
