package gr.ihu.omada12.crypto.model.analysis;

import gr.ihu.omada12.crypto.model.cipher.CaesarCipher;
import gr.ihu.omada12.crypto.model.cipher.ClassicalCipher;
import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.cipher.VigenereCipher;

import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: LAB-09

/**
 * Παράγει τα βήματα της κρυπτογράφησης γράμμα-γράμμα, για εκπαιδευτική οπτικοποίηση.
 * Χρησιμοποιεί τις ίδιες συναρτήσεις με τους πραγματικούς αλγορίθμους, άρα το αποτέλεσμα είναι ίδιο.
 */
public class CipherStepper {

    public List<CipherStep> caesar(String text, int shift) throws CryptoException {
        new CaesarCipher().validateKey(String.valueOf(shift));
        List<CipherStep> steps = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ClassicalCipher.isLatinLetter(c)) {
                steps.add(new CipherStep(i, c, ' ', shift, ClassicalCipher.shift(c, shift)));
            }
        }
        return steps;
    }

    public List<CipherStep> vigenere(String text, String key) throws CryptoException {
        new VigenereCipher().validateKey(key);
        List<CipherStep> steps = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ClassicalCipher.isLatinLetter(c)) {
                int shift = VigenereCipher.keyShift(key, j);
                char keyLetter = Character.toUpperCase(key.charAt(j % key.length()));
                steps.add(new CipherStep(i, c, keyLetter, shift, ClassicalCipher.shift(c, shift)));
                j++;
            }
        }
        return steps;
    }
}
