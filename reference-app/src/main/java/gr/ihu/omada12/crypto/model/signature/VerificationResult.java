package gr.ihu.omada12.crypto.model.signature;

// Υλοποιεί: SIG-03, SIG-06

/** «Έγκυρη» ή «Μη έγκυρη», μαζί με τα στοιχεία της υπογραφής για εμφάνιση. */
public class VerificationResult {
    private final boolean valid;
    private final boolean keyMatches;
    private final SignatureRecord record;

    public VerificationResult(boolean valid, boolean keyMatches, SignatureRecord record) {
        this.valid = valid;
        this.keyMatches = keyMatches;
        this.record = record;
    }

    public boolean isValid() { return valid; }

    /** false όταν η υπογραφή έγινε με άλλο κλειδί από αυτό που δόθηκε. */
    public boolean isKeyMatching() { return keyMatches; }

    public SignatureRecord getRecord() { return record; }

    /** Κλειδί bundle για το μήνυμα αποτελέσματος. */
    public String getMessageKey() { return valid ? "sig.result.valid" : "sig.result.invalid"; }
}
