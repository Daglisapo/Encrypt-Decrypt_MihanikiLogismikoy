package gr.ihu.omada12.crypto;

import javafx.application.Application;

// Υλοποιεί: NFR-10 (εκκίνηση από IntelliJ/jar χωρίς ρύθμιση module-path)

/**
 * Κλάση main που ΔΕΝ κληρονομεί από Application. Έτσι το IntelliJ (Run) ξεκινά την εφαρμογή
 * χωρίς το μήνυμα «JavaFX runtime components are missing».
 */
public final class Launcher {
    private Launcher() { }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
