package gr.ihu.omada12.crypto.model.history;

// Υλοποιεί: APP-01, APP-09

/** Οι 6 ενότητες της εφαρμογής, όπως εμφανίζονται στην αρχική οθόνη και στο ιστορικό. */
public enum AppModule {
    ENCRYPTION, VAULT, SIGNATURE, STEGO, LAB, GENERAL;

    /** Κλειδί bundle με το όνομα της ενότητας: module.encryption, module.vault, … */
    public String getKey() { return "module." + name().toLowerCase(); }
}
