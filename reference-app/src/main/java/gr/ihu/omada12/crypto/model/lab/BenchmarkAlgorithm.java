package gr.ihu.omada12.crypto.model.lab;

import java.security.GeneralSecurityException;

// Υλοποιεί: LAB-01, NFR-11

/**
 * Αλγόριθμος που συμμετέχει στη σύγκριση (πρότυπο Strategy). Κάθε υλοποίηση φτιάχνει δικό της
 * τυχαίο κλειδί, άρα η σύγκριση δεν χρειάζεται κωδικό ούτε PBKDF2 (που θα «έκρυβε» τη διαφορά).
 */
public interface BenchmarkAlgorithm {
    String getName();

    byte[] encrypt(byte[] data) throws GeneralSecurityException;

    byte[] decrypt(byte[] data) throws GeneralSecurityException;
}
