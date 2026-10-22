package gr.ihu.omada12.crypto.controller.signature;

import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.signature.PublicKeyPem;
import gr.ihu.omada12.crypto.model.signature.SignatureFileCodec;
import gr.ihu.omada12.crypto.model.signature.SignatureVerifier;
import gr.ihu.omada12.crypto.model.signature.VerificationResult;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import gr.ihu.omada12.crypto.view.Formats;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.nio.file.Files;
import java.nio.file.Path;

// Υλοποιεί: SIG-03, SIG-06 (UC-18)

/** Καρτέλα «Επαλήθευση»: αρχείο + .sig + δημόσιο κλειδί → «Έγκυρη» ή «Μη έγκυρη», με τα στοιχεία της υπογραφής. */
public class VerifyController {
    private final AppContext ctx;
    private final SignatureFileCodec codec = new SignatureFileCodec();
    private final PublicKeyPem pem = new PublicKeyPem();
    private final SignatureVerifier verifier = new SignatureVerifier();
    private FileSelection document;
    private FileSelection signature;
    private FileSelection publicKey;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label documentLabel;
    @FXML private Button chooseDocument;
    @FXML private Label signatureLabel;
    @FXML private Button chooseSignature;
    @FXML private Label publicKeyLabel;
    @FXML private Button choosePublicKey;
    @FXML private Label resultLabel;
    @FXML private Label signedAtValue;
    @FXML private Label fingerprintValue;
    @FXML private Label keyMatchValue;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public VerifyController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.SIGNATURE, feedback, actions);
        document = new FileSelection(ctx, documentLabel, chooseDocument, "sig.chooseFileTitle");
        signature = new FileSelection(ctx, signatureLabel, chooseSignature, "sig.chooseSigTitle",
                ctx.files().filter("filter.signature", "*" + SignatureFileCodec.EXTENSION));
        publicKey = new FileSelection(ctx, publicKeyLabel, choosePublicKey, "sig.choosePubTitle",
                ctx.files().filter("filter.publicKey", "*" + PublicKeyPem.EXTENSION));
        // αν δίπλα στο αρχείο υπάρχει ήδη αρχείο.sig, το επιλέγουμε αυτόματα
        document.pathProperty().addListener((o, a, path) -> {
            if (path == null) return;
            Path sig = SignatureFileCodec.signatureFileFor(path);
            if (signature.get() == null && Files.exists(sig)) signature.set(sig);
        });
    }

    @FXML private void verify() {
        Path file = document.get();
        Path sig = signature.get();
        Path pub = publicKey.get();
        if (file == null || sig == null || pub == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        // στο ιστορικό γράφεται μία φορά, με αποτέλεσμα «Επιτυχία» μόνο αν η υπογραφή είναι έγκυρη
        runner.run(null, file.getFileName().toString(), p -> verifier.verify(file, codec.read(sig), pem.importFrom(pub)), r -> {
            show(r);
            ctx.getHistory().record(AppModule.SIGNATURE, "action.verify", file.getFileName().toString(), r.isValid());
        });
    }

    private void show(VerificationResult r) {
        resultLabel.setText(ctx.getI18n().text(r.getMessageKey()));
        resultLabel.getStyleClass().removeAll("result-valid", "result-invalid");
        resultLabel.getStyleClass().add(r.isValid() ? "result-valid" : "result-invalid");
        signedAtValue.setText(Formats.dateTime(r.getRecord().getSignedAt()));
        fingerprintValue.setText(r.getRecord().getKeyFingerprint());
        keyMatchValue.setText(ctx.getI18n().text(r.isKeyMatching() ? "common.yes" : "common.no"));
    }
}
