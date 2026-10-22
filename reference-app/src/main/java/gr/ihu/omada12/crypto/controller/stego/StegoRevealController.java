package gr.ihu.omada12.crypto.controller.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.io.FileProcessor;
import gr.ihu.omada12.crypto.model.stego.HiddenFile;
import gr.ihu.omada12.crypto.model.stego.RevealedPayload;
import gr.ihu.omada12.crypto.model.stego.StegoService;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import gr.ihu.omada12.crypto.view.Formats;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import java.nio.file.Path;

// Υλοποιεί: STG-02, STG-06 (UC-21)

/** Καρτέλα «Ανάκτηση»: εικόνα (+ κωδικός αν χρειάζεται) → κρυμμένο κείμενο ή αρχείο. */
public class StegoRevealController {
    private final AppContext ctx;
    private final StegoService stego = new StegoService();
    private FileSelection image;
    private HiddenFile foundFile;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label imageLabel;
    @FXML private Button chooseImage;
    @FXML private PasswordField passwordField;
    @FXML private TextArea revealedText;
    @FXML private Button saveFileButton;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public StegoRevealController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.STEGO, feedback, actions);
        image = new FileSelection(ctx, imageLabel, chooseImage, "stego.chooseStegoTitle",
                ctx.files().filter("filter.images", "*.png", "*.bmp"));
        saveFileButton.managedProperty().bind(saveFileButton.visibleProperty());
        saveFileButton.setVisible(false);
    }

    @FXML private void reveal() {
        Path source = image.get();
        if (source == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        char[] pw = passwordField.getText().toCharArray();
        runner.run("action.stegoReveal", source.getFileName().toString(), p -> stego.reveal(source, pw), this::show);
    }

    private void show(RevealedPayload payload) {
        foundFile = null;
        saveFileButton.setVisible(payload.isFile());
        try {
            if (payload.isFile()) {
                foundFile = payload.getFile();
                revealedText.setText("");
                feedback.ok("stego.revealedFile", foundFile.getName(), Formats.size(foundFile.getData().length));
            } else {
                revealedText.setText(payload.getText());
                feedback.ok(payload.getKind().isEncrypted() ? "stego.revealedEncrypted" : "stego.revealed");
            }
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    @FXML private void saveFile() {
        if (foundFile == null) return;
        Path target = ctx.files().save("stego.saveSecretTitle", foundFile.getName());
        if (target == null) return;
        try {
            new FileProcessor().write(target, foundFile.getData());
            feedback.ok("common.savedTo", target.getFileName());
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }
}
