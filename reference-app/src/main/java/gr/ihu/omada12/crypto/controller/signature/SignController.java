package gr.ihu.omada12.crypto.controller.signature;

import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.signature.FileSigner;
import gr.ihu.omada12.crypto.model.signature.PrivateKeyStore;
import gr.ihu.omada12.crypto.model.signature.SignatureFileCodec;
import gr.ihu.omada12.crypto.model.signature.SignatureRecord;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import gr.ihu.omada12.crypto.view.Formats;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Pane;

import java.nio.file.Path;

// Υλοποιεί: SIG-02 (UC-17)

/** Καρτέλα «Υπογραφή»: αρχείο + ιδιωτικό κλειδί + κωδικός → ξεχωριστό αρχείο .sig */
public class SignController {
    private final AppContext ctx;
    private final PrivateKeyStore store = new PrivateKeyStore();
    private final FileSigner signer = new FileSigner();
    private final SignatureFileCodec codec = new SignatureFileCodec();
    private FileSelection document;
    private FileSelection privateKey;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label documentLabel;
    @FXML private Button chooseDocument;
    @FXML private Label keyLabel;
    @FXML private Button chooseKey;
    @FXML private PasswordField passwordField;
    @FXML private Label detailsLabel;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public SignController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.SIGNATURE, feedback, actions);
        document = new FileSelection(ctx, documentLabel, chooseDocument, "sig.chooseFileTitle");
        privateKey = new FileSelection(ctx, keyLabel, chooseKey, "sig.chooseKeyTitle",
                ctx.files().filter("filter.privateKey", "*" + PrivateKeyStore.EXTENSION));
    }

    @FXML private void sign() {
        Path file = document.get();
        Path key = privateKey.get();
        if (file == null || key == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        Path target = ctx.files().save("sig.saveSigTitle", SignatureFileCodec.signatureFileFor(file).getFileName().toString(),
                ctx.files().filter("filter.signature", "*" + SignatureFileCodec.EXTENSION));
        if (target == null) return;
        char[] pw = passwordField.getText().toCharArray();
        runner.run("action.sign", file.getFileName().toString(), p -> {
            SignatureRecord record = signer.sign(file, store.load(key, pw));
            codec.write(record, target);
            return record;
        }, record -> {
            detailsLabel.setText(ctx.getI18n().text("sig.details", Formats.dateTime(record.getSignedAt()),
                    record.getKeyFingerprint()));
            feedback.ok("sig.signed", target.getFileName());
        });
    }
}
