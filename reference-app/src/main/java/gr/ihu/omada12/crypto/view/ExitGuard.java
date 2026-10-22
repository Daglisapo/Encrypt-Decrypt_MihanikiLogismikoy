package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.history.AppModule;

// Υλοποιεί: APP-03, VLT-08 (κλείδωμα κατά την έξοδο)

/** Έξοδος με επιβεβαίωση. Αν υπάρχει ανοιχτό θησαυροφυλάκιο, κλειδώνει πριν κλείσει η εφαρμογή. */
public final class ExitGuard {
    private ExitGuard() { }

    /** @return true αν ο χρήστης επιβεβαίωσε την έξοδο (και κλειδώθηκε ό,τι ήταν ανοιχτό) */
    public static boolean confirmExit(AppContext ctx) {
        boolean vaultOpen = ctx.getVaultSession().isOpen();
        if (!ctx.dialogs().confirm(vaultOpen ? "exit.confirmWithVault" : "exit.confirm")) return false;
        if (vaultOpen) {
            String name = ctx.getVaultSession().current().getName();
            PreviewWindow.closeAll();
            ctx.getVaultSession().lock();
            ctx.getHistory().record(AppModule.VAULT, "action.vaultLock", name, true);
        }
        return true;
    }
}
