package gr.ihu.omada12.crypto.model.lab;

import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

// Υλοποιεί: LAB-01

/** ChaCha20-Poly1305: εναλλακτική του AES-GCM, γρήγορη και σε επεξεργαστές χωρίς εντολές AES. */
public class ChaCha20Benchmark extends JceBenchmarkAlgorithm {
    public ChaCha20Benchmark() throws GeneralSecurityException { super("ChaCha20", 256); }

    @Override public String getName() { return "ChaCha20-Poly1305"; }

    @Override protected String transformation() { return "ChaCha20-Poly1305"; }

    @Override protected int ivLength() { return 12; }

    @Override protected AlgorithmParameterSpec parameters(byte[] iv) { return new IvParameterSpec(iv); }
}
