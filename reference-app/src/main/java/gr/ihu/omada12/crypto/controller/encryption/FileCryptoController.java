package gr.ihu.omada12.crypto.controller.encryption;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.file.BatchEncryptor;
import gr.ihu.omada12.crypto.model.file.BatchReport;
import gr.ihu.omada12.crypto.model.file.FileCryptoService;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.DropZone;
import gr.ihu.omada12.crypto.view.Feedback;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: ENC-04, ENC-05, ENC-06, ENC-10, ENC-11 (UC-03, UC-04)

/**
 * Καρτέλα «Αρχεία»: επιλογή με σύρσιμο ή παράθυρο, κρυπτογράφηση ενός ή πολλών αρχείων / φακέλου,
 * αποκρυπτογράφηση, μπάρα προόδου και ακύρωση.
 */
public class FileCryptoController {
    private final AppContext ctx;
    private final FileCryptoService service = new FileCryptoService();
    private final BatchEncryptor batch = new BatchEncryptor(service);
    private final List<Path> selection = new ArrayList<>();
    private Path baseFolder;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label dropLabel;
    @FXML private ListView<String> fileList;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Pane actions;
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    public FileCryptoController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.ENCRYPTION, feedback, actions, progressBar, cancelButton);
        new DropZone(dropLabel, this::onDropped);
        refreshSelection();
    }

    private void onDropped(List<Path> paths) {
        if (paths.size() == 1 && Files.isDirectory(paths.get(0))) {
            selectFolder(paths.get(0));
        } else {
            selectFiles(paths);
        }
    }

    @FXML private void chooseFiles() {
        List<Path> chosen = ctx.files().openMany("enc.file.chooseTitle");
        if (!chosen.isEmpty()) selectFiles(chosen);
    }

    @FXML private void chooseFolder() {
        Path dir = ctx.files().directory("enc.file.chooseFolderTitle");
        if (dir != null) selectFolder(dir);
    }

    private void selectFiles(List<Path> paths) {
        baseFolder = null;
        selection.clear();
        for (Path p : paths) {
            if (Files.isRegularFile(p)) selection.add(p);
        }
        refreshSelection();
    }

    private void selectFolder(Path dir) {
        try {
            selection.clear();
            selection.addAll(batch.collect(dir));
            baseFolder = dir;
        } catch (CryptoException e) {
            feedback.error(e);
        }
        refreshSelection();
    }

    private void refreshSelection() {
        List<String> names = new ArrayList<>();
        for (Path p : selection) names.add(baseFolder == null ? p.getFileName().toString() : baseFolder.relativize(p).toString());
        fileList.setItems(FXCollections.observableArrayList(names));
        dropLabel.setText(selection.isEmpty() ? ctx.getI18n().text("enc.file.drop")
                : ctx.getI18n().text("enc.file.selected", selection.size()));
    }

    @FXML private void encrypt() {
        char[] pw = passwordField.getText().toCharArray();
        if (!checkSelection(false) || !checkPassword(pw, true)) return;
        if (selection.size() == 1 && baseFolder == null) {
            Path source = selection.get(0);
            Path target = ctx.files().save("enc.file.saveTitle", FileCryptoService.encryptedName(source).getFileName().toString());
            if (target == null) return;
            runner.run("action.encryptFile", source.getFileName().toString(), p -> {
                service.encrypt(source, target, pw, p);
                return target;
            }, out -> feedback.ok("enc.file.encrypted", out.getFileName()));
        } else {
            Path outDir = ctx.files().directory("enc.file.outputFolderTitle");
            if (outDir == null) return;
            List<Path> files = new ArrayList<>(selection);
            String name = baseFolder == null ? "" : baseFolder.getFileName().toString();
            runner.run("action.encryptBatch", name, p -> batch.encryptAll(files, baseFolder, outDir, pw, p), this::showReport);
        }
    }

    @FXML private void decrypt() {
        char[] pw = passwordField.getText().toCharArray();
        if (!checkSelection(true) || !checkPassword(pw, false)) return;
        Path source = selection.get(0);
        Path target = ctx.files().save("enc.file.saveTitle", FileCryptoService.decryptedName(source).getFileName().toString());
        if (target == null) return;
        runner.run("action.decryptFile", source.getFileName().toString(), p -> {
            service.decrypt(source, target, pw, p);
            return target;
        }, out -> feedback.ok("enc.file.decrypted", out.getFileName()));
    }

    private void showReport(BatchReport report) {
        if (report.getFailed().isEmpty()) {
            feedback.ok("enc.file.batchDone", report.getSucceeded().size(), report.total());
        } else {
            feedback.errorKey("enc.file.batchFailures", report.getSucceeded().size(), report.total(),
                    report.getFailed().keySet().iterator().next().getFileName());
        }
    }

    private boolean checkSelection(boolean single) {
        if (selection.isEmpty()) {
            feedback.errorKey("error.noFileSelected");
            return false;
        }
        if (single && selection.size() != 1) {
            feedback.errorKey("error.oneFileOnly");
            return false;
        }
        return true;
    }

    private boolean checkPassword(char[] pw, boolean needConfirm) {
        if (pw.length == 0) {
            feedback.errorKey("error.emptyPassword");
            return false;
        }
        if (needConfirm && !passwordField.getText().equals(confirmField.getText())) {
            feedback.errorKey("error.passwordMismatch");
            return false;
        }
        return true;
    }
}
