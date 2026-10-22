package gr.ihu.omada12.crypto.model.lab;

import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

// Υλοποιεί: LAB-01

/** AES-256-GCM: κρυπτογράφηση + έλεγχος ακεραιότητας (ο αλγόριθμος που χρησιμοποιεί η εφαρμογή). */
public class AesGcmBenchmark extends JceBenchmarkAlgorithm {
    public AesGcmBenchmark() throws GeneralSecurityException { super("AES", 256); }

    @Override public String getName() { return "AES-256-GCM"; }

    @Override protected String transformation() { return "AES/GCM/NoPadding"; }

    @Override protected int ivLength() { return 12; }

    @Override protected AlgorithmParameterSpec parameters(byte[] iv) { return new GCMParameterSpec(128, iv); }
}
