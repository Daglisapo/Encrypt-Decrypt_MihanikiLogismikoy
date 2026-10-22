package gr.ihu.omada12.crypto.model.analysis;

// Υλοποιεί: LAB-05

/** Μία υποψήφια αποκρυπτογράφηση της κρυπτανάλυσης. */
public class Candidate {
    private final int shift;
    private final String text;
    private final double score;

    public Candidate(int shift, String text, double score) {
        this.shift = shift;
        this.text = text;
        this.score = score;
    }

    public int getShift() { return shift; }

    public String getText() { return text; }

    public double getScore() { return score; }
}
