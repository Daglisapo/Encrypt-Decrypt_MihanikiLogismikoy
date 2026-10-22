package gr.ihu.omada12.crypto.controller.vault;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.vault.Vault;
import gr.ihu.omada12.crypto.model.vault.VaultEntry;
import gr.ihu.omada12.crypto.model.vault.VaultFilter;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.ChoiceFilter;
import gr.ihu.omada12.crypto.view.DropZone;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.Formats;
import gr.ihu.omada12.crypto.view.PreviewWindow;
import gr.ihu.omada12.crypto.view.Screen;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: VLT-03, VLT-04, VLT-05, VLT-07 (UC-09, UC-10, UC-13)· οι υπόλοιπες ενέργειες στο VaultEntryActions

/**
 * Οθόνη UI-04: τα περιεχόμενα του ξεκλείδωτου θησαυροφυλακίου. Ο controller κρατά τον πίνακα,
 * τα φίλτρα, την προσθήκη αρχείων (και με σύρσιμο) και το κλείδωμα. Οι ενέργειες πάνω στα
 * επιλεγμένα αρχεία (εξαγωγή, διαγραφή, ετικέτα, προεπισκόπηση, αλλαγή κωδικού) γίνονται στο VaultEntryActions.
 */
public class VaultController {
    private final AppContext ctx;
    private Vault vault;
    private ChoiceFilter types;
    private ChoiceFilter tags;
    private Feedback feedback;
    private VaultEntryActions entryActions;

    @FXML private Label vaultNameLabel;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> tagFilter;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<VaultEntry> table;
    @FXML private TableColumn<VaultEntry, String> nameColumn;
    @FXML private TableColumn<VaultEntry, String> typeColumn;
    @FXML private TableColumn<VaultEntry, String> sizeColumn;
    @FXML private TableColumn<VaultEntry, String> dateColumn;
    @FXML private TableColumn<VaultEntry, String> tagColumn;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public VaultController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        if (!ctx.getVaultSession().isOpen()) {
            // κλειδωμένο (π.χ. «Πίσω» μετά από αυτόματο κλείδωμα): επιστροφή στη λίστα
            Platform.runLater(() -> ctx.getNavigator().replace(Screen.VAULT_LIST));
            return;
        }
        vault = ctx.getVaultSession().current();
        feedback = new Feedback(statusLabel, ctx.getI18n());
        entryActions = new VaultEntryActions(ctx, vault, feedback, new ActionRunner(ctx, AppModule.VAULT, feedback, actions));
        vaultNameLabel.setText(vault.getName());
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        nameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        typeColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getType()));
        sizeColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.size(c.getValue().getSize())));
        dateColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.dateTime(c.getValue().getAddedAt())));
        tagColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTag()));
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) preview();
        });
        new DropZone(table, this::addFiles);
        searchField.textProperty().addListener((o, a, b) -> refresh());
        types = new ChoiceFilter(typeFilter, ctx.getI18n().text("common.all"), this::refresh);
        tags = new ChoiceFilter(tagFilter, ctx.getI18n().text("common.all"), this::refresh);
        fromDate.valueProperty().addListener((o, a, b) -> refresh());
        toDate.valueProperty().addListener((o, a, b) -> refresh());
        refresh();
    }

    /** Ξαναγεμίζει τον πίνακα από το θησαυροφυλάκιο εφαρμόζοντας τα φίλτρα (VLT-05). */
    private void refresh() {
        try {
            List<VaultEntry> entries = vault.getEntries();
            types.update(VaultFilter.typesOf(entries));
            tags.update(VaultFilter.tagsOf(entries));
            VaultFilter filter = new VaultFilter().text(searchField.getText())
                    .type(types.value()).tag(tags.value())
                    .from(fromDate.getValue()).to(toDate.getValue());
            List<VaultEntry> shown = filter.apply(entries);
            table.setItems(FXCollections.observableArrayList(shown));
            countLabel.setText(ctx.getI18n().text("vault.count", shown.size(), entries.size()));
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    @FXML private void clearFilters() {
        searchField.clear();
        types.reset();
        tags.reset();
        fromDate.setValue(null);
        toDate.setValue(null);
    }

    @FXML private void chooseFiles() {
        List<Path> files = ctx.files().openMany("vault.addTitle");
        if (!files.isEmpty()) addFiles(files);
    }

    /** VLT-03: τα αρχεία κρυπτογραφούνται μέσα στο δοχείο· τα αρχικά δεν αλλάζουν. */
    private void addFiles(List<Path> files) {
        int added = 0;
        for (Path p : files) {
            if (!Files.isRegularFile(p)) continue;
            try {
                vault.addFile(p, "");
                added++;
                ctx.getHistory().record(AppModule.VAULT, "action.vaultAdd", p.getFileName().toString(), true);
            } catch (CryptoException e) {
                feedback.error(e);
                ctx.getHistory().record(AppModule.VAULT, "action.vaultAdd", p.getFileName().toString(), false);
            }
        }
        refresh();
        if (added > 0) feedback.ok("vault.added", added);
    }

    @FXML private void exportSelected() {
        List<VaultEntry> selected = selection();
        if (!selected.isEmpty()) entryActions.export(selected);
    }

    @FXML private void deleteSelected() {
        List<VaultEntry> selected = selection();
        if (!selected.isEmpty() && entryActions.delete(selected)) refresh();
    }

    @FXML private void editTag() {
        List<VaultEntry> selected = selection();
        if (!selected.isEmpty() && entryActions.tag(selected)) refresh();
    }

    @FXML private void preview() {
        List<VaultEntry> selected = selection();
        if (selected.size() == 1) entryActions.preview(selected.get(0));
    }

    @FXML private void changePassword() { entryActions.changePassword(); }

    /** VLT-07: κλείδωμα με ένα κουμπί. */
    @FXML private void lock() {
        String name = vault.getName();
        PreviewWindow.closeAll();
        ctx.getVaultSession().lock();
        ctx.getHistory().record(AppModule.VAULT, "action.vaultLock", name, true);
        ctx.getNavigator().replace(Screen.VAULT_LIST);
    }

    private List<VaultEntry> selection() {
        List<VaultEntry> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) feedback.errorKey("vault.selectFirst");
        return selected;
    }
}
