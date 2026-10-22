package gr.ihu.omada12.crypto.model.analysis;

// Υλοποιεί: LAB-09

/** Ένα βήμα της οπτικοποίησης: γράμμα εισόδου, γράμμα κλειδιού, μετατόπιση, γράμμα εξόδου. */
public class CipherStep {
    private final int position;
    private final char input;
    private final char keyLetter;
    private final int shift;
    private final char output;

    public CipherStep(int position, char input, char keyLetter, int shift, char output) {
        this.position = position;
        this.input = input;
        this.keyLetter = keyLetter;
        this.shift = shift;
        this.output = output;
    }

    public int getPosition() { return position; }

    public char getInput() { return input; }

    /** Για Caesar είναι κενό (' '), γιατί η μετατόπιση είναι ίδια για όλα τα γράμματα. */
    public char getKeyLetter() { return keyLetter; }

    public int getShift() { return shift; }

    public char getOutput() { return output; }
}
