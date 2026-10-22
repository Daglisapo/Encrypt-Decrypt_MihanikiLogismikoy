package gr.ihu.omada12.crypto.controller.general;

import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.history.HistoryEntry;
import gr.ihu.omada12.crypto.model.history.HistoryFilter;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.Formats;
import gr.ihu.omada12.crypto.view.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;

// Υλοποιεί: APP-09, APP-10 (UC-26)

/** Καρτέλα «Ιστορικό»: ημερομηνία, ενότητα, ενέργεια, αρχείο, αποτέλεσμα, με φίλτρα και καθαρισμό. */
public class HistoryController {
    private final AppContext ctx;
    private Feedback feedback;

    @FXML private ComboBox<String> moduleFilter;
    @FXML private CheckBox failuresOnly;
    @FXML private TextField searchField;
    @FXML private TableView<HistoryEntry> table;
    @FXML private TableColumn<HistoryEntry, String> dateColumn;
    @FXML private TableColumn<HistoryEntry, String> moduleColumn;
    @FXML private TableColumn<HistoryEntry, String> actionColumn;
    @FXML private TableColumn<HistoryEntry, String> fileColumn;
    @FXML private TableColumn<HistoryEntry, String> resultColumn;
    @FXML private Label disabledLabel;
    @FXML private Label statusLabel;

    public HistoryController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        I18n i18n = ctx.getI18n();
        feedback = new Feedback(statusLabel, i18n);
        moduleFilter.getItems().add(i18n.text("common.all"));
        for (AppModule m : AppModule.values()) moduleFilter.getItems().add(i18n.text(m.getKey()));
        moduleFilter.getSelectionModel().select(0);
        dateColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.dateTime(c.getValue().getTimestamp())));
        moduleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(i18n.text(c.getValue().getModule().getKey())));
        actionColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(i18n.text(c.getValue().getActionKey())));
        fileColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFileName()));
        resultColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                i18n.text(c.getValue().isSuccess() ? "history.success" : "history.failure")));
        disabledLabel.managedProperty().bind(disabledLabel.visibleProperty());
        disabledLabel.setVisible(!ctx.getHistory().isEnabled());
        moduleFilter.valueProperty().addListener((o, a, b) -> refresh());
        failuresOnly.selectedProperty().addListener((o, a, b) -> refresh());
        searchField.textProperty().addListener((o, a, b) -> refresh());
        refresh();
    }

    @FXML private void refresh() {
        int index = moduleFilter.getSelectionModel().getSelectedIndex();
        HistoryFilter filter = new HistoryFilter()
                .module(index <= 0 ? null : AppModule.values()[index - 1])
                .failuresOnly(failuresOnly.isSelected())
                .text(searchField.getText());
        try {
            table.setItems(FXCollections.observableArrayList(ctx.getHistory().find(filter)));
        } catch (IOException e) {
            feedback.errorKey("error.fileRead", "history.log");
        }
    }

    /** APP-10 + APP-05: καθαρισμός μόνο μετά από επιβεβαίωση. */
    @FXML private void clear() {
        if (!ctx.dialogs().confirm("history.clearConfirm")) return;
        try {
            ctx.getHistory().clear();
            feedback.ok("history.cleared");
        } catch (IOException e) {
            feedback.errorKey("error.fileWrite", "history.log");
        }
        refresh();
    }
}
