package gr.ihu.omada12.crypto.controller.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.stego.ImageFormatPolicy;
import gr.ihu.omada12.crypto.model.stego.StegoService;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Υλοποιεί: STG-01, STG-03, STG-04, STG-05, STG-06, NFR-08 (UC-20)

/**
 * Καρτέλα «Απόκρυψη»: εικόνα-φορέας, μήνυμα ή μικρό αρχείο, προαιρετικός κωδικός, ένδειξη
 * χωρητικότητας σε πραγματικό χρόνο και σύγκριση αρχικής/νέας εικόνας δίπλα-δίπλα.
 */
public class StegoHideController {
    private final AppContext ctx;
    private final StegoService stego = new StegoService();
    private BufferedImage cover;
    private FileSelection coverFile;
    private FileSelection secretFile;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label coverLabel;
    @FXML private Button chooseCover;
    @FXML private Label formatWarning;
    @FXML private Label capacityLabel;
    @FXML private RadioButton textMode;
    @FXML private RadioButton fileMode;
    @FXML private TextArea messageArea;
    @FXML private Pane filePane;
    @FXML private Label secretLabel;
    @FXML private Button chooseSecret;
    @FXML private Label usageLabel;
    @FXML private CheckBox encryptCheck;
    @FXML private PasswordField passwordField;
    @FXML private ImageView originalView;
    @FXML private ImageView resultView;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public StegoHideController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.STEGO, feedback, actions);
        coverFile = new FileSelection(ctx, coverLabel, chooseCover, "stego.chooseCoverTitle",
                ctx.files().filter("filter.images", "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        secretFile = new FileSelection(ctx, secretLabel, chooseSecret, "stego.chooseSecretTitle");
        coverFile.pathProperty().addListener((o, a, p) -> loadCover(p));
        secretFile.pathProperty().addListener((o, a, p) -> updateUsage());
        messageArea.textProperty().addListener((o, a, b) -> updateUsage());
        encryptCheck.selectedProperty().addListener((o, a, b) -> updateUsage());
        passwordField.disableProperty().bind(encryptCheck.selectedProperty().not());
        messageArea.managedProperty().bind(messageArea.visibleProperty());
        filePane.managedProperty().bind(filePane.visibleProperty());
        textMode.selectedProperty().addListener((o, a, b) -> updateMode());
        updateMode();
    }

    private void updateMode() {
        messageArea.setVisible(textMode.isSelected());
        filePane.setVisible(fileMode.isSelected());
        updateUsage();
    }

    /** NFR-08: PNG/BMP δεκτά, JPEG με προειδοποίηση, άλλα απορρίπτονται. */
    private void loadCover(Path path) {
        cover = null;
        formatWarning.setText("");
        originalView.setImage(null);
        resultView.setImage(null);
        if (path == null) return;
        ImageFormatPolicy.Support support = ImageFormatPolicy.check(path);
        if (support == ImageFormatPolicy.Support.UNSUPPORTED) {
            feedback.errorKey("stego.unsupported");
            return;
        }
        if (support == ImageFormatPolicy.Support.JPEG_WARNING) formatWarning.setText(ctx.getI18n().text("stego.jpegWarning"));
        try {
            cover = stego.readImage(path);
            originalView.setImage(new Image(path.toUri().toString()));
            feedback.clear();
        } catch (CryptoException e) {
            feedback.error(e);
        }
        updateUsage();
    }

    /** STG-04: πόσα bytes χρειάζονται και πόσα χωράνε, με κόκκινο όταν δεν χωράει. */
    private void updateUsage() {
        if (cover == null) {
            capacityLabel.setText("");
            usageLabel.setText("");
            return;
        }
        long free = stego.freeBytes(cover, encryptCheck.isSelected());
        long needed = neededBytes();
        capacityLabel.setText(ctx.getI18n().text("stego.capacity", cover.getWidth(), cover.getHeight(), free, free / 2));
        usageLabel.setText(ctx.getI18n().text("stego.usage", needed, free));
        usageLabel.getStyleClass().removeAll("status-error", "status-ok");
        usageLabel.getStyleClass().add(needed > free ? "status-error" : "status-ok");
    }

    private long neededBytes() {
        if (textMode.isSelected()) return StegoService.bytesOf(messageArea.getText());
        Path secret = secretFile.get();
        try {
            return secret == null ? 0 : Files.size(secret) + 2 + StegoService.bytesOf(secret.getFileName().toString());
        } catch (IOException e) {
            return 0;
        }
    }

    @FXML private void hide() {
        Path source = coverFile.get();
        if (source == null || cover == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        if (encryptCheck.isSelected() && passwordField.getText().isEmpty()) {
            feedback.errorKey("error.emptyPassword");
            return;
        }
        Path target = ctx.files().save("stego.saveTitle", baseName(source) + "-stego.png", ctx.files().filter("filter.png", "*.png"));
        if (target == null) return;
        char[] pw = encryptCheck.isSelected() ? passwordField.getText().toCharArray() : null;
        String message = messageArea.getText();
        Path secret = secretFile.get();
        boolean text = textMode.isSelected();
        runner.run("action.stegoHide", source.getFileName().toString(),
                p -> text ? stego.hideText(source, message, pw, target) : stego.hideFile(source, secret, pw, target),
                out -> {
                    resultView.setImage(new Image(out.toUri().toString())); // STG-05: δίπλα-δίπλα
                    feedback.ok("stego.hidden", out.getFileName());
                });
    }

    private static String baseName(Path file) {
        String n = file.getFileName().toString();
        int dot = n.lastIndexOf('.');
        return dot > 0 ? n.substring(0, dot) : n;
    }
}
