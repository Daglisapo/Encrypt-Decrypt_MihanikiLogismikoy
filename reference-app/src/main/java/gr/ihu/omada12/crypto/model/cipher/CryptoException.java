package gr.ihu.omada12.crypto.model.cipher;

// Υλοποιεί: APP-04, NFR-09

/**
 * Ενιαία εξαίρεση για κάθε αποτυχία της εφαρμογής.
 * Δεν κρατά έτοιμο κείμενο αλλά ΚΛΕΙΔΙ του ResourceBundle (π.χ. "error.wrongKeyOrCorrupted"),
 * ώστε το μήνυμα να εμφανίζεται στη γλώσσα του χρήστη (ΕΛ/EN) και να μην υπάρχουν
 * κείμενα διεπαφής μέσα στο μοντέλο.
 */
public class CryptoException extends Exception {
    private final String messageKey;
    private final Object[] args;

    public CryptoException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public CryptoException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.args = new Object[0];
    }

    /** Το κλειδί του μηνύματος στα αρχεία πόρων. */
    public String getMessageKey() { return messageKey; }

    /** Τιμές που μπαίνουν στις θέσεις {0}, {1}… του μηνύματος. */
    public Object[] getArgs() { return args.clone(); }
}
