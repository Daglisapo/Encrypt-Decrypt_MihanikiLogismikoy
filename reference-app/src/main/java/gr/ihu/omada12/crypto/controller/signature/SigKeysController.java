package gr.ihu.omada12.crypto.controller.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.signature.PrivateKeyStore;
import gr.ihu.omada12.crypto.model.signature.PublicKeyPem;
import gr.ihu.omada12.crypto.model.signature.SignatureAlgorithm;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import gr.ihu.omada12.crypto.view.StrengthIndicator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;

// Υλοποιεί: SIG-01, SIG-04, SIG-06 (UC-16)

/**
 * Καρτέλα «Κλειδιά»: δημιουργία ζεύγους (ιδιωτικό με κωδικό + δημόσιο), εξαγωγή δημόσιου
 * από υπάρχον ιδιωτικό, και έλεγχος αποτυπώματος ενός δημόσιου κλειδιού που λάβαμε.
 */
public class SigKeysController {
    private final AppContext ctx;
    private final PrivateKeyStore store = new PrivateKeyStore();
    private final PublicKeyPem pem = new PublicKeyPem();
    private FileSelection privateKeyFile;
    private FileSelection publicKeyFile;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private TextField keyNameField;
    @FXML private PasswordField keyPassword;
    @FXML private PasswordField keyConfirm;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label fingerprintLabel;
    @FXML private Label privateKeyLabel;
    @FXML private Button choosePrivateKey;
    @FXML private PasswordField exportPassword;
    @FXML private Label publicKeyLabel;
    @FXML private Button choosePublicKey;
    @FXML private Label importedFingerprint;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public SigKeysController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.SIGNATURE, feedback, actions);
        new StrengthIndicator(ctx.getI18n(), keyPassword, strengthBar, strengthLabel);
        privateKeyFile = new FileSelection(ctx, privateKeyLabel, choosePrivateKey, "sig.chooseKeyTitle",
                ctx.files().filter("filter.privateKey", "*" + PrivateKeyStore.EXTENSION));
        publicKeyFile = new FileSelection(ctx, publicKeyLabel, choosePublicKey, "sig.choosePubTitle",
                ctx.files().filter("filter.publicKey", "*" + PublicKeyPem.EXTENSION));
        publicKeyFile.pathProperty().addListener((o, a, path) -> showImportedFingerprint(path));
    }

    /** SIG-01: νέο ζεύγος → όνομα.o12key (κρυπτογραφημένο) + όνομα.pub */
    @FXML private void generate() {
        String name = keyNameField.getText().trim();
        if (!name.matches("[\\p{L}\\p{N} _-]{1,40}")) {
            feedback.errorKey("error.keyName");
            return;
        }
        if (keyPassword.getText().isEmpty() || !keyPassword.getText().equals(keyConfirm.getText())) {
            feedback.errorKey(keyPassword.getText().isEmpty() ? "error.emptyPassword" : "error.passwordMismatch");
            return;
        }
        Path dir = ctx.files().directory("sig.keysFolderTitle");
        if (dir == null) return;
        Path priv = dir.resolve(name + PrivateKeyStore.EXTENSION);
        Path pub = dir.resolve(name + PublicKeyPem.EXTENSION);
        if ((Files.exists(priv) || Files.exists(pub)) && !ctx.dialogs().confirm("common.overwriteConfirm", name)) return;
        char[] pw = keyPassword.getText().toCharArray();
        runner.run("action.sigKeyGenerate", name, p -> {
            KeyPair pair = SignatureAlgorithm.generateKeyPair();
            store.save(pair, priv, pw);
            pem.export(pair.getPublic(), pub);
            return SignatureAlgorithm.fingerprint(pair.getPublic());
        }, fp -> {
            fingerprintLabel.setText(fp);
            feedback.ok("sig.keysCreated", priv.getFileName(), pub.getFileName());
        });
    }

    /** SIG-04: εξαγωγή δημόσιου κλειδιού για να το στείλουμε σε άλλον. */
    @FXML private void exportPublic() {
        Path source = privateKeyFile.get();
        if (source == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        String base = source.getFileName().toString().replace(PrivateKeyStore.EXTENSION, "");
        Path target = ctx.files().save("sig.savePubTitle", base + PublicKeyPem.EXTENSION,
                ctx.files().filter("filter.publicKey", "*" + PublicKeyPem.EXTENSION));
        if (target == null) return;
        char[] pw = exportPassword.getText().toCharArray();
        runner.run("action.sigKeyExport", target.getFileName().toString(), p -> {
            pem.export(store.load(source, pw).getPublic(), target);
            return target;
        }, out -> feedback.ok("common.savedTo", out.getFileName()));
    }

    /** SIG-04 + SIG-06: εισαγωγή δημόσιου κλειδιού και εμφάνιση αποτυπώματος για σύγκριση. */
    private void showImportedFingerprint(Path path) {
        if (path == null) return;
        try {
            importedFingerprint.setText(SignatureAlgorithm.fingerprint(pem.importFrom(path)));
            feedback.clear();
        } catch (CryptoException e) {
            importedFingerprint.setText("");
            feedback.error(e);
        }
    }
}
