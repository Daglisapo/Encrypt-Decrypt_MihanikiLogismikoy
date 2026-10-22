package gr.ihu.omada12.crypto.controller.encryption;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.file.PdfLocker;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Pane;

import java.nio.file.Path;

// Υλοποιεί: ENC-07 (UC-05)

/** Καρτέλα «PDF»: κλείδωμα με κωδικό που ανοίγει σε κάθε PDF reader, και αφαίρεση του κωδικού. */
public class PdfController {
    private final AppContext ctx;
    private final PdfLocker locker = new PdfLocker();
    private FileSelection pdf;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label pdfLabel;
    @FXML private Button choosePdfButton;
    @FXML private Label stateLabel;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public PdfController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.ENCRYPTION, feedback, actions);
        pdf = new FileSelection(ctx, pdfLabel, choosePdfButton, "enc.pdf.chooseTitle", ctx.files().filter("filter.pdf", "*.pdf"));
        pdf.pathProperty().addListener((obs, old, path) -> showState(path));
    }

    /** Δείχνει αν το επιλεγμένο PDF είναι ήδη κλειδωμένο. */
    private void showState(Path path) {
        if (path == null) return;
        try {
            stateLabel.setText(ctx.getI18n().text(locker.isLocked(path) ? "enc.pdf.stateLocked" : "enc.pdf.stateOpen"));
        } catch (CryptoException e) {
            stateLabel.setText("");
            feedback.error(e);
        }
    }

    @FXML private void lock() {
        Path source = pdf.get();
        if (source == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        if (!passwordField.getText().equals(confirmField.getText())) {
            feedback.errorKey("error.passwordMismatch");
            return;
        }
        Path target = ctx.files().save("enc.pdf.saveTitle", baseName(source) + "-locked.pdf", ctx.files().filter("filter.pdf", "*.pdf"));
        if (target == null) return;
        char[] pw = passwordField.getText().toCharArray();
        runner.run("action.pdfLock", source.getFileName().toString(), p -> {
            locker.lock(source, target, pw);
            return target;
        }, out -> feedback.ok("enc.pdf.locked", out.getFileName()));
    }

    @FXML private void unlock() {
        Path source = pdf.get();
        if (source == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        Path target = ctx.files().save("enc.pdf.saveTitle", baseName(source) + "-unlocked.pdf", ctx.files().filter("filter.pdf", "*.pdf"));
        if (target == null) return;
        char[] pw = passwordField.getText().toCharArray();
        runner.run("action.pdfUnlock", source.getFileName().toString(), p -> {
            locker.unlock(source, target, pw);
            return target;
        }, out -> feedback.ok("enc.pdf.unlocked", out.getFileName()));
    }

    private static String baseName(Path file) {
        String n = file.getFileName().toString();
        return n.toLowerCase().endsWith(".pdf") ? n.substring(0, n.length() - 4) : n;
    }
}
