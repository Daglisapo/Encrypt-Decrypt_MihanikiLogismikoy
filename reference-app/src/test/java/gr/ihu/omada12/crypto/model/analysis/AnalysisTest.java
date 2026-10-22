package gr.ihu.omada12.crypto.model.analysis;

import gr.ihu.omada12.crypto.model.cipher.CaesarCipher;
import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.cipher.VigenereCipher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** LAB-05, LAB-06, LAB-09 */
class AnalysisTest {
    private static final String PANGRAM = "The quick brown fox jumps over the lazy dog";

    @Test // TC-06 (οδηγός ρόλου): η πρώτη υποψήφια είναι η σωστή
    void bruteForceFindsShiftThree() {
        List<Candidate> candidates = new BruteForceAnalyzer().crack("Wkh txlfn eurzq ira mxpsv ryhu wkh odcb grj");
        assertEquals(25, candidates.size());
        assertEquals(3, candidates.get(0).getShift());
        assertEquals(PANGRAM, candidates.get(0).getText());
    }

    @Test // για ΚΑΘΕ μετατόπιση 1-25 η σωστή βγαίνει πρώτη σε κανονικό αγγλικό κείμενο
    void bruteForceWorksForAllShifts() {
        String plain = "Cryptography is the practice of secure communication in the presence of adversaries";
        CaesarCipher caesar = new CaesarCipher();
        for (int s = 1; s <= 25; s++) {
            assertEquals(s, new BruteForceAnalyzer().crack(caesar.apply(plain, s)).get(0).getShift(), "shift " + s);
        }
    }

    @Test // LAB-06
    void frequenciesSumToHundred() {
        double[] p = new FrequencyAnalyzer().percentages("aab");
        assertEquals(100.0 * 2 / 3, p[0], 1e-9);
        assertEquals(100.0 / 3, p[1], 1e-9);
        double sum = 0;
        for (double v : new FrequencyAnalyzer().percentages(PANGRAM)) sum += v;
        assertEquals(100.0, sum, 1e-9);
    }

    @Test // LAB-09: τα βήματα δίνουν το ίδιο αποτέλεσμα με τον πραγματικό αλγόριθμο
    void stepsMatchRealCiphers() throws CryptoException {
        List<CipherStep> v = new CipherStepper().vigenere("ATTACK", "LEMON");
        assertEquals('L', v.get(0).getKeyLetter());
        assertEquals(11, v.get(0).getShift());
        StringBuilder out = new StringBuilder();
        for (CipherStep s : v) out.append(s.getOutput());
        assertEquals(new VigenereCipher().encrypt("ATTACK", "LEMON"), out.toString());

        List<CipherStep> c = new CipherStepper().caesar("Hi!", 3);
        assertEquals(2, c.size()); // το «!» δεν είναι γράμμα
        assertEquals('K', c.get(0).getOutput());
        assertThrows(CryptoException.class, () -> new CipherStepper().caesar("abc", 30));
        assertTrue(new CipherStepper().vigenere("123", "KEY").isEmpty());
    }
}
