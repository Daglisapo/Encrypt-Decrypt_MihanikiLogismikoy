package gr.ihu.omada12.crypto.controller.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.vault.Vault;
import gr.ihu.omada12.crypto.model.vault.VaultInfo;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.Formats;
import gr.ihu.omada12.crypto.view.Screen;
import gr.ihu.omada12.crypto.view.StrengthIndicator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.Optional;

// Υλοποιεί: VLT-01, VLT-02, VLT-11, VLT-12, NFR-04 (UC-07, UC-08, UC-15)

/**
 * Οθόνη UI-03: λίστα θησαυροφυλακίων (ξεκλείδωμα, μετονομασία, διαγραφή) και φόρμα δημιουργίας
 * νέου, με επιβεβαίωση κωδικού, ένδειξη ισχύος και ρητή ενημέρωση ότι ο κωδικός δεν ανακτάται.
 */
public class VaultListController {
    private final AppContext ctx;
    private Feedback listFeedback;
    private Feedback createFeedback;
    private ActionRunner unlockRunner;
    private ActionRunner createRunner;

    @FXML private ListView<VaultInfo> vaultList;
    @FXML private PasswordField unlockPassword;
    @FXML private Pane listActions;
    @FXML private Label listStatus;
    @FXML private TextField nameField;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private CheckBox understandCheck;
    @FXML private Button createButton;
    @FXML private Pane createForm;
    @FXML private Label createStatus;

    public VaultListController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        listFeedback = new Feedback(listStatus, ctx.getI18n());
        createFeedback = new Feedback(createStatus, ctx.getI18n());
        unlockRunner = new ActionRunner(ctx, AppModule.VAULT, listFeedback, listActions);
        createRunner = new ActionRunner(ctx, AppModule.VAULT, createFeedback, createForm);
        new StrengthIndicator(ctx.getI18n(), newPassword, strengthBar, strengthLabel);
        createButton.disableProperty().bind(understandCheck.selectedProperty().not());
        vaultList.setCellFactory(list -> new ListCell<VaultInfo>() {
            @Override protected void updateItem(VaultInfo v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null
                        : v.getName() + "   ·   " + Formats.dateTime(v.getModified()) + "   ·   " + Formats.size(v.getSizeBytes()));
            }
        });
        refresh();
    }

    private void refresh() {
        try {
            vaultList.setItems(FXCollections.observableArrayList(ctx.getVaults().list()));
            if (vaultList.getItems().isEmpty()) listFeedback.info("vault.list.empty");
        } catch (CryptoException e) {
            listFeedback.error(e);
        }
    }

    @FXML private void create() {
        String name = nameField.getText();
        char[] pw = newPassword.getText().toCharArray();
        char[] confirm = confirmPassword.getText().toCharArray();
        createRunner.run("action.vaultCreate", name, p -> ctx.getVaults().create(name, pw, confirm), this::open);
    }

    @FXML private void unlock() {
        VaultInfo selected = vaultList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            listFeedback.errorKey("vault.list.selectFirst");
            return;
        }
        char[] pw = unlockPassword.getText().toCharArray();
        unlockRunner.run("action.vaultUnlock", selected.getName(), p -> ctx.getVaults().unlock(selected.getName(), pw), this::open);
    }

    private void open(Vault vault) {
        ctx.getVaultSession().open(vault);
        ctx.getNavigator().show(Screen.VAULT);
    }

    @FXML private void rename() {
        VaultInfo selected = vaultList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            listFeedback.errorKey("vault.list.selectFirst");
            return;
        }
        Optional<String> newName = ctx.dialogs().askText("vault.list.renamePrompt", selected.getName());
        if (newName.isEmpty() || newName.get().equals(selected.getName())) return;
        try {
            lockIfOpen(selected.getName());
            ctx.getVaults().rename(selected.getName(), newName.get());
            ctx.getHistory().record(AppModule.VAULT, "action.vaultRename", newName.get(), true);
            listFeedback.ok("vault.list.renamed", newName.get());
        } catch (CryptoException e) {
            listFeedback.error(e);
        }
        refresh();
    }

    /** VLT-11 + APP-05: διαγραφή ολόκληρου θησαυροφυλακίου μόνο μετά από επιβεβαίωση. */
    @FXML private void delete() {
        VaultInfo selected = vaultList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            listFeedback.errorKey("vault.list.selectFirst");
            return;
        }
        if (!ctx.dialogs().confirm("vault.list.deleteConfirm", selected.getName())) return;
        try {
            lockIfOpen(selected.getName());
            ctx.getVaults().delete(selected.getName());
            ctx.getHistory().record(AppModule.VAULT, "action.vaultDelete", selected.getName(), true);
            listFeedback.ok("vault.list.deleted", selected.getName());
        } catch (CryptoException e) {
            listFeedback.error(e);
        }
        refresh();
    }

    private void lockIfOpen(String name) {
        if (ctx.getVaultSession().isOpen() && ctx.getVaultSession().current().getName().equals(name)) {
            ctx.getVaultSession().lock();
        }
    }
}
