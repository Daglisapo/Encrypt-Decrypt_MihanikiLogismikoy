package gr.ihu.omada12.crypto.controller.signature;

import gr.ihu.omada12.crypto.model.crypto.Hex;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.signature.FileHasher;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import gr.ihu.omada12.crypto.view.Views;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.nio.file.Path;
import java.util.function.Consumer;

// Υλοποιεί: SIG-05 (UC-19)

/** Καρτέλα «Αποτύπωμα»: SHA-256 ενός αρχείου και σύγκριση δύο αρχείων. */
public class HashController {
    private final AppContext ctx;
    private final FileHasher hasher = new FileHasher();
    private FileSelection fileA;
    private FileSelection fileB;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label fileALabel;
    @FXML private Button chooseA;
    @FXML private TextField hashA;
    @FXML private Label fileBLabel;
    @FXML private Button chooseB;
    @FXML private TextField hashB;
    @FXML private Label compareResult;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public HashController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.SIGNATURE, feedback, actions);
        fileA = new FileSelection(ctx, fileALabel, chooseA, "sig.chooseFileTitle");
        fileB = new FileSelection(ctx, fileBLabel, chooseB, "sig.chooseFileTitle");
        fileA.pathProperty().addListener((o, a, p) -> hash(p, hashA::setText));
        fileB.pathProperty().addListener((o, a, p) -> hash(p, hashB::setText));
    }

    private void hash(Path file, Consumer<String> target) {
        compareResult.setText("");
        if (file == null) return;
        runner.run("action.hash", file.getFileName().toString(), p -> Hex.encode(hasher.sha256(file)), target);
    }

    @FXML private void copyA() {
        Views.copyToClipboard(hashA.getText());
        feedback.ok("enc.text.copied");
    }

    @FXML private void compare() {
        if (hashA.getText().isEmpty() || hashB.getText().isEmpty()) {
            feedback.errorKey("sig.hash.needTwo");
            return;
        }
        boolean same = hashA.getText().equals(hashB.getText());
        compareResult.setText(ctx.getI18n().text(same ? "sig.hash.same" : "sig.hash.different"));
        compareResult.getStyleClass().removeAll("result-valid", "result-invalid");
        compareResult.getStyleClass().add(same ? "result-valid" : "result-invalid");
    }
}
