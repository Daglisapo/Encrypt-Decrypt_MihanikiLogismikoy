package gr.ihu.omada12.crypto.controller;

import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.ExitGuard;
import gr.ihu.omada12.crypto.view.Screen;
import javafx.application.Platform;
import javafx.fxml.FXML;

// Υλοποιεί: APP-01, APP-03, NFR-07

/** Αρχική οθόνη (UI-01): οι 6 ενότητες σε «πλακίδια» και η έξοδος. Κάθε ενότητα είναι 1 κλικ μακριά. */
public class HomeController {
    private final AppContext ctx;

    public HomeController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void openEncryption() { ctx.getNavigator().show(Screen.ENCRYPTION); }

    /** Αν υπάρχει ήδη ξεκλείδωτο θησαυροφυλάκιο, πάμε κατευθείαν σε αυτό. */
    @FXML private void openVault() {
        ctx.getNavigator().show(ctx.getVaultSession().isOpen() ? Screen.VAULT : Screen.VAULT_LIST);
    }

    @FXML private void openSignature() { ctx.getNavigator().show(Screen.SIGNATURE); }

    @FXML private void openStego() { ctx.getNavigator().show(Screen.STEGO); }

    @FXML private void openLab() { ctx.getNavigator().show(Screen.LAB); }

    @FXML private void openGeneral() { ctx.getNavigator().show(Screen.GENERAL); }

    @FXML private void exit() {
        if (ExitGuard.confirmExit(ctx)) Platform.exit();
    }
}
