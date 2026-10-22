package gr.ihu.omada12.crypto.controller.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.vault.PreviewKind;
import gr.ihu.omada12.crypto.model.vault.Vault;
import gr.ihu.omada12.crypto.model.vault.VaultEntry;
import gr.ihu.omada12.crypto.model.vault.VaultManager;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.ChangePasswordDialog;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.PreviewWindow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Υλοποιεί: VLT-06, VLT-09, VLT-10, VLT-13, VLT-14, APP-05 (UC-10, UC-11, UC-12, UC-14)

/**
 * Οι ενέργειες πάνω στα επιλεγμένα αρχεία του ανοιχτού θησαυροφυλακίου: εξαγωγή, διαγραφή,
 * ετικέτα, προεπισκόπηση και αλλαγή κύριου κωδικού. Χωρίστηκε από το VaultController ώστε
 * ο controller να κρατά μόνο τον πίνακα και τα φίλτρα (μικρότερο CBO, μία ευθύνη ανά κλάση).
 */
class VaultEntryActions {
    private final AppContext ctx;
    private final Vault vault;
    private final Feedback feedback;
    private final ActionRunner runner;

    VaultEntryActions(AppContext ctx, Vault vault, Feedback feedback, ActionRunner runner) {
        this.ctx = ctx;
        this.vault = vault;
        this.feedback = feedback;
        this.runner = runner;
    }

    /** VLT-06 + APP-05: εξαγωγή, με επιβεβαίωση πριν αντικατασταθεί υπάρχον αρχείο. */
    void export(List<VaultEntry> selected) {
        Path dir = ctx.files().directory("vault.exportTitle");
        if (dir == null) return;
        int exported = 0;
        for (VaultEntry e : selected) {
            try {
                Path target = vault.exportTarget(e.getId(), dir);
                if (Files.exists(target) && !ctx.dialogs().confirm("common.overwriteConfirm", target.getFileName())) continue;
                vault.export(e.getId(), dir);
                exported++;
                ctx.getHistory().record(AppModule.VAULT, "action.vaultExport", e.getName(), true);
            } catch (CryptoException ex) {
                feedback.error(ex);
            }
        }
        if (exported > 0) feedback.ok("vault.exported", exported, dir);
    }

    /** VLT-09: διαγραφή με επιβεβαίωση. Επιστρέφει true αν άλλαξε το περιεχόμενο. */
    boolean delete(List<VaultEntry> selected) {
        if (!ctx.dialogs().confirm("vault.deleteConfirm", selected.size())) return false;
        try {
            for (VaultEntry e : selected) {
                vault.delete(e.getId());
                ctx.getHistory().record(AppModule.VAULT, "action.vaultDeleteEntry", e.getName(), true);
            }
            feedback.ok("vault.deleted", selected.size());
        } catch (CryptoException e) {
            feedback.error(e);
        }
        return true;
    }

    /** VLT-13: ετικέτα σε ένα ή περισσότερα αρχεία (κενή = αφαίρεση). */
    boolean tag(List<VaultEntry> selected) {
        Optional<String> tag = ctx.dialogs().askText("vault.tagPrompt", selected.get(0).getTag());
        if (tag.isEmpty()) return false;
        try {
            for (VaultEntry e : selected) vault.setTag(e.getId(), tag.get());
            feedback.ok("vault.tagged", selected.size());
        } catch (CryptoException e) {
            feedback.error(e);
        }
        return true;
    }

    /** VLT-14: προεπισκόπηση από τη μνήμη, χωρίς εξαγωγή στον δίσκο. */
    void preview(VaultEntry entry) {
        PreviewKind kind = PreviewKind.of(entry.getType());
        if (kind == PreviewKind.NONE) {
            feedback.errorKey("vault.previewUnsupported", entry.getType());
            return;
        }
        try {
            PreviewWindow.show(ctx, entry.getName(), vault.read(entry.getId()), kind);
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    /** VLT-10: νέος κύριος κωδικός, με τους ίδιους κανόνες ισχύος όπως στη δημιουργία. */
    void changePassword() {
        Optional<ChangePasswordDialog.Result> result = ChangePasswordDialog.ask(ctx);
        if (result.isEmpty()) return;
        ChangePasswordDialog.Result r = result.get();
        runner.run("action.vaultChangePassword", vault.getName(), p -> {
            VaultManager.checkNewPassword(r.getNext(), r.getConfirm());
            vault.changePassword(r.getCurrent(), r.getNext());
            return vault;
        }, v -> feedback.ok("vault.passwordChanged"));
    }
}
