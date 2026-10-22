package gr.ihu.omada12.crypto.model.lab;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

// Υλοποιεί: LAB-01

/**
 * Κοινός κώδικας για αλγορίθμους της Java (javax.crypto): τυχαίο IV μπροστά στο κρυπτοκείμενο.
 * Οι υποκλάσεις δίνουν μόνο όνομα, transformation, μήκος IV και παραμέτρους (πρότυπο Template Method).
 */
public abstract class JceBenchmarkAlgorithm implements BenchmarkAlgorithm {
    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    protected JceBenchmarkAlgorithm(String keyAlgorithm, int keyBits) throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance(keyAlgorithm);
        generator.init(keyBits);
        this.key = generator.generateKey();
    }

    protected abstract String transformation();

    protected abstract int ivLength();

    protected abstract AlgorithmParameterSpec parameters(byte[] iv);

    @Override public byte[] encrypt(byte[] data) throws GeneralSecurityException {
        byte[] iv = new byte[ivLength()];
        random.nextBytes(iv);
        Cipher c = Cipher.getInstance(transformation());
        c.init(Cipher.ENCRYPT_MODE, key, parameters(iv));
        byte[] ct = c.doFinal(data);
        return ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array();
    }

    @Override public byte[] decrypt(byte[] data) throws GeneralSecurityException {
        byte[] iv = Arrays.copyOf(data, ivLength());
        Cipher c = Cipher.getInstance(transformation());
        c.init(Cipher.DECRYPT_MODE, key, parameters(iv));
        return c.doFinal(data, iv.length, data.length - iv.length);
    }
}
