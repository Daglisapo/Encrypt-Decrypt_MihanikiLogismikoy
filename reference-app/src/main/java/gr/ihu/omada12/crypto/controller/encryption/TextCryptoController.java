package gr.ihu.omada12.crypto.controller.encryption;

import gr.ihu.omada12.crypto.model.cipher.CipherFactory;
import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.cipher.TextCipher;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.key.KeyManager;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.Views;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Υλοποιεί: ENC-01, ENC-02, ENC-03, ENC-09 (UC-01, UC-02)

/**
 * Καρτέλα «Κείμενο»: επιλογή αλγορίθμου, κλειδί/κωδικός, κρυπτογράφηση και αποκρυπτογράφηση,
 * αντιγραφή στο πρόχειρο και αποθήκευση σε αρχείο. Μιλά μόνο με το TextCipher (Strategy).
 */
public class TextCryptoController {
    private final AppContext ctx;
    private final KeyManager keys = new KeyManager();
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private ComboBox<String> algorithmBox;
    @FXML private Label hintLabel;
    @FXML private PasswordField passwordField;
    @FXML private TextField keyField;
    @FXML private HBox keyButtons;
    @FXML private TextArea inputArea;
    @FXML private TextArea outputArea;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public TextCryptoController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.ENCRYPTION, feedback, actions);
        passwordField.managedProperty().bind(passwordField.visibleProperty());
        keyField.managedProperty().bind(keyField.visibleProperty());
        keyButtons.managedProperty().bind(keyButtons.visibleProperty());
        algorithmBox.getItems().setAll(CipherFactory.availableAlgorithms());
        algorithmBox.setValue(CipherFactory.AES);
        algorithmBox.valueProperty().addListener((obs, old, value) -> updateKeyMode());
        updateKeyMode();
    }

    /** AES με κωδικό → πεδίο κωδικού (κρυφό). Οι υπόλοιποι → απλό πεδίο κλειδιού. */
    private void updateKeyMode() {
        TextCipher cipher = CipherFactory.create(algorithmBox.getValue());
        hintLabel.setText(ctx.getI18n().text(cipher.getKeyHintKey()));
        boolean password = CipherFactory.AES.equals(cipher.getName());
        passwordField.setVisible(password);
        keyField.setVisible(!password);
        keyButtons.setVisible(CipherFactory.AES_KEY.equals(cipher.getName()));
    }

    @FXML private void encrypt() { run(true); }

    @FXML private void decrypt() { run(false); }

    private void run(boolean encrypt) {
        TextCipher cipher = CipherFactory.create(algorithmBox.getValue());
        String key = passwordField.isVisible() ? passwordField.getText() : keyField.getText();
        String input = inputArea.getText();
        // στο ιστορικό μπαίνει μόνο η ενέργεια, ΟΧΙ κείμενο ή κλειδί (NFR-03)
        runner.run(encrypt ? "action.encryptText" : "action.decryptText", "",
                p -> encrypt ? cipher.encrypt(input, key) : cipher.decrypt(input, key),
                result -> {
                    outputArea.setText(result);
                    feedback.ok(encrypt ? "enc.text.encrypted" : "enc.text.decrypted", cipher.getName());
                });
    }

    /** Το αποτέλεσμα γίνεται είσοδος, π.χ. για άμεση αποκρυπτογράφηση. */
    @FXML private void swap() {
        inputArea.setText(outputArea.getText());
        outputArea.clear();
    }

    @FXML private void copy() {
        Views.copyToClipboard(outputArea.getText());
        feedback.ok("enc.text.copied");
    }

    @FXML private void saveOutput() {
        Path target = ctx.files().save("enc.text.saveTitle", "result.txt", ctx.files().filter("filter.text", "*.txt"));
        if (target == null) return;
        try {
            Files.writeString(target, outputArea.getText(), StandardCharsets.UTF_8);
            feedback.ok("common.savedTo", target.getFileName());
        } catch (IOException e) {
            feedback.error(new CryptoException("error.fileWrite", target.getFileName()));
        }
    }

    @FXML private void generateKey() {
        try {
            keyField.setText(keys.generateAesKey());
            feedback.ok("enc.key.generated");
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    @FXML private void saveKey() {
        Path target = ctx.files().save("enc.key.saveTitle", "my-aes.key", ctx.files().filter("filter.key", "*.key"));
        if (target == null) return;
        try {
            keys.save(keyField.getText(), target);
            feedback.ok("enc.key.saved", target.getFileName());
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    @FXML private void loadKey() {
        Path source = ctx.files().open("enc.key.loadTitle", ctx.files().filter("filter.key", "*.key"));
        if (source == null) return;
        try {
            keyField.setText(keys.load(source));
            feedback.ok("enc.key.loaded", source.getFileName());
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }
}
