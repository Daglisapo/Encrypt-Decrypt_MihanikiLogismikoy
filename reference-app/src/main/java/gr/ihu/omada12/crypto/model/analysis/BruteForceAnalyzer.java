package gr.ihu.omada12.crypto.model.analysis;

import gr.ihu.omada12.crypto.model.cipher.CaesarCipher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Υλοποιεί: LAB-05

/**
 * Κρυπτανάλυση Caesar με brute force: δοκιμάζει και τις 25 μετατοπίσεις και τις ταξινομεί
 * με το τεστ χ². Η πρώτη της λίστας είναι η «πιθανότερη». Σε πολύ μικρά κείμενα η στατιστική
 * μπορεί να κάνει λάθος, γι' αυτό η διεπαφή δείχνει πάντα και τις 25.
 */
public class BruteForceAnalyzer {
    private final CaesarCipher caesar = new CaesarCipher();
    private final FrequencyAnalyzer frequencies = new FrequencyAnalyzer();

    public List<Candidate> crack(String cipherText) {
        List<Candidate> result = new ArrayList<>();
        for (int s = 1; s <= 25; s++) {
            String plain = caesar.apply(cipherText, -s);
            result.add(new Candidate(s, plain, frequencies.chiSquared(plain)));
        }
        result.sort(Comparator.comparingDouble(Candidate::getScore));
        return result;
    }
}
