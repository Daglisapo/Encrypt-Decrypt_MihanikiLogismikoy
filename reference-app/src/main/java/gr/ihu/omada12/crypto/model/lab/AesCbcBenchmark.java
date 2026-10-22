package gr.ihu.omada12.crypto.model.lab;

import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

// Υλοποιεί: LAB-01

/** AES-256-CBC με PKCS#5 padding: παλαιότερη λειτουργία, χωρίς έλεγχο ακεραιότητας. */
public class AesCbcBenchmark extends JceBenchmarkAlgorithm {
    public AesCbcBenchmark() throws GeneralSecurityException { super("AES", 256); }

    @Override public String getName() { return "AES-256-CBC"; }

    @Override protected String transformation() { return "AES/CBC/PKCS5Padding"; }

    @Override protected int ivLength() { return 16; }

    @Override protected AlgorithmParameterSpec parameters(byte[] iv) { return new IvParameterSpec(iv); }
}
